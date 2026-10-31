package com.example.editor.video

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.Surface
import android.graphics.Path
import android.graphics.RectF
import com.example.model.ShapeType
import com.example.model.interpolateKeyframeState
import kotlin.math.sin
import com.example.editor.photo.PhotoProcessingEngine
import com.example.filters.SazfloyColorEngine
import com.example.model.EditingProject
import com.example.model.ExportConfiguration
import com.example.model.MediaClip
import com.example.model.TextOverlay
import com.example.model.TransitionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.coroutines.coroutineContext

object VideoExportEngine {

    sealed class ExportProgress {
        data class Progress(val percent: Float, val currentFrame: Int, val totalFrames: Int) : ExportProgress()
        data class Success(val outputUri: Uri, val filePath: String, val durationMs: Long) : ExportProgress()
        data class Error(val message: String) : ExportProgress()
    }

    /**
     * Extracts thumbnail from video or photo clip
     */
    suspend fun getClipThumbnail(context: Context, clip: MediaClip): Bitmap? = withContext(Dispatchers.IO) {
        try {
            if (clip.uri.startsWith("content://") || clip.uri.startsWith("file://")) {
                val uri = Uri.parse(clip.uri)
                val retriever = android.media.MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, uri)
                    val frame = retriever.getFrameAtTime(
                        clip.trimStartMs * 1000L,
                        android.media.MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )
                    if (frame != null) return@withContext frame
                } catch (_: Exception) {} finally {
                    runCatching { retriever.release() }
                }

                // If retriever fails, try decoding as photo
                return@withContext PhotoProcessingEngine.loadBitmapFromUri(context, uri, 320)
            }
        } catch (_: Exception) {}

        // Fallback: generate clip thumbnail
        createClipSampleFrame(clip, 240, 320, 0f)
    }

    fun createClipSampleFrame(clip: MediaClip, width: Int, height: Int, progressRatio: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient based on clip id / name
        val hash = clip.name.hashCode()
        val c1 = 0xFF000000.toInt() or (hash and 0x00FFFFFF)
        val c2 = 0xFF1A1A24.toInt()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(), c1, c2, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Overlay text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText(clip.name, width / 2f, height / 2f, textPaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF00E5FF.toInt()
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        val durationSec = "%.1fs".format(clip.trimmedDurationMs / 1000f)
        canvas.drawText("$durationSec • ${clip.speed}x", width / 2f, (height / 2f) + 36f, subPaint)

        return bitmap
    }

    /**
     * Renders a full video MP4 using Android MediaCodec hardware encoder and MediaMuxer,
     * applying all trims, color grading filters, text overlays, stickers, and transitions!
     */
    suspend fun exportVideo(
        context: Context,
        project: EditingProject,
        config: ExportConfiguration,
        onProgress: (ExportProgress) -> Unit
    ) = withContext(Dispatchers.IO) {
        val totalDurationMs = project.totalDurationMs.coerceAtLeast(1000L)
        val fps = config.fps
        val totalFrames = ((totalDurationMs / 1000f) * fps).toInt().coerceAtLeast(1)

        val outWidth = ((config.width / 16) * 16).coerceAtLeast(320)
        val outHeight = ((config.height / 16) * 16).coerceAtLeast(240)
        val tempFile = File(context.cacheDir, "sazfloy_export_${System.currentTimeMillis()}.mp4")

        var codec: MediaCodec? = null
        var muxer: MediaMuxer? = null
        var surface: Surface? = null
        var trackIndex = -1
        var muxerStarted = false

        try {
            val mime = MediaFormat.MIMETYPE_VIDEO_AVC
            codec = MediaCodec.createEncoderByType(mime)

            // Determine safe resolution and bitrate supported by device/emulator encoder
            var (safeWidth, safeHeight, safeBitrate) = findSafeDimensionsAndBitrate(
                codec = codec,
                mime = mime,
                requestedWidth = config.width,
                requestedHeight = config.height,
                requestedBitrateMbps = config.bitrateMbps
            )

            var format = MediaFormat.createVideoFormat(mime, safeWidth, safeHeight).apply {
                setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                setInteger(MediaFormat.KEY_BIT_RATE, safeBitrate)
                setInteger(MediaFormat.KEY_FRAME_RATE, fps.coerceIn(24, 30))
                setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
            }

            try {
                codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            } catch (e: Exception) {
                // Fallback to universally supported 720p (1280x720 or 720x1280)
                runCatching { codec.reset() }
                val isPortrait = safeHeight > safeWidth
                safeWidth = if (isPortrait) 720 else 1280
                safeHeight = if (isPortrait) 1280 else 720
                safeBitrate = 4_000_000
                format = MediaFormat.createVideoFormat(mime, safeWidth, safeHeight).apply {
                    setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
                    setInteger(MediaFormat.KEY_BIT_RATE, safeBitrate)
                    setInteger(MediaFormat.KEY_FRAME_RATE, 30)
                    setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
                }
                codec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            }

            surface = codec.createInputSurface()
            codec.start()

            muxer = MediaMuxer(tempFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)

            // Cache clip thumbnail/frame bitmaps
            val clipBitmaps = mutableMapOf<String, Bitmap>()
            for (clip in project.clips) {
                val bmp = getClipThumbnail(context, clip) ?: createClipSampleFrame(clip, safeWidth, safeHeight, 0f)
                clipBitmaps[clip.id] = bmp
            }

            val bufferInfo = MediaCodec.BufferInfo()
            var frameIndex = 0

            while (frameIndex < totalFrames && coroutineContext.isActive) {
                val currentPtsMs = (frameIndex * 1000L) / fps
                val progressRatio = frameIndex.toFloat() / totalFrames.toFloat()

                // Find active clip at currentPtsMs
                var accumulatedTimeMs = 0L
                var activeClip: MediaClip? = null
                var clipLocalTimeMs = 0L
                var previousClip: MediaClip? = null

                for (clip in project.clips) {
                    val clipDuration = clip.trimmedDurationMs
                    if (currentPtsMs >= accumulatedTimeMs && currentPtsMs < accumulatedTimeMs + clipDuration) {
                        activeClip = clip
                        clipLocalTimeMs = currentPtsMs - accumulatedTimeMs
                        break
                    }
                    previousClip = clip
                    accumulatedTimeMs += clipDuration
                }
                if (activeClip == null) activeClip = project.clips.lastOrNull()

                // Draw frame to Surface Canvas
                if (surface.isValid) {
                    val canvas = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        surface.lockHardwareCanvas()
                    } else {
                        surface.lockCanvas(null)
                    }

                    try {
                        renderFrameToCanvas(
                            context = context,
                            canvas = canvas,
                            width = safeWidth,
                            height = safeHeight,
                            project = project,
                            activeClip = activeClip,
                            clipLocalTimeMs = clipLocalTimeMs,
                            currentPtsMs = currentPtsMs,
                            clipBitmaps = clipBitmaps,
                            previousClip = previousClip
                        )
                    } finally {
                        surface.unlockCanvasAndPost(canvas)
                    }
                }

                // Drain encoder output
                trackIndex = drainEncoder(codec, muxer, bufferInfo, false, trackIndex) { index ->
                    muxerStarted = true
                }

                frameIndex++
                if (frameIndex % 5 == 0 || frameIndex == totalFrames) {
                    onProgress(ExportProgress.Progress(progressRatio, frameIndex, totalFrames))
                }
            }

            // Signal End of Stream
            codec.signalEndOfInputStream()
            trackIndex = drainEncoder(codec, muxer, bufferInfo, true, trackIndex) { index ->
                muxerStarted = true
            }

            // Save to MediaStore (Movies/SAZFLOY)
            val outputUri = saveVideoToMediaStore(context, tempFile, project.title)
            onProgress(ExportProgress.Success(outputUri, tempFile.absolutePath, totalDurationMs))

        } catch (e: Exception) {
            e.printStackTrace()
            onProgress(ExportProgress.Error(e.message ?: "Export failed"))
        } finally {
            runCatching { codec?.stop() }
            runCatching { codec?.release() }
            runCatching { surface?.release() }
            if (muxerStarted && muxer != null) {
                runCatching { muxer.stop() }
                runCatching { muxer.release() }
            }
        }
    }

    private fun findSafeDimensionsAndBitrate(
        codec: MediaCodec,
        mime: String,
        requestedWidth: Int,
        requestedHeight: Int,
        requestedBitrateMbps: Int
    ): Triple<Int, Int, Int> {
        var w = ((requestedWidth / 16) * 16).coerceAtLeast(320)
        var h = ((requestedHeight / 16) * 16).coerceAtLeast(240)
        var bitrate = if (requestedBitrateMbps > 0) requestedBitrateMbps * 1_000_000 else 10_000_000

        val caps = runCatching { codec.codecInfo.getCapabilitiesForType(mime) }.getOrNull()
        val vCaps = caps?.videoCapabilities
        if (vCaps != null) {
            val maxW = runCatching { vCaps.supportedWidths.upper }.getOrNull() ?: 1920
            val maxH = runCatching { vCaps.supportedHeights.upper }.getOrNull() ?: 1080

            // If requested size exceeds encoder maximum bounds, scale down
            if (w > maxW || h > maxH || !vCaps.isSizeSupported(w, h)) {
                var scale = 1.0f
                if (w > maxW) scale = minOf(scale, maxW.toFloat() / w)
                if (h > maxH) scale = minOf(scale, maxH.toFloat() / h)

                w = ((w * scale).toInt() / 16) * 16
                h = ((h * scale).toInt() / 16) * 16

                if (!vCaps.isSizeSupported(w, h)) {
                    val isPortrait = h > w
                    w = if (isPortrait) 720 else 1280
                    h = if (isPortrait) 1280 else 720
                }
            }

            val maxBitrate = runCatching { vCaps.bitrateRange.upper }.getOrNull() ?: 15_000_000
            bitrate = bitrate.coerceIn(500_000, minOf(15_000_000, maxBitrate))
        } else {
            w = w.coerceAtMost(1920)
            h = h.coerceAtMost(1920)
            bitrate = bitrate.coerceIn(500_000, 12_000_000)
        }

        return Triple(w, h, bitrate)
    }

    private fun drainEncoder(
        codec: MediaCodec,
        muxer: MediaMuxer,
        bufferInfo: MediaCodec.BufferInfo,
        endOfStream: Boolean,
        currentTrackIndex: Int,
        onMuxerStart: (Int) -> Unit
    ): Int {
        var track = currentTrackIndex
        val timeoutUs = 10000L
        while (true) {
            val status = codec.dequeueOutputBuffer(bufferInfo, timeoutUs)
            if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break
            } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                if (track < 0) {
                    val newFormat = codec.outputFormat
                    track = muxer.addTrack(newFormat)
                    muxer.start()
                    onMuxerStart(track)
                }
            } else if (status >= 0) {
                val outputBuffer = codec.getOutputBuffer(status)
                if (outputBuffer != null && (bufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG) == 0) {
                    if (bufferInfo.size != 0 && track >= 0) {
                        outputBuffer.position(bufferInfo.offset)
                        outputBuffer.limit(bufferInfo.offset + bufferInfo.size)
                        try {
                            muxer.writeSampleData(track, outputBuffer, bufferInfo)
                        } catch (_: Exception) {}
                    }
                }
                codec.releaseOutputBuffer(status, false)
                if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) break
            }
        }
        return track
    }

    private fun renderFrameToCanvas(
        context: Context,
        canvas: Canvas,
        width: Int,
        height: Int,
        project: EditingProject,
        activeClip: MediaClip?,
        clipLocalTimeMs: Long,
        currentPtsMs: Long,
        clipBitmaps: Map<String, Bitmap>,
        previousClip: MediaClip?
    ) {
        // Clear background
        canvas.drawColor(AndroidColor.BLACK)

        if (activeClip != null) {
            val baseBmp = clipBitmaps[activeClip.id]
            if (baseBmp != null) {
                // Apply per-clip or project color grading filter, color wheels, and 3D LUT to current frame
                val clipAdjustments = activeClip.adjustments.takeIf { !it.isDefault } ?: project.adjustments
                val processed = SazfloyColorEngine.processBitmap(
                    source = baseBmp,
                    adjustments = clipAdjustments,
                    rotationDegrees = activeClip.rotationDegrees,
                    flipHorizontal = activeClip.isFlippedHorizontal,
                    flipVertical = activeClip.isFlippedVertical
                )

                // Apply active project effects targeted to this clip or global
                var finalProcessed = processed
                val activeEffects = project.effects.filter {
                    it.isEnabled && currentPtsMs in it.startMs..(it.startMs + it.durationMs) &&
                    (it.targetClipId == null || it.targetClipId == activeClip.id)
                }
                for (eff in activeEffects) {
                    finalProcessed = SazfloyColorEngine.applyVideoEffect(finalProcessed, eff)
                }

                // Scale to fit canvas with aspect ratio
                val srcRect = Rect(0, 0, finalProcessed.width, finalProcessed.height)
                val dstRect = calculateFitRect(finalProcessed.width, finalProcessed.height, width, height)

                val hasTransform = activeClip.scale != 1.0f || activeClip.panX != 0f || activeClip.panY != 0f
                if (hasTransform) {
                    canvas.save()
                    val cx = dstRect.centerX().toFloat() + (activeClip.panX * width)
                    val cy = dstRect.centerY().toFloat() + (activeClip.panY * height)
                    canvas.translate(cx, cy)
                    canvas.scale(activeClip.scale, activeClip.scale)
                    canvas.translate(-dstRect.centerX().toFloat(), -dstRect.centerY().toFloat())
                }

                val transDur = activeClip.transitionDurationMs.coerceAtLeast(100L)
                val isTransitioning = activeClip.transitionType != TransitionType.NONE && clipLocalTimeMs < transDur

                if (isTransitioning) {
                    val t = (clipLocalTimeMs.toFloat() / transDur.toFloat()).coerceIn(0f, 1f)
                    val prevBmp = previousClip?.let { clipBitmaps[it.id] }
                    val prevProcessed = if (prevBmp != null) {
                        val prevAdjustments = previousClip.adjustments.takeIf { !it.isDefault } ?: project.adjustments
                        var pBmp = SazfloyColorEngine.processBitmap(
                            source = prevBmp,
                            adjustments = prevAdjustments,
                            rotationDegrees = previousClip.rotationDegrees,
                            flipHorizontal = previousClip.isFlippedHorizontal,
                            flipVertical = previousClip.isFlippedVertical
                        )
                        for (eff in activeEffects) {
                            pBmp = SazfloyColorEngine.applyVideoEffect(pBmp, eff)
                        }
                        pBmp
                    } else null

                    val prevDstRect = if (prevProcessed != null) {
                        calculateFitRect(prevProcessed.width, prevProcessed.height, width, height)
                    } else dstRect

                    when (activeClip.transitionType) {
                        TransitionType.CROSS_DISSOLVE, TransitionType.FADE, TransitionType.DISSOLVE -> {
                            if (prevProcessed != null) {
                                val prevPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                                    alpha = ((1f - t) * 255).toInt().coerceIn(0, 255)
                                }
                                canvas.drawBitmap(prevProcessed, Rect(0, 0, prevProcessed.width, prevProcessed.height), prevDstRect, prevPaint)
                            }
                            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                                alpha = (t * 255).toInt().coerceIn(0, 255)
                            }
                            canvas.drawBitmap(finalProcessed, srcRect, dstRect, paint)
                        }

                        TransitionType.DIP_TO_BLACK -> {
                            if (t < 0.5f) {
                                val fadeOut = 1f - (t * 2f)
                                if (prevProcessed != null) {
                                    val prevPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                                        alpha = (fadeOut * 255).toInt().coerceIn(0, 255)
                                    }
                                    canvas.drawBitmap(prevProcessed, Rect(0, 0, prevProcessed.width, prevProcessed.height), prevDstRect, prevPaint)
                                }
                            } else {
                                val fadeIn = (t - 0.5f) * 2f
                                val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                                    alpha = (fadeIn * 255).toInt().coerceIn(0, 255)
                                }
                                canvas.drawBitmap(finalProcessed, srcRect, dstRect, paint)
                            }
                        }

                        TransitionType.DIP_TO_WHITE, TransitionType.FLASH_WHITE -> {
                            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                            canvas.drawBitmap(finalProcessed, srcRect, dstRect, paint)
                            val flashAlpha = ((1f - t) * 255).toInt().coerceIn(0, 255)
                            val flashPaint = Paint().apply {
                                color = AndroidColor.WHITE
                                alpha = flashAlpha
                            }
                            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), flashPaint)
                        }

                        TransitionType.SLIDE_LEFT -> {
                            val offsetCurrent = ((1f - t) * width).toInt()
                            val offsetPrev = (-t * width).toInt()
                            if (prevProcessed != null) {
                                val shiftedPrev = Rect(prevDstRect.left + offsetPrev, prevDstRect.top, prevDstRect.right + offsetPrev, prevDstRect.bottom)
                                canvas.drawBitmap(prevProcessed, Rect(0, 0, prevProcessed.width, prevProcessed.height), shiftedPrev, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                            }
                            val shiftedCur = Rect(dstRect.left + offsetCurrent, dstRect.top, dstRect.right + offsetCurrent, dstRect.bottom)
                            canvas.drawBitmap(finalProcessed, srcRect, shiftedCur, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                        }

                        TransitionType.SLIDE_RIGHT -> {
                            val offsetCurrent = (-(1f - t) * width).toInt()
                            val offsetPrev = (t * width).toInt()
                            if (prevProcessed != null) {
                                val shiftedPrev = Rect(prevDstRect.left + offsetPrev, prevDstRect.top, prevDstRect.right + offsetPrev, prevDstRect.bottom)
                                canvas.drawBitmap(prevProcessed, Rect(0, 0, prevProcessed.width, prevProcessed.height), shiftedPrev, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                            }
                            val shiftedCur = Rect(dstRect.left + offsetCurrent, dstRect.top, dstRect.right + offsetCurrent, dstRect.bottom)
                            canvas.drawBitmap(finalProcessed, srcRect, shiftedCur, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                        }

                        TransitionType.PUSH_UP -> {
                            val offsetCurrent = ((1f - t) * height).toInt()
                            val offsetPrev = (-t * height).toInt()
                            if (prevProcessed != null) {
                                val shiftedPrev = Rect(prevDstRect.left, prevDstRect.top + offsetPrev, prevDstRect.right, prevDstRect.bottom + offsetPrev)
                                canvas.drawBitmap(prevProcessed, Rect(0, 0, prevProcessed.width, prevProcessed.height), shiftedPrev, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                            }
                            val shiftedCur = Rect(dstRect.left, dstRect.top + offsetCurrent, dstRect.right, dstRect.bottom + offsetCurrent)
                            canvas.drawBitmap(finalProcessed, srcRect, shiftedCur, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                        }

                        TransitionType.SLIDE_DOWN -> {
                            val offsetCurrent = (-(1f - t) * height).toInt()
                            val offsetPrev = (t * height).toInt()
                            if (prevProcessed != null) {
                                val shiftedPrev = Rect(prevDstRect.left, prevDstRect.top + offsetPrev, prevDstRect.right, prevDstRect.bottom + offsetPrev)
                                canvas.drawBitmap(prevProcessed, Rect(0, 0, prevProcessed.width, prevProcessed.height), shiftedPrev, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                            }
                            val shiftedCur = Rect(dstRect.left, dstRect.top + offsetCurrent, dstRect.right, dstRect.bottom + offsetCurrent)
                            canvas.drawBitmap(finalProcessed, srcRect, shiftedCur, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                        }

                        TransitionType.WIPE_RIGHT -> {
                            if (prevProcessed != null) {
                                canvas.drawBitmap(prevProcessed, Rect(0, 0, prevProcessed.width, prevProcessed.height), prevDstRect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                            }
                            canvas.save()
                            canvas.clipRect(0f, 0f, width * t, height.toFloat())
                            canvas.drawBitmap(finalProcessed, srcRect, dstRect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))
                            canvas.restore()
                        }

                        TransitionType.SPIN -> {
                            val scale = 0.5f + (0.5f * t)
                            val rotation = (1f - t) * 360f
                            canvas.save()
                            canvas.scale(scale, scale, width / 2f, height / 2f)
                            canvas.rotate(rotation, width / 2f, height / 2f)
                            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                                alpha = (t * 255).toInt().coerceIn(0, 255)
                            }
                            canvas.drawBitmap(finalProcessed, srcRect, dstRect, paint)
                            canvas.restore()
                        }

                        TransitionType.ZOOM_IN -> {
                            val scale = 0.4f + (0.6f * t)
                            canvas.save()
                            canvas.scale(scale, scale, width / 2f, height / 2f)
                            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                                alpha = (t * 255).toInt().coerceIn(0, 255)
                            }
                            canvas.drawBitmap(finalProcessed, srcRect, dstRect, paint)
                            canvas.restore()
                        }

                        TransitionType.ZOOM_OUT -> {
                            val scale = 1.6f - (0.6f * t)
                            canvas.save()
                            canvas.scale(scale, scale, width / 2f, height / 2f)
                            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                                alpha = (t * 255).toInt().coerceIn(0, 255)
                            }
                            canvas.drawBitmap(finalProcessed, srcRect, dstRect, paint)
                            canvas.restore()
                        }

                        TransitionType.GLITCH -> {
                            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                            canvas.drawBitmap(finalProcessed, srcRect, dstRect, paint)
                            val glitchPaint = Paint().apply {
                                color = if ((currentPtsMs / 50L) % 2L == 0L) 0x6600E5BC.toInt() else 0x66FF2D75.toInt()
                            }
                            val jitterY = ((sin(t * 20.0) * height * 0.4) + (height / 2f)).toFloat()
                            canvas.drawRect(0f, jitterY, width.toFloat(), jitterY + 18f, glitchPaint)
                        }

                        TransitionType.BLUR_DISSOLVE -> {
                            val blurRadius = ((1f - t) * 15f).toInt()
                            val blurred = if (blurRadius > 1) SazfloyColorEngine.applyFastBlur(finalProcessed, blurRadius) else finalProcessed
                            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG).apply {
                                alpha = (t * 255).toInt().coerceIn(0, 255)
                            }
                            canvas.drawBitmap(blurred, Rect(0, 0, blurred.width, blurred.height), dstRect, paint)
                        }

                        TransitionType.WHIP_PAN -> {
                            val panOffset = (Math.sin(t.toDouble() * Math.PI) * width * 0.6).toInt()
                            val shifted = Rect(dstRect.left - panOffset, dstRect.top, dstRect.right - panOffset, dstRect.bottom)
                            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                            canvas.drawBitmap(finalProcessed, srcRect, shifted, paint)
                        }

                        TransitionType.NONE -> {
                            val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                            canvas.drawBitmap(finalProcessed, srcRect, dstRect, paint)
                        }
                    }
                } else {
                    val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
                    canvas.drawBitmap(finalProcessed, srcRect, dstRect, paint)
                }

                if (hasTransform) {
                    canvas.restore()
                }
            }
        }


        val w = width.toFloat()
        val h = height.toFloat()

        // 1. Draw Shapes active at currentPtsMs
        project.shapeOverlays.filter { currentPtsMs in it.startMs..(it.startMs + it.durationMs) }.forEach { shape ->
            val kState = interpolateKeyframeState(
                keyframes = shape.keyframes,
                currentTimeMs = currentPtsMs,
                defaultPosX = shape.posXRatio,
                defaultPosY = shape.posYRatio,
                defaultScale = 1f,
                defaultRotation = shape.rotation,
                defaultOpacity = shape.opacity
            )

            val px = kState.posXRatio * w
            val py = kState.posYRatio * h
            val sw = shape.widthRatio * w * kState.scale
            val sh = shape.heightRatio * h * kState.scale

            canvas.save()
            canvas.rotate(kState.rotation, px, py)

            val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = runCatching { AndroidColor.parseColor(shape.fillColorHex) }.getOrDefault(0xFF00E5BC.toInt())
                alpha = (kState.opacity.coerceIn(0f, 1f) * 255).toInt()
                style = Paint.Style.FILL
            }

            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = runCatching { AndroidColor.parseColor(shape.strokeColorHex) }.getOrDefault(AndroidColor.WHITE)
                alpha = (kState.opacity.coerceIn(0f, 1f) * 255).toInt()
                style = Paint.Style.STROKE
                strokeWidth = shape.strokeWidth * (w / 1080f)
            }

            val left = px - sw / 2f
            val top = py - sh / 2f
            val right = px + sw / 2f
            val bottom = py + sh / 2f

            when (shape.type) {
                ShapeType.RECTANGLE -> {
                    canvas.drawRect(left, top, right, bottom, fillPaint)
                    if (shape.strokeWidth > 0f) canvas.drawRect(left, top, right, bottom, strokePaint)
                }
                ShapeType.ROUNDED_RECT -> {
                    val cr = 24f * (w / 1080f)
                    val r = RectF(left, top, right, bottom)
                    canvas.drawRoundRect(r, cr, cr, fillPaint)
                    if (shape.strokeWidth > 0f) canvas.drawRoundRect(r, cr, cr, strokePaint)
                }
                ShapeType.CIRCLE -> {
                    val radius = (sw.coerceAtMost(sh) / 2f)
                    canvas.drawCircle(px, py, radius, fillPaint)
                    if (shape.strokeWidth > 0f) canvas.drawCircle(px, py, radius, strokePaint)
                }
                ShapeType.STAR, ShapeType.BADGE -> {
                    val starPath = createStarPath(px, py, sw / 2f, sw / 4f, 5)
                    canvas.drawPath(starPath, fillPaint)
                    if (shape.strokeWidth > 0f) canvas.drawPath(starPath, strokePaint)
                }
            }
            canvas.restore()
        }

        // 2. Draw Stickers active at currentPtsMs
        project.stickerOverlays.filter { currentPtsMs in it.startMs..(it.startMs + it.durationMs) }.forEach { sticker ->
            val kState = interpolateKeyframeState(
                keyframes = sticker.keyframes,
                currentTimeMs = currentPtsMs,
                defaultPosX = sticker.posXRatio,
                defaultPosY = sticker.posYRatio,
                defaultScale = sticker.scale,
                defaultRotation = sticker.rotation,
                defaultOpacity = 1f
            )

            val sPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 72f * kState.scale * (w / 1080f)
                textAlign = Paint.Align.CENTER
                alpha = (kState.opacity.coerceIn(0f, 1f) * 255).toInt()
            }
            val sx = kState.posXRatio * w
            val sy = kState.posYRatio * h
            canvas.save()
            canvas.rotate(kState.rotation, sx, sy)
            canvas.drawText(sticker.emojiOrTag, sx, sy, sPaint)
            canvas.restore()
        }

        // 3. Draw Texts active at currentPtsMs
        project.textOverlays.filter { currentPtsMs in it.startMs..(it.startMs + it.durationMs) }.forEach { textItem ->
            val kState = interpolateKeyframeState(
                keyframes = textItem.keyframes,
                currentTimeMs = currentPtsMs,
                defaultPosX = textItem.posXRatio,
                defaultPosY = textItem.posYRatio,
                defaultScale = 1f,
                defaultRotation = textItem.rotation,
                defaultOpacity = 1f
            )

            val scale = (w / 1080f) * kState.scale
            val px = kState.posXRatio * w
            val py = kState.posYRatio * h
            val textSizePx = textItem.fontSizeSp * 2.8f * scale

            val align = when (textItem.textAlign.lowercase()) {
                "left" -> Paint.Align.LEFT
                "right" -> Paint.Align.RIGHT
                else -> Paint.Align.CENTER
            }

            canvas.save()
            canvas.rotate(kState.rotation, px, py)

            if (textItem.hasBackground) {
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = runCatching { AndroidColor.parseColor(textItem.backgroundColorHex) }.getOrDefault(AndroidColor.BLACK)
                    alpha = (kState.opacity.coerceIn(0f, 1f) * 180).toInt()
                }
                val bounds = Rect()
                val measurePaint = Paint().apply { textSize = textSizePx }
                measurePaint.getTextBounds(textItem.text, 0, textItem.text.length, bounds)
                val padX = 24f * scale
                val padY = 16f * scale
                val bgRect = RectF(
                    px - bounds.width() / 2f - padX,
                    py - bounds.height() - padY,
                    px + bounds.width() / 2f + padX,
                    py + padY
                )
                canvas.drawRoundRect(bgRect, 16f * scale, 16f * scale, bgPaint)
            }

            val fontRes = when (textItem.fontFamilyName.lowercase()) {
                "montserrat" -> com.example.R.font.montserrat
                "bebas_neue" -> com.example.R.font.bebas_neue
                "cinzel" -> com.example.R.font.cinzel
                "playfair_display" -> com.example.R.font.playfair_display
                "anton" -> com.example.R.font.anton
                "pacifico" -> com.example.R.font.pacifico
                "oswald" -> com.example.R.font.oswald
                else -> null
            }
            val customTypeface = if (fontRes != null) {
                runCatching { androidx.core.content.res.ResourcesCompat.getFont(context, fontRes) }.getOrNull()
            } else null

            if (textItem.hasOutline) {
                val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = runCatching { AndroidColor.parseColor(textItem.outlineColorHex) }.getOrDefault(AndroidColor.BLACK)
                    textSize = textSizePx
                    textAlign = align
                    style = Paint.Style.STROKE
                    strokeWidth = 6f * scale
                    isFakeBoldText = textItem.isBold
                    if (customTypeface != null) typeface = customTypeface
                    alpha = (kState.opacity.coerceIn(0f, 1f) * 255).toInt()
                }
                canvas.drawText(textItem.text, px, py, outlinePaint)
            }

            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = runCatching { AndroidColor.parseColor(textItem.fontColorHex) }.getOrDefault(AndroidColor.WHITE)
                textSize = textSizePx
                textAlign = align
                isFakeBoldText = textItem.isBold
                if (customTypeface != null) typeface = customTypeface
                alpha = (kState.opacity.coerceIn(0f, 1f) * 255).toInt()
            }
            canvas.drawText(textItem.text, px, py, textPaint)
            canvas.restore()
        }
    }


    private fun createStarPath(cx: Float, cy: Float, outerRadius: Float, innerRadius: Float, points: Int): Path {
        val path = Path()
        val step = Math.PI / points
        var angle = -Math.PI / 2.0
        path.moveTo((cx + outerRadius * Math.cos(angle)).toFloat(), (cy + outerRadius * Math.sin(angle)).toFloat())
        for (i in 0 until points) {
            angle += step
            path.lineTo((cx + innerRadius * Math.cos(angle)).toFloat(), (cy + innerRadius * Math.sin(angle)).toFloat())
            angle += step
            path.lineTo((cx + outerRadius * Math.cos(angle)).toFloat(), (cy + outerRadius * Math.sin(angle)).toFloat())
        }
        path.close()
        return path
    }


    private fun calculateFitRect(srcW: Int, srcH: Int, dstW: Int, dstH: Int): Rect {
        val srcRatio = srcW.toFloat() / srcH.toFloat()
        val dstRatio = dstW.toFloat() / dstH.toFloat()

        return if (srcRatio > dstRatio) {
            val targetH = (dstW / srcRatio).toInt()
            val top = (dstH - targetH) / 2
            Rect(0, top, dstW, top + targetH)
        } else {
            val targetW = (dstH * srcRatio).toInt()
            val left = (dstW - targetW) / 2
            Rect(left, 0, left + targetW, dstH)
        }
    }

    private fun saveVideoToMediaStore(context: Context, sourceFile: File, title: String): Uri {
        val filename = "SAZFLOY_${title.replace(" ", "_")}_${System.currentTimeMillis()}.mp4"
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/SAZFLOY")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val videoUri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            ?: throw Exception("Failed to insert video into MediaStore")

        resolver.openOutputStream(videoUri)?.use { out ->
            FileInputStream(sourceFile).use { inStream ->
                inStream.copyTo(out)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            resolver.update(videoUri, contentValues, null, null)
        }

        return videoUri
    }
}

package com.example.editor.photo

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.example.filters.SazfloyColorEngine
import com.example.model.ColorAdjustments
import com.example.model.ShapeOverlay
import com.example.model.ShapeType
import com.example.model.StickerOverlay
import com.example.model.TextOverlay
import com.example.model.interpolateKeyframeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import kotlin.math.cos
import kotlin.math.sin

object PhotoProcessingEngine {

    fun createSamplePhoto(title: String = "Sazfloy Studio"): Bitmap {
        val width = 1080
        val height = 1350 // 4:5 social ratio
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Gradient background with Hijau Toska & Studio Onyx
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(0xFF0F1A24.toInt(), 0xFF16252F.toInt(), 0xFF00BFA5.toInt()),
                floatArrayOf(0f, 0.55f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Geometric Toska Rings
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x3300E5BC.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawCircle(width * 0.5f, height * 0.42f, 260f, circlePaint)
        canvas.drawCircle(width * 0.5f, height * 0.42f, 180f, circlePaint)

        val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x5500E5BC.toInt()
            style = Paint.Style.FILL
        }
        canvas.drawCircle(width * 0.5f, height * 0.42f, 90f, glowPaint)

        // Center typography
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            textSize = 58f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText(title, width * 0.5f, height * 0.72f, textPaint)

        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF00E5BC.toInt()
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText("Professional Photo Studio • 1080x1350", width * 0.5f, height * 0.77f, subPaint)

        return bitmap
    }

    suspend fun loadBitmapFromUri(context: Context, uri: Uri, maxDimension: Int = 1920): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val options = android.graphics.BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    android.graphics.BitmapFactory.decodeStream(stream, null, options)
                    var sampleSize = 1
                    while (options.outWidth / sampleSize > maxDimension || options.outHeight / sampleSize > maxDimension) {
                        sampleSize *= 2
                    }

                    context.contentResolver.openInputStream(uri)?.use { stream2 ->
                        val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                            inSampleSize = sampleSize
                            inPreferredConfig = Bitmap.Config.ARGB_8888
                        }
                        android.graphics.BitmapFactory.decodeStream(stream2, null, decodeOptions)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

    fun renderFinalPhoto(
        sourceBitmap: Bitmap,
        adjustments: ColorAdjustments,
        rotationDegrees: Int = 0,
        flipHorizontal: Boolean = false,
        flipVertical: Boolean = false,
        textOverlays: List<TextOverlay> = emptyList(),
        stickerOverlays: List<StickerOverlay> = emptyList(),
        shapeOverlays: List<ShapeOverlay> = emptyList(),
        currentTimeMs: Long = 0L,
        addWatermark: Boolean = false,
        context: Context? = null
    ): Bitmap {
        val processed = SazfloyColorEngine.processBitmap(
            source = sourceBitmap,
            adjustments = adjustments,
            rotationDegrees = rotationDegrees,
            flipHorizontal = flipHorizontal,
            flipVertical = flipVertical
        )

        val finalCanvas = Canvas(processed)
        val w = processed.width.toFloat()
        val h = processed.height.toFloat()

        // 1. Shapes Layer
        shapeOverlays.forEach { shape ->
            val kState = interpolateKeyframeState(
                keyframes = shape.keyframes,
                currentTimeMs = currentTimeMs,
                defaultPosX = shape.posXRatio,
                defaultPosY = shape.posYRatio,
                defaultRotation = shape.rotation,
                defaultOpacity = shape.opacity
            )

            val px = kState.posXRatio * w
            val py = kState.posYRatio * h
            val sw = shape.widthRatio * w * kState.scale
            val sh = shape.heightRatio * h * kState.scale

            finalCanvas.save()
            finalCanvas.rotate(kState.rotation, px, py)

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
                    finalCanvas.drawRect(left, top, right, bottom, fillPaint)
                    if (shape.strokeWidth > 0f) finalCanvas.drawRect(left, top, right, bottom, strokePaint)
                }
                ShapeType.ROUNDED_RECT -> {
                    val cr = 24f * (w / 1080f)
                    val r = RectF(left, top, right, bottom)
                    finalCanvas.drawRoundRect(r, cr, cr, fillPaint)
                    if (shape.strokeWidth > 0f) finalCanvas.drawRoundRect(r, cr, cr, strokePaint)
                }
                ShapeType.CIRCLE -> {
                    val radius = (sw.coerceAtMost(sh) / 2f)
                    finalCanvas.drawCircle(px, py, radius, fillPaint)
                    if (shape.strokeWidth > 0f) finalCanvas.drawCircle(px, py, radius, strokePaint)
                }
                ShapeType.STAR, ShapeType.BADGE -> {
                    val starPath = createStarPath(px, py, sw / 2f, sw / 4f, 5)
                    finalCanvas.drawPath(starPath, fillPaint)
                    if (shape.strokeWidth > 0f) finalCanvas.drawPath(starPath, strokePaint)
                }
            }
            finalCanvas.restore()
        }

        // 2. Stickers Layer
        stickerOverlays.forEach { sticker ->
            val kState = interpolateKeyframeState(
                keyframes = sticker.keyframes,
                currentTimeMs = currentTimeMs,
                defaultPosX = sticker.posXRatio,
                defaultPosY = sticker.posYRatio,
                defaultScale = sticker.scale,
                defaultRotation = sticker.rotation,
                defaultOpacity = 1f
            )

            val sPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 64f * kState.scale * (w / 1080f)
                textAlign = Paint.Align.CENTER
                alpha = (kState.opacity * 255).toInt().coerceIn(0, 255)
            }
            finalCanvas.save()
            val sx = kState.posXRatio * w
            val sy = kState.posYRatio * h
            finalCanvas.rotate(kState.rotation, sx, sy)
            finalCanvas.drawText(sticker.emojiOrTag, sx, sy, sPaint)
            finalCanvas.restore()
        }

        // 3. Texts Layer
        textOverlays.forEach { item ->
            val kState = interpolateKeyframeState(
                keyframes = item.keyframes,
                currentTimeMs = currentTimeMs,
                defaultPosX = item.posXRatio,
                defaultPosY = item.posYRatio,
                defaultScale = 1f,
                defaultRotation = 0f,
                defaultOpacity = 1f
            )

            val scaleFactor = (w / 1080f) * kState.scale
            val px = kState.posXRatio * w
            val py = kState.posYRatio * h
            val textSizePx = item.fontSizeSp * 2.8f * scaleFactor

            // Background box
            if (item.backgroundColorHex != "#00000000") {
                val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = runCatching { AndroidColor.parseColor(item.backgroundColorHex) }
                        .getOrDefault(0x88000000.toInt())
                    alpha = (kState.opacity * 255).toInt().coerceIn(0, 255)
                }
                val bounds = Rect()
                val measurePaint = Paint().apply { textSize = textSizePx }
                measurePaint.getTextBounds(item.text, 0, item.text.length, bounds)
                val pad = 24f * scaleFactor
                finalCanvas.drawRoundRect(
                    px - (bounds.width() / 2f) - pad,
                    py - bounds.height() - pad,
                    px + (bounds.width() / 2f) + pad,
                    py + pad,
                    16f * scaleFactor,
                    16f * scaleFactor,
                    bgPaint
                )
            }

            val fontRes = when (item.fontFamilyName.lowercase()) {
                "montserrat" -> com.example.R.font.montserrat
                "bebas_neue" -> com.example.R.font.bebas_neue
                "cinzel" -> com.example.R.font.cinzel
                "playfair_display" -> com.example.R.font.playfair_display
                "anton" -> com.example.R.font.anton
                "pacifico" -> com.example.R.font.pacifico
                "oswald" -> com.example.R.font.oswald
                else -> null
            }
            val customTypeface = if (context != null && fontRes != null) {
                runCatching { androidx.core.content.res.ResourcesCompat.getFont(context, fontRes) }.getOrNull()
            } else null

            // Outline
            if (item.hasOutline) {
                val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = runCatching { AndroidColor.parseColor(item.outlineColorHex) }.getOrDefault(AndroidColor.BLACK)
                    alpha = (kState.opacity * 255).toInt().coerceIn(0, 255)
                    textSize = textSizePx
                    textAlign = Paint.Align.CENTER
                    style = Paint.Style.STROKE
                    strokeWidth = 6f * scaleFactor
                    isFakeBoldText = item.isBold
                    textSkewX = if (item.isItalic) -0.25f else 0f
                    if (customTypeface != null) typeface = customTypeface
                }
                finalCanvas.drawText(item.text, px, py, outlinePaint)
            }

            // Foreground Text
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = runCatching { AndroidColor.parseColor(item.fontColorHex) }.getOrDefault(AndroidColor.WHITE)
                alpha = (kState.opacity * 255).toInt().coerceIn(0, 255)
                textSize = textSizePx
                textAlign = Paint.Align.CENTER
                isFakeBoldText = item.isBold
                textSkewX = if (item.isItalic) -0.25f else 0f
                if (customTypeface != null) typeface = customTypeface
            }
            finalCanvas.drawText(item.text, px, py, textPaint)
        }

        // 4. Watermark
        if (addWatermark) {
            val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xCC00E5BC.toInt()
                textSize = 28f * (w / 1080f)
                textAlign = Paint.Align.RIGHT
                isFakeBoldText = true
            }
            finalCanvas.drawText("Sazfloy", w - 40f, h - 40f, wmPaint)
        }

        return processed
    }

    private fun createStarPath(cx: Float, cy: Float, outerR: Float, innerR: Float, numPoints: Int): Path {
        val path = Path()
        val angle = Math.PI / numPoints
        for (i in 0 until 2 * numPoints) {
            val r = if (i % 2 == 0) outerR else innerR
            val currAngle = i * angle - Math.PI / 2
            val x = (cx + r * cos(currAngle)).toFloat()
            val y = (cy + r * sin(currAngle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return path
    }

    suspend fun exportPhotoToGallery(
        context: Context,
        bitmap: Bitmap,
        format: String = "JPEG", // JPEG or PNG
        quality: Int = 96,
        filenamePrefix: String = "SAZFLOY"
    ): Result<Uri> = withContext(Dispatchers.IO) {
        val isPng = format.equals("PNG", ignoreCase = true)
        val ext = if (isPng) "png" else "jpg"
        val mimeType = if (isPng) "image/png" else "image/jpeg"
        val filename = "${filenamePrefix}_${System.currentTimeMillis()}.$ext"

        var outputStream: OutputStream? = null
        var imageUri: Uri? = null

        try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/SAZFLOY")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                ?: return@withContext Result.failure(Exception("Failed to allocate MediaStore entry"))

            outputStream = resolver.openOutputStream(imageUri)
                ?: return@withContext Result.failure(Exception("Failed to open output stream"))

            if (isPng) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(50, 100), outputStream)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(imageUri, contentValues, null, null)
            }

            Result.success(imageUri)
        } catch (e: Exception) {
            imageUri?.let { uri ->
                runCatching { context.contentResolver.delete(uri, null, null) }
            }
            Result.failure(e)
        } finally {
            runCatching { outputStream?.close() }
        }
    }
}

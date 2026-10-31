package com.example.media

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.model.AudioTrack
import com.example.model.ClipType
import com.example.model.MediaClip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object MediaManager {

    /**
     * Imports a media URI from Android Photo Picker safely into app internal storage
     * so that permissions never expire and files remain accessible permanently.
     */
    suspend fun importMediaFromPicker(
        context: Context,
        sourceUri: Uri,
        isVideo: Boolean
    ): MediaClip = withContext(Dispatchers.IO) {
        val clipId = UUID.randomUUID().toString()
        // Try persisting permission first
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                sourceUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        // Cache into app internal media directory for rock-solid stability
        val mediaDir = File(context.filesDir, "sazfloy_media").apply { mkdirs() }
        val ext = if (isVideo) "mp4" else "jpg"
        val destFile = File(mediaDir, "clip_${clipId.take(8)}.$ext")

        try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (_: Exception) {}

        val finalUriString = if (destFile.exists() && destFile.length() > 0) {
            Uri.fromFile(destFile).toString()
        } else {
            sourceUri.toString()
        }

        // Generate and cache thumbnail image
        val thumbsDir = File(context.filesDir, "sazfloy_thumbs").apply { mkdirs() }
        val thumbFile = File(thumbsDir, "thumb_${clipId.take(8)}.jpg")

        var durationMs = 3000L
        var rotation = 0

        if (isVideo) {
            durationMs = 4000L
            val retriever = MediaMetadataRetriever()
            try {
                if (destFile.exists()) {
                    retriever.setDataSource(destFile.absolutePath)
                } else {
                    retriever.setDataSource(context, sourceUri)
                }
                val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                if (durStr != null) {
                    durationMs = durStr.toLongOrNull()?.coerceAtLeast(500L) ?: 4000L
                }
                val rotStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                if (rotStr != null) {
                    rotation = rotStr.toIntOrNull() ?: 0
                }

                // Extract and save frame thumbnail
                val frame = retriever.getFrameAtTime(0L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                if (frame != null) {
                    FileOutputStream(thumbFile).use { out ->
                        frame.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                }
            } catch (_: Exception) {
            } finally {
                runCatching { retriever.release() }
            }

            MediaClip(
                id = clipId,
                uri = finalUriString,
                thumbnailUri = if (thumbFile.exists() && thumbFile.length() > 0) Uri.fromFile(thumbFile).toString() else null,
                name = "Video Clip",
                type = ClipType.VIDEO,
                durationMs = durationMs,
                trimStartMs = 0L,
                trimEndMs = durationMs,
                rotationDegrees = rotation
            )
        } else {
            // Generate photo thumbnail
            try {
                val bmp = com.example.editor.photo.PhotoProcessingEngine.loadBitmapFromUri(context, Uri.parse(finalUriString), 240)
                if (bmp != null) {
                    FileOutputStream(thumbFile).use { out ->
                        bmp.compress(Bitmap.CompressFormat.JPEG, 85, out)
                    }
                }
            } catch (_: Exception) {}

            MediaClip(
                id = clipId,
                uri = finalUriString,
                thumbnailUri = if (thumbFile.exists() && thumbFile.length() > 0) Uri.fromFile(thumbFile).toString() else null,
                name = "Photo Clip",
                type = ClipType.PHOTO,
                durationMs = 3000L,
                trimStartMs = 0L,
                trimEndMs = 3000L
            )
        }
    }

    /**
     * Extract thumbnail from video or photo
     */
    suspend fun extractThumbnail(context: Context, clip: MediaClip, timeMs: Long = 0L): Bitmap? =
        withContext(Dispatchers.IO) {
            try {
                if (!clip.thumbnailUri.isNullOrBlank()) {
                    val thumbUri = Uri.parse(clip.thumbnailUri)
                    val thumbPath = thumbUri.path
                    if (thumbPath != null && File(thumbPath).exists()) {
                        val bmp = android.graphics.BitmapFactory.decodeFile(thumbPath)
                        if (bmp != null) return@withContext bmp
                    }
                }

                if (clip.uri.isNotBlank()) {
                    val uri = Uri.parse(clip.uri)
                    if (clip.type == ClipType.VIDEO) {
                        val retriever = MediaMetadataRetriever()
                        try {
                            if (clip.uri.startsWith("file://")) {
                                retriever.setDataSource(File(uri.path ?: "").absolutePath)
                            } else {
                                retriever.setDataSource(context, uri)
                            }
                            val frame = retriever.getFrameAtTime(
                                (clip.trimStartMs + timeMs) * 1000L,
                                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                            )
                            if (frame != null) return@withContext frame
                        } finally {
                            runCatching { retriever.release() }
                        }
                    } else {
                        return@withContext com.example.editor.photo.PhotoProcessingEngine.loadBitmapFromUri(context, uri, 320)
                    }
                }
            } catch (_: Exception) {}
            null
        }

    /**
     * Automatically detect if media is video or photo and import
     */
    suspend fun importMediaAuto(context: Context, sourceUri: Uri): MediaClip {
        val mimeType = runCatching { context.contentResolver.getType(sourceUri) }.getOrNull()?.lowercase()
        var isVideo = mimeType?.startsWith("video") == true || sourceUri.toString().contains("video")
        if (!isVideo && mimeType == null) {
            val uriStr = sourceUri.toString().lowercase()
            if (uriStr.endsWith(".mp4") || uriStr.endsWith(".mkv") || uriStr.endsWith(".mov") || uriStr.endsWith(".3gp")) {
                isVideo = true
            } else if (uriStr.endsWith(".jpg") || uriStr.endsWith(".jpeg") || uriStr.endsWith(".png") || uriStr.endsWith(".webp")) {
                isVideo = false
            } else {
                val retriever = MediaMetadataRetriever()
                try {
                    retriever.setDataSource(context, sourceUri)
                    val hasVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO)
                    val dur = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    isVideo = hasVideo == "yes" || (dur != null && dur.toLongOrNull() ?: 0L > 0L)
                } catch (_: Exception) {} finally {
                    runCatching { retriever.release() }
                }
            }
        }
        return importMediaFromPicker(context, sourceUri, isVideo)
    }

    /**
     * Safely imports audio file (.mp3, .wav, .m4a) into app internal storage
     */
    suspend fun importAudio(context: Context, sourceUri: Uri): AudioTrack = withContext(Dispatchers.IO) {
        runCatching {
            context.contentResolver.takePersistableUriPermission(sourceUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val audioDir = File(context.filesDir, "sazfloy_audio").apply { mkdirs() }
        val destFile = File(audioDir, "audio_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.mp3")
        try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output -> input.copyTo(output) }
            }
        } catch (_: Exception) {}

        val finalUri = if (destFile.exists() && destFile.length() > 0) Uri.fromFile(destFile).toString() else sourceUri.toString()

        val retriever = MediaMetadataRetriever()
        var durMs = 15000L
        var trackTitle = "Custom Audio"
        try {
            if (destFile.exists()) {
                retriever.setDataSource(destFile.absolutePath)
            } else {
                retriever.setDataSource(context, sourceUri)
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull()?.let {
                if (it > 0) durMs = it
            }
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)?.let {
                if (it.isNotBlank()) trackTitle = it
            }
        } catch (_: Exception) {
        } finally {
            runCatching { retriever.release() }
        }

        AudioTrack(
            id = UUID.randomUUID().toString(),
            uri = finalUri,
            title = trackTitle,
            durationMs = durMs,
            volume = 1.0f
        )
    }
}

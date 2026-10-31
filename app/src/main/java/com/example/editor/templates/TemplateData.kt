package com.example.editor.templates

import com.example.model.AspectRatioMode
import com.example.model.AudioTrack
import com.example.model.ClipType
import com.example.model.ColorAdjustments
import com.example.model.EditingProject
import com.example.model.MediaClip
import com.example.model.ProjectType
import com.example.model.TextOverlay
import com.example.model.TransitionType
import java.util.UUID

data class VideoTemplate(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val aspectRatio: AspectRatioMode,
    val durationText: String,
    val filterPresetId: String,
    val adjustments: ColorAdjustments,
    val clips: List<MediaClip>,
    val texts: List<TextOverlay>,
    val audioTrack: AudioTrack?
) {
    fun instantiateProject(): EditingProject {
        return EditingProject(
            id = UUID.randomUUID().toString(),
            title = title,
            type = ProjectType.VIDEO,
            aspectRatio = aspectRatio,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            clips = clips.map { it.copy(id = UUID.randomUUID().toString()) },
            textOverlays = texts.map { it.copy(id = UUID.randomUUID().toString()) },
            audioTracks = audioTrack?.let { listOf(it.copy(id = UUID.randomUUID().toString())) } ?: emptyList(),
            adjustments = adjustments
        )
    }
}

object TemplateData {

    val TEMPLATES: List<VideoTemplate> = listOf(
        VideoTemplate(
            id = "cinematic_urban",
            title = "Cinematic Urban",
            category = "Cinematic",
            description = "High contrast anamorphic style with cinematic bars and dramatic transitions.",
            aspectRatio = AspectRatioMode.RATIO_16_9,
            durationText = "9.0s",
            filterPresetId = "cinematic",
            adjustments = ColorAdjustments(filterPresetId = "cinematic", contrast = 18f, vignette = 30f),
            clips = listOf(
                MediaClip(name = "Intro Cityscape", durationMs = 3000L, transitionType = TransitionType.FADE),
                MediaClip(name = "Urban Movement", durationMs = 3000L, transitionType = TransitionType.ZOOM_IN),
                MediaClip(name = "Dusk Lights", durationMs = 3000L, transitionType = TransitionType.FLASH_WHITE)
            ),
            texts = listOf(
                TextOverlay(text = "SAZFLOY CINEMA", startMs = 0L, durationMs = 3000L, posYRatio = 0.5f, fontSizeSp = 32f),
                TextOverlay(text = "URBAN STORIES", startMs = 3000L, durationMs = 3000L, posYRatio = 0.8f, fontSizeSp = 22f)
            ),
            audioTrack = AudioTrack(uri = "built_in_cinematic", title = "Midnight Beats", durationMs = 9000L)
        ),
        VideoTemplate(
            id = "travel_wanderlust",
            title = "Travel Wanderlust",
            category = "Travel",
            description = "Vibrant sunny hues perfect for vacation, road trips, and adventures.",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            durationText = "7.5s",
            filterPresetId = "warm",
            adjustments = ColorAdjustments(filterPresetId = "warm", saturation = 20f, temperature = 25f),
            clips = listOf(
                MediaClip(name = "Takeoff / Departure", durationMs = 2500L, transitionType = TransitionType.SLIDE_LEFT),
                MediaClip(name = "Scenic View", durationMs = 2500L, transitionType = TransitionType.DISSOLVE),
                MediaClip(name = "Sunset Horizon", durationMs = 2500L, transitionType = TransitionType.FADE)
            ),
            texts = listOf(
                TextOverlay(text = "EXPLORE THE WORLD", startMs = 500L, durationMs = 3500L, posYRatio = 0.7f, fontSizeSp = 26f)
            ),
            audioTrack = AudioTrack(uri = "built_in_travel", title = "Tropical Breeze", durationMs = 7500L)
        ),
        VideoTemplate(
            id = "daily_vlog",
            title = "Daily Vlog Story",
            category = "Daily vlog",
            description = "Clean aesthetic with natural tones and upbeat pacing.",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            durationText = "6.0s",
            filterPresetId = "natural",
            adjustments = ColorAdjustments(filterPresetId = "natural", brightness = 6f, saturation = 10f),
            clips = listOf(
                MediaClip(name = "Morning Routine", durationMs = 2000L, transitionType = TransitionType.FADE),
                MediaClip(name = "Coffee Moments", durationMs = 2000L, transitionType = TransitionType.DISSOLVE),
                MediaClip(name = "Working Space", durationMs = 2000L, transitionType = TransitionType.NONE)
            ),
            texts = listOf(
                TextOverlay(text = "A Day In My Life ✨", startMs = 0L, durationMs = 2500L, posYRatio = 0.2f, fontSizeSp = 24f)
            ),
            audioTrack = AudioTrack(uri = "built_in_vlog", title = "Acoustic Morning", durationMs = 6000L)
        ),
        VideoTemplate(
            id = "school_memories",
            title = "School Memories",
            category = "School memories",
            description = "Nostalgic film style to commemorate campus, friends, and milestones.",
            aspectRatio = AspectRatioMode.RATIO_16_9,
            durationText = "8.0s",
            filterPresetId = "film",
            adjustments = ColorAdjustments(filterPresetId = "film", contrast = 10f, highlights = -10f),
            clips = listOf(
                MediaClip(name = "Hallway Steps", durationMs = 2500L, transitionType = TransitionType.DISSOLVE),
                MediaClip(name = "Graduation Day", durationMs = 3000L, transitionType = TransitionType.ZOOM_IN),
                MediaClip(name = "Classroom Laughs", durationMs = 2500L, transitionType = TransitionType.FADE)
            ),
            texts = listOf(
                TextOverlay(text = "CLASS OF 2026", startMs = 0L, durationMs = 4000L, posYRatio = 0.75f, fontSizeSp = 28f)
            ),
            audioTrack = AudioTrack(uri = "built_in_school", title = "Memories Forever", durationMs = 8000L)
        ),
        VideoTemplate(
            id = "birthday_bash",
            title = "Birthday Celebration",
            category = "Birthday",
            description = "Festive, vibrant tones with playful pop text animations.",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            durationText = "6.5s",
            filterPresetId = "portrait",
            adjustments = ColorAdjustments(filterPresetId = "portrait", brightness = 10f, saturation = 15f),
            clips = listOf(
                MediaClip(name = "Cake & Candles", durationMs = 3000L, transitionType = TransitionType.FLASH_WHITE),
                MediaClip(name = "Celebration Cheers", durationMs = 3500L, transitionType = TransitionType.FADE)
            ),
            texts = listOf(
                TextOverlay(text = "HAPPY BIRTHDAY! 🎂", startMs = 0L, durationMs = 3500L, posYRatio = 0.5f, fontSizeSp = 30f)
            ),
            audioTrack = AudioTrack(uri = "built_in_birthday", title = "Party Anthem", durationMs = 6500L)
        ),
        VideoTemplate(
            id = "photo_slideshow",
            title = "Dynamic Photo Slideshow",
            category = "Photo slideshow",
            description = "Turns favorite photos into an elegant rhythmic slideshow montage.",
            aspectRatio = AspectRatioMode.RATIO_1_1,
            durationText = "8.0s",
            filterPresetId = "natural",
            adjustments = ColorAdjustments(filterPresetId = "natural", contrast = 12f),
            clips = listOf(
                MediaClip(name = "Photo Frame 1", type = ClipType.PHOTO, durationMs = 2000L, transitionType = TransitionType.DISSOLVE),
                MediaClip(name = "Photo Frame 2", type = ClipType.PHOTO, durationMs = 2000L, transitionType = TransitionType.SLIDE_LEFT),
                MediaClip(name = "Photo Frame 3", type = ClipType.PHOTO, durationMs = 2000L, transitionType = TransitionType.ZOOM_IN),
                MediaClip(name = "Photo Frame 4", type = ClipType.PHOTO, durationMs = 2000L, transitionType = TransitionType.FADE)
            ),
            texts = listOf(
                TextOverlay(text = "Special Moments", startMs = 0L, durationMs = 4000L, posYRatio = 0.8f, fontSizeSp = 22f)
            ),
            audioTrack = AudioTrack(uri = "built_in_slideshow", title = "Gentle Flow", durationMs = 8000L)
        ),
        VideoTemplate(
            id = "portrait_reel",
            title = "Portrait Viral Reel",
            category = "Portrait reel",
            description = "High engagement vertical format tailored for TikTok and Instagram Reels.",
            aspectRatio = AspectRatioMode.RATIO_9_16,
            durationText = "6.0s",
            filterPresetId = "portrait",
            adjustments = ColorAdjustments(filterPresetId = "portrait", contrast = 14f, brightness = 8f),
            clips = listOf(
                MediaClip(name = "Hook Shot", durationMs = 1500L, transitionType = TransitionType.FLASH_WHITE),
                MediaClip(name = "Main Highlight", durationMs = 2500L, transitionType = TransitionType.ZOOM_IN),
                MediaClip(name = "Outro Action", durationMs = 2000L, transitionType = TransitionType.FADE)
            ),
            texts = listOf(
                TextOverlay(text = "WATCH TILL THE END 👀", startMs = 0L, durationMs = 2500L, posYRatio = 0.25f, fontSizeSp = 24f)
            ),
            audioTrack = AudioTrack(uri = "built_in_reel", title = "Viral Trend Beat", durationMs = 6000L)
        ),
        VideoTemplate(
            id = "landscape_cinematic",
            title = "Landscape Epic Cinema",
            category = "Landscape cinematic",
            description = "Widescreen epic scope with lush colors and deep contrast.",
            aspectRatio = AspectRatioMode.RATIO_16_9,
            durationText = "9.0s",
            filterPresetId = "landscape",
            adjustments = ColorAdjustments(filterPresetId = "landscape", contrast = 16f, saturation = 22f),
            clips = listOf(
                MediaClip(name = "Mountain Range", durationMs = 3000L, transitionType = TransitionType.FADE),
                MediaClip(name = "River Flow", durationMs = 3000L, transitionType = TransitionType.DISSOLVE),
                MediaClip(name = "Sunset Valley", durationMs = 3000L, transitionType = TransitionType.FADE)
            ),
            texts = listOf(
                TextOverlay(text = "THE BEAUTY OF NATURE", startMs = 1000L, durationMs = 4000L, posYRatio = 0.5f, fontSizeSp = 30f)
            ),
            audioTrack = AudioTrack(uri = "built_in_nature", title = "Epic Horizons", durationMs = 9000L)
        ),
        VideoTemplate(
            id = "minimalist_clean",
            title = "Minimalist Clean",
            category = "Minimalist",
            description = "Crisp, uncluttered aesthetic for modern portfolios and product showcases.",
            aspectRatio = AspectRatioMode.RATIO_4_5,
            durationText = "6.0s",
            filterPresetId = "cool",
            adjustments = ColorAdjustments(filterPresetId = "cool", contrast = 10f, saturation = -5f),
            clips = listOf(
                MediaClip(name = "Detail Focus", durationMs = 3000L, transitionType = TransitionType.DISSOLVE),
                MediaClip(name = "Product Overview", durationMs = 3000L, transitionType = TransitionType.FADE)
            ),
            texts = listOf(
                TextOverlay(text = "SIMPLICITY & FORM", startMs = 500L, durationMs = 3000L, posYRatio = 0.7f, fontSizeSp = 20f)
            ),
            audioTrack = AudioTrack(uri = "built_in_minimal", title = "Ambient Pulse", durationMs = 6000L)
        ),
        VideoTemplate(
            id = "documentary_film",
            title = "Documentary Heritage",
            category = "Documentary",
            description = "Classic Black & White storytelling tone with high dynamic range.",
            aspectRatio = AspectRatioMode.RATIO_16_9,
            durationText = "7.5s",
            filterPresetId = "bw",
            adjustments = ColorAdjustments(filterPresetId = "bw", contrast = 22f, vignette = 20f),
            clips = listOf(
                MediaClip(name = "Historical Chronicle", durationMs = 3500L, transitionType = TransitionType.DISSOLVE),
                MediaClip(name = "Interview Scene", durationMs = 4000L, transitionType = TransitionType.FADE)
            ),
            texts = listOf(
                TextOverlay(text = "A HISTORICAL JOURNEY", startMs = 500L, durationMs = 4000L, posYRatio = 0.8f, fontSizeSp = 24f)
            ),
            audioTrack = AudioTrack(uri = "built_in_doc", title = "Subtle Reflections", durationMs = 7500L)
        )
    )
}

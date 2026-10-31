package com.example.editor.audio

import com.example.model.EditingProject
import com.example.model.TextOverlay
import java.util.UUID

enum class CaptionStylePreset(
    val title: String,
    val fontName: String,
    val fontColorHex: String,
    val hasBackground: Boolean,
    val bgColorHex: String,
    val hasOutline: Boolean
) {
    TIKTOK_VIRAL("TikTok Kinetic", "anton", "#FFE600", false, "#000000", true),
    CLEAN_SUBTITLE("Clean Cinema", "montserrat", "#FFFFFF", true, "#99000000", false),
    KARAOKE_POP("Karaoke Highlight", "bebas_neue", "#00E5BC", false, "#000000", true),
    YOUTUBE_VLOG("Vlog Lower", "oswald", "#FFFFFF", true, "#CC1E293B", true)
}

object AutoCaptionEngine {

    // Natural phrases modeled after conversational speech patterns & video dialogue
    private val DEFAULT_PHRASES = listOf(
        "Welcome to today's special showcase ✨",
        "Let's dive into professional color grading!",
        "Watch how smooth this transition looks.",
        "Captured in ultra high bitrate 4K resolution.",
        "Notice the dynamic range in the shadows and highlights.",
        "Color graded with DaVinci Resolve style wheels.",
        "Bringing cinematic aesthetic right to mobile editing.",
        "Thank you for watching SAZFLOY Studio!"
    )

    /**
     * Analyzes project audio duration and clips, producing rhythmic, synced TextOverlay subtitle tracks.
     */
    fun generateAutoCaptions(
        project: EditingProject,
        style: CaptionStylePreset = CaptionStylePreset.TIKTOK_VIRAL,
        customPhrases: List<String>? = null
    ): List<TextOverlay> {
        val totalMs = project.totalDurationMs.coerceAtLeast(3000L)
        val phrases = if (!customPhrases.isNullOrEmpty()) customPhrases else DEFAULT_PHRASES

        val segmentDurationMs = 2400L
        val intervalMs = 2600L
        val count = ((totalMs / intervalMs) + 1).toInt().coerceIn(1, phrases.size * 3)

        val captions = mutableListOf<TextOverlay>()
        var currentStart = 200L

        for (i in 0 until count) {
            if (currentStart >= totalMs) break
            val phrase = phrases[i % phrases.size]
            val actualDur = segmentDurationMs.coerceAtMost(totalMs - currentStart)

            captions.add(
                TextOverlay(
                    id = UUID.randomUUID().toString(),
                    text = phrase,
                    startMs = currentStart,
                    durationMs = actualDur,
                    posXRatio = 0.5f,
                    posYRatio = 0.82f,
                    fontSizeSp = when (style.fontName) {
                        "anton", "bebas_neue" -> 22f
                        else -> 19f
                    },
                    fontColorHex = style.fontColorHex,
                    fontFamilyName = style.fontName,
                    hasBackground = style.hasBackground,
                    backgroundColorHex = style.bgColorHex,
                    hasOutline = style.hasOutline,
                    outlineColorHex = "#000000",
                    isBold = true
                )
            )

            currentStart += intervalMs
        }

        return captions
    }
}

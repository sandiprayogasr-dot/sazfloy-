package com.example.editor.templates

import com.example.model.ShapeOverlay
import com.example.model.ShapeType
import com.example.model.TextOverlay
import com.example.model.TitleTemplate
import java.util.UUID

object TitleTemplates {

    val CATALOG: List<TitleTemplate> = listOf(
        TitleTemplate(
            id = "cinematic_epic",
            name = "Cinematic Epic",
            category = "Cinema",
            primaryText = "SAZFLOY STUDIOS",
            secondaryText = "A FILM BY CREATOR",
            fontFamilyName = "cinzel",
            fontColorHex = "#FFE082",
            secondaryColorHex = "#CCCCCC",
            hasBackdrop = false,
            hasOutline = true,
            outlineColorHex = "#000000"
        ),
        TitleTemplate(
            id = "cyberpunk_neon",
            name = "Cyberpunk Neon",
            category = "Glitch / Sci-Fi",
            primaryText = "CYBERNETIC 2026",
            secondaryText = "OVERDRIVE PROTOCOL",
            fontFamilyName = "anton",
            fontColorHex = "#00E5BC",
            secondaryColorHex = "#FF2D75",
            hasBackdrop = true,
            backdropColorHex = "#CC0D131A",
            hasOutline = true,
            outlineColorHex = "#FF2D75",
            shapeType = ShapeType.ROUNDED_RECT,
            shapeFillColor = "#1A0033"
        ),
        TitleTemplate(
            id = "minimal_lower_third",
            name = "Modern Lower Third",
            category = "Minimal / News",
            primaryText = "Alex Morgan",
            secondaryText = "Senior Film Director & Colorist",
            fontFamilyName = "montserrat",
            fontColorHex = "#FFFFFF",
            secondaryColorHex = "#00E5BC",
            hasBackdrop = true,
            backdropColorHex = "#E6121B24",
            hasOutline = false,
            shapeType = ShapeType.RECTANGLE,
            shapeFillColor = "#00BFA5"
        ),
        TitleTemplate(
            id = "youtube_vlog_intro",
            name = "YouTube Creator Pop",
            category = "Vlog / Social",
            primaryText = "EPIC NEW ADVENTURE!",
            secondaryText = "DON'T FORGET TO SUBSCRIBE 🔔",
            fontFamilyName = "bebas_neue",
            fontColorHex = "#FFEB3B",
            secondaryColorHex = "#FFFFFF",
            hasBackdrop = true,
            backdropColorHex = "#E6D32F2F",
            hasOutline = true,
            outlineColorHex = "#000000"
        ),
        TitleTemplate(
            id = "luxury_editorial",
            name = "Luxury Vogue",
            category = "Fashion / Aesthetic",
            primaryText = "THE SUMMER EDITORIAL",
            secondaryText = "PARIS • MILAN • TOKYO",
            fontFamilyName = "playfair_display",
            fontColorHex = "#FFF8E7",
            secondaryColorHex = "#D7CCC8",
            hasBackdrop = false,
            hasOutline = true,
            outlineColorHex = "#1A1A1A"
        ),
        TitleTemplate(
            id = "breaking_news",
            name = "Breaking Broadcast",
            category = "News / Media",
            primaryText = "BREAKING SPECIAL REPORT",
            secondaryText = "LIVE FROM LOCATION • 10:45 AM",
            fontFamilyName = "oswald",
            fontColorHex = "#FFFFFF",
            secondaryColorHex = "#FFCDD2",
            hasBackdrop = true,
            backdropColorHex = "#E6B71C1C",
            hasOutline = true,
            outlineColorHex = "#000000",
            shapeType = ShapeType.RECTANGLE,
            shapeFillColor = "#B71C1C"
        ),
        TitleTemplate(
            id = "aesthetic_handwritten",
            name = "Aesthetic Script",
            category = "Vlog / Lifestyle",
            primaryText = "Golden Hour Whispers",
            secondaryText = "memories made with love",
            fontFamilyName = "pacifico",
            fontColorHex = "#FFE57F",
            secondaryColorHex = "#FFFFFF",
            hasBackdrop = false,
            hasOutline = true,
            outlineColorHex = "#3E2723"
        ),
        TitleTemplate(
            id = "bold_streamer",
            name = "Streamer Champion",
            category = "Gaming",
            primaryText = "VICTORY ROYALE #1",
            secondaryText = "INSANE CLUTCH MOMENT",
            fontFamilyName = "anton",
            fontColorHex = "#00E5BC",
            secondaryColorHex = "#7C4DFF",
            hasBackdrop = true,
            backdropColorHex = "#E61A0033",
            hasOutline = true,
            outlineColorHex = "#000000"
        )
    )

    fun createTitleOverlays(
        template: TitleTemplate,
        startMs: Long,
        durationMs: Long = 3500L
    ): List<TextOverlay> {
        val result = mutableListOf<TextOverlay>()

        // Primary Title
        result.add(
            TextOverlay(
                id = UUID.randomUUID().toString(),
                text = template.primaryText,
                startMs = startMs,
                durationMs = durationMs,
                posXRatio = 0.5f,
                posYRatio = if (template.secondaryText != null) 0.70f else 0.75f,
                fontSizeSp = when (template.fontFamilyName) {
                    "bebas_neue", "anton" -> 32f
                    "cinzel" -> 28f
                    else -> 26f
                },
                fontColorHex = template.fontColorHex,
                fontFamilyName = template.fontFamilyName,
                hasBackground = template.hasBackdrop,
                backgroundColorHex = template.backdropColorHex,
                hasOutline = template.hasOutline,
                outlineColorHex = template.outlineColorHex,
                isBold = true
            )
        )

        // Secondary subtitle if present
        if (!template.secondaryText.isNullOrBlank()) {
            result.add(
                TextOverlay(
                    id = UUID.randomUUID().toString(),
                    text = template.secondaryText,
                    startMs = startMs + 200L,
                    durationMs = (durationMs - 200L).coerceAtLeast(1000L),
                    posXRatio = 0.5f,
                    posYRatio = 0.78f,
                    fontSizeSp = 16f,
                    fontColorHex = template.secondaryColorHex ?: "#FFFFFF",
                    fontFamilyName = if (template.fontFamilyName == "pacifico") "montserrat" else template.fontFamilyName,
                    hasBackground = false,
                    hasOutline = true,
                    outlineColorHex = "#000000",
                    isBold = false
                )
            )
        }

        return result
    }
}

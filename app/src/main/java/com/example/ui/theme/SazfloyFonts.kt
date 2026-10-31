package com.example.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R

data class SazfloyFontItem(
    val id: String,
    val displayName: String,
    val category: String,
    val family: FontFamily
)

object SazfloyFonts {
    val Montserrat = FontFamily(Font(R.font.montserrat, FontWeight.Normal))
    val BebasNeue = FontFamily(Font(R.font.bebas_neue, FontWeight.Normal))
    val Cinzel = FontFamily(Font(R.font.cinzel, FontWeight.Normal))
    val PlayfairDisplay = FontFamily(Font(R.font.playfair_display, FontWeight.Normal))
    val Anton = FontFamily(Font(R.font.anton, FontWeight.Normal))
    val Pacifico = FontFamily(Font(R.font.pacifico, FontWeight.Normal))
    val Oswald = FontFamily(Font(R.font.oswald, FontWeight.Normal))

    val AVAILABLE_FONTS: List<SazfloyFontItem> = listOf(
        SazfloyFontItem("montserrat", "Montserrat", "Clean Modern", Montserrat),
        SazfloyFontItem("bebas_neue", "Bebas Neue", "Cinema Block", BebasNeue),
        SazfloyFontItem("cinzel", "Cinzel", "Epic Serif", Cinzel),
        SazfloyFontItem("playfair_display", "Playfair", "Luxury Classic", PlayfairDisplay),
        SazfloyFontItem("anton", "Anton", "Bold Headline", Anton),
        SazfloyFontItem("pacifico", "Pacifico", "Script Handwritten", Pacifico),
        SazfloyFontItem("oswald", "Oswald", "Condensed Punch", Oswald),
        SazfloyFontItem("sans", "Sans Serif", "Default", FontFamily.SansSerif),
        SazfloyFontItem("serif", "Serif", "Default", FontFamily.Serif),
        SazfloyFontItem("monospace", "Monospace", "Code/Retro", FontFamily.Monospace)
    )

    fun getFontFamily(id: String?): FontFamily {
        if (id.isNullOrBlank()) return Montserrat
        return AVAILABLE_FONTS.firstOrNull { it.id.equals(id, ignoreCase = true) }?.family ?: Montserrat
    }
}

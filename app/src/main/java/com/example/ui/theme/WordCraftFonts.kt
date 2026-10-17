package com.example.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.R

data class FontOption(
    val id: String,
    val displayName: String,
    val category: String,
    val fontFamily: FontFamily,
    val docxFontName: String
)

object WordCraftFonts {

    val InterFamily = FontFamily(Font(R.font.inter, FontWeight.Normal))
    val LoraFamily = FontFamily(Font(R.font.lora, FontWeight.Normal))
    val MerriweatherFamily = FontFamily(Font(R.font.merriweather, FontWeight.Normal))
    val PlayfairDisplayFamily = FontFamily(Font(R.font.playfair_display, FontWeight.Normal))
    val RobotoMonoFamily = FontFamily(Font(R.font.roboto_mono, FontWeight.Normal))
    val PacificoFamily = FontFamily(Font(R.font.pacifico, FontWeight.Normal))

    val availableFonts: List<FontOption> = listOf(
        FontOption(
            id = "Inter",
            displayName = "Inter",
            category = "Google Sans",
            fontFamily = InterFamily,
            docxFontName = "Inter"
        ),
        FontOption(
            id = "Merriweather",
            displayName = "Merriweather",
            category = "Editorial Serif",
            fontFamily = MerriweatherFamily,
            docxFontName = "Merriweather"
        ),
        FontOption(
            id = "Lora",
            displayName = "Lora",
            category = "Book Serif",
            fontFamily = LoraFamily,
            docxFontName = "Lora"
        ),
        FontOption(
            id = "Playfair Display",
            displayName = "Playfair Display",
            category = "Display Serif",
            fontFamily = PlayfairDisplayFamily,
            docxFontName = "Playfair Display"
        ),
        FontOption(
            id = "Roboto Mono",
            displayName = "Roboto Mono",
            category = "Monospace",
            fontFamily = RobotoMonoFamily,
            docxFontName = "Roboto Mono"
        ),
        FontOption(
            id = "Pacifico",
            displayName = "Pacifico",
            category = "Handwriting",
            fontFamily = PacificoFamily,
            docxFontName = "Pacifico"
        ),
        FontOption(
            id = "Sans Serif",
            displayName = "System Sans",
            category = "System",
            fontFamily = FontFamily.SansSerif,
            docxFontName = "Calibri"
        ),
        FontOption(
            id = "Serif",
            displayName = "System Serif",
            category = "System",
            fontFamily = FontFamily.Serif,
            docxFontName = "Times New Roman"
        ),
        FontOption(
            id = "Monospace",
            displayName = "System Mono",
            category = "System",
            fontFamily = FontFamily.Monospace,
            docxFontName = "Courier New"
        ),
        FontOption(
            id = "Cursive",
            displayName = "System Cursive",
            category = "System",
            fontFamily = FontFamily.Cursive,
            docxFontName = "Comic Sans MS"
        )
    )

    val presetFontSizes: List<Int> = listOf(
        8, 9, 10, 11, 12, 14, 15, 16, 18, 20, 22, 24, 28, 32, 36, 48, 72
    )

    fun resolveFontFamily(fontId: String?): FontFamily {
        if (fontId.isNullOrBlank()) return InterFamily
        return availableFonts.find { it.id.equals(fontId, ignoreCase = true) }?.fontFamily
            ?: InterFamily
    }

    fun resolveDocxFontName(fontId: String?): String {
        if (fontId.isNullOrBlank()) return "Calibri"
        return availableFonts.find { it.id.equals(fontId, ignoreCase = true) }?.docxFontName
            ?: fontId
    }
}

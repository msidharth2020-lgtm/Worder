package com.example.model

import java.nio.charset.StandardCharsets
import java.text.Normalizer

data class UnicodeSymbol(
    val symbol: String,
    val codePointHex: String,
    val name: String,
    val category: String
)

data class UnicodeCharInfo(
    val charDisplay: String,
    val codePointHex: String,
    val decimalCode: Int,
    val utf8Hex: String,
    val characterName: String
)

object UnicodeCatalog {

    val categories = listOf(
        "All",
        "Typography",
        "Math & Logic",
        "Greek",
        "Currency",
        "Arrows",
        "Fractions & Super",
        "Shapes"
    )

    val symbols: List<UnicodeSymbol> = listOf(
        // Typography & Legal
        UnicodeSymbol("—", "U+2014", "Em Dash", "Typography"),
        UnicodeSymbol("–", "U+2013", "En Dash", "Typography"),
        UnicodeSymbol("…", "U+2026", "Horizontal Ellipsis", "Typography"),
        UnicodeSymbol("•", "U+2022", "Bullet", "Typography"),
        UnicodeSymbol("§", "U+00A7", "Section Sign", "Typography"),
        UnicodeSymbol("¶", "U+00B6", "Pilcrow / Paragraph Sign", "Typography"),
        UnicodeSymbol("†", "U+2020", "Dagger", "Typography"),
        UnicodeSymbol("‡", "U+2021", "Double Dagger", "Typography"),
        UnicodeSymbol("©", "U+00A9", "Copyright Sign", "Typography"),
        UnicodeSymbol("®", "U+00AE", "Registered Sign", "Typography"),
        UnicodeSymbol("™", "U+2122", "Trade Mark Sign", "Typography"),
        UnicodeSymbol("‰", "U+2030", "Per Mille Sign", "Typography"),
        UnicodeSymbol("№", "U+2116", "Numero Sign", "Typography"),
        UnicodeSymbol("«", "U+00AB", "Left Double Angle Quote", "Typography"),
        UnicodeSymbol("»", "U+00BB", "Right Double Angle Quote", "Typography"),
        UnicodeSymbol("°", "U+00B0", "Degree Sign", "Typography"),

        // Math & Logic
        UnicodeSymbol("±", "U+00B1", "Plus-Minus Sign", "Math & Logic"),
        UnicodeSymbol("×", "U+00D7", "Multiplication Sign", "Math & Logic"),
        UnicodeSymbol("÷", "U+00F7", "Division Sign", "Math & Logic"),
        UnicodeSymbol("≈", "U+2248", "Almost Equal To", "Math & Logic"),
        UnicodeSymbol("≠", "U+2260", "Not Equal To", "Math & Logic"),
        UnicodeSymbol("≤", "U+2264", "Less-Than or Equal To", "Math & Logic"),
        UnicodeSymbol("≥", "U+2265", "Greater-Than or Equal To", "Math & Logic"),
        UnicodeSymbol("∞", "U+221E", "Infinity", "Math & Logic"),
        UnicodeSymbol("∑", "U+2211", "N-Ary Summation", "Math & Logic"),
        UnicodeSymbol("∏", "U+220F", "N-Ary Product", "Math & Logic"),
        UnicodeSymbol("√", "U+221A", "Square Root", "Math & Logic"),
        UnicodeSymbol("∫", "U+222B", "Integral", "Math & Logic"),
        UnicodeSymbol("∂", "U+2202", "Partial Differential", "Math & Logic"),
        UnicodeSymbol("∆", "U+2206", "Increment / Delta", "Math & Logic"),
        UnicodeSymbol("∇", "U+2207", "Nabla / Gradient", "Math & Logic"),
        UnicodeSymbol("∈", "U+2208", "Element Of", "Math & Logic"),
        UnicodeSymbol("∉", "U+2209", "Not an Element Of", "Math & Logic"),
        UnicodeSymbol("∩", "U+2229", "Intersection", "Math & Logic"),
        UnicodeSymbol("∪", "U+222A", "Union", "Math & Logic"),
        UnicodeSymbol("∀", "U+2200", "For All", "Math & Logic"),
        UnicodeSymbol("∃", "U+2203", "There Exists", "Math & Logic"),
        UnicodeSymbol("∴", "U+2234", "Therefore", "Math & Logic"),

        // Greek
        UnicodeSymbol("α", "U+03B1", "Greek Small Alpha", "Greek"),
        UnicodeSymbol("β", "U+03B2", "Greek Small Beta", "Greek"),
        UnicodeSymbol("γ", "U+03B3", "Greek Small Gamma", "Greek"),
        UnicodeSymbol("δ", "U+03B4", "Greek Small Delta", "Greek"),
        UnicodeSymbol("ε", "U+03B5", "Greek Small Epsilon", "Greek"),
        UnicodeSymbol("θ", "U+03B8", "Greek Small Theta", "Greek"),
        UnicodeSymbol("λ", "U+03BB", "Greek Small Lambda", "Greek"),
        UnicodeSymbol("μ", "U+03BC", "Greek Small Mu", "Greek"),
        UnicodeSymbol("π", "U+03C0", "Greek Small Pi", "Greek"),
        UnicodeSymbol("σ", "U+03C3", "Greek Small Sigma", "Greek"),
        UnicodeSymbol("φ", "U+03C6", "Greek Small Phi", "Greek"),
        UnicodeSymbol("ω", "U+03C9", "Greek Small Omega", "Greek"),
        UnicodeSymbol("Ω", "U+03A9", "Greek Capital Omega", "Greek"),
        UnicodeSymbol("Σ", "U+03A3", "Greek Capital Sigma", "Greek"),
        UnicodeSymbol("Δ", "U+0394", "Greek Capital Delta", "Greek"),

        // Currency
        UnicodeSymbol("€", "U+20AC", "Euro Sign", "Currency"),
        UnicodeSymbol("£", "U+00A3", "Pound Sterling", "Currency"),
        UnicodeSymbol("¥", "U+00A5", "Yen / Yuan Sign", "Currency"),
        UnicodeSymbol("₹", "U+20B9", "Indian Rupee Sign", "Currency"),
        UnicodeSymbol("₩", "U+20A9", "Korean Won Sign", "Currency"),
        UnicodeSymbol("₽", "U+20BD", "Russian Ruble Sign", "Currency"),
        UnicodeSymbol("₺", "U+20BA", "Turkish Lira Sign", "Currency"),
        UnicodeSymbol("₿", "U+20BF", "Bitcoin Sign", "Currency"),
        UnicodeSymbol("¢", "U+00A2", "Cent Sign", "Currency"),
        UnicodeSymbol("₱", "U+20B1", "Peso Sign", "Currency"),

        // Arrows
        UnicodeSymbol("←", "U+2190", "Leftwards Arrow", "Arrows"),
        UnicodeSymbol("↑", "U+2191", "Upwards Arrow", "Arrows"),
        UnicodeSymbol("→", "U+2192", "Rightwards Arrow", "Arrows"),
        UnicodeSymbol("↓", "U+2193", "Downwards Arrow", "Arrows"),
        UnicodeSymbol("↔", "U+2194", "Left Right Arrow", "Arrows"),
        UnicodeSymbol("⇒", "U+21D2", "Rightwards Double Arrow", "Arrows"),
        UnicodeSymbol("⇔", "U+21D4", "Left Right Double Arrow", "Arrows"),
        UnicodeSymbol("↗", "U+2197", "North East Arrow", "Arrows"),
        UnicodeSymbol("↘", "U+2198", "South East Arrow", "Arrows"),
        UnicodeSymbol("⌘", "U+2318", "Place of Interest / Command", "Arrows"),

        // Fractions & Superscripts
        UnicodeSymbol("½", "U+00BD", "Vulgar Fraction One Half", "Fractions & Super"),
        UnicodeSymbol("⅓", "U+2153", "Vulgar Fraction One Third", "Fractions & Super"),
        UnicodeSymbol("⅔", "U+2154", "Vulgar Fraction Two Thirds", "Fractions & Super"),
        UnicodeSymbol("¼", "U+00BC", "Vulgar Fraction One Quarter", "Fractions & Super"),
        UnicodeSymbol("¾", "U+00BE", "Vulgar Fraction Three Quarters", "Fractions & Super"),
        UnicodeSymbol("¹", "U+00B9", "Superscript One", "Fractions & Super"),
        UnicodeSymbol("²", "U+00B2", "Superscript Two", "Fractions & Super"),
        UnicodeSymbol("³", "U+00B3", "Superscript Three", "Fractions & Super"),
        UnicodeSymbol("ⁿ", "U+207F", "Superscript Latin Small N", "Fractions & Super"),
        UnicodeSymbol("₀", "U+2080", "Subscript Zero", "Fractions & Super"),
        UnicodeSymbol("₁", "U+2081", "Subscript One", "Fractions & Super"),
        UnicodeSymbol("₂", "U+2082", "Subscript Two", "Fractions & Super"),

        // Shapes & Symbols
        UnicodeSymbol("✓", "U+2713", "Check Mark", "Shapes"),
        UnicodeSymbol("✔", "U+2714", "Heavy Check Mark", "Shapes"),
        UnicodeSymbol("✕", "U+2715", "Multiplication X", "Shapes"),
        UnicodeSymbol("★", "U+2605", "Black Star", "Shapes"),
        UnicodeSymbol("☆", "U+2606", "White Star", "Shapes"),
        UnicodeSymbol("♠", "U+2660", "Black Spade Suit", "Shapes"),
        UnicodeSymbol("♣", "U+2663", "Black Club Suit", "Shapes"),
        UnicodeSymbol("♥", "U+2665", "Black Heart Suit", "Shapes"),
        UnicodeSymbol("♦", "U+2666", "Black Diamond Suit", "Shapes"),
        UnicodeSymbol("♪", "U+266A", "Eighth Note", "Shapes"),
        UnicodeSymbol("♫", "U+266B", "Beamed Eighth Notes", "Shapes")
    )

    /**
     * Parses a hex string like "221E" or "U+03BB" into its Unicode character and official name.
     */
    fun decodeHexCodePoint(hexInput: String): Pair<String, String>? {
        val cleaned = hexInput.trim()
            .removePrefix("U+")
            .removePrefix("u+")
            .removePrefix("0x")
            .trim()
        if (cleaned.isEmpty() || cleaned.length > 6) return null
        return try {
            val codePoint = cleaned.toInt(16)
            if (Character.isValidCodePoint(codePoint) && codePoint > 0) {
                val chars = String(Character.toChars(codePoint))
                val name = Character.getName(codePoint) ?: "UNICODE CHARACTER U+${cleaned.uppercase()}"
                chars to name
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * WordCraft / Microsoft Word style Alt+X toggle:
     * If text ends with "U+XXXX" or 4-6 hex digits, converts it to the Unicode symbol.
     * Otherwise converts the final character into its "U+XXXX" representation.
     */
    fun toggleTrailingUnicodeHex(text: String): String {
        if (text.isEmpty()) return text
        val regex = Regex("(?i)(?:U\\+)?([0-9A-F]{2,6})$")
        val match = regex.find(text)
        if (match != null) {
            val hexPart = match.groupValues[1]
            val decoded = decodeHexCodePoint(hexPart)
            if (decoded != null) {
                return text.substring(0, match.range.first) + decoded.first
            }
        }
        // Fallback: convert last code point to U+XXXX
        val lastCodePoint = Character.codePointBefore(text, text.length)
        val charCount = Character.charCount(lastCodePoint)
        val hexStr = String.format("U+%04X", lastCodePoint)
        return text.substring(0, text.length - charCount) + hexStr
    }

    /**
     * Inspects up to the first 30 Unicode code points of a string.
     */
    fun inspectUnicodeString(text: String, maxChars: Int = 30): List<UnicodeCharInfo> {
        val list = mutableListOf<UnicodeCharInfo>()
        var offset = 0
        var count = 0
        while (offset < text.length && count < maxChars) {
            val cp = Character.codePointAt(text, offset)
            val charStr = String(Character.toChars(cp))
            val utf8Bytes = charStr.toByteArray(StandardCharsets.UTF_8)
                .joinToString(" ") { String.format("%02X", it) }
            val hexCode = String.format("U+%04X", cp)
            val name = Character.getName(cp) ?: "UNKNOWN"
            list.add(
                UnicodeCharInfo(
                    charDisplay = if (Character.isISOControl(cp)) "␀" else charStr,
                    codePointHex = hexCode,
                    decimalCode = cp,
                    utf8Hex = utf8Bytes,
                    characterName = name
                )
            )
            offset += Character.charCount(cp)
            count++
        }
        return list
    }

    fun normalize(text: String, formName: String): String {
        val form = when (formName.uppercase()) {
            "NFD" -> Normalizer.Form.NFD
            "NFKC" -> Normalizer.Form.NFKC
            "NFKD" -> Normalizer.Form.NFKD
            else -> Normalizer.Form.NFC
        }
        return Normalizer.normalize(text, form)
    }
}

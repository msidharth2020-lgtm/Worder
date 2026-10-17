package com.example.model

import java.util.UUID

enum class BlockType {
    PARAGRAPH,
    TITLE,
    SUBTITLE,
    HEADING_1,
    HEADING_2,
    HEADING_3,
    QUOTE,
    CODE,
    BULLET_LIST,
    NUMBERED_LIST,
    CHECKLIST,
    CALLOUT,
    TABLE,
    HORIZONTAL_RULE,
    PAGE_BREAK
}

enum class TextAlignment {
    LEFT,
    CENTER,
    RIGHT,
    JUSTIFY
}

enum class CalloutType {
    INFO,
    WARNING,
    SUCCESS,
    TIP
}

enum class TableStyle {
    DEFAULT,
    BANDED_BLUE,
    MINIMAL_GRAY,
    ACCENT_BORDER
}

enum class PageSize(val displayName: String, val widthDp: Int, val heightDp: Int) {
    A4("A4 (210 x 297 mm)", 720, 1018),
    LETTER("US Letter (8.5 x 11 in)", 700, 905),
    LEGAL("US Legal (8.5 x 14 in)", 700, 1150)
}

enum class PageOrientation {
    PORTRAIT,
    LANDSCAPE
}

enum class PageMargins(val displayName: String, val paddingDp: Int) {
    NORMAL("Normal (1 in)", 40),
    NARROW("Narrow (0.5 in)", 20),
    MODERATE("Moderate (0.75 in)", 30),
    WIDE("Wide (1.25 in)", 56)
}

data class TextSpan(
    val start: Int,
    val end: Int,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val colorHex: String? = null,
    val highlightHex: String? = null,
    val fontSizeSp: Int? = null,
    val fontFamily: String? = null,
    val linkUrl: String? = null
)

data class TableData(
    val headers: List<String> = emptyList(),
    val rows: List<List<String>> = emptyList(),
    val style: TableStyle = TableStyle.BANDED_BLUE
)

data class DocBlock(
    val id: String = UUID.randomUUID().toString(),
    val type: BlockType = BlockType.PARAGRAPH,
    val text: String = "",
    val spans: List<TextSpan> = emptyList(),
    val alignment: TextAlignment = TextAlignment.LEFT,
    val lineSpacing: Float = 1.25f,
    val indentLevel: Int = 0,
    val isChecked: Boolean = false,
    val tableData: TableData? = null,
    val calloutType: CalloutType? = null,
    val footnote: String? = null
)

data class Comment(
    val id: String = UUID.randomUUID().toString(),
    val blockId: String,
    val author: String = "Author",
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val resolved: Boolean = false
)

data class WordCraftDoc(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled Document",
    val author: String = "WordCraft User",
    val createdDate: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val blocks: List<DocBlock> = emptyList(),
    val comments: List<Comment> = emptyList(),
    val margins: PageMargins = PageMargins.NORMAL,
    val pageSize: PageSize = PageSize.A4,
    val orientation: PageOrientation = PageOrientation.PORTRAIT,
    val watermark: String? = null,
    val headerText: String? = "WordCraft Document",
    val footerText: String? = "Page 1",
    val showPageNumbers: Boolean = true
)

data class DocumentStats(
    val words: Int,
    val characters: Int,
    val charactersNoSpaces: Int,
    val paragraphs: Int,
    val readingTimeMinutes: Double
) {
    companion object {
        fun calculate(doc: WordCraftDoc): DocumentStats {
            var totalWords = 0
            var totalChars = 0
            var totalCharsNoSpaces = 0
            var totalParagraphs = 0

            for (block in doc.blocks) {
                if (block.type == BlockType.TABLE && block.tableData != null) {
                    val cells = block.tableData.headers + block.tableData.rows.flatten()
                    for (cell in cells) {
                        val trimmed = cell.trim()
                        if (trimmed.isNotEmpty()) {
                            totalWords += trimmed.split("\\s+".toRegex()).size
                            totalChars += cell.length
                            totalCharsNoSpaces += cell.replace("\\s".toRegex(), "").length
                        }
                    }
                    totalParagraphs++
                } else if (block.text.isNotBlank()) {
                    val trimmed = block.text.trim()
                    totalWords += trimmed.split("\\s+".toRegex()).size
                    totalChars += block.text.length
                    totalCharsNoSpaces += block.text.replace("\\s".toRegex(), "").length
                    totalParagraphs++
                }
            }

            val readTime = if (totalWords > 0) Math.max(0.5, totalWords / 200.0) else 0.0
            return DocumentStats(
                words = totalWords,
                characters = totalChars,
                charactersNoSpaces = totalCharsNoSpaces,
                paragraphs = Math.max(1, totalParagraphs),
                readingTimeMinutes = readTime
            )
        }
    }
}

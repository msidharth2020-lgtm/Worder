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
    PAGE_BREAK,
    EQUATION,
    DRAWING,
    CHART,
    SMART_ART,
    SHAPE,
    SIGNATURE_LINE,
    TOC,
    BIBLIOGRAPHY;

    fun defaultFontSizeSp(): Int = when (this) {
        TITLE -> 28
        HEADING_1 -> 22
        HEADING_2 -> 18
        SUBTITLE, HEADING_3 -> 16
        EQUATION -> 18
        CODE, CALLOUT -> 14
        else -> 15
    }
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

enum class TableStyle(val displayName: String) {
    DEFAULT("Plain Grid"),
    BANDED_BLUE("Banded Blue"),
    MINIMAL_GRAY("Minimal Gray"),
    ACCENT_BORDER("Accent Navy")
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

enum class DocumentThemePreset(
    val displayName: String,
    val headingFont: String,
    val bodyFont: String,
    val primaryHex: String,
    val accentHex: String
) {
    OFFICE("Office Studio", "Inter", "Inter", "#182A6A", "#3B5BDB"),
    EDITORIAL("Editorial Press", "Playfair Display", "Merriweather", "#2B2D42", "#8D0801"),
    BOOK_CLASSIC("Book Classic", "Lora", "Lora", "#1B4332", "#2D6A4F"),
    TECHNICAL("Technical Spec", "Inter", "Roboto Mono", "#0B2545", "#134074"),
    CREATIVE("Creative Script", "Pacifico", "Inter", "#5F0F40", "#9A031E")
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

data class InkStroke(
    val points: List<Pair<Float, Float>> = emptyList(),
    val colorHex: String = "#182A6A",
    val strokeWidth: Float = 3.5f,
    val tool: String = "Pen"
)

data class ChartData(
    val title: String = "Quarterly Metrics",
    val chartType: String = "BAR", // BAR, LINE, PIE
    val labels: List<String> = listOf("Q1", "Q2", "Q3", "Q4"),
    val values: List<Float> = listOf(42f, 68f, 55f, 89f),
    val accentColorHex: String = "#3B5BDB"
)

data class SmartArtData(
    val layoutType: String = "PROCESS", // PROCESS, CYCLE, HIERARCHY, PYRAMID
    val nodes: List<String> = listOf("Research", "Design", "Implement", "Verify")
)

data class CitationSource(
    val id: String = UUID.randomUUID().toString(),
    val tag: String,
    val author: String,
    val title: String,
    val year: String,
    val publisher: String = "Journal / Press"
)

data class MailMergeRecipient(
    val firstName: String,
    val lastName: String,
    val company: String,
    val email: String,
    val city: String
)

data class DocBlock(
    val id: String = UUID.randomUUID().toString(),
    val type: BlockType = BlockType.PARAGRAPH,
    val text: String = "",
    val spans: List<TextSpan> = emptyList(),
    val alignment: TextAlignment = TextAlignment.LEFT,
    val fontFamily: String = "Inter",
    val fontSizeSp: Int? = null,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val strikethrough: Boolean = false,
    val textColorHex: String? = null,
    val highlightColorHex: String? = null,
    val lineSpacing: Float = 1.25f,
    val indentLevel: Int = 0,
    val dropCap: Boolean = false,
    val isChecked: Boolean = false,
    val tableData: TableData? = null,
    val calloutType: CalloutType? = null,
    val footnote: String? = null,
    val inkStrokes: List<InkStroke> = emptyList(),
    val chartData: ChartData? = null,
    val smartArtData: SmartArtData? = null,
    val shapeKind: String? = null // RECTANGLE, ROUNDED_RECT, OVAL, DIAMOND, BANNER
) {
    val effectiveFontSizeSp: Int
        get() = fontSizeSp ?: type.defaultFontSizeSp()
}

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
    val showPageNumbers: Boolean = true,
    // WordCraft Web v0.7.0 extended document settings
    val themePreset: DocumentThemePreset = DocumentThemePreset.OFFICE,
    val pageColorHex: String? = null,
    val pageBorder: Boolean = false,
    val columns: Int = 1,
    val showLineNumbers: Boolean = false,
    val showRuler: Boolean = true,
    val showGridlines: Boolean = false,
    val trackChanges: Boolean = false,
    val readOnlyLocked: Boolean = false,
    val citationStyle: String = "APA",
    val sources: List<CitationSource> = listOf(
        CitationSource(
            tag = "Knuth1984",
            author = "Knuth, D. E.",
            title = "Literate Programming & Digital Typography",
            year = "1984",
            publisher = "Stanford University"
        ),
        CitationSource(
            tag = "Bringhurst2012",
            author = "Bringhurst, R.",
            title = "The Elements of Typographic Style",
            year = "2012",
            publisher = "Hartley & Marks"
        )
    ),
    val footnotes: List<String> = emptyList()
)

data class AccessibilityIssue(
    val severity: String, // "Warning" or "Tip"
    val title: String,
    val description: String,
    val blockId: String? = null
)

data class DocumentStats(
    val words: Int,
    val characters: Int,
    val charactersNoSpaces: Int,
    val paragraphs: Int,
    val sentences: Int,
    val readingTimeMinutes: Double
) {
    companion object {
        fun calculate(doc: WordCraftDoc): DocumentStats {
            var totalWords = 0
            var totalChars = 0
            var totalCharsNoSpaces = 0
            var totalParagraphs = 0
            var totalSentences = 0

            for (block in doc.blocks) {
                if (block.type == BlockType.TABLE && block.tableData != null) {
                    val cells = block.tableData.headers + block.tableData.rows.flatten()
                    for (cell in cells) {
                        val trimmed = cell.trim()
                        if (trimmed.isNotEmpty()) {
                            totalWords += trimmed.split("\\s+".toRegex()).size
                            totalChars += cell.length
                            totalCharsNoSpaces += cell.replace("\\s".toRegex(), "").length
                            totalSentences += trimmed.split("[.!?]+".toRegex()).filter { it.isNotBlank() }.size
                        }
                    }
                    totalParagraphs++
                } else if (block.text.isNotBlank()) {
                    val trimmed = block.text.trim()
                    totalWords += trimmed.split("\\s+".toRegex()).size
                    totalChars += block.text.length
                    totalCharsNoSpaces += block.text.replace("\\s".toRegex(), "").length
                    totalSentences += trimmed.split("[.!?]+".toRegex()).filter { it.isNotBlank() }.size
                    totalParagraphs++
                }
            }

            val readTime = if (totalWords > 0) Math.max(0.5, totalWords / 200.0) else 0.0
            return DocumentStats(
                words = totalWords,
                characters = totalChars,
                charactersNoSpaces = totalCharsNoSpaces,
                paragraphs = Math.max(1, totalParagraphs),
                sentences = Math.max(1, totalSentences),
                readingTimeMinutes = readTime
            )
        }

        fun checkAccessibility(doc: WordCraftDoc): List<AccessibilityIssue> {
            val issues = mutableListOf<AccessibilityIssue>()
            if (doc.title.isBlank() || doc.title.equals("Untitled Document", ignoreCase = true)) {
                issues.add(
                    AccessibilityIssue(
                        severity = "Warning",
                        title = "Default Document Title",
                        description = "Give the document a descriptive title so screen readers can announce it."
                    )
                )
            }
            val hasHeading = doc.blocks.any {
                it.type in listOf(BlockType.TITLE, BlockType.HEADING_1, BlockType.HEADING_2, BlockType.HEADING_3)
            }
            if (!hasHeading && doc.blocks.size > 3) {
                issues.add(
                    AccessibilityIssue(
                        severity = "Tip",
                        title = "No Heading Structure",
                        description = "Add Heading 1 / Heading 2 blocks to allow outline navigation."
                    )
                )
            }
            for (block in doc.blocks) {
                if (block.type == BlockType.TABLE && block.tableData != null) {
                    if (block.tableData.headers.any { it.isBlank() }) {
                        issues.add(
                            AccessibilityIssue(
                                severity = "Warning",
                                title = "Empty Table Header Cell",
                                description = "Table header cells should have descriptive column labels.",
                                blockId = block.id
                            )
                        )
                    }
                }
                if (block.text.length > 600) {
                    issues.add(
                        AccessibilityIssue(
                            severity = "Tip",
                            title = "Very Long Paragraph (${block.text.length} chars)",
                            description = "Consider splitting dense paragraphs for easier reading.",
                            blockId = block.id
                        )
                    )
                }
            }
            return issues
        }
    }
}

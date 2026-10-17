package com.example.export

import android.content.Context
import com.example.model.BlockType
import com.example.model.DocBlock
import com.example.model.WordCraftDoc
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object DocxExporter {

    fun exportToDocx(context: Context, doc: WordCraftDoc): File {
        val docsDir = File(context.cacheDir, "docs")
        if (!docsDir.exists()) docsDir.mkdirs()

        val safeTitle = doc.title.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val file = File(docsDir, "$safeTitle.docx")

        ZipOutputStream(FileOutputStream(file)).use { zip ->
            // 1. [Content_Types].xml
            zip.putNextEntry(ZipEntry("[Content_Types].xml"))
            zip.write(contentTypesXml.toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()

            // 2. _rels/.rels
            zip.putNextEntry(ZipEntry("_rels/.rels"))
            zip.write(relsXml.toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()

            // 3. word/_rels/document.xml.rels
            zip.putNextEntry(ZipEntry("word/_rels/document.xml.rels"))
            zip.write(documentRelsXml.toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()

            // 4. word/styles.xml
            zip.putNextEntry(ZipEntry("word/styles.xml"))
            zip.write(stylesXml.toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()

            // 5. word/document.xml
            zip.putNextEntry(ZipEntry("word/document.xml"))
            val docXml = buildDocumentXml(doc)
            zip.write(docXml.toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()
        }

        return file
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun buildDocumentXml(doc: WordCraftDoc): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
        sb.append("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">\n")
        sb.append("  <w:body>\n")

        for (block in doc.blocks) {
            when (block.type) {
                BlockType.TITLE -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:pStyle w:val=\"Title\"/><w:jc w:val=\"").append(block.alignment.name.lowercase()).append("\"/></w:pPr>\n")
                    sb.append("      <w:r><w:t>").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.SUBTITLE -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:pStyle w:val=\"Subtitle\"/><w:jc w:val=\"").append(block.alignment.name.lowercase()).append("\"/></w:pPr>\n")
                    sb.append("      <w:r><w:t>").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.HEADING_1 -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr>\n")
                    sb.append("      <w:r><w:t>").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.HEADING_2 -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:pStyle w:val=\"Heading2\"/></w:pPr>\n")
                    sb.append("      <w:r><w:t>").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.HEADING_3 -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:pStyle w:val=\"Heading3\"/></w:pPr>\n")
                    sb.append("      <w:r><w:t>").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.QUOTE -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:pStyle w:val=\"Quote\"/></w:pPr>\n")
                    sb.append("      <w:r><w:rPr><w:i/></w:rPr><w:t>").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.BULLET_LIST -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:pStyle w:val=\"ListParagraph\"/><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr></w:pPr>\n")
                    sb.append("      <w:r><w:t>• ").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.NUMBERED_LIST -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:pStyle w:val=\"ListParagraph\"/></w:pPr>\n")
                    sb.append("      <w:r><w:t>1. ").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.CHECKLIST -> {
                    val box = if (block.isChecked) "[X] " else "[ ] "
                    sb.append("    <w:p>\n")
                    sb.append("      <w:r><w:t>").append(box).append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.TABLE -> {
                    val table = block.tableData
                    if (table != null) {
                        sb.append("    <w:tbl>\n")
                        sb.append("      <w:tblPr><w:tblBorders><w:top w:val=\"single\" w:sz=\"4\"/><w:bottom w:val=\"single\" w:sz=\"4\"/><w:insideH w:val=\"single\" w:sz=\"4\"/><w:insideV w:val=\"single\" w:sz=\"4\"/></w:tblBorders></w:tblPr>\n")
                        // Header row
                        if (table.headers.isNotEmpty()) {
                            sb.append("      <w:tr>\n")
                            for (header in table.headers) {
                                sb.append("        <w:tc><w:tcPr><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"EAEAEA\"/></w:tcPr><w:p><w:r><w:rPr><w:b/></w:rPr><w:t>").append(escapeXml(header)).append("</w:t></w:p></w:tc>\n")
                            }
                            sb.append("      </w:tr>\n")
                        }
                        // Data rows
                        for (row in table.rows) {
                            sb.append("      <w:tr>\n")
                            for (cell in row) {
                                sb.append("        <w:tc><w:p><w:r><w:t>").append(escapeXml(cell)).append("</w:t></w:p></w:tc>\n")
                            }
                            sb.append("      </w:tr>\n")
                        }
                        sb.append("    </w:tbl>\n")
                    }
                }
                BlockType.HORIZONTAL_RULE -> {
                    sb.append("    <w:p><w:pPr><w:pBdr><w:bottom w:val=\"single\" w:sz=\"12\" w:space=\"1\" w:color=\"CCCCCC\"/></w:pBdr></w:pPr></w:p>\n")
                }
                BlockType.PAGE_BREAK -> {
                    sb.append("    <w:p><w:r><w:br w:type=\"page\"/></w:r></w:p>\n")
                }
                BlockType.CALLOUT -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:pBdr><w:left w:val=\"single\" w:sz=\"24\" w:space=\"4\" w:color=\"3B5BDB\"/></w:pBdr></w:pPr>\n")
                    sb.append("      <w:r><w:t>").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                else -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:r><w:t>").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
            }
        }

        // Section properties
        sb.append("    <w:sectPr>\n")
        sb.append("      <w:pgSz w:w=\"11906\" w:h=\"16838\"/>\n")
        sb.append("      <w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"720\" w:footer=\"720\"/>\n")
        sb.append("    </w:sectPr>\n")
        sb.append("  </w:body>\n")
        sb.append("</w:document>\n")

        return sb.toString()
    }

    fun exportToMarkdown(doc: WordCraftDoc): String {
        val sb = StringBuilder()
        sb.append("# ").append(doc.title).append("\n\n")

        for (block in doc.blocks) {
            when (block.type) {
                BlockType.TITLE -> sb.append("# ").append(block.text).append("\n\n")
                BlockType.SUBTITLE -> sb.append("### ").append(block.text).append("\n\n")
                BlockType.HEADING_1 -> sb.append("## ").append(block.text).append("\n\n")
                BlockType.HEADING_2 -> sb.append("### ").append(block.text).append("\n\n")
                BlockType.HEADING_3 -> sb.append("#### ").append(block.text).append("\n\n")
                BlockType.QUOTE -> sb.append("> ").append(block.text).append("\n\n")
                BlockType.CODE -> sb.append("```\n").append(block.text).append("\n```\n\n")
                BlockType.BULLET_LIST -> sb.append("- ").append(block.text).append("\n")
                BlockType.NUMBERED_LIST -> sb.append("1. ").append(block.text).append("\n")
                BlockType.CHECKLIST -> sb.append(if (block.isChecked) "- [x] " else "- [ ] ").append(block.text).append("\n")
                BlockType.HORIZONTAL_RULE -> sb.append("\n---\n\n")
                BlockType.PAGE_BREAK -> sb.append("\n<!-- pagebreak -->\n\n")
                BlockType.CALLOUT -> sb.append("> **").append(block.calloutType?.name ?: "NOTE").append("**: ").append(block.text).append("\n\n")
                BlockType.TABLE -> {
                    val table = block.tableData
                    if (table != null && table.headers.isNotEmpty()) {
                        sb.append("| ").append(table.headers.joinToString(" | ")).append(" |\n")
                        sb.append("| ").append(table.headers.map { "---" }.joinToString(" | ")).append(" |\n")
                        for (row in table.rows) {
                            sb.append("| ").append(row.joinToString(" | ")).append(" |\n")
                        }
                        sb.append("\n")
                    }
                }
                else -> sb.append(block.text).append("\n\n")
            }
        }
        return sb.toString()
    }

    fun exportToHtml(doc: WordCraftDoc): String {
        val sb = StringBuilder()
        sb.append("<!DOCTYPE html>\n<html>\n<head>\n")
        sb.append("<meta charset=\"utf-8\">\n")
        sb.append("<title>").append(escapeXml(doc.title)).append("</title>\n")
        sb.append("<style>\n")
        sb.append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; max-width: 800px; margin: 40px auto; padding: 0 20px; line-height: 1.6; color: #212529; }\n")
        sb.append("h1, h2, h3 { color: #182A6A; }\n")
        sb.append("blockquote { border-left: 4px solid #3B5BDB; padding-left: 16px; margin: 20px 0; font-style: italic; color: #495057; }\n")
        sb.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n")
        sb.append("th, td { border: 1px solid #DEE2E6; padding: 10px 14px; text-align: left; }\n")
        sb.append("th { background-color: #F1F3F5; font-weight: 600; }\n")
        sb.append(".callout { background-color: #EDF2FF; border-left: 4px solid #3B5BDB; padding: 12px 16px; border-radius: 4px; margin: 16px 0; }\n")
        sb.append("</style>\n</head>\n<body>\n")

        for (block in doc.blocks) {
            when (block.type) {
                BlockType.TITLE -> sb.append("<h1>").append(escapeXml(block.text)).append("</h1>\n")
                BlockType.SUBTITLE -> sb.append("<p style=\"font-size: 1.2em; color: #495057;\">").append(escapeXml(block.text)).append("</p>\n")
                BlockType.HEADING_1 -> sb.append("<h2>").append(escapeXml(block.text)).append("</h2>\n")
                BlockType.HEADING_2 -> sb.append("<h3>").append(escapeXml(block.text)).append("</h3>\n")
                BlockType.HEADING_3 -> sb.append("<h4>").append(escapeXml(block.text)).append("</h4>\n")
                BlockType.QUOTE -> sb.append("<blockquote>").append(escapeXml(block.text)).append("</blockquote>\n")
                BlockType.CODE -> sb.append("<pre><code>").append(escapeXml(block.text)).append("</code></pre>\n")
                BlockType.BULLET_LIST -> sb.append("<ul><li>").append(escapeXml(block.text)).append("</li></ul>\n")
                BlockType.NUMBERED_LIST -> sb.append("<ol><li>").append(escapeXml(block.text)).append("</li></ol>\n")
                BlockType.CHECKLIST -> {
                    val chk = if (block.isChecked) "checked" else ""
                    sb.append("<div><input type=\"checkbox\" ").append(chk).append(" disabled> ").append(escapeXml(block.text)).append("</div>\n")
                }
                BlockType.CALLOUT -> sb.append("<div class=\"callout\">").append(escapeXml(block.text)).append("</div>\n")
                BlockType.HORIZONTAL_RULE -> sb.append("<hr>\n")
                BlockType.PAGE_BREAK -> sb.append("<div style=\"page-break-after: always;\"></div>\n")
                BlockType.TABLE -> {
                    val table = block.tableData
                    if (table != null) {
                        sb.append("<table>\n<thead><tr>")
                        for (header in table.headers) sb.append("<th>").append(escapeXml(header)).append("</th>")
                        sb.append("</tr></thead>\n<tbody>\n")
                        for (row in table.rows) {
                            sb.append("<tr>")
                            for (cell in row) sb.append("<td>").append(escapeXml(cell)).append("</td>")
                            sb.append("</tr>\n")
                        }
                        sb.append("</tbody></table>\n")
                    }
                }
                else -> sb.append("<p>").append(escapeXml(block.text)).append("</p>\n")
            }
        }
        sb.append("</body>\n</html>\n")
        return sb.toString()
    }

    fun exportToPlainText(doc: WordCraftDoc): String {
        val sb = StringBuilder()
        sb.append(doc.title).append("\n\n")
        for (block in doc.blocks) {
            when (block.type) {
                BlockType.TABLE -> {
                    block.tableData?.let { t ->
                        sb.append(t.headers.joinToString("\t")).append("\n")
                        for (row in t.rows) sb.append(row.joinToString("\t")).append("\n")
                        sb.append("\n")
                    }
                }
                BlockType.HORIZONTAL_RULE -> sb.append("----------------------------------------\n\n")
                BlockType.CHECKLIST -> sb.append(if (block.isChecked) "[X] " else "[ ] ").append(block.text).append("\n")
                BlockType.BULLET_LIST -> sb.append("• ").append(block.text).append("\n")
                BlockType.NUMBERED_LIST -> sb.append("1. ").append(block.text).append("\n")
                else -> sb.append(block.text).append("\n\n")
            }
        }
        return sb.toString()
    }

    private const val contentTypesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
  <Override PartName="/word/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"/>
</Types>"""

    private const val relsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

    private const val documentRelsXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    private const val stylesXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:styles xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:docDefaults>
    <w:rPrDefault><w:rPr><w:rFonts w:ascii="Calibri" w:hAnsi="Calibri"/><w:sz w:val="22"/></w:rPr></w:rPrDefault>
  </w:docDefaults>
  <w:style w:type="paragraph" w:styleId="Title"><w:name w:val="Title"/><w:rPr><w:sz w:val="52"/><w:b/><w:color w:val="182A6A"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Subtitle"><w:name w:val="Subtitle"/><w:rPr><w:sz w:val="26"/><w:color w:val="555555"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Heading1"><w:name w:val="Heading 1"/><w:rPr><w:sz w:val="36"/><w:b/><w:color w:val="2B47B5"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Heading2"><w:name w:val="Heading 2"/><w:rPr><w:sz w:val="28"/><w:b/><w:color w:val="3B5BDB"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Heading3"><w:name w:val="Heading 3"/><w:rPr><w:sz w:val="24"/><w:b/><w:color w:val="333333"/></w:rPr></w:style>
  <w:style w:type="paragraph" w:styleId="Quote"><w:name w:val="Quote"/><w:rPr><w:i/><w:color w:val="555555"/></w:rPr></w:style>
</w:styles>"""
}

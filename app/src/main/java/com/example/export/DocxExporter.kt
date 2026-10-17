package com.example.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import com.example.model.BlockType
import com.example.model.DocBlock
import com.example.model.WordCraftDoc
import com.example.ui.theme.WordCraftFonts
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object DocxExporter {

    fun exportToDocx(context: Context, doc: WordCraftDoc): File {
        val docsDir = File(context.cacheDir, "docs")
        if (!docsDir.exists()) docsDir.mkdirs()

        val safeTitle = doc.title.replace("[^a-zA-Z0-9_-]".toRegex(), "_").ifBlank { "Document" }
        val file = File(docsDir, "$safeTitle.docx")

        ZipOutputStream(FileOutputStream(file)).use { zip ->
            zip.putNextEntry(ZipEntry("[Content_Types].xml"))
            zip.write(contentTypesXml.toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("_rels/.rels"))
            zip.write(relsXml.toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("word/_rels/document.xml.rels"))
            zip.write(documentRelsXml.toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("word/styles.xml"))
            zip.write(stylesXml.toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("word/document.xml"))
            val docXml = buildDocumentXml(doc)
            zip.write(docXml.toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()
        }

        return file
    }

    /**
     * Generates a multi-page native PDF document matching wordcraft-web-0.7.0's PDF/Print engine.
     */
    fun exportToPdf(context: Context, doc: WordCraftDoc): File {
        val docsDir = File(context.cacheDir, "docs")
        if (!docsDir.exists()) docsDir.mkdirs()
        val safeTitle = doc.title.replace("[^a-zA-Z0-9_-]".toRegex(), "_").ifBlank { "Document" }
        val file = File(docsDir, "$safeTitle.pdf")

        val pdfDocument = buildPdfDocument(doc)
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }

    /**
     * Launches Android's system PrintManager dialog (equivalent to wordcraft-web-0.7.0 print_pdf).
     */
    fun printDocument(context: Context, doc: WordCraftDoc) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager ?: return
        val jobName = "WordCraft_${doc.title}"
        printManager.print(
            jobName,
            object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder("${doc.title}.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(PrintDocumentInfo.PAGE_COUNT_UNKNOWN)
                        .build()
                    callback.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback
                ) {
                    try {
                        val pdfDoc = buildPdfDocument(doc)
                        FileOutputStream(destination.fileDescriptor).use { out ->
                            pdfDoc.writeTo(out)
                        }
                        pdfDoc.close()
                        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback.onWriteFailed(e.message)
                    }
                }
            },
            null
        )
    }

    private fun buildPdfDocument(doc: WordCraftDoc): PdfDocument {
        val pdf = PdfDocument()
        val pageWidth = if (doc.orientation.name == "LANDSCAPE") 842 else 595
        val pageHeight = if (doc.orientation.name == "LANDSCAPE") 595 else 842
        val margin = doc.margins.paddingDp.coerceIn(24, 64).toFloat()

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var currentPage = pdf.startPage(pageInfo)
        var canvas: Canvas = currentPage.canvas
        var yPos = margin + 20f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun drawPageChrome(c: Canvas, pNum: Int) {
            doc.pageColorHex?.let { hex ->
                runCatching {
                    val bgPaint = Paint().apply { color = Color.parseColor(hex) }
                    c.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), bgPaint)
                }
            }
            if (doc.pageBorder) {
                val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    style = Paint.Style.STROKE
                    strokeWidth = 1.5f
                    color = Color.parseColor("#182A6A")
                }
                c.drawRect(margin * 0.6f, margin * 0.6f, pageWidth - margin * 0.6f, pageHeight - margin * 0.6f, borderPaint)
            }
            if (!doc.watermark.isNullOrBlank()) {
                c.save()
                val wmPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(24, 24, 42, 106)
                    textSize = 54f
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign = Paint.Align.CENTER
                }
                c.translate(pageWidth / 2f, pageHeight / 2f)
                c.rotate(-30f)
                c.drawText(doc.watermark!!, 0f, 0f, wmPaint)
                c.restore()
            }
            val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.DKGRAY
                textSize = 9f
            }
            doc.headerText?.let {
                c.drawText(it, margin, margin - 4f, metaPaint)
            }
            if (doc.showPageNumbers) {
                val footerLabel = "${doc.footerText ?: doc.title}  •  Page $pNum"
                c.drawText(footerLabel, margin, pageHeight - margin / 2f, metaPaint)
            }
        }

        fun nextPage() {
            pdf.finishPage(currentPage)
            pageNumber++
            pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            currentPage = pdf.startPage(pageInfo)
            canvas = currentPage.canvas
            drawPageChrome(canvas, pageNumber)
            yPos = margin + 20f
        }

        drawPageChrome(canvas, pageNumber)

        val contentWidth = pageWidth - (margin * 2)

        for (block in doc.blocks) {
            if (block.type == BlockType.PAGE_BREAK) {
                nextPage()
                continue
            }
            if (yPos > pageHeight - margin - 40f) {
                nextPage()
            }

            paint.textSize = block.effectiveFontSizeSp.toFloat() * 0.95f
            paint.typeface = when {
                block.bold && block.italic -> Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
                block.bold || block.type in listOf(BlockType.TITLE, BlockType.HEADING_1, BlockType.HEADING_2) ->
                    Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                block.italic || block.type == BlockType.QUOTE ->
                    Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                block.type == BlockType.CODE -> Typeface.MONOSPACE
                else -> Typeface.DEFAULT
            }
            paint.color = block.textColorHex?.let {
                runCatching { Color.parseColor(it) }.getOrNull()
            } ?: when (block.type) {
                BlockType.TITLE -> Color.parseColor("#182A6A")
                BlockType.HEADING_1 -> Color.parseColor("#2B47B5")
                BlockType.HEADING_2 -> Color.parseColor("#3B5BDB")
                else -> Color.parseColor("#212529")
            }

            when (block.type) {
                BlockType.HORIZONTAL_RULE -> {
                    val linePaint = Paint().apply {
                        color = Color.LTGRAY
                        strokeWidth = 1.2f
                    }
                    canvas.drawLine(margin, yPos, pageWidth - margin, yPos, linePaint)
                    yPos += 16f
                }
                BlockType.TABLE -> {
                    val table = block.tableData
                    if (table != null) {
                        val cols = table.headers.size.coerceAtLeast(1)
                        val colW = contentWidth / cols
                        paint.textSize = 10f
                        paint.typeface = Typeface.DEFAULT_BOLD
                        table.headers.forEachIndexed { cIdx, h ->
                            canvas.drawText(h.take(18), margin + cIdx * colW + 4f, yPos, paint)
                        }
                        yPos += 16f
                        paint.typeface = Typeface.DEFAULT
                        for (row in table.rows) {
                            if (yPos > pageHeight - margin - 30f) nextPage()
                            row.forEachIndexed { cIdx, cell ->
                                canvas.drawText(cell.take(18), margin + cIdx * colW + 4f, yPos, paint)
                            }
                            yPos += 15f
                        }
                        yPos += 8f
                    }
                }
                else -> {
                    val prefix = when (block.type) {
                        BlockType.BULLET_LIST -> "• "
                        BlockType.NUMBERED_LIST -> "1. "
                        BlockType.CHECKLIST -> if (block.isChecked) "[x] " else "[ ] "
                        BlockType.EQUATION -> "∑ "
                        else -> ""
                    }
                    val fullText = prefix + block.text
                    val words = fullText.split(" ")
                    var line = StringBuilder()
                    val lineHeight = paint.textSize * 1.4f
                    for (word in words) {
                        val testLine = if (line.isEmpty()) word else "$line $word"
                        if (paint.measureText(testLine) > contentWidth) {
                            if (yPos > pageHeight - margin - 30f) nextPage()
                            canvas.drawText(line.toString(), margin, yPos, paint)
                            yPos += lineHeight
                            line = StringBuilder(word)
                        } else {
                            line = StringBuilder(testLine)
                        }
                    }
                    if (line.isNotEmpty()) {
                        if (yPos > pageHeight - margin - 30f) nextPage()
                        canvas.drawText(line.toString(), margin, yPos, paint)
                        yPos += lineHeight
                    }
                    yPos += 6f
                }
            }
        }

        pdf.finishPage(currentPage)
        return pdf
    }

    /**
     * Exports to OpenDocument Text (.odt) ZIP container (matching wordcraft-web-0.7.0 ODT support).
     */
    fun exportToOdt(context: Context, doc: WordCraftDoc): File {
        val docsDir = File(context.cacheDir, "docs")
        if (!docsDir.exists()) docsDir.mkdirs()
        val safeTitle = doc.title.replace("[^a-zA-Z0-9_-]".toRegex(), "_").ifBlank { "Document" }
        val file = File(docsDir, "$safeTitle.odt")

        val contentXml = buildString {
            append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
            append("<office:document-content xmlns:office=\"urn:oasis:names:tc:opendocument:xmlns:office:1.0\" ")
            append("xmlns:text=\"urn:oasis:names:tc:opendocument:xmlns:text:1.0\" office:version=\"1.2\">\n")
            append("  <office:body><office:text>\n")
            for (block in doc.blocks) {
                when (block.type) {
                    BlockType.TITLE, BlockType.HEADING_1 ->
                        append("    <text:h text:outline-level=\"1\">").append(escapeXml(block.text)).append("</text:h>\n")
                    BlockType.SUBTITLE, BlockType.HEADING_2 ->
                        append("    <text:h text:outline-level=\"2\">").append(escapeXml(block.text)).append("</text:h>\n")
                    BlockType.HEADING_3 ->
                        append("    <text:h text:outline-level=\"3\">").append(escapeXml(block.text)).append("</text:h>\n")
                    else ->
                        append("    <text:p>").append(escapeXml(block.text)).append("</text:p>\n")
                }
            }
            append("  </office:text></office:body>\n</office:document-content>")
        }

        val manifestXml = """<?xml version="1.0" encoding="UTF-8"?>
<manifest:manifest xmlns:manifest="urn:oasis:names:tc:opendocument:xmlns:manifest:1.0" manifest:version="1.2">
  <manifest:file-entry manifest:full-path="/" manifest:media-type="application/vnd.oasis.opendocument.text"/>
  <manifest:file-entry manifest:full-path="content.xml" manifest:media-type="text/xml"/>
</manifest:manifest>"""

        ZipOutputStream(FileOutputStream(file)).use { zip ->
            zip.putNextEntry(ZipEntry("mimetype"))
            zip.write("application/vnd.oasis.opendocument.text".toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("META-INF/manifest.xml"))
            zip.write(manifestXml.toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()

            zip.putNextEntry(ZipEntry("content.xml"))
            zip.write(contentXml.toByteArray(StandardCharsets.UTF_8))
            zip.closeEntry()
        }
        return file
    }

    /**
     * Exports to Rich Text Format (.rtf) matching wordcraft-web-0.7.0 RTF export.
     */
    fun exportToRtf(doc: WordCraftDoc): String {
        val sb = StringBuilder()
        sb.append("{\\rtf1\\ansi\\deff0\n")
        sb.append("{\\fonttbl{\\f0 Calibri;}{\\f1 Times New Roman;}{\\f2 Courier New;}}\n")
        sb.append("{\\info{\\title ").append(doc.title).append("}{\\author ").append(doc.author).append("}}\n")
        for (block in doc.blocks) {
            val fsHalfPoints = block.effectiveFontSizeSp * 2
            sb.append("\\pard\\fs").append(fsHalfPoints).append(" ")
            if (block.bold || block.type in listOf(BlockType.TITLE, BlockType.HEADING_1, BlockType.HEADING_2)) sb.append("\\b ")
            if (block.italic || block.type == BlockType.QUOTE) sb.append("\\i ")
            if (block.underline) sb.append("\\ul ")
            val escaped = block.text
                .replace("\\", "\\\\")
                .replace("{", "\\{")
                .replace("}", "\\}")
            sb.append(escaped)
            if (block.underline) sb.append("\\ul0 ")
            if (block.italic || block.type == BlockType.QUOTE) sb.append("\\i0 ")
            if (block.bold || block.type in listOf(BlockType.TITLE, BlockType.HEADING_1, BlockType.HEADING_2)) sb.append("\\b0 ")
            sb.append("\\par\n")
        }
        sb.append("}")
        return sb.toString()
    }

    /**
     * Exports to LaTeX (.tex) matching wordcraft-web-0.7.0 TeX export.
     */
    fun exportToLaTeX(doc: WordCraftDoc): String {
        val sb = StringBuilder()
        sb.append("\\documentclass[11pt,a4paper]{article}\n")
        sb.append("\\usepackage[utf8]{inputenc}\n")
        sb.append("\\usepackage{amsmath,amssymb}\n")
        sb.append("\\title{").append(doc.title).append("}\n")
        sb.append("\\author{").append(doc.author).append("}\n")
        sb.append("\\begin{document}\n\\maketitle\n\n")
        for (block in doc.blocks) {
            when (block.type) {
                BlockType.TITLE, BlockType.HEADING_1 -> sb.append("\\section{").append(block.text).append("}\n\n")
                BlockType.SUBTITLE, BlockType.HEADING_2 -> sb.append("\\subsection{").append(block.text).append("}\n\n")
                BlockType.HEADING_3 -> sb.append("\\subsubsection{").append(block.text).append("}\n\n")
                BlockType.EQUATION -> sb.append("\\[\n").append(block.text).append("\n\\]\n\n")
                BlockType.QUOTE -> sb.append("\\begin{quote}\n").append(block.text).append("\n\\end{quote}\n\n")
                BlockType.CODE -> sb.append("\\begin{verbatim}\n").append(block.text).append("\n\\end{verbatim}\n\n")
                BlockType.BULLET_LIST -> sb.append("\\begin{itemize}\n  \\item ").append(block.text).append("\n\\end{itemize}\n\n")
                BlockType.NUMBERED_LIST -> sb.append("\\begin{enumerate}\n  \\item ").append(block.text).append("\n\\end{enumerate}\n\n")
                else -> sb.append(block.text).append("\n\n")
            }
        }
        sb.append("\\end{document}\n")
        return sb.toString()
    }

    /**
     * Imports a document (.docx, .md, .txt, .html) from an Android content Uri.
     */
    fun importDocumentFromUri(context: Context, uri: Uri, displayName: String?): WordCraftDoc? {
        return try {
            val fileName = displayName ?: uri.lastPathSegment ?: "Imported_Document"
            val cleanTitle = fileName.substringBeforeLast(".")
            if (fileName.endsWith(".docx", ignoreCase = true)) {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    ZipInputStream(stream).use { zip ->
                        var entry = zip.nextEntry
                        while (entry != null) {
                            if (entry.name == "word/document.xml") {
                                val xml = BufferedReader(InputStreamReader(zip, StandardCharsets.UTF_8)).readText()
                                val paragraphs = Regex("<w:t[^>]*>(.*?)</w:t>")
                                    .findAll(xml)
                                    .map { it.groupValues[1] }
                                    .filter { it.isNotBlank() }
                                    .toList()
                                val blocks = if (paragraphs.isNotEmpty()) {
                                    paragraphs.mapIndexed { idx, p ->
                                        DocBlock(
                                            type = if (idx == 0) BlockType.TITLE else BlockType.PARAGRAPH,
                                            text = p.replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">")
                                        )
                                    }
                                } else {
                                    listOf(DocBlock(type = BlockType.PARAGRAPH, text = "Imported DOCX Document"))
                                }
                                return WordCraftDoc(title = cleanTitle, blocks = blocks)
                            }
                            entry = zip.nextEntry
                        }
                    }
                }
            }
            // Fallback for .md, .txt, .html, etc.
            val rawText = context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream, StandardCharsets.UTF_8)).readText()
            } ?: return null

            val lines = rawText.lines()
            val blocks = mutableListOf<DocBlock>()
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue
                when {
                    trimmed.startsWith("# ") -> blocks.add(DocBlock(type = BlockType.TITLE, text = trimmed.removePrefix("# ").trim()))
                    trimmed.startsWith("## ") -> blocks.add(DocBlock(type = BlockType.HEADING_1, text = trimmed.removePrefix("## ").trim()))
                    trimmed.startsWith("### ") -> blocks.add(DocBlock(type = BlockType.HEADING_2, text = trimmed.removePrefix("### ").trim()))
                    trimmed.startsWith("- [x] ") -> blocks.add(DocBlock(type = BlockType.CHECKLIST, text = trimmed.removePrefix("- [x] ").trim(), isChecked = true))
                    trimmed.startsWith("- [ ] ") -> blocks.add(DocBlock(type = BlockType.CHECKLIST, text = trimmed.removePrefix("- [ ] ").trim(), isChecked = false))
                    trimmed.startsWith("- ") || trimmed.startsWith("* ") ->
                        blocks.add(DocBlock(type = BlockType.BULLET_LIST, text = trimmed.drop(2).trim()))
                    trimmed.startsWith("> ") -> blocks.add(DocBlock(type = BlockType.QUOTE, text = trimmed.removePrefix("> ").trim()))
                    trimmed == "---" -> blocks.add(DocBlock(type = BlockType.HORIZONTAL_RULE))
                    else -> blocks.add(DocBlock(type = BlockType.PARAGRAPH, text = trimmed))
                }
            }
            if (blocks.isEmpty()) {
                blocks.add(DocBlock(type = BlockType.PARAGRAPH, text = rawText))
            }
            WordCraftDoc(id = UUID.randomUUID().toString(), title = cleanTitle, blocks = blocks)
        } catch (e: Exception) {
            null
        }
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun buildRunPropsXml(block: DocBlock): String {
        val sb = StringBuilder("<w:rPr>")
        val docxFont = WordCraftFonts.resolveDocxFontName(block.fontFamily)
        sb.append("<w:rFonts w:ascii=\"").append(escapeXml(docxFont))
            .append("\" w:hAnsi=\"").append(escapeXml(docxFont))
            .append("\" w:cs=\"").append(escapeXml(docxFont)).append("\"/>")
        val halfPoints = block.effectiveFontSizeSp * 2
        sb.append("<w:sz w:val=\"").append(halfPoints).append("\"/>")
        sb.append("<w:szCs w:val=\"").append(halfPoints).append("\"/>")
        if (block.bold) sb.append("<w:b/>")
        if (block.italic || block.type == BlockType.QUOTE) sb.append("<w:i/>")
        if (block.underline) sb.append("<w:u w:val=\"single\"/>")
        if (block.strikethrough) sb.append("<w:strike/>")
        block.textColorHex?.removePrefix("#")?.takeIf { it.length == 6 }?.let { hex ->
            sb.append("<w:color w:val=\"").append(hex.uppercase()).append("\"/>")
        }
        sb.append("</w:rPr>")
        return sb.toString()
    }

    private fun buildDocumentXml(doc: WordCraftDoc): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
        sb.append("<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">\n")
        sb.append("  <w:body>\n")

        for (block in doc.blocks) {
            val rPr = buildRunPropsXml(block)
            when (block.type) {
                BlockType.TITLE -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:pStyle w:val=\"Title\"/><w:jc w:val=\"").append(block.alignment.name.lowercase()).append("\"/></w:pPr>\n")
                    sb.append("      <w:r>").append(rPr).append("<w:t>").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.SUBTITLE -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:pStyle w:val=\"Subtitle\"/><w:jc w:val=\"").append(block.alignment.name.lowercase()).append("\"/></w:pPr>\n")
                    sb.append("      <w:r>").append(rPr).append("<w:t>").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.HEADING_1 -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:pStyle w:val=\"Heading1\"/></w:pPr>\n")
                    sb.append("      <w:r>").append(rPr).append("<w:t>").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.HEADING_2 -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:pStyle w:val=\"Heading2\"/></w:pPr>\n")
                    sb.append("      <w:r>").append(rPr).append("<w:t>").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.HEADING_3 -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:pStyle w:val=\"Heading3\"/></w:pPr>\n")
                    sb.append("      <w:r>").append(rPr).append("<w:t>").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.QUOTE -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:pStyle w:val=\"Quote\"/></w:pPr>\n")
                    sb.append("      <w:r>").append(rPr).append("<w:t>").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.BULLET_LIST -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:pStyle w:val=\"ListParagraph\"/><w:numPr><w:ilvl w:val=\"0\"/><w:numId w:val=\"1\"/></w:numPr></w:pPr>\n")
                    sb.append("      <w:r>").append(rPr).append("<w:t>• ").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.NUMBERED_LIST -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:pStyle w:val=\"ListParagraph\"/></w:pPr>\n")
                    sb.append("      <w:r>").append(rPr).append("<w:t>1. ").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.CHECKLIST -> {
                    val box = if (block.isChecked) "[X] " else "[ ] "
                    sb.append("    <w:p>\n")
                    sb.append("      <w:r>").append(rPr).append("<w:t>").append(box).append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                BlockType.TABLE -> {
                    val table = block.tableData
                    if (table != null) {
                        sb.append("    <w:tbl>\n")
                        sb.append("      <w:tblPr><w:tblBorders><w:top w:val=\"single\" w:sz=\"4\"/><w:bottom w:val=\"single\" w:sz=\"4\"/><w:insideH w:val=\"single\" w:sz=\"4\"/><w:insideV w:val=\"single\" w:sz=\"4\"/></w:tblBorders></w:tblPr>\n")
                        if (table.headers.isNotEmpty()) {
                            sb.append("      <w:tr>\n")
                            for (header in table.headers) {
                                sb.append("        <w:tc><w:tcPr><w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"EAEAEA\"/></w:tcPr><w:p><w:r><w:rPr><w:b/></w:rPr><w:t>").append(escapeXml(header)).append("</w:t></w:p></w:tc>\n")
                            }
                            sb.append("      </w:tr>\n")
                        }
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
                    sb.append("      <w:r>").append(rPr).append("<w:t>").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
                else -> {
                    sb.append("    <w:p>\n")
                    sb.append("      <w:pPr><w:jc w:val=\"").append(block.alignment.name.lowercase()).append("\"/></w:pPr>\n")
                    sb.append("      <w:r>").append(rPr).append("<w:t>").append(escapeXml(block.text)).append("</w:t></w:r>\n")
                    sb.append("    </w:p>\n")
                }
            }
        }

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
                BlockType.EQUATION -> sb.append("$$\n").append(block.text).append("\n$$\n\n")
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
        sb.append("body { font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; max-width: 800px; margin: 40px auto; padding: 0 20px; line-height: 1.6; color: #212529; }\n")
        sb.append("h1, h2, h3 { color: #182A6A; }\n")
        sb.append("blockquote { border-left: 4px solid #3B5BDB; padding-left: 16px; margin: 20px 0; font-style: italic; color: #495057; }\n")
        sb.append("table { width: 100%; border-collapse: collapse; margin: 20px 0; }\n")
        sb.append("th, td { border: 1px solid #DEE2E6; padding: 10px 14px; text-align: left; }\n")
        sb.append("th { background-color: #F1F3F5; font-weight: 600; }\n")
        sb.append(".callout { background-color: #EDF2FF; border-left: 4px solid #3B5BDB; padding: 12px 16px; border-radius: 4px; margin: 16px 0; }\n")
        sb.append(".equation { background-color: #F8F9FA; border: 1px solid #DEE2E6; padding: 12px; text-align: center; font-family: serif; font-size: 1.25em; margin: 16px 0; }\n")
        sb.append("</style>\n</head>\n<body>\n")

        for (block in doc.blocks) {
            val inlineStyle = buildString {
                append("font-family: '").append(escapeXml(block.fontFamily)).append("', sans-serif; ")
                append("font-size: ").append(block.effectiveFontSizeSp).append("pt; ")
                if (block.bold) append("font-weight: bold; ")
                if (block.italic) append("font-style: italic; ")
                if (block.underline) append("text-decoration: underline; ")
                block.textColorHex?.let { append("color: ").append(it).append("; ") }
            }
            when (block.type) {
                BlockType.TITLE -> sb.append("<h1 style=\"").append(inlineStyle).append("\">").append(escapeXml(block.text)).append("</h1>\n")
                BlockType.SUBTITLE -> sb.append("<p style=\"").append(inlineStyle).append("\">").append(escapeXml(block.text)).append("</p>\n")
                BlockType.HEADING_1 -> sb.append("<h2 style=\"").append(inlineStyle).append("\">").append(escapeXml(block.text)).append("</h2>\n")
                BlockType.HEADING_2 -> sb.append("<h3 style=\"").append(inlineStyle).append("\">").append(escapeXml(block.text)).append("</h3>\n")
                BlockType.HEADING_3 -> sb.append("<h4 style=\"").append(inlineStyle).append("\">").append(escapeXml(block.text)).append("</h4>\n")
                BlockType.EQUATION -> sb.append("<div class=\"equation\">").append(escapeXml(block.text)).append("</div>\n")
                BlockType.QUOTE -> sb.append("<blockquote style=\"").append(inlineStyle).append("\">").append(escapeXml(block.text)).append("</blockquote>\n")
                BlockType.CODE -> sb.append("<pre><code style=\"").append(inlineStyle).append("\">").append(escapeXml(block.text)).append("</code></pre>\n")
                BlockType.BULLET_LIST -> sb.append("<ul><li style=\"").append(inlineStyle).append("\">").append(escapeXml(block.text)).append("</li></ul>\n")
                BlockType.NUMBERED_LIST -> sb.append("<ol><li style=\"").append(inlineStyle).append("\">").append(escapeXml(block.text)).append("</li></ol>\n")
                BlockType.CHECKLIST -> {
                    val chk = if (block.isChecked) "checked" else ""
                    sb.append("<div style=\"").append(inlineStyle).append("\"><input type=\"checkbox\" ").append(chk).append(" disabled> ").append(escapeXml(block.text)).append("</div>\n")
                }
                BlockType.CALLOUT -> sb.append("<div class=\"callout\" style=\"").append(inlineStyle).append("\">").append(escapeXml(block.text)).append("</div>\n")
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
                else -> sb.append("<p style=\"").append(inlineStyle).append("\">").append(escapeXml(block.text)).append("</p>\n")
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

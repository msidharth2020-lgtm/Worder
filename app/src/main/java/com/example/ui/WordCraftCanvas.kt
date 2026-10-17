package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BlockType
import com.example.model.CalloutType
import com.example.model.ChartData
import com.example.model.DocBlock
import com.example.model.InkStroke
import com.example.model.PageOrientation
import com.example.model.SmartArtData
import com.example.model.TableData
import com.example.model.TableStyle
import com.example.model.TextAlignment
import com.example.ui.theme.WordCraftCanvasDark
import com.example.ui.theme.WordCraftCanvasLight
import com.example.ui.theme.WordCraftFonts
import com.example.ui.theme.WordCraftPaperDark
import com.example.ui.theme.WordCraftPaperLight
import com.example.ui.theme.WordCraftPrimary
import com.example.viewmodel.ViewMode
import com.example.viewmodel.WordCraftViewModel

@Composable
fun WordCraftCanvas(
    viewModel: WordCraftViewModel,
    modifier: Modifier = Modifier
) {
    val doc by viewModel.document.collectAsState()
    val activeBlockId by viewModel.activeBlockId.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val isDarkCanvas by viewModel.isDarkCanvas.collectAsState()
    val zoom by viewModel.zoomPercent.collectAsState()

    val canvasBg = if (isDarkCanvas) WordCraftCanvasDark else WordCraftCanvasLight
    val customPageColor = remember(doc.pageColorHex) {
        doc.pageColorHex?.let {
            runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
        }
    }
    val paperBg = customPageColor ?: if (isDarkCanvas) WordCraftPaperDark else WordCraftPaperLight
    val textPrimaryColor = if (isDarkCanvas && customPageColor == null) Color(0xFFE2E2E6) else Color(0xFF212529)
    val textMutedColor = if (isDarkCanvas && customPageColor == null) Color(0xFF9E9EA4) else Color(0xFF6C757D)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(canvasBg)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        val pagePadding = when (viewMode) {
            ViewMode.PRINT_LAYOUT -> 16.dp
            ViewMode.WEB_LAYOUT -> 8.dp
            ViewMode.READING_MODE -> 24.dp
        }

        val baseMaxWidth = when (viewMode) {
            ViewMode.PRINT_LAYOUT -> if (doc.orientation == PageOrientation.LANDSCAPE) 980.dp else 760.dp
            ViewMode.WEB_LAYOUT -> 880.dp
            ViewMode.READING_MODE -> 680.dp
        }

        Column(
            modifier = Modifier
                .padding(pagePadding)
                .widthIn(max = baseMaxWidth)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Optional Horizontal Ruler (from wordcraft-web-0.7.0 View Tab)
            if (doc.showRuler && viewMode != ViewMode.READING_MODE) {
                WordCraftHorizontalRuler(
                    marginsDp = doc.margins.paddingDp,
                    isDark = isDarkCanvas
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Status Badges if Track Changes or Read-Only Lock or Multi-column is active
            if (doc.trackChanges || doc.readOnlyLocked || doc.columns > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (doc.trackChanges) {
                        Surface(
                            color = Color(0xFFFFF3BF),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.TrackChanges, contentDescription = null, tint = Color(0xFFD9480F), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Track Changes Active", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFD9480F))
                            }
                        }
                    }
                    if (doc.readOnlyLocked) {
                        Surface(
                            color = Color(0xFFFFE3E3),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFC92A2A), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Read-Only Protected", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFC92A2A))
                            }
                        }
                    }
                    if (doc.columns > 1) {
                        Surface(
                            color = WordCraftPrimary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${doc.columns}-Column Layout",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = WordCraftPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Paper Sheet Container
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (viewMode == ViewMode.PRINT_LAYOUT) 6.dp else 1.dp,
                        shape = RoundedCornerShape(if (viewMode == ViewMode.PRINT_LAYOUT) 4.dp else 0.dp)
                    )
                    .then(
                        if (doc.pageBorder) {
                            Modifier.border(2.dp, WordCraftPrimary.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
                        } else Modifier
                    )
                    .testTag("document_paper_sheet"),
                color = paperBg,
                shape = RoundedCornerShape(if (viewMode == ViewMode.PRINT_LAYOUT) 4.dp else 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (doc.showGridlines) {
                                Modifier.drawBehind {
                                    val step = 28.dp.toPx()
                                    val gridColor = Color(0xFFADB5BD).copy(alpha = 0.22f)
                                    var x = 0f
                                    while (x < size.width) {
                                        drawLine(gridColor, Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                                        x += step
                                    }
                                    var y = 0f
                                    while (y < size.height) {
                                        drawLine(gridColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                                        y += step
                                    }
                                }
                            } else Modifier
                        )
                        .padding(
                            horizontal = doc.margins.paddingDp.dp,
                            vertical = (doc.margins.paddingDp * 0.8f).dp
                        )
                ) {
                    // Watermark (if present)
                    if (doc.watermark != null) {
                        Text(
                            text = doc.watermark!!,
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimaryColor.copy(alpha = 0.08f),
                            modifier = Modifier
                                .align(Alignment.Center)
                                .rotate(-30f)
                        )
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Header text in Print Layout
                        if (viewMode == ViewMode.PRINT_LAYOUT && doc.headerText != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = doc.headerText!!,
                                    fontSize = 11.sp,
                                    color = textMutedColor
                                )
                                Text(
                                    text = doc.title,
                                    fontSize = 11.sp,
                                    color = textMutedColor
                                )
                            }
                            Divider(color = textMutedColor.copy(alpha = 0.2f), modifier = Modifier.padding(bottom = 16.dp))
                        }

                        // Blocks rendering (supports Line Numbers & Track Changes gutter)
                        doc.blocks.forEachIndexed { index, block ->
                            val isActive = block.id == activeBlockId
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                if (doc.showLineNumbers) {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = textMutedColor.copy(alpha = 0.7f),
                                        modifier = Modifier
                                            .width(28.dp)
                                            .padding(top = 6.dp),
                                        textAlign = TextAlign.End
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }

                                if (doc.trackChanges && isActive) {
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(32.dp)
                                            .background(Color(0xFFD9480F), RoundedCornerShape(2.dp))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                Box(modifier = Modifier.weight(1f)) {
                                    DocBlockItem(
                                        block = block,
                                        isActive = isActive,
                                        readOnly = doc.readOnlyLocked || viewMode == ViewMode.READING_MODE,
                                        zoomPercent = zoom,
                                        textPrimaryColor = textPrimaryColor,
                                        textMutedColor = textMutedColor,
                                        onBlockClick = { viewModel.setActiveBlockId(block.id) },
                                        onTextChange = { newText -> viewModel.updateBlockText(block.id, newText) },
                                        onToggleChecklist = { viewModel.toggleChecklist(block.id) },
                                        onTableCellChange = { r, c, valText -> viewModel.updateTableCell(block.id, r, c, valText) },
                                        onOpenDrawingStudio = { viewModel.setShowDrawingDialog(true) }
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        // Footnotes section (from References tab)
                        if (doc.footnotes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(
                                color = textMutedColor.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth(0.35f).padding(bottom = 8.dp)
                            )
                            doc.footnotes.forEachIndexed { idx, note ->
                                Text(
                                    text = "[${idx + 1}] $note",
                                    fontSize = 11.sp,
                                    color = textMutedColor,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }

                        // Footer in Print Layout
                        if (viewMode == ViewMode.PRINT_LAYOUT && doc.showPageNumbers) {
                            Divider(color = textMutedColor.copy(alpha = 0.2f), modifier = Modifier.padding(top = 24.dp, bottom = 12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = doc.footerText ?: "WordCraft",
                                    fontSize = 11.sp,
                                    color = textMutedColor
                                )
                                Text(
                                    text = "Page 1 of 1",
                                    fontSize = 11.sp,
                                    color = textMutedColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun WordCraftHorizontalRuler(
    marginsDp: Int,
    isDark: Boolean
) {
    val bgColor = if (isDark) Color(0xFF2B2D30) else Color(0xFFE9ECEF)
    val tickColor = if (isDark) Color(0xFFADB5BD) else Color(0xFF495057)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(22.dp),
        color = bgColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val marginPx = marginsDp.dp.toPx()
            // Shade margin areas
            drawRect(
                color = Color.Black.copy(alpha = 0.08f),
                topLeft = Offset(0f, 0f),
                size = Size(marginPx, size.height)
            )
            drawRect(
                color = Color.Black.copy(alpha = 0.08f),
                topLeft = Offset((size.width - marginPx).coerceAtLeast(0f), 0f),
                size = Size(marginPx, size.height)
            )
            val stepPx = 36.dp.toPx()
            var x = stepPx
            var inch = 1
            while (x < size.width - 10f) {
                val isMajor = inch % 2 == 0
                val h = if (isMajor) size.height * 0.55f else size.height * 0.3f
                drawLine(
                    color = tickColor,
                    start = Offset(x, size.height - h),
                    end = Offset(x, size.height),
                    strokeWidth = if (isMajor) 2f else 1f
                )
                x += stepPx
                inch++
            }
        }
    }
}

@Composable
private fun DocBlockItem(
    block: DocBlock,
    isActive: Boolean,
    readOnly: Boolean,
    zoomPercent: Int,
    textPrimaryColor: Color,
    textMutedColor: Color,
    onBlockClick: () -> Unit,
    onTextChange: (String) -> Unit,
    onToggleChecklist: () -> Unit,
    onTableCellChange: (Int, Int, String) -> Unit,
    onOpenDrawingStudio: () -> Unit
) {
    val textAlign = when (block.alignment) {
        TextAlignment.LEFT -> TextAlign.Left
        TextAlignment.CENTER -> TextAlign.Center
        TextAlignment.RIGHT -> TextAlign.Right
        TextAlignment.JUSTIFY -> TextAlign.Justify
    }

    val resolvedFontFamily = remember(block.fontFamily, block.type) {
        if (block.type == BlockType.CODE && block.fontFamily == "Inter") {
            WordCraftFonts.RobotoMonoFamily
        } else {
            WordCraftFonts.resolveFontFamily(block.fontFamily)
        }
    }

    val scaledFontSize = ((block.effectiveFontSizeSp * zoomPercent) / 100f).sp

    val customColor = remember(block.textColorHex) {
        block.textColorHex?.let {
            runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
        }
    }

    val highlightColor = remember(block.highlightColorHex) {
        block.highlightColorHex?.let {
            runCatching { Color(android.graphics.Color.parseColor(it)) }.getOrNull()
        }
    }

    val decorations = remember(block.underline, block.strikethrough) {
        val list = mutableListOf<TextDecoration>()
        if (block.underline) list.add(TextDecoration.Underline)
        if (block.strikethrough) list.add(TextDecoration.LineThrough)
        if (list.isEmpty()) TextDecoration.None else TextDecoration.combine(list)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .then(
                if (highlightColor != null) Modifier.background(highlightColor) else Modifier
            )
            .border(
                width = if (isActive) 1.5.dp else 0.dp,
                color = if (isActive) WordCraftPrimary else Color.Transparent,
                shape = RoundedCornerShape(4.dp)
            )
            .clickable(onClick = onBlockClick)
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag("block_${block.id}")
    ) {
        when (block.type) {
            BlockType.TITLE -> {
                EditableText(
                    text = block.text,
                    onTextChange = onTextChange,
                    fontSize = scaledFontSize,
                    fontWeight = if (block.bold) FontWeight.ExtraBold else FontWeight.Bold,
                    fontStyle = if (block.italic) FontStyle.Italic else FontStyle.Normal,
                    fontFamily = resolvedFontFamily,
                    textDecoration = decorations,
                    lineSpacingMultiplier = block.lineSpacing,
                    color = customColor ?: Color(0xFF182A6A),
                    textAlign = textAlign,
                    isActive = isActive && !readOnly
                )
            }
            BlockType.SUBTITLE -> {
                EditableText(
                    text = block.text,
                    onTextChange = onTextChange,
                    fontSize = scaledFontSize,
                    fontWeight = if (block.bold) FontWeight.Bold else FontWeight.Normal,
                    fontStyle = if (block.italic) FontStyle.Normal else FontStyle.Italic,
                    fontFamily = resolvedFontFamily,
                    textDecoration = decorations,
                    lineSpacingMultiplier = block.lineSpacing,
                    color = customColor ?: textMutedColor,
                    textAlign = textAlign,
                    isActive = isActive && !readOnly
                )
            }
            BlockType.HEADING_1 -> {
                EditableText(
                    text = block.text,
                    onTextChange = onTextChange,
                    fontSize = scaledFontSize,
                    fontWeight = FontWeight.Bold,
                    fontStyle = if (block.italic) FontStyle.Italic else FontStyle.Normal,
                    fontFamily = resolvedFontFamily,
                    textDecoration = decorations,
                    lineSpacingMultiplier = block.lineSpacing,
                    color = customColor ?: WordCraftPrimary,
                    textAlign = textAlign,
                    isActive = isActive && !readOnly
                )
            }
            BlockType.HEADING_2 -> {
                EditableText(
                    text = block.text,
                    onTextChange = onTextChange,
                    fontSize = scaledFontSize,
                    fontWeight = if (block.bold) FontWeight.Bold else FontWeight.SemiBold,
                    fontStyle = if (block.italic) FontStyle.Italic else FontStyle.Normal,
                    fontFamily = resolvedFontFamily,
                    textDecoration = decorations,
                    lineSpacingMultiplier = block.lineSpacing,
                    color = customColor ?: Color(0xFF3B5BDB),
                    textAlign = textAlign,
                    isActive = isActive && !readOnly
                )
            }
            BlockType.HEADING_3 -> {
                EditableText(
                    text = block.text,
                    onTextChange = onTextChange,
                    fontSize = scaledFontSize,
                    fontWeight = if (block.bold) FontWeight.Bold else FontWeight.SemiBold,
                    fontStyle = if (block.italic) FontStyle.Italic else FontStyle.Normal,
                    fontFamily = resolvedFontFamily,
                    textDecoration = decorations,
                    lineSpacingMultiplier = block.lineSpacing,
                    color = customColor ?: textPrimaryColor,
                    textAlign = textAlign,
                    isActive = isActive && !readOnly
                )
            }
            BlockType.QUOTE -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF1F3F5), RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(28.dp)
                            .background(WordCraftPrimary, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    EditableText(
                        text = block.text,
                        onTextChange = onTextChange,
                        fontSize = scaledFontSize,
                        fontWeight = if (block.bold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = FontStyle.Italic,
                        fontFamily = resolvedFontFamily,
                        textDecoration = decorations,
                        lineSpacingMultiplier = block.lineSpacing,
                        color = customColor ?: Color(0xFF495057),
                        textAlign = textAlign,
                        isActive = isActive && !readOnly
                    )
                }
            }
            BlockType.CODE -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF212529), RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    EditableText(
                        text = block.text,
                        onTextChange = onTextChange,
                        fontSize = scaledFontSize,
                        fontWeight = if (block.bold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (block.italic) FontStyle.Italic else FontStyle.Normal,
                        fontFamily = resolvedFontFamily,
                        textDecoration = decorations,
                        lineSpacingMultiplier = block.lineSpacing,
                        color = customColor ?: Color(0xFFE9ECEF),
                        textAlign = TextAlign.Left,
                        isActive = isActive && !readOnly
                    )
                }
            }
            BlockType.BULLET_LIST -> {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "•",
                        fontSize = scaledFontSize,
                        fontWeight = FontWeight.Bold,
                        color = WordCraftPrimary,
                        modifier = Modifier.padding(start = (block.indentLevel * 16).dp, end = 8.dp, top = 2.dp)
                    )
                    EditableText(
                        text = block.text,
                        onTextChange = onTextChange,
                        fontSize = scaledFontSize,
                        fontWeight = if (block.bold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (block.italic) FontStyle.Italic else FontStyle.Normal,
                        fontFamily = resolvedFontFamily,
                        textDecoration = decorations,
                        lineSpacingMultiplier = block.lineSpacing,
                        color = customColor ?: textPrimaryColor,
                        textAlign = textAlign,
                        isActive = isActive && !readOnly
                    )
                }
            }
            BlockType.NUMBERED_LIST -> {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "1.",
                        fontSize = scaledFontSize,
                        fontWeight = FontWeight.SemiBold,
                        color = WordCraftPrimary,
                        modifier = Modifier.padding(start = (block.indentLevel * 16).dp, end = 8.dp, top = 2.dp)
                    )
                    EditableText(
                        text = block.text,
                        onTextChange = onTextChange,
                        fontSize = scaledFontSize,
                        fontWeight = if (block.bold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (block.italic) FontStyle.Italic else FontStyle.Normal,
                        fontFamily = resolvedFontFamily,
                        textDecoration = decorations,
                        lineSpacingMultiplier = block.lineSpacing,
                        color = customColor ?: textPrimaryColor,
                        textAlign = textAlign,
                        isActive = isActive && !readOnly
                    )
                }
            }
            BlockType.CHECKLIST -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = block.isChecked,
                        onCheckedChange = { if (!readOnly) onToggleChecklist() },
                        colors = CheckboxDefaults.colors(checkedColor = WordCraftPrimary),
                        modifier = Modifier.size(28.dp).padding(end = 4.dp)
                    )
                    EditableText(
                        text = block.text,
                        onTextChange = onTextChange,
                        fontSize = scaledFontSize,
                        fontWeight = if (block.bold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (block.italic) FontStyle.Italic else FontStyle.Normal,
                        fontFamily = resolvedFontFamily,
                        textDecoration = if (block.isChecked) TextDecoration.LineThrough else decorations,
                        lineSpacingMultiplier = block.lineSpacing,
                        color = customColor ?: if (block.isChecked) textMutedColor else textPrimaryColor,
                        textAlign = textAlign,
                        isActive = isActive && !readOnly
                    )
                }
            }
            BlockType.CALLOUT -> {
                val bannerColor = when (block.calloutType) {
                    CalloutType.WARNING -> Color(0xFFFFF9DB)
                    CalloutType.TIP, CalloutType.SUCCESS -> Color(0xFFEBFBEE)
                    else -> Color(0xFFEDF2FF)
                }
                val borderColor = when (block.calloutType) {
                    CalloutType.WARNING -> Color(0xFFF59F00)
                    CalloutType.TIP, CalloutType.SUCCESS -> Color(0xFF37B24D)
                    else -> WordCraftPrimary
                }
                val icon = when (block.calloutType) {
                    CalloutType.WARNING -> Icons.Default.Warning
                    CalloutType.TIP, CalloutType.SUCCESS -> Icons.Default.Lightbulb
                    else -> Icons.Default.Info
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bannerColor, RoundedCornerShape(4.dp))
                        .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Callout",
                        tint = borderColor,
                        modifier = Modifier.size(20.dp).padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    EditableText(
                        text = block.text,
                        onTextChange = onTextChange,
                        fontSize = scaledFontSize,
                        fontWeight = if (block.bold) FontWeight.Bold else FontWeight.Medium,
                        fontStyle = if (block.italic) FontStyle.Italic else FontStyle.Normal,
                        fontFamily = resolvedFontFamily,
                        textDecoration = decorations,
                        lineSpacingMultiplier = block.lineSpacing,
                        color = customColor ?: Color(0xFF212529),
                        textAlign = textAlign,
                        isActive = isActive && !readOnly
                    )
                }
            }
            BlockType.EQUATION -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    color = Color(0xFFF8F9FA),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WordCraftPrimary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Functions,
                            contentDescription = "Equation",
                            tint = WordCraftPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            EditableText(
                                text = block.text,
                                onTextChange = onTextChange,
                                fontSize = scaledFontSize,
                                fontWeight = FontWeight.Medium,
                                fontStyle = FontStyle.Italic,
                                fontFamily = WordCraftFonts.MerriweatherFamily,
                                textDecoration = decorations,
                                lineSpacingMultiplier = block.lineSpacing,
                                color = Color(0xFF182A6A),
                                textAlign = TextAlign.Center,
                                isActive = isActive && !readOnly
                            )
                        }
                    }
                }
            }
            BlockType.DRAWING -> {
                DrawingBlockRenderer(
                    strokes = block.inkStrokes,
                    caption = block.text,
                    onOpenStudio = onOpenDrawingStudio
                )
            }
            BlockType.CHART -> {
                block.chartData?.let { chart ->
                    ChartBlockRenderer(chart = chart)
                }
            }
            BlockType.SMART_ART -> {
                block.smartArtData?.let { smartArt ->
                    SmartArtBlockRenderer(smartArt = smartArt)
                }
            }
            BlockType.SHAPE -> {
                ShapeBlockRenderer(
                    shapeKind = block.shapeKind ?: "ROUNDED_RECT",
                    text = block.text,
                    onTextChange = onTextChange,
                    isActive = isActive && !readOnly
                )
            }
            BlockType.SIGNATURE_LINE -> {
                SignatureLineRenderer(
                    signerText = block.text,
                    onTextChange = onTextChange,
                    isActive = isActive && !readOnly
                )
            }
            BlockType.TOC, BlockType.BIBLIOGRAPHY -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    color = Color(0xFFF8F9FA),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDEE2E6))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (block.type == BlockType.TOC) "TABLE OF CONTENTS" else "REFERENCES & BIBLIOGRAPHY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = WordCraftPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        EditableText(
                            text = block.text,
                            onTextChange = onTextChange,
                            fontSize = scaledFontSize,
                            fontWeight = FontWeight.Normal,
                            fontStyle = FontStyle.Normal,
                            fontFamily = resolvedFontFamily,
                            textDecoration = decorations,
                            lineSpacingMultiplier = block.lineSpacing,
                            color = Color(0xFF212529),
                            textAlign = TextAlign.Left,
                            isActive = isActive && !readOnly
                        )
                    }
                }
            }
            BlockType.TABLE -> {
                block.tableData?.let { table ->
                    TableRenderer(
                        table = table,
                        isActive = isActive,
                        onCellChange = onTableCellChange
                    )
                }
            }
            BlockType.HORIZONTAL_RULE -> {
                Divider(
                    color = textMutedColor.copy(alpha = 0.35f),
                    thickness = 1.5.dp,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
            BlockType.PAGE_BREAK -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(modifier = Modifier.weight(1f), color = WordCraftPrimary.copy(alpha = 0.3f))
                    Text(
                        text = "— PAGE BREAK —",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WordCraftPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Divider(modifier = Modifier.weight(1f), color = WordCraftPrimary.copy(alpha = 0.3f))
                }
            }
            else -> {
                // Default Paragraph (with optional Drop Cap from Insert tab)
                if (block.dropCap && block.text.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = block.text.take(1),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = WordCraftFonts.PlayfairDisplayFamily,
                            color = WordCraftPrimary,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        EditableText(
                            text = block.text,
                            onTextChange = onTextChange,
                            fontSize = scaledFontSize,
                            fontWeight = if (block.bold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (block.italic) FontStyle.Italic else FontStyle.Normal,
                            fontFamily = resolvedFontFamily,
                            textDecoration = decorations,
                            lineSpacingMultiplier = block.lineSpacing,
                            color = customColor ?: textPrimaryColor,
                            textAlign = textAlign,
                            isActive = isActive && !readOnly
                        )
                    }
                } else {
                    EditableText(
                        text = block.text,
                        onTextChange = onTextChange,
                        fontSize = scaledFontSize,
                        fontWeight = if (block.bold) FontWeight.Bold else FontWeight.Normal,
                        fontStyle = if (block.italic) FontStyle.Italic else FontStyle.Normal,
                        fontFamily = resolvedFontFamily,
                        textDecoration = decorations,
                        lineSpacingMultiplier = block.lineSpacing,
                        color = customColor ?: textPrimaryColor,
                        textAlign = textAlign,
                        isActive = isActive && !readOnly
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawingBlockRenderer(
    strokes: List<InkStroke>,
    caption: String,
    onOpenStudio: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        color = Color.White,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCED4DA))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Brush, contentDescription = null, tint = WordCraftPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(caption.ifBlank { "Freehand Ink Canvas" }, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF495057))
                }
                OutlinedButton(
                    onClick = onOpenStudio,
                    modifier = Modifier.height(30.dp)
                ) {
                    Text("Edit Ink / Replay", fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFFF8F9FA), RoundedCornerShape(4.dp))
            ) {
                strokes.forEach { stroke ->
                    if (stroke.points.size >= 2) {
                        val color = runCatching { Color(android.graphics.Color.parseColor(stroke.colorHex)) }.getOrDefault(WordCraftPrimary)
                        val path = Path().apply {
                            moveTo(stroke.points.first().first, stroke.points.first().second)
                            for (i in 1 until stroke.points.size) {
                                lineTo(stroke.points[i].first, stroke.points[i].second)
                            }
                        }
                        drawPath(
                            path = path,
                            color = if (stroke.tool == "Highlighter") color.copy(alpha = 0.38f) else color,
                            style = Stroke(width = stroke.strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartBlockRenderer(chart: ChartData) {
    val barColor = remember(chart.accentColorHex) {
        runCatching { Color(android.graphics.Color.parseColor(chart.accentColorHex)) }.getOrDefault(WordCraftPrimary)
    }
    val maxVal = (chart.values.maxOrNull() ?: 100f).coerceAtLeast(1f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        color = Color(0xFFF8F9FA),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDEE2E6))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(chart.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF182A6A))
            Spacer(modifier = Modifier.height(10.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                val count = chart.values.size.coerceAtLeast(1)
                val slotWidth = size.width / count
                if (chart.chartType == "LINE") {
                    val path = Path()
                    chart.values.forEachIndexed { i, v ->
                        val x = slotWidth * i + slotWidth / 2f
                        val y = size.height - (v / maxVal) * (size.height * 0.8f) - 12f
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        drawCircle(barColor, radius = 5.dp.toPx(), center = Offset(x, y))
                    }
                    drawPath(path, color = barColor, style = Stroke(width = 3.dp.toPx()))
                } else {
                    chart.values.forEachIndexed { i, v ->
                        val barW = (slotWidth * 0.55f).coerceAtMost(48.dp.toPx())
                        val barH = (v / maxVal) * (size.height * 0.82f)
                        val x = slotWidth * i + (slotWidth - barW) / 2f
                        val y = size.height - barH
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, y),
                            size = Size(barW, barH),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                chart.labels.forEachIndexed { idx, label ->
                    val v = chart.values.getOrNull(idx) ?: 0f
                    Text("$label (${v.toInt()})", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Color(0xFF495057))
                }
            }
        }
    }
}

@Composable
private fun SmartArtBlockRenderer(smartArt: SmartArtData) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        color = Color(0xFFF1F3F5),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "SmartArt • ${smartArt.layoutType}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = WordCraftPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                smartArt.nodes.forEachIndexed { index, node ->
                    Surface(
                        color = WordCraftPrimary,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = node,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                    if (index < smartArt.nodes.lastIndex) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = WordCraftPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShapeBlockRenderer(
    shapeKind: String,
    text: String,
    onTextChange: (String) -> Unit,
    isActive: Boolean
) {
    val shape = when (shapeKind) {
        "OVAL" -> CircleShape
        "RECTANGLE" -> RoundedCornerShape(0.dp)
        else -> RoundedCornerShape(12.dp)
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 24.dp),
        color = Color(0xFFE7F5FF),
        shape = shape,
        border = androidx.compose.foundation.BorderStroke(2.dp, WordCraftPrimary)
    ) {
        Box(
            modifier = Modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            EditableText(
                text = text,
                onTextChange = onTextChange,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF182A6A),
                textAlign = TextAlign.Center,
                isActive = isActive
            )
        }
    }
}

@Composable
private fun SignatureLineRenderer(
    signerText: String,
    onTextChange: (String) -> Unit,
    isActive: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = "X",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF495057),
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Divider(
            color = Color(0xFF212529),
            thickness = 1.5.dp,
            modifier = Modifier.width(260.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        EditableText(
            text = signerText,
            onTextChange = onTextChange,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF495057),
            textAlign = TextAlign.Left,
            isActive = isActive
        )
    }
}

@Composable
private fun EditableText(
    text: String,
    onTextChange: (String) -> Unit,
    fontSize: androidx.compose.ui.unit.TextUnit,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    fontFamily: FontFamily? = null,
    textDecoration: TextDecoration = TextDecoration.None,
    lineSpacingMultiplier: Float = 1.35f,
    color: Color,
    textAlign: TextAlign,
    isActive: Boolean
) {
    if (isActive) {
        var tfv by remember(isActive) {
            mutableStateOf(
                TextFieldValue(
                    text = text,
                    selection = TextRange(text.length)
                )
            )
        }

        LaunchedEffect(text) {
            if (tfv.text != text) {
                tfv = tfv.copy(text = text)
            }
        }

        OutlinedTextField(
            value = tfv,
            onValueChange = { newValue ->
                tfv = newValue
                if (newValue.text != text) {
                    onTextChange(newValue.text)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = fontSize,
                fontWeight = fontWeight ?: FontWeight.Normal,
                fontStyle = fontStyle ?: FontStyle.Normal,
                fontFamily = fontFamily,
                textDecoration = textDecoration,
                color = color,
                textAlign = textAlign,
                lineHeight = (fontSize.value * lineSpacingMultiplier).sp
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = WordCraftPrimary.copy(alpha = 0.5f),
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    } else {
        Text(
            text = if (text.isBlank()) " " else text,
            fontSize = fontSize,
            fontWeight = fontWeight ?: FontWeight.Normal,
            fontStyle = fontStyle ?: FontStyle.Normal,
            fontFamily = fontFamily,
            textDecoration = textDecoration,
            color = color,
            textAlign = textAlign,
            lineHeight = (fontSize.value * lineSpacingMultiplier).sp,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun TableRenderer(
    table: TableData,
    isActive: Boolean,
    onCellChange: (Int, Int, String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .horizontalScroll(rememberScrollState()),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCED4DA))
    ) {
        Column {
            // Header Row
            if (table.headers.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .background(Color(0xFFE7F5FF))
                        .border(0.5.dp, Color(0xFFCED4DA))
                ) {
                    table.headers.forEachIndexed { colIdx, header ->
                        TableCell(
                            text = header,
                            isHeader = true,
                            onTextChange = { onCellChange(-1, colIdx, it) }
                        )
                    }
                }
            }

            // Data Rows
            table.rows.forEachIndexed { rowIdx, row ->
                val rowBg = if (rowIdx % 2 == 1 && table.style == TableStyle.BANDED_BLUE) {
                    Color(0xFFF8F9FA)
                } else Color.White

                Row(
                    modifier = Modifier
                        .background(rowBg)
                        .border(0.5.dp, Color(0xFFE9ECEF))
                ) {
                    row.forEachIndexed { colIdx, cell ->
                        TableCell(
                            text = cell,
                            isHeader = false,
                            onTextChange = { onCellChange(rowIdx, colIdx, it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCell(
    text: String,
    isHeader: Boolean,
    onTextChange: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(160.dp)
            .border(0.5.dp, Color(0xFFDEE2E6))
            .clickable { isEditing = !isEditing }
            .padding(8.dp)
    ) {
        if (isEditing) {
            var cellTfv by remember(isEditing) {
                mutableStateOf(
                    TextFieldValue(
                        text = text,
                        selection = TextRange(text.length)
                    )
                )
            }

            LaunchedEffect(text) {
                if (cellTfv.text != text) {
                    cellTfv = cellTfv.copy(text = text)
                }
            }

            OutlinedTextField(
                value = cellTfv,
                onValueChange = { newVal ->
                    cellTfv = newVal
                    if (newVal.text != text) {
                        onTextChange(newVal.text)
                    }
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 13.sp,
                    fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
                ),
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = if (text.isBlank()) "-" else text,
                fontSize = 13.sp,
                fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                color = if (isHeader) Color(0xFF182A6A) else Color(0xFF212529)
            )
        }
    }
}

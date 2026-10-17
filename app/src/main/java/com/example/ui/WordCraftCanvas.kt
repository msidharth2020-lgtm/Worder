package com.example.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BlockType
import com.example.model.CalloutType
import com.example.model.DocBlock
import com.example.model.TableData
import com.example.model.TableStyle
import com.example.model.TextAlignment
import com.example.model.WordCraftDoc
import com.example.ui.theme.WordCraftCanvasDark
import com.example.ui.theme.WordCraftCanvasLight
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
    val paperBg = if (isDarkCanvas) WordCraftPaperDark else WordCraftPaperLight
    val textPrimaryColor = if (isDarkCanvas) Color(0xFFE2E2E6) else Color(0xFF212529)
    val textMutedColor = if (isDarkCanvas) Color(0xFF9E9EA4) else Color(0xFF6C757D)

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

        val maxWidth = when (viewMode) {
            ViewMode.PRINT_LAYOUT -> 760.dp
            ViewMode.WEB_LAYOUT -> 820.dp
            ViewMode.READING_MODE -> 680.dp
        }

        Column(
            modifier = Modifier
                .padding(pagePadding)
                .widthIn(max = maxWidth)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Paper Sheet Container
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (viewMode == ViewMode.PRINT_LAYOUT) 6.dp else 1.dp,
                        shape = RoundedCornerShape(if (viewMode == ViewMode.PRINT_LAYOUT) 4.dp else 0.dp)
                    )
                    .testTag("document_paper_sheet"),
                color = paperBg,
                shape = RoundedCornerShape(if (viewMode == ViewMode.PRINT_LAYOUT) 4.dp else 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = doc.margins.paddingDp.dp, vertical = (doc.margins.paddingDp * 0.8f).dp)
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

                        // Blocks rendering
                        doc.blocks.forEachIndexed { index, block ->
                            val isActive = block.id == activeBlockId
                            DocBlockItem(
                                block = block,
                                isActive = isActive,
                                textPrimaryColor = textPrimaryColor,
                                textMutedColor = textMutedColor,
                                onBlockClick = { viewModel.setActiveBlockId(block.id) },
                                onTextChange = { newText -> viewModel.updateBlockText(block.id, newText) },
                                onToggleChecklist = { viewModel.toggleChecklist(block.id) },
                                onTableCellChange = { r, c, valText -> viewModel.updateTableCell(block.id, r, c, valText) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
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
private fun DocBlockItem(
    block: DocBlock,
    isActive: Boolean,
    textPrimaryColor: Color,
    textMutedColor: Color,
    onBlockClick: () -> Unit,
    onTextChange: (String) -> Unit,
    onToggleChecklist: () -> Unit,
    onTableCellChange: (Int, Int, String) -> Unit
) {
    val textAlign = when (block.alignment) {
        TextAlignment.LEFT -> TextAlign.Left
        TextAlignment.CENTER -> TextAlign.Center
        TextAlignment.RIGHT -> TextAlign.Right
        TextAlignment.JUSTIFY -> TextAlign.Justify
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
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
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF182A6A),
                    textAlign = textAlign,
                    isActive = isActive
                )
            }
            BlockType.SUBTITLE -> {
                EditableText(
                    text = block.text,
                    onTextChange = onTextChange,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    fontStyle = FontStyle.Italic,
                    color = textMutedColor,
                    textAlign = textAlign,
                    isActive = isActive
                )
            }
            BlockType.HEADING_1 -> {
                EditableText(
                    text = block.text,
                    onTextChange = onTextChange,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = WordCraftPrimary,
                    textAlign = textAlign,
                    isActive = isActive
                )
            }
            BlockType.HEADING_2 -> {
                EditableText(
                    text = block.text,
                    onTextChange = onTextChange,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF3B5BDB),
                    textAlign = textAlign,
                    isActive = isActive
                )
            }
            BlockType.HEADING_3 -> {
                EditableText(
                    text = block.text,
                    onTextChange = onTextChange,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimaryColor,
                    textAlign = textAlign,
                    isActive = isActive
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
                        fontSize = 15.sp,
                        fontStyle = FontStyle.Italic,
                        color = Color(0xFF495057),
                        textAlign = textAlign,
                        isActive = isActive
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
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFE9ECEF),
                        textAlign = TextAlign.Left,
                        isActive = isActive
                    )
                }
            }
            BlockType.BULLET_LIST -> {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "•",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = WordCraftPrimary,
                        modifier = Modifier.padding(start = (block.indentLevel * 16).dp, end = 8.dp, top = 2.dp)
                    )
                    EditableText(
                        text = block.text,
                        onTextChange = onTextChange,
                        fontSize = 15.sp,
                        color = textPrimaryColor,
                        textAlign = textAlign,
                        isActive = isActive
                    )
                }
            }
            BlockType.NUMBERED_LIST -> {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "1.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WordCraftPrimary,
                        modifier = Modifier.padding(start = (block.indentLevel * 16).dp, end = 8.dp, top = 2.dp)
                    )
                    EditableText(
                        text = block.text,
                        onTextChange = onTextChange,
                        fontSize = 15.sp,
                        color = textPrimaryColor,
                        textAlign = textAlign,
                        isActive = isActive
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
                        onCheckedChange = { onToggleChecklist() },
                        colors = CheckboxDefaults.colors(checkedColor = WordCraftPrimary),
                        modifier = Modifier.size(28.dp).padding(end = 4.dp)
                    )
                    EditableText(
                        text = block.text,
                        onTextChange = onTextChange,
                        fontSize = 15.sp,
                        color = if (block.isChecked) textMutedColor else textPrimaryColor,
                        textAlign = textAlign,
                        isActive = isActive
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF212529),
                        textAlign = textAlign,
                        isActive = isActive
                    )
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
                // Default Paragraph
                EditableText(
                    text = block.text,
                    onTextChange = onTextChange,
                    fontSize = 15.sp,
                    color = textPrimaryColor,
                    textAlign = textAlign,
                    isActive = isActive
                )
            }
        }
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
                color = color,
                textAlign = textAlign,
                lineHeight = (fontSize.value * 1.35f).sp
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
            color = color,
            textAlign = textAlign,
            lineHeight = (fontSize.value * 1.35f).sp,
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

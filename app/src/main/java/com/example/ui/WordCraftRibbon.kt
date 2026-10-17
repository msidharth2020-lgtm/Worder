package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pageview
import androidx.compose.material.icons.filled.South
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BlockType
import com.example.model.CalloutType
import com.example.model.PageMargins
import com.example.model.PageOrientation
import com.example.model.PageSize
import com.example.model.TextAlignment
import com.example.ui.theme.WordCraftPrimary
import com.example.ui.theme.WordCraftRibbonBgLight
import com.example.viewmodel.RibbonTab
import com.example.viewmodel.ViewMode
import com.example.viewmodel.WordCraftViewModel

@Composable
fun WordCraftRibbon(
    viewModel: WordCraftViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.activeRibbonTab.collectAsState()
    val activeBlockId by viewModel.activeBlockId.collectAsState()
    val doc by viewModel.document.collectAsState()
    val activeBlock = remember(doc, activeBlockId) { doc.blocks.find { it.id == activeBlockId } }

    val tabs = remember(activeBlock) {
        if (activeBlock?.type == BlockType.TABLE) {
            RibbonTab.values().toList()
        } else {
            listOf(RibbonTab.HOME, RibbonTab.INSERT, RibbonTab.LAYOUT, RibbonTab.REVIEW, RibbonTab.VIEW)
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Tabs Strip
            ScrollableTabRow(
                selectedTabIndex = tabs.indexOf(activeTab).coerceAtLeast(0),
                edgePadding = 8.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = WordCraftPrimary,
                indicator = { tabPositions ->
                    val index = tabs.indexOf(activeTab).coerceAtLeast(0)
                    if (index in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = WordCraftPrimary,
                            height = 3.dp
                        )
                    }
                },
                divider = {}
            ) {
                tabs.forEach { tab ->
                    val isSelected = activeTab == tab
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.setActiveRibbonTab(tab) },
                        modifier = Modifier.testTag("ribbon_tab_${tab.name.lowercase()}"),
                        text = {
                            Text(
                                text = tab.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) WordCraftPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

            // Active Tab Tools Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when (activeTab) {
                    RibbonTab.HOME -> HomeRibbonTools(viewModel, activeBlock)
                    RibbonTab.INSERT -> InsertRibbonTools(viewModel, activeBlock)
                    RibbonTab.LAYOUT -> LayoutRibbonTools(viewModel)
                    RibbonTab.REVIEW -> ReviewRibbonTools(viewModel, activeBlock)
                    RibbonTab.VIEW -> ViewRibbonTools(viewModel)
                    RibbonTab.TABLE_TOOLS -> TableRibbonTools(viewModel, activeBlock)
                }
            }
        }
    }
}

@Composable
private fun HomeRibbonTools(
    viewModel: WordCraftViewModel,
    activeBlock: com.example.model.DocBlock?
) {
    // Styles Dropdown Gallery
    var showStylesMenu by remember { mutableStateOf(false) }
    val currentStyleName = when (activeBlock?.type) {
        BlockType.TITLE -> "Title"
        BlockType.SUBTITLE -> "Subtitle"
        BlockType.HEADING_1 -> "Heading 1"
        BlockType.HEADING_2 -> "Heading 2"
        BlockType.HEADING_3 -> "Heading 3"
        BlockType.QUOTE -> "Quote"
        BlockType.CODE -> "Code"
        BlockType.BULLET_LIST -> "Bullet List"
        BlockType.NUMBERED_LIST -> "Numbered"
        BlockType.CHECKLIST -> "Checklist"
        else -> "Normal"
    }

    Box {
        RibbonActionButton(
            icon = Icons.Default.Title,
            label = currentStyleName,
            onClick = { showStylesMenu = true },
            testTag = "ribbon_styles_button"
        )
        DropdownMenu(
            expanded = showStylesMenu,
            onDismissRequest = { showStylesMenu = false }
        ) {
            listOf(
                "Normal" to BlockType.PARAGRAPH,
                "Title" to BlockType.TITLE,
                "Subtitle" to BlockType.SUBTITLE,
                "Heading 1" to BlockType.HEADING_1,
                "Heading 2" to BlockType.HEADING_2,
                "Heading 3" to BlockType.HEADING_3,
                "Quote" to BlockType.QUOTE,
                "Code Block" to BlockType.CODE
            ).forEach { (label, type) ->
                DropdownMenuItem(
                    text = { Text(label, fontWeight = if (label.startsWith("Heading") || label == "Title") FontWeight.Bold else FontWeight.Normal) },
                    onClick = {
                        activeBlock?.let { viewModel.updateBlockType(it.id, type) }
                        showStylesMenu = false
                    }
                )
            }
        }
    }

    RibbonDivider()

    // Alignment buttons
    val currentAlign = activeBlock?.alignment ?: TextAlignment.LEFT
    RibbonIconButton(
        icon = Icons.Default.FormatAlignLeft,
        label = "Left",
        isSelected = currentAlign == TextAlignment.LEFT,
        onClick = { activeBlock?.let { viewModel.updateBlockAlignment(it.id, TextAlignment.LEFT) } },
        testTag = "align_left_button"
    )
    RibbonIconButton(
        icon = Icons.Default.FormatAlignCenter,
        label = "Center",
        isSelected = currentAlign == TextAlignment.CENTER,
        onClick = { activeBlock?.let { viewModel.updateBlockAlignment(it.id, TextAlignment.CENTER) } },
        testTag = "align_center_button"
    )
    RibbonIconButton(
        icon = Icons.Default.FormatAlignRight,
        label = "Right",
        isSelected = currentAlign == TextAlignment.RIGHT,
        onClick = { activeBlock?.let { viewModel.updateBlockAlignment(it.id, TextAlignment.RIGHT) } },
        testTag = "align_right_button"
    )
    RibbonIconButton(
        icon = Icons.Default.FormatAlignJustify,
        label = "Justify",
        isSelected = currentAlign == TextAlignment.JUSTIFY,
        onClick = { activeBlock?.let { viewModel.updateBlockAlignment(it.id, TextAlignment.JUSTIFY) } },
        testTag = "align_justify_button"
    )

    RibbonDivider()

    // List buttons
    RibbonIconButton(
        icon = Icons.Default.FormatListBulleted,
        label = "Bullets",
        isSelected = activeBlock?.type == BlockType.BULLET_LIST,
        onClick = {
            activeBlock?.let {
                val newType = if (it.type == BlockType.BULLET_LIST) BlockType.PARAGRAPH else BlockType.BULLET_LIST
                viewModel.updateBlockType(it.id, newType)
            }
        },
        testTag = "bullet_list_button"
    )
    RibbonIconButton(
        icon = Icons.Default.FormatListNumbered,
        label = "Numbered",
        isSelected = activeBlock?.type == BlockType.NUMBERED_LIST,
        onClick = {
            activeBlock?.let {
                val newType = if (it.type == BlockType.NUMBERED_LIST) BlockType.PARAGRAPH else BlockType.NUMBERED_LIST
                viewModel.updateBlockType(it.id, newType)
            }
        },
        testTag = "numbered_list_button"
    )
    RibbonIconButton(
        icon = Icons.Default.Checklist,
        label = "Checklist",
        isSelected = activeBlock?.type == BlockType.CHECKLIST,
        onClick = {
            activeBlock?.let {
                val newType = if (it.type == BlockType.CHECKLIST) BlockType.PARAGRAPH else BlockType.CHECKLIST
                viewModel.updateBlockType(it.id, newType)
            }
        },
        testTag = "checklist_button"
    )

    RibbonDivider()

    // Paragraph add/delete actions
    RibbonActionButton(
        icon = Icons.Default.Add,
        label = "Add Para",
        onClick = { viewModel.insertBlock(activeBlock?.id, BlockType.PARAGRAPH) },
        testTag = "add_para_button"
    )
    if (activeBlock != null) {
        RibbonActionButton(
            icon = Icons.Default.Delete,
            label = "Delete",
            onClick = { viewModel.deleteBlock(activeBlock.id) },
            testTag = "delete_block_button"
        )
    }
}

@Composable
private fun InsertRibbonTools(
    viewModel: WordCraftViewModel,
    activeBlock: com.example.model.DocBlock?
) {
    var showTableDialog by remember { mutableStateOf(false) }

    RibbonActionButton(
        icon = Icons.Default.TableChart,
        label = "Table (3x3)",
        onClick = { viewModel.insertTable(rows = 3, cols = 3, afterBlockId = activeBlock?.id) },
        testTag = "insert_table_button"
    )

    var showCalloutMenu by remember { mutableStateOf(false) }
    Box {
        RibbonActionButton(
            icon = Icons.Default.Info,
            label = "Callout Note",
            onClick = { showCalloutMenu = true },
            testTag = "insert_callout_button"
        )
        DropdownMenu(
            expanded = showCalloutMenu,
            onDismissRequest = { showCalloutMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Info Note (Blue)") },
                onClick = {
                    viewModel.insertCallout(activeBlock?.id, CalloutType.INFO, "Note: Add information here.")
                    showCalloutMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Warning Banner (Amber)") },
                onClick = {
                    viewModel.insertCallout(activeBlock?.id, CalloutType.WARNING, "Warning: Pay attention to this detail.")
                    showCalloutMenu = false
                }
            )
            DropdownMenuItem(
                text = { Text("Tip Box (Green)") },
                onClick = {
                    viewModel.insertCallout(activeBlock?.id, CalloutType.TIP, "Pro Tip: Use keyboard shortcuts for speed.")
                    showCalloutMenu = false
                }
            )
        }
    }

    RibbonActionButton(
        icon = Icons.Default.ViewAgenda,
        label = "Divider Line",
        onClick = { viewModel.insertBlock(activeBlock?.id, BlockType.HORIZONTAL_RULE) },
        testTag = "insert_divider_button"
    )

    RibbonActionButton(
        icon = Icons.Default.South,
        label = "Page Break",
        onClick = { viewModel.insertBlock(activeBlock?.id, BlockType.PAGE_BREAK) },
        testTag = "insert_page_break_button"
    )

    RibbonActionButton(
        icon = Icons.Default.FormatQuote,
        label = "Quote Block",
        onClick = { viewModel.insertBlock(activeBlock?.id, BlockType.QUOTE, "Your inspirational quote here...") },
        testTag = "insert_quote_button"
    )
}

@Composable
private fun LayoutRibbonTools(viewModel: WordCraftViewModel) {
    val doc by viewModel.document.collectAsState()

    var showMarginsMenu by remember { mutableStateOf(false) }
    Box {
        RibbonActionButton(
            icon = Icons.Default.ViewAgenda,
            label = "Margins: ${doc.margins.displayName.split(" ").first()}",
            onClick = { showMarginsMenu = true },
            testTag = "layout_margins_button"
        )
        DropdownMenu(expanded = showMarginsMenu, onDismissRequest = { showMarginsMenu = false }) {
            PageMargins.values().forEach { m ->
                DropdownMenuItem(
                    text = { Text(m.displayName) },
                    onClick = {
                        viewModel.setPageMargins(m)
                        showMarginsMenu = false
                    }
                )
            }
        }
    }

    var showOrientationMenu by remember { mutableStateOf(false) }
    Box {
        RibbonActionButton(
            icon = Icons.Default.ViewCarousel,
            label = doc.orientation.name.lowercase().capitalize(),
            onClick = { showOrientationMenu = true },
            testTag = "layout_orientation_button"
        )
        DropdownMenu(expanded = showOrientationMenu, onDismissRequest = { showOrientationMenu = false }) {
            PageOrientation.values().forEach { o ->
                DropdownMenuItem(
                    text = { Text(o.name.lowercase().capitalize()) },
                    onClick = {
                        viewModel.setPageOrientation(o)
                        showOrientationMenu = false
                    }
                )
            }
        }
    }

    var showPaperMenu by remember { mutableStateOf(false) }
    Box {
        RibbonActionButton(
            icon = Icons.Default.Pageview,
            label = doc.pageSize.displayName.split(" ").first(),
            onClick = { showPaperMenu = true },
            testTag = "layout_paper_button"
        )
        DropdownMenu(expanded = showPaperMenu, onDismissRequest = { showPaperMenu = false }) {
            PageSize.values().forEach { s ->
                DropdownMenuItem(
                    text = { Text(s.displayName) },
                    onClick = {
                        viewModel.setPageSize(s)
                        showPaperMenu = false
                    }
                )
            }
        }
    }

    var showWatermarkMenu by remember { mutableStateOf(false) }
    Box {
        RibbonActionButton(
            icon = Icons.Default.Bookmark,
            label = if (doc.watermark != null) "Watermark: ${doc.watermark}" else "Watermark",
            onClick = { showWatermarkMenu = true },
            testTag = "layout_watermark_button"
        )
        DropdownMenu(expanded = showWatermarkMenu, onDismissRequest = { showWatermarkMenu = false }) {
            listOf(null, "DRAFT", "CONFIDENTIAL", "SAMPLE", "URGENT").forEach { w ->
                DropdownMenuItem(
                    text = { Text(w ?: "None (Remove)") },
                    onClick = {
                        viewModel.setWatermark(w)
                        showWatermarkMenu = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ReviewRibbonTools(
    viewModel: WordCraftViewModel,
    activeBlock: com.example.model.DocBlock?
) {
    val showNav by viewModel.showNavPane.collectAsState()
    val showComments by viewModel.showCommentsPane.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    RibbonActionButton(
        icon = Icons.Default.Info,
        label = "Document Stats",
        onClick = { viewModel.setShowStatsDialog(true) },
        testTag = "review_stats_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Menu,
        label = "Outline Navigation",
        isSelected = showNav,
        onClick = { viewModel.toggleNavPane() },
        testTag = "review_outline_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Comment,
        label = "Comments Pane",
        isSelected = showComments,
        onClick = { viewModel.toggleCommentsPane() },
        testTag = "review_comments_button"
    )

    RibbonActionButton(
        icon = Icons.Default.VolumeUp,
        label = if (isSpeaking) "Stop Reading" else "Read Aloud",
        isSelected = isSpeaking,
        onClick = { viewModel.toggleSpeech() },
        testTag = "review_read_aloud_button"
    )
}

@Composable
private fun ViewRibbonTools(viewModel: WordCraftViewModel) {
    val viewMode by viewModel.viewMode.collectAsState()
    val isDarkCanvas by viewModel.isDarkCanvas.collectAsState()
    val zoom by viewModel.zoomPercent.collectAsState()

    RibbonActionButton(
        icon = Icons.Default.Pageview,
        label = "Print Layout",
        isSelected = viewMode == ViewMode.PRINT_LAYOUT,
        onClick = { viewModel.setViewMode(ViewMode.PRINT_LAYOUT) },
        testTag = "view_print_layout_button"
    )

    RibbonActionButton(
        icon = Icons.Default.ViewAgenda,
        label = "Continuous",
        isSelected = viewMode == ViewMode.WEB_LAYOUT,
        onClick = { viewModel.setViewMode(ViewMode.WEB_LAYOUT) },
        testTag = "view_continuous_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Bookmark,
        label = "Read Mode",
        isSelected = viewMode == ViewMode.READING_MODE,
        onClick = { viewModel.setViewMode(ViewMode.READING_MODE) },
        testTag = "view_read_mode_button"
    )

    RibbonDivider()

    RibbonActionButton(
        icon = if (isDarkCanvas) Icons.Default.LightMode else Icons.Default.DarkMode,
        label = if (isDarkCanvas) "White Paper" else "Dark Canvas",
        isSelected = isDarkCanvas,
        onClick = { viewModel.toggleDarkCanvas() },
        testTag = "view_dark_canvas_button"
    )

    RibbonDivider()

    listOf(75, 100, 125, 150).forEach { z ->
        RibbonActionButton(
            label = "$z%",
            isSelected = zoom == z,
            onClick = { viewModel.setZoomPercent(z) },
            testTag = "zoom_${z}_button"
        )
    }
}

@Composable
private fun TableRibbonTools(
    viewModel: WordCraftViewModel,
    activeBlock: com.example.model.DocBlock?
) {
    if (activeBlock == null || activeBlock.type != BlockType.TABLE) {
        Text("Select a table in the document to edit rows and columns.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }

    RibbonActionButton(
        icon = Icons.Default.Add,
        label = "Insert Row",
        onClick = { viewModel.addTableRow(activeBlock.id) },
        testTag = "table_add_row_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Add,
        label = "Insert Column",
        onClick = { viewModel.addTableCol(activeBlock.id) },
        testTag = "table_add_col_button"
    )

    val rowCount = activeBlock.tableData?.rows?.size ?: 0
    if (rowCount > 1) {
        RibbonActionButton(
            icon = Icons.Default.Delete,
            label = "Delete Last Row",
            onClick = { viewModel.deleteTableRow(activeBlock.id, rowCount - 1) },
            testTag = "table_del_row_button"
        )
    }

    RibbonActionButton(
        icon = Icons.Default.Delete,
        label = "Delete Table",
        onClick = { viewModel.deleteBlock(activeBlock.id) },
        testTag = "table_delete_button"
    )
}

@Composable
private fun RibbonActionButton(
    icon: ImageVector? = null,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = if (isSelected) WordCraftPrimary.copy(alpha = 0.15f) else Color.Transparent,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    modifier = Modifier.size(16.dp),
                    tint = if (isSelected) WordCraftPrimary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) WordCraftPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RibbonIconButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = if (isSelected) WordCraftPrimary.copy(alpha = 0.2f) else Color.Transparent,
        shape = RoundedCornerShape(6.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(18.dp),
                tint = if (isSelected) WordCraftPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RibbonDivider() {
    Divider(
        modifier = Modifier
            .height(24.dp)
            .width(1.dp)
            .padding(horizontal = 2.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
    )
}

private fun String.capitalize(): String =
    this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

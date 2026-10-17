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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BorderOuter
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatIndentDecrease
import androidx.compose.material.icons.filled.FormatIndentIncrease
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pageview
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.South
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import com.example.model.DocumentThemePreset
import com.example.model.PageMargins
import com.example.model.PageOrientation
import com.example.model.PageSize
import com.example.model.TableStyle
import com.example.model.TextAlignment
import com.example.ui.theme.WordCraftFonts
import com.example.ui.theme.WordCraftPrimary
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
    val activeBlock = remember(doc, activeBlockId) {
        doc.blocks.find { it.id == activeBlockId } ?: doc.blocks.firstOrNull()
    }

    val tabs = remember(activeBlock) {
        if (activeBlock?.type == BlockType.TABLE) {
            RibbonTab.values().toList()
        } else {
            RibbonTab.values().filterNot { it == RibbonTab.TABLE_TOOLS }
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Tabs Strip (All 11 standard tabs + contextual Table tab from wordcraft-web-0.7.0)
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
                    RibbonTab.DRAW -> DrawRibbonTools(viewModel)
                    RibbonTab.DESIGN -> DesignRibbonTools(viewModel)
                    RibbonTab.LAYOUT -> LayoutRibbonTools(viewModel)
                    RibbonTab.REFERENCES -> ReferencesRibbonTools(viewModel)
                    RibbonTab.MAILINGS -> MailingsRibbonTools(viewModel)
                    RibbonTab.ZOTERO -> ZoteroRibbonTools(viewModel)
                    RibbonTab.REVIEW -> ReviewRibbonTools(viewModel)
                    RibbonTab.VIEW -> ViewRibbonTools(viewModel)
                    RibbonTab.DEVELOPER -> DeveloperRibbonTools(viewModel)
                    RibbonTab.HELP -> HelpRibbonTools(viewModel)
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
    // 1. Font Family Selector
    var showFontMenu by remember { mutableStateOf(false) }
    val currentFontName = activeBlock?.fontFamily ?: "Inter"

    Box {
        RibbonActionButton(
            icon = Icons.Default.FontDownload,
            label = currentFontName,
            onClick = { showFontMenu = true },
            testTag = "ribbon_font_family_button"
        )
        DropdownMenu(
            expanded = showFontMenu,
            onDismissRequest = { showFontMenu = false }
        ) {
            WordCraftFonts.availableFonts.forEach { fontOpt ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = fontOpt.displayName,
                                fontFamily = fontOpt.fontFamily,
                                fontSize = 15.sp,
                                fontWeight = if (currentFontName == fontOpt.id) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = fontOpt.category,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    onClick = {
                        activeBlock?.let { viewModel.updateBlockFontFamily(it.id, fontOpt.id) }
                        showFontMenu = false
                    }
                )
            }
            Divider()
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Apply \"$currentFontName\" to Entire Document",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WordCraftPrimary
                    )
                },
                onClick = {
                    viewModel.updateAllBlocksFontFamily(currentFontName)
                    showFontMenu = false
                }
            )
        }
    }

    // 2. Font Size Selector & Stepper Buttons
    var showFontSizeMenu by remember { mutableStateOf(false) }
    val currentFontSize = activeBlock?.effectiveFontSizeSp ?: 15

    Box {
        RibbonActionButton(
            icon = Icons.Default.FormatSize,
            label = "${currentFontSize} pt",
            onClick = { showFontSizeMenu = true },
            testTag = "ribbon_font_size_button"
        )
        DropdownMenu(
            expanded = showFontSizeMenu,
            onDismissRequest = { showFontSizeMenu = false }
        ) {
            WordCraftFonts.presetFontSizes.forEach { sizePt ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "$sizePt pt",
                            fontWeight = if (sizePt == currentFontSize) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        activeBlock?.let { viewModel.updateBlockFontSize(it.id, sizePt) }
                        showFontSizeMenu = false
                    }
                )
            }
        }
    }

    RibbonIconButton(
        icon = Icons.Default.TextIncrease,
        label = "Increase Font Size",
        onClick = { activeBlock?.let { viewModel.stepBlockFontSize(it.id, +2) } },
        testTag = "font_size_increase_button"
    )
    RibbonIconButton(
        icon = Icons.Default.TextDecrease,
        label = "Decrease Font Size",
        onClick = { activeBlock?.let { viewModel.stepBlockFontSize(it.id, -2) } },
        testTag = "font_size_decrease_button"
    )

    RibbonDivider()

    // 3. Bold, Italic, Underline, Strikethrough, Change Case, Text Color, Highlight, Clear Formatting
    RibbonIconButton(
        icon = Icons.Default.FormatBold,
        label = "Bold",
        isSelected = activeBlock?.bold == true,
        onClick = { activeBlock?.let { viewModel.toggleBlockBold(it.id) } },
        testTag = "format_bold_button"
    )
    RibbonIconButton(
        icon = Icons.Default.FormatItalic,
        label = "Italic",
        isSelected = activeBlock?.italic == true,
        onClick = { activeBlock?.let { viewModel.toggleBlockItalic(it.id) } },
        testTag = "format_italic_button"
    )
    RibbonIconButton(
        icon = Icons.Default.FormatUnderlined,
        label = "Underline",
        isSelected = activeBlock?.underline == true,
        onClick = { activeBlock?.let { viewModel.toggleBlockUnderline(it.id) } },
        testTag = "format_underline_button"
    )
    RibbonIconButton(
        icon = Icons.Default.FormatStrikethrough,
        label = "Strikethrough",
        isSelected = activeBlock?.strikethrough == true,
        onClick = { activeBlock?.let { viewModel.toggleBlockStrikethrough(it.id) } },
        testTag = "format_strikethrough_button"
    )

    // Change Case Menu (Aa)
    var showCaseMenu by remember { mutableStateOf(false) }
    Box {
        RibbonActionButton(
            icon = Icons.Default.TextFields,
            label = "Aa",
            onClick = { showCaseMenu = true },
            testTag = "change_case_button"
        )
        DropdownMenu(expanded = showCaseMenu, onDismissRequest = { showCaseMenu = false }) {
            listOf(
                "Sentence case." to "SENTENCE",
                "lowercase" to "LOWER",
                "UPPERCASE" to "UPPER",
                "Capitalize Each Word" to "TITLE",
                "tOGGLE cASE" to "TOGGLE"
            ).forEach { (label, mode) ->
                DropdownMenuItem(
                    text = { Text(label, fontSize = 13.sp) },
                    onClick = {
                        activeBlock?.let { viewModel.changeBlockCase(it.id, mode) }
                        showCaseMenu = false
                    }
                )
            }
        }
    }

    var showColorMenu by remember { mutableStateOf(false) }
    Box {
        RibbonIconButton(
            icon = Icons.Default.FormatColorText,
            label = "Text Color",
            isSelected = !activeBlock?.textColorHex.isNullOrBlank(),
            onClick = { showColorMenu = true },
            testTag = "format_color_button"
        )
        DropdownMenu(
            expanded = showColorMenu,
            onDismissRequest = { showColorMenu = false }
        ) {
            val colors = listOf(
                null to "Default Ink",
                "#182A6A" to "Navy Blue",
                "#2B47B5" to "Royal Blue",
                "#C92A2A" to "Crimson Red",
                "#2B8A3E" to "Forest Green",
                "#E67700" to "Deep Amber",
                "#5F3DC4" to "Royal Purple",
                "#495057" to "Slate Gray"
            )
            colors.forEach { (hex, label) ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (hex != null) Color(android.graphics.Color.parseColor(hex))
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, fontSize = 13.sp)
                        }
                    },
                    onClick = {
                        activeBlock?.let { viewModel.updateBlockTextColor(it.id, hex) }
                        showColorMenu = false
                    }
                )
            }
        }
    }

    var showHighlightMenu by remember { mutableStateOf(false) }
    Box {
        RibbonIconButton(
            icon = Icons.Default.FormatColorFill,
            label = "Highlight",
            isSelected = !activeBlock?.highlightColorHex.isNullOrBlank(),
            onClick = { showHighlightMenu = true },
            testTag = "format_highlight_button"
        )
        DropdownMenu(expanded = showHighlightMenu, onDismissRequest = { showHighlightMenu = false }) {
            listOf(
                null to "No Highlight",
                "#FFF3BF" to "Yellow Marker",
                "#D3F9D8" to "Mint Green",
                "#D0EBFF" to "Sky Blue",
                "#FFE3E3" to "Soft Rose"
            ).forEach { (hex, label) ->
                DropdownMenuItem(
                    text = { Text(label, fontSize = 13.sp) },
                    onClick = {
                        activeBlock?.let { viewModel.updateBlockHighlightColor(it.id, hex) }
                        showHighlightMenu = false
                    }
                )
            }
        }
    }

    RibbonIconButton(
        icon = Icons.Default.FormatClear,
        label = "Clear Formatting",
        onClick = { activeBlock?.let { viewModel.clearBlockFormatting(it.id) } },
        testTag = "clear_formatting_button"
    )

    RibbonDivider()

    // 4. Styles Dropdown Gallery
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

    // 5. Alignment & Indent buttons
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
    RibbonIconButton(
        icon = Icons.Default.FormatIndentIncrease,
        label = "Increase Indent",
        onClick = { activeBlock?.let { viewModel.changeBlockIndent(it.id, +1) } },
        testTag = "indent_increase_button"
    )
    RibbonIconButton(
        icon = Icons.Default.FormatIndentDecrease,
        label = "Decrease Indent",
        onClick = { activeBlock?.let { viewModel.changeBlockIndent(it.id, -1) } },
        testTag = "indent_decrease_button"
    )

    RibbonDivider()

    // 6. List buttons
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

    // 7. Quick Unicode & Translate shortcuts
    RibbonActionButton(
        icon = Icons.Default.Code,
        label = "Ω Symbol",
        onClick = { viewModel.setShowUnicodeDialog(true) },
        testTag = "home_unicode_button"
    )
    RibbonActionButton(
        icon = Icons.Default.Translate,
        label = "Translate",
        onClick = { viewModel.setShowTranslateDialog(true) },
        testTag = "home_translate_button"
    )

    RibbonDivider()

    // 8. Paragraph add/delete actions
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
    RibbonActionButton(
        icon = Icons.Default.MenuBook,
        label = "Cover Page",
        onClick = { viewModel.insertCoverPage() },
        testTag = "insert_cover_page_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Functions,
        label = "π Equation",
        onClick = { viewModel.setShowEquationDialog(true) },
        testTag = "insert_equation_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Code,
        label = "Ω Symbols",
        onClick = { viewModel.setShowUnicodeDialog(true) },
        testTag = "insert_unicode_button"
    )

    RibbonActionButton(
        icon = Icons.Default.SwapHoriz,
        label = "Alt+X",
        onClick = { viewModel.convertTrailingHexInActiveBlock() },
        testTag = "insert_alt_x_button"
    )

    RibbonDivider()

    RibbonActionButton(
        icon = Icons.Default.TableChart,
        label = "Table (3x3)",
        onClick = { viewModel.insertTable(rows = 3, cols = 3, afterBlockId = activeBlock?.id) },
        testTag = "insert_table_button"
    )

    RibbonActionButton(
        icon = Icons.Default.BarChart,
        label = "Chart / SmartArt",
        onClick = { viewModel.setShowChartSmartArtDialog(true) },
        testTag = "insert_chart_smartart_button"
    )

    var showShapeMenu by remember { mutableStateOf(false) }
    Box {
        RibbonActionButton(
            icon = Icons.Default.BorderOuter,
            label = "Shapes",
            onClick = { showShapeMenu = true },
            testTag = "insert_shapes_button"
        )
        DropdownMenu(expanded = showShapeMenu, onDismissRequest = { showShapeMenu = false }) {
            listOf(
                "Rounded Banner" to "ROUNDED_RECT",
                "Rectangle Box" to "RECTANGLE",
                "Oval Badge" to "OVAL",
                "Decision Diamond" to "DIAMOND"
            ).forEach { (label, kind) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        viewModel.insertShapeBlock(kind, label)
                        showShapeMenu = false
                    }
                )
            }
        }
    }

    var showCalloutMenu by remember { mutableStateOf(false) }
    Box {
        RibbonActionButton(
            icon = Icons.Default.Info,
            label = "Callout",
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
        icon = Icons.Default.TextFields,
        label = "Drop Cap",
        isSelected = activeBlock?.dropCap == true,
        onClick = { activeBlock?.let { viewModel.toggleBlockDropCap(it.id) } },
        testTag = "insert_drop_cap_button"
    )

    RibbonActionButton(
        icon = Icons.Default.VerifiedUser,
        label = "Signature Line",
        onClick = { viewModel.insertSignatureLineBlock() },
        testTag = "insert_signature_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Schedule,
        label = "Date & Time",
        onClick = { viewModel.insertDateTimeStamp() },
        testTag = "insert_datetime_button"
    )

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
private fun DrawRibbonTools(viewModel: WordCraftViewModel) {
    RibbonActionButton(
        icon = Icons.Default.Brush,
        label = "Freehand Ink Pad & Replay",
        onClick = { viewModel.setShowDrawingDialog(true) },
        testTag = "draw_ink_pad_button"
    )

    RibbonDivider()

    RibbonActionButton(
        icon = Icons.Default.BorderOuter,
        label = "Ink to Shape",
        onClick = { viewModel.insertShapeBlock("ROUNDED_RECT", "Converted Ink Shape") },
        testTag = "draw_ink_to_shape_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Functions,
        label = "Ink to Math",
        onClick = { viewModel.setShowEquationDialog(true) },
        testTag = "draw_ink_to_math_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Add,
        label = "Drawing Canvas",
        onClick = { viewModel.setShowDrawingDialog(true) },
        testTag = "draw_canvas_button"
    )
}

@Composable
private fun DesignRibbonTools(viewModel: WordCraftViewModel) {
    val doc by viewModel.document.collectAsState()

    var showThemeMenu by remember { mutableStateOf(false) }
    Box {
        RibbonActionButton(
            icon = Icons.Default.Palette,
            label = "Theme: ${doc.themePreset.displayName}",
            onClick = { showThemeMenu = true },
            testTag = "design_theme_button"
        )
        DropdownMenu(expanded = showThemeMenu, onDismissRequest = { showThemeMenu = false }) {
            DocumentThemePreset.values().forEach { preset ->
                DropdownMenuItem(
                    text = { Text("${preset.displayName} (${preset.headingFont} / ${preset.bodyFont})") },
                    onClick = {
                        viewModel.applyDocumentTheme(preset)
                        showThemeMenu = false
                    }
                )
            }
        }
    }

    var showSpacingMenu by remember { mutableStateOf(false) }
    Box {
        RibbonActionButton(
            icon = Icons.Default.ViewAgenda,
            label = "Paragraph Spacing",
            onClick = { showSpacingMenu = true },
            testTag = "design_para_spacing_button"
        )
        DropdownMenu(expanded = showSpacingMenu, onDismissRequest = { showSpacingMenu = false }) {
            listOf(
                "Compact (1.0)" to 1.0f,
                "Tight (1.15)" to 1.15f,
                "Default (1.25)" to 1.25f,
                "Open (1.5)" to 1.5f,
                "Relaxed (1.75)" to 1.75f,
                "Double (2.0)" to 2.0f
            ).forEach { (label, spVal) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        viewModel.setDocumentParagraphSpacing(spVal)
                        showSpacingMenu = false
                    }
                )
            }
        }
    }

    RibbonDivider()

    var showWatermarkMenu by remember { mutableStateOf(false) }
    Box {
        RibbonActionButton(
            icon = Icons.Default.Bookmark,
            label = if (doc.watermark != null) "Watermark: ${doc.watermark}" else "Watermark",
            onClick = { showWatermarkMenu = true },
            testTag = "design_watermark_button"
        )
        DropdownMenu(expanded = showWatermarkMenu, onDismissRequest = { showWatermarkMenu = false }) {
            listOf(null, "DRAFT", "CONFIDENTIAL", "DO NOT COPY", "SAMPLE", "ASAP", "URGENT").forEach { w ->
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

    var showPageColorMenu by remember { mutableStateOf(false) }
    Box {
        RibbonActionButton(
            icon = Icons.Default.FormatColorFill,
            label = "Page Color",
            isSelected = doc.pageColorHex != null,
            onClick = { showPageColorMenu = true },
            testTag = "design_page_color_button"
        )
        DropdownMenu(expanded = showPageColorMenu, onDismissRequest = { showPageColorMenu = false }) {
            listOf(
                null to "Classic White Paper",
                "#FFFDF7" to "Warm Ivory / Cream",
                "#F1F3F5" to "Soft Studio Gray",
                "#EDF2FF" to "Pale Blueprint Blue",
                "#EBFBEE" to "Sage Mint Paper"
            ).forEach { (hex, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        viewModel.setPageColor(hex)
                        showPageColorMenu = false
                    }
                )
            }
        }
    }

    RibbonActionButton(
        icon = Icons.Default.BorderOuter,
        label = if (doc.pageBorder) "Page Border ✓" else "Page Borders",
        isSelected = doc.pageBorder,
        onClick = { viewModel.togglePageBorder() },
        testTag = "design_page_border_button"
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

    var showColumnsMenu by remember { mutableStateOf(false) }
    Box {
        RibbonActionButton(
            icon = Icons.Default.ViewColumn,
            label = "Columns: ${doc.columns}",
            onClick = { showColumnsMenu = true },
            testTag = "layout_columns_button"
        )
        DropdownMenu(expanded = showColumnsMenu, onDismissRequest = { showColumnsMenu = false }) {
            listOf(1 to "One Column", 2 to "Two Columns", 3 to "Three Columns").forEach { (c, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        viewModel.setPageColumns(c)
                        showColumnsMenu = false
                    }
                )
            }
        }
    }

    RibbonActionButton(
        icon = Icons.Default.FormatListNumbered,
        label = if (doc.showLineNumbers) "Line Numbers ✓" else "Line Numbers",
        isSelected = doc.showLineNumbers,
        onClick = { viewModel.toggleLineNumbers() },
        testTag = "layout_line_numbers_button"
    )
}

@Composable
private fun ReferencesRibbonTools(viewModel: WordCraftViewModel) {
    val doc by viewModel.document.collectAsState()

    RibbonActionButton(
        icon = Icons.Default.MenuBook,
        label = "Table of Contents",
        onClick = { viewModel.insertTableOfContents() },
        testTag = "refs_toc_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Add,
        label = "Insert Footnote",
        onClick = { viewModel.setShowReferencesDialog(true) },
        testTag = "refs_footnote_button"
    )

    RibbonDivider()

    RibbonActionButton(
        icon = Icons.Default.Bookmark,
        label = "Insert Citation (${doc.citationStyle})",
        onClick = { viewModel.setShowReferencesDialog(true) },
        testTag = "refs_citation_button"
    )

    RibbonActionButton(
        icon = Icons.Default.MenuBook,
        label = "Bibliography",
        onClick = { viewModel.insertBibliographyBlock() },
        testTag = "refs_bibliography_button"
    )
}

@Composable
private fun MailingsRibbonTools(viewModel: WordCraftViewModel) {
    RibbonActionButton(
        icon = Icons.Default.MailOutline,
        label = "Start Mail Merge Studio",
        onClick = { viewModel.setShowMailingsDialog(true) },
        testTag = "mailings_studio_button"
    )

    RibbonDivider()

    RibbonActionButton(
        icon = Icons.Default.Add,
        label = "Address Block",
        onClick = { viewModel.insertMergeField("AddressBlock") },
        testTag = "mailings_address_block_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Add,
        label = "Greeting Line",
        onClick = { viewModel.insertMergeField("GreetingLine") },
        testTag = "mailings_greeting_line_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Code,
        label = "«First_Name»",
        onClick = { viewModel.insertMergeField("First_Name") },
        testTag = "mailings_first_name_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Code,
        label = "«Company»",
        onClick = { viewModel.insertMergeField("Company") },
        testTag = "mailings_company_button"
    )
}

@Composable
private fun ZoteroRibbonTools(viewModel: WordCraftViewModel) {
    val items by viewModel.zoteroItems.collectAsState()
    val cslStyle by viewModel.zoteroCslStyle.collectAsState()
    var showStyleMenu by remember { mutableStateOf(false) }

    // 1. Add/Edit Citation (ui.zotero.addEditCitation)
    RibbonActionButton(
        icon = Icons.Default.Bookmark,
        label = "Add/Edit Citation",
        onClick = { viewModel.setShowZoteroDialog(true) },
        testTag = "zotero_add_edit_citation"
    )

    // 2. Add Note (ui.zotero.addNote)
    RibbonActionButton(
        icon = Icons.Default.Comment,
        label = "Add Note",
        onClick = {
            items.firstOrNull()?.let { viewModel.insertZoteroNote(it) }
                ?: viewModel.setShowZoteroDialog(true)
        },
        testTag = "zotero_add_note"
    )

    // 3. Move Past Citation (caret.pastCitation)
    RibbonActionButton(
        icon = Icons.Default.FormatIndentIncrease,
        label = "Move Past Citation",
        onClick = { viewModel.movePastZoteroCitation() },
        testTag = "zotero_move_past_citation"
    )

    RibbonDivider()

    // 4. Add/Edit Bibliography (ui.zotero.addEditBibliography)
    RibbonActionButton(
        icon = Icons.Default.MenuBook,
        label = "Add/Edit Bibliography",
        onClick = { viewModel.insertOrUpdateZoteroBibliography() },
        testTag = "zotero_add_edit_bibliography"
    )

    RibbonDivider()

    // 5. Refresh (ui.zotero.refresh)
    RibbonActionButton(
        icon = Icons.Default.TrackChanges,
        label = "Refresh",
        onClick = { viewModel.refreshZoteroCitations() },
        testTag = "zotero_refresh"
    )

    // 6. Document Preferences (ui.zotero.setDocPrefs)
    Box {
        RibbonActionButton(
            icon = Icons.Default.Menu,
            label = "Doc Prefs (${cslStyle.id.uppercase()})",
            onClick = { showStyleMenu = true },
            testTag = "zotero_doc_prefs"
        )
        DropdownMenu(
            expanded = showStyleMenu,
            onDismissRequest = { showStyleMenu = false }
        ) {
            com.example.export.CslStyle.entries.forEach { style ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = style.displayName,
                            fontWeight = if (cslStyle == style) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        viewModel.setZoteroCslStyle(style)
                        showStyleMenu = false
                    }
                )
            }
            Divider()
            DropdownMenuItem(
                text = { Text("Open Zotero Studio & DOI Search...", color = WordCraftPrimary, fontWeight = FontWeight.Bold) },
                onClick = {
                    showStyleMenu = false
                    viewModel.setShowZoteroDialog(true)
                }
            )
        }
    }

    // 7. Unlink Citations (ui.zotero.removeCodes)
    RibbonActionButton(
        icon = Icons.Default.FormatClear,
        label = "Unlink Citations",
        onClick = { viewModel.unlinkZoteroCitations() },
        testTag = "zotero_unlink_citations"
    )
}

@Composable
private fun ReviewRibbonTools(viewModel: WordCraftViewModel) {
    val doc by viewModel.document.collectAsState()
    val showNav by viewModel.showNavPane.collectAsState()
    val showComments by viewModel.showCommentsPane.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    RibbonActionButton(
        icon = Icons.Default.Translate,
        label = "Google Translate",
        onClick = { viewModel.setShowTranslateDialog(true) },
        testTag = "review_translate_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Code,
        label = "Unicode Inspector",
        onClick = { viewModel.setShowUnicodeDialog(true) },
        testTag = "review_unicode_button"
    )

    RibbonActionButton(
        icon = Icons.Default.VerifiedUser,
        label = "Check Accessibility",
        onClick = { viewModel.setShowAccessibilityDialog(true) },
        testTag = "review_a11y_button"
    )

    RibbonDivider()

    RibbonActionButton(
        icon = Icons.Default.Info,
        label = "Word Count",
        onClick = { viewModel.setShowStatsDialog(true) },
        testTag = "review_stats_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Menu,
        label = "Outline Pane",
        isSelected = showNav,
        onClick = { viewModel.toggleNavPane() },
        testTag = "review_outline_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Comment,
        label = "Comments",
        isSelected = showComments,
        onClick = { viewModel.toggleCommentsPane() },
        testTag = "review_comments_button"
    )

    RibbonActionButton(
        icon = Icons.Default.TrackChanges,
        label = if (doc.trackChanges) "Track Changes ✓" else "Track Changes",
        isSelected = doc.trackChanges,
        onClick = { viewModel.toggleTrackChanges() },
        testTag = "review_track_changes_button"
    )

    RibbonActionButton(
        icon = if (doc.readOnlyLocked) Icons.Default.Lock else Icons.Default.LockOpen,
        label = if (doc.readOnlyLocked) "Locked (Read-Only)" else "Restrict Editing",
        isSelected = doc.readOnlyLocked,
        onClick = { viewModel.toggleReadOnlyLock() },
        testTag = "review_restrict_editing_button"
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
    val doc by viewModel.document.collectAsState()
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
        label = "Web Layout",
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
        icon = Icons.Default.Straighten,
        label = if (doc.showRuler) "Ruler ✓" else "Ruler",
        isSelected = doc.showRuler,
        onClick = { viewModel.toggleRuler() },
        testTag = "view_ruler_button"
    )

    RibbonActionButton(
        icon = Icons.Default.GridOn,
        label = if (doc.showGridlines) "Gridlines ✓" else "Gridlines",
        isSelected = doc.showGridlines,
        onClick = { viewModel.toggleGridlines() },
        testTag = "view_gridlines_button"
    )

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
private fun DeveloperRibbonTools(viewModel: WordCraftViewModel) {
    RibbonActionButton(
        icon = Icons.Default.TextFields,
        label = "Rich Text Control",
        onClick = { viewModel.insertContentControl("TEXT") },
        testTag = "dev_text_control_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Checklist,
        label = "Check Box Control",
        onClick = { viewModel.insertContentControl("CHECKBOX") },
        testTag = "dev_checkbox_control_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Schedule,
        label = "Date Picker Control",
        onClick = { viewModel.insertContentControl("DATE") },
        testTag = "dev_date_control_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Menu,
        label = "Drop-Down Control",
        onClick = { viewModel.insertContentControl("DROPDOWN") },
        testTag = "dev_dropdown_control_button"
    )

    RibbonDivider()

    RibbonActionButton(
        icon = Icons.Default.Code,
        label = "UTF-8 & Codepoint Inspector",
        onClick = { viewModel.setShowUnicodeDialog(true) },
        testTag = "dev_unicode_inspector_button"
    )
}

@Composable
private fun HelpRibbonTools(viewModel: WordCraftViewModel) {
    RibbonActionButton(
        icon = Icons.Default.HelpOutline,
        label = "Shortcuts & v0.7.0 Guide",
        onClick = { viewModel.setShowAccessibilityDialog(true) },
        testTag = "help_guide_button"
    )

    RibbonActionButton(
        icon = Icons.Default.VerifiedUser,
        label = "Accessibility Audit",
        onClick = { viewModel.setShowAccessibilityDialog(true) },
        testTag = "help_a11y_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Info,
        label = "Document Statistics",
        onClick = { viewModel.setShowStatsDialog(true) },
        testTag = "help_stats_button"
    )
}

@Composable
private fun TableRibbonTools(
    viewModel: WordCraftViewModel,
    activeBlock: com.example.model.DocBlock?
) {
    if (activeBlock == null || activeBlock.type != BlockType.TABLE) {
        Text("Select a table in the document to edit rows, styles, and formulas.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

    RibbonDivider()

    var showTableStyleMenu by remember { mutableStateOf(false) }
    Box {
        RibbonActionButton(
            icon = Icons.Default.Palette,
            label = "Table Style",
            onClick = { showTableStyleMenu = true },
            testTag = "table_style_button"
        )
        DropdownMenu(expanded = showTableStyleMenu, onDismissRequest = { showTableStyleMenu = false }) {
            TableStyle.values().forEach { st ->
                DropdownMenuItem(
                    text = { Text(st.displayName) },
                    onClick = {
                        viewModel.updateTableStyle(activeBlock.id, st)
                        showTableStyleMenu = false
                    }
                )
            }
        }
    }

    RibbonActionButton(
        icon = Icons.Default.Functions,
        label = "fx =SUM Row",
        onClick = { viewModel.appendTableFormulaSummaryRow(activeBlock.id, "SUM") },
        testTag = "table_formula_sum_button"
    )

    RibbonActionButton(
        icon = Icons.Default.Functions,
        label = "fx =AVG Row",
        onClick = { viewModel.appendTableFormulaSummaryRow(activeBlock.id, "AVG") },
        testTag = "table_formula_avg_button"
    )

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

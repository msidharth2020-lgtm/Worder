package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BorderColor
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignJustify
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatClear
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatColorText
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextDecrease
import androidx.compose.material.icons.filled.TextIncrease
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import com.example.model.BlockType
import com.example.model.TextAlignment
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.export.CslStyle
import com.example.export.TranslationService
import com.example.export.ZoteroItem
import com.example.ui.theme.WordCraftFonts
import com.example.ui.theme.WordCraftPrimary
import com.example.viewmodel.WordCraftViewModel

/**
 * Floating Selection & Word Hover Mini-Toolbar that appears whenever the user selects text
 * or hovers/taps over words in the active block.
 * Provides instant Highlight, Underline, Bold, Italic, Font/Size, Definition & Thesaurus,
 * Google Translate, Read Aloud, Case Transform, and Zotero Citation actions.
 */
/**
 * Microsoft Word Two-Row Floating Mini-Toolbar matching the video:
 * Displays when text is selected or a word is focused.
 * Row 1: Font Family Dropdown, Font Size Dropdown, Grow/Shrink Font, Format Painter, Clear Formatting, Styles
 * Row 2: Bold, Italic, Underline, Strikethrough, Highlight Marker (palette), Text Color, Alignments, Lists,
 *        Google Translate, Definition & Thesaurus, Zotero Cite, Read Aloud, and Word Context Menu (Cut/Copy/Paste).
 */
@Composable
fun WordCraftSelectionHoverToolbar(
    viewModel: WordCraftViewModel,
    modifier: Modifier = Modifier
) {
    val selectedWord by viewModel.selectedOrHoveredWord.collectAsState()
    val expandedTab by viewModel.hoverToolbarExpandedTab.collectAsState()
    val definition by viewModel.wordDefinition.collectAsState()
    val isLoadingDef by viewModel.isLoadingDefinition.collectAsState()
    val inlineTranslation by viewModel.inlineTranslationResult.collectAsState()
    val isInlineTranslating by viewModel.isInlineTranslating.collectAsState()
    val targetLangCode by viewModel.inlineTargetLang.collectAsState()
    val isDark by viewModel.isDarkCanvas.collectAsState()
    val doc by viewModel.document.collectAsState()
    val activeBlockId by viewModel.activeBlockId.collectAsState()
    val activeBlock = remember(doc, activeBlockId) {
        doc.blocks.find { it.id == activeBlockId } ?: doc.blocks.firstOrNull()
    }

    val context = LocalContext.current

    if (selectedWord.isBlank()) return

    val currentFontName = activeBlock?.fontFamily ?: "Aptos"
    val currentFontSize = activeBlock?.effectiveFontSizeSp ?: 11

    var showFontMenu by remember { mutableStateOf(false) }
    var showFontSizeMenu by remember { mutableStateOf(false) }
    var showStyleMenu by remember { mutableStateOf(false) }
    var showContextMenu by remember { mutableStateOf(false) }

    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFCBD5E1)
    val textPrimary = if (isDark) Color.White else Color(0xFF1E293B)
    val textMuted = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val chipBg = if (isDark) Color(0xFF334155) else Color(0xFFF1F5F9)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .shadow(6.dp, RoundedCornerShape(8.dp))
            .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
            .testTag("selection_hover_toolbar"),
        color = cardBg,
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(6.dp)) {
            // Header Bar: Active Selected Word + Quick Actions (Copy, Paste, Close)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = WordCraftPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "“${selectedWord.take(24)}${if (selectedWord.length > 24) "…" else ""}”",
                            color = WordCraftPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Copy
                    MiniIconToolButton(
                        icon = Icons.Default.ContentCopy,
                        tooltip = "Copy Selection",
                        onClick = { viewModel.copySelectedText(context) }
                    )
                    // Cut
                    MiniIconToolButton(
                        icon = Icons.Default.ContentCut,
                        tooltip = "Cut Selection",
                        onClick = { viewModel.cutSelectedText(context) }
                    )
                    // Context Menu / More
                    Box {
                        MiniIconToolButton(
                            icon = Icons.Default.MoreVert,
                            tooltip = "More Actions",
                            onClick = { showContextMenu = true }
                        )
                        DropdownMenu(
                            expanded = showContextMenu,
                            onDismissRequest = { showContextMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Paste (Merge Formatting)", fontSize = 13.sp) },
                                onClick = {
                                    viewModel.pasteFromClipboard(context, "MERGE")
                                    showContextMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Keep Text Only", fontSize = 13.sp) },
                                onClick = {
                                    viewModel.pasteFromClipboard(context, "TEXT_ONLY")
                                    showContextMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Paste Special...", fontSize = 13.sp) },
                                onClick = {
                                    viewModel.pasteFromClipboard(context, "SPECIAL")
                                    showContextMenu = false
                                }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Google Translate", fontSize = 13.sp) },
                                onClick = {
                                    viewModel.setHoverToolbarExpandedTab("TRANSLATE")
                                    showContextMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Define & Synonyms", fontSize = 13.sp) },
                                onClick = {
                                    viewModel.setHoverToolbarExpandedTab("DEFINE")
                                    showContextMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Add Citation (Zotero)", fontSize = 13.sp) },
                                onClick = {
                                    viewModel.setShowZoteroDialog(true)
                                    showContextMenu = false
                                }
                            )
                        }
                    }
                    // Close toolbar
                    MiniIconToolButton(
                        icon = Icons.Default.Close,
                        tooltip = "Close Mini-Toolbar",
                        onClick = { viewModel.clearSelectionOrHover() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ==========================================
            // ROW 1: Font Family, Size, Steppers, Styles
            // ==========================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1. Font Family Dropdown
                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { showFontMenu = true }
                            .testTag("hover_font_family_button"),
                        color = chipBg,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentFontName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Font Dropdown",
                                tint = textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showFontMenu,
                        onDismissRequest = { showFontMenu = false }
                    ) {
                        WordCraftFonts.availableFonts.forEach { fontOpt ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = fontOpt.displayName,
                                            fontFamily = fontOpt.fontFamily,
                                            fontSize = 14.sp,
                                            fontWeight = if (fontOpt.id == currentFontName) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (fontOpt.id == currentFontName) {
                                            Text("✓", color = WordCraftPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp))
                                        }
                                    }
                                },
                                onClick = {
                                    viewModel.updateBlockFontFamily(fontOpt.id)
                                    showFontMenu = false
                                }
                            )
                        }
                    }
                }

                // 2. Font Size Dropdown
                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { showFontSizeMenu = true }
                            .testTag("hover_font_size_button"),
                        color = chipBg,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$currentFontSize",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Font Size Dropdown",
                                tint = textMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showFontSizeMenu,
                        onDismissRequest = { showFontSizeMenu = false }
                    ) {
                        WordCraftFonts.presetFontSizes.forEach { sz ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "$sz",
                                        fontWeight = if (sz == currentFontSize) FontWeight.Bold else FontWeight.Normal,
                                        color = if (sz == currentFontSize) WordCraftPrimary else Color.Unspecified
                                    )
                                },
                                onClick = {
                                    viewModel.updateBlockFontSize(sz)
                                    showFontSizeMenu = false
                                }
                            )
                        }
                    }
                }

                // Grow Font (A^)
                MiniIconToolButton(
                    icon = Icons.Default.TextIncrease,
                    tooltip = "Grow Font (A^)",
                    onClick = { viewModel.stepBlockFontSize(2) }
                )

                // Shrink Font (Av)
                MiniIconToolButton(
                    icon = Icons.Default.TextDecrease,
                    tooltip = "Shrink Font (Av)",
                    onClick = { viewModel.stepBlockFontSize(-2) }
                )

                // Format Painter
                MiniIconToolButton(
                    icon = Icons.Default.FormatPaint,
                    tooltip = "Format Painter",
                    onClick = { viewModel.copySelectedText(context) }
                )

                // Clear Formatting
                MiniIconToolButton(
                    icon = Icons.Default.FormatClear,
                    tooltip = "Clear All Formatting",
                    onClick = { viewModel.clearBlockFormatting() }
                )

                // Styles Gallery Dropdown
                Box {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { showStyleMenu = true },
                        color = chipBg,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = activeBlock?.type?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.titlecase() } ?: "Normal",
                                fontSize = 11.sp,
                                color = textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                    DropdownMenu(
                        expanded = showStyleMenu,
                        onDismissRequest = { showStyleMenu = false }
                    ) {
                        listOf(
                            BlockType.PARAGRAPH to "Normal Text",
                            BlockType.HEADING_1 to "Heading 1",
                            BlockType.HEADING_2 to "Heading 2",
                            BlockType.HEADING_3 to "Heading 3",
                            BlockType.TITLE to "Title",
                            BlockType.QUOTE to "Quote Callout"
                        ).forEach { (bType, label) ->
                            DropdownMenuItem(
                                text = { Text(label, fontSize = 13.sp) },
                                onClick = {
                                    activeBlock?.let { viewModel.updateBlockType(it.id, bType) }
                                    showStyleMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ============================================================
            // ROW 2: B, I, U, Strike, Highlight, Text Color, Align, Translate
            // ============================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Bold (B)
                MiniToggleToolButton(
                    label = "B",
                    isSelected = activeBlock?.bold == true,
                    fontWeight = FontWeight.Black,
                    tooltip = "Bold (Ctrl+B)",
                    onClick = { viewModel.toggleSpanBold() }
                )

                // Italic (I)
                MiniToggleToolButton(
                    label = "I",
                    isSelected = activeBlock?.italic == true,
                    fontStyle = FontStyle.Italic,
                    tooltip = "Italic (Ctrl+I)",
                    onClick = { viewModel.toggleSpanItalic() }
                )

                // Underline (U)
                MiniToggleToolButton(
                    label = "U",
                    isSelected = activeBlock?.underline == true,
                    tooltip = "Underline (Ctrl+U)",
                    underline = true,
                    onClick = { viewModel.toggleSpanUnderline() }
                )

                // Strikethrough (ab)
                MiniToggleToolButton(
                    label = "ab",
                    isSelected = activeBlock?.strikethrough == true,
                    tooltip = "Strikethrough",
                    onClick = { viewModel.toggleSpanStrikethrough() }
                )

                // Highlight Color Marker Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            val next = if (expandedTab == "HIGHLIGHT") null else "HIGHLIGHT"
                            viewModel.setHoverToolbarExpandedTab(next)
                        }
                        .testTag("hover_action_highlight"),
                    color = if (expandedTab == "HIGHLIGHT") WordCraftPrimary.copy(alpha = 0.18f) else chipBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BorderColor,
                            contentDescription = "Highlight Color",
                            tint = if (expandedTab == "HIGHLIGHT") WordCraftPrimary else textPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(3.dp)
                                .background(Color(0xFFE879F9), RoundedCornerShape(1.dp))
                        )
                    }
                }

                // Text Color Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            val next = if (expandedTab == "TEXT_COLOR") null else "TEXT_COLOR"
                            viewModel.setHoverToolbarExpandedTab(next)
                        }
                        .testTag("hover_action_text_color"),
                    color = if (expandedTab == "TEXT_COLOR") WordCraftPrimary.copy(alpha = 0.18f) else chipBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("A", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textPrimary)
                        Box(
                            modifier = Modifier
                                .width(14.dp)
                                .height(3.dp)
                                .background(Color(0xFFDC2626), RoundedCornerShape(1.dp))
                        )
                    }
                }

                // Text Alignments (Left, Center, Right, Justify)
                MiniIconToolButton(
                    icon = Icons.Default.FormatAlignLeft,
                    tooltip = "Align Left",
                    isSelected = activeBlock?.alignment == TextAlignment.LEFT,
                    onClick = { viewModel.updateBlockAlignment(TextAlignment.LEFT) }
                )
                MiniIconToolButton(
                    icon = Icons.Default.FormatAlignCenter,
                    tooltip = "Center",
                    isSelected = activeBlock?.alignment == TextAlignment.CENTER,
                    onClick = { viewModel.updateBlockAlignment(TextAlignment.CENTER) }
                )
                MiniIconToolButton(
                    icon = Icons.Default.FormatAlignRight,
                    tooltip = "Align Right",
                    isSelected = activeBlock?.alignment == TextAlignment.RIGHT,
                    onClick = { viewModel.updateBlockAlignment(TextAlignment.RIGHT) }
                )

                // Bullet List
                MiniIconToolButton(
                    icon = Icons.Default.FormatListBulleted,
                    tooltip = "Bullets",
                    isSelected = activeBlock?.type == BlockType.BULLET_LIST,
                    onClick = { activeBlock?.let { viewModel.updateBlockType(it.id, BlockType.BULLET_LIST) } }
                )

                // Numbered List
                MiniIconToolButton(
                    icon = Icons.Default.FormatListNumbered,
                    tooltip = "Numbering",
                    isSelected = activeBlock?.type == BlockType.NUMBERED_LIST,
                    onClick = { activeBlock?.let { viewModel.updateBlockType(it.id, BlockType.NUMBERED_LIST) } }
                )

                // Google Translate Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            val next = if (expandedTab == "TRANSLATE") null else "TRANSLATE"
                            viewModel.setHoverToolbarExpandedTab(next)
                            if (next == "TRANSLATE") viewModel.translateActiveSelection()
                        }
                        .testTag("hover_action_translate"),
                    color = if (expandedTab == "TRANSLATE") WordCraftPrimary.copy(alpha = 0.18f) else chipBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Google Translate",
                            tint = if (expandedTab == "TRANSLATE") WordCraftPrimary else Color(0xFF2563EB),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Translate", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                    }
                }

                // Definition / Thesaurus / Unicode Button
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable {
                            val next = if (expandedTab == "DEFINE") null else "DEFINE"
                            viewModel.setHoverToolbarExpandedTab(next)
                            if (next == "DEFINE") viewModel.lookupActiveSelectionDefinition()
                        }
                        .testTag("hover_action_define"),
                    color = if (expandedTab == "DEFINE") WordCraftPrimary.copy(alpha = 0.18f) else chipBg,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Word Definition",
                            tint = if (expandedTab == "DEFINE") WordCraftPrimary else Color(0xFF0D9488),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Define", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = textPrimary)
                    }
                }

                // Zotero Cite
                MiniIconToolButton(
                    icon = Icons.Default.BookmarkAdd,
                    tooltip = "Add Zotero Citation",
                    onClick = { viewModel.setShowZoteroDialog(true) }
                )

                // Read Aloud / TTS
                MiniIconToolButton(
                    icon = Icons.Default.VolumeUp,
                    tooltip = "Read Aloud",
                    onClick = { viewModel.speakSelection() }
                )
            }

            // ==============================================================
            // EXPANDABLE SUB-POPOVERS (Highlight Palette, Colors, Translate, Dict)
            // ==============================================================
            AnimatedVisibility(visible = expandedTab != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    color = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                ) {
                    when (expandedTab) {
                        "HIGHLIGHT" -> {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Text Highlight Color (Word Palette)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Close",
                                        fontSize = 11.sp,
                                        color = WordCraftPrimary,
                                        modifier = Modifier.clickable { viewModel.setHoverToolbarExpandedTab(null) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                val highlightPalette = listOf(
                                    "Yellow" to "#FEF08A",
                                    "Bright Green" to "#86EFAC",
                                    "Cyan" to "#67E8F9",
                                    "Magenta" to "#E879F9",
                                    "Orange" to "#FDBA74",
                                    "Red" to "#FCA5A5",
                                    "Blue" to "#93C5FD",
                                    "Lavender" to "#E9D5FF"
                                )
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    highlightPalette.forEach { (name, hex) ->
                                        val swatchColor = Color(android.graphics.Color.parseColor(hex))
                                        Box(
                                            modifier = Modifier
                                                .size(26.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(swatchColor)
                                                .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(4.dp))
                                                .clickable { viewModel.applySpanHighlight(hex) }
                                                .testTag("hover_hl_$name")
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = chipBg,
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.clickable { viewModel.applySpanHighlight(null) }
                                    ) {
                                        Text(
                                            text = "No Color",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        "TEXT_COLOR" -> {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Font Color",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                    Text(
                                        text = "Close",
                                        fontSize = 11.sp,
                                        color = WordCraftPrimary,
                                        modifier = Modifier.clickable { viewModel.setHoverToolbarExpandedTab(null) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                val textColors = listOf(
                                    "Black" to "#000000",
                                    "Dark Red" to "#991B1B",
                                    "Red" to "#DC2626",
                                    "Orange" to "#EA580C",
                                    "Green" to "#16A34A",
                                    "Blue" to "#2563EB",
                                    "Purple" to "#9333EA",
                                    "Gray" to "#4B5563"
                                )
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    textColors.forEach { (name, hex) ->
                                        val swatchColor = Color(android.graphics.Color.parseColor(hex))
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(swatchColor)
                                                .border(1.dp, Color(0xFFCBD5E1), CircleShape)
                                                .clickable { viewModel.applySpanTextColor(hex) }
                                                .testTag("hover_tc_$name")
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = chipBg,
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.clickable { viewModel.applySpanTextColor(null) }
                                    ) {
                                        Text(
                                            text = "Automatic",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = textPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        "DEFINE" -> {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Word Definition, Phonetics & Unicode",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WordCraftPrimary
                                    )
                                    Text(
                                        text = "Close",
                                        fontSize = 11.sp,
                                        color = textMuted,
                                        modifier = Modifier.clickable { viewModel.setHoverToolbarExpandedTab(null) }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                if (isLoadingDef) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = WordCraftPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Looking up \"$selectedWord\"...", fontSize = 12.sp, color = textPrimary)
                                    }
                                } else if (definition != null) {
                                    val def = definition!!
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = def.word,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = textPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = def.phonetic,
                                                fontSize = 12.sp,
                                                fontStyle = FontStyle.Italic,
                                                color = textMuted
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "• ${def.syllableEstimate} syl • ${def.unicodeSummary}",
                                                fontSize = 10.sp,
                                                color = textMuted
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.speakSelection() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.VolumeUp, contentDescription = "Pronounce", tint = WordCraftPrimary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    def.meanings.take(2).forEach { meaning ->
                                        Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    color = WordCraftPrimary.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        text = meaning.partOfSpeech,
                                                        color = WordCraftPrimary,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = meaning.definition,
                                                    fontSize = 12.sp,
                                                    color = textPrimary
                                                )
                                            }
                                            if (meaning.synonyms.isNotEmpty()) {
                                                Row(
                                                    modifier = Modifier
                                                        .padding(top = 4.dp)
                                                        .horizontalScroll(rememberScrollState()),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text("Synonyms:", fontSize = 10.sp, color = textMuted)
                                                    meaning.synonyms.take(4).forEach { syn ->
                                                        Surface(
                                                            color = chipBg,
                                                            shape = RoundedCornerShape(4.dp),
                                                            modifier = Modifier.clickable {
                                                                viewModel.replaceSelectionWithText(syn)
                                                            }
                                                        ) {
                                                            Text(
                                                                text = syn,
                                                                color = WordCraftPrimary,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.SemiBold,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "TRANSLATE" -> {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Google Translate Selection",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2563EB)
                                    )
                                    Text(
                                        text = "Full Web App",
                                        fontSize = 11.sp,
                                        color = WordCraftPrimary,
                                        modifier = Modifier.clickable {
                                            TranslationService.openInGoogleTranslate(context, selectedWord, targetLangCode)
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                // Quick Target Language Chips
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    TranslationService.supportedLanguages.filter { it.code != "auto" }.take(10).forEach { lang ->
                                        val isSelected = lang.code == targetLangCode
                                        Surface(
                                            color = if (isSelected) WordCraftPrimary else chipBg,
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.clickable { viewModel.setInlineTargetLang(lang.code) }
                                        ) {
                                            Text(
                                                text = lang.name,
                                                color = if (isSelected) Color.White else textPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                if (isInlineTranslating) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = WordCraftPrimary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Translating...", fontSize = 12.sp, color = textPrimary)
                                    }
                                } else if (!inlineTranslation.isNullOrBlank()) {
                                    Text(
                                        text = inlineTranslation!!,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = { viewModel.replaceSelectionWithText(inlineTranslation!!) },
                                            colors = ButtonDefaults.buttonColors(containerColor = WordCraftPrimary),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Replace", fontSize = 11.sp)
                                        }
                                        OutlinedButton(
                                            onClick = { viewModel.appendTranslationToSelection(inlineTranslation!!) },
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Insert After", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniIconToolButton(
    icon: ImageVector,
    tooltip: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) WordCraftPrimary.copy(alpha = 0.2f) else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            tint = if (isSelected) WordCraftPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
private fun MiniToggleToolButton(
    label: String,
    isSelected: Boolean,
    tooltip: String,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    underline: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) WordCraftPrimary.copy(alpha = 0.2f) else Color.Transparent,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            color = if (isSelected) WordCraftPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun HoverMiniActionChip(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = if (isSelected) WordCraftPrimary else Color(0xFF334155),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Full Zotero Citation, Note, Bibliography & CSL Preferences Studio Dialog
 * matching `wordcraft-web-0.7.0` (`zotero.rs` & `ribbon.rs` `fn zotero`).
 */
@Composable
fun WordCraftZoteroDialog(
    viewModel: WordCraftViewModel,
    onDismiss: () -> Unit
) {
    val items by viewModel.zoteroItems.collectAsState()
    val cslStyle by viewModel.zoteroCslStyle.collectAsState()
    val storeRefs by viewModel.zoteroStoreRefsInDoc.collectAsState()
    val searchResults by viewModel.zoteroSearchResults.collectAsState()
    val isSearching by viewModel.isSearchingZotero.collectAsState()
    val statusMsg by viewModel.zoteroStatusMessage.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var crossRefQuery by remember { mutableStateOf("") }
    var locatorPage by remember { mutableStateOf("") }

    // Custom Zotero item fields
    var newAuthor by remember { mutableStateOf("") }
    var newTitle by remember { mutableStateOf("") }
    var newYear by remember { mutableStateOf("2025") }
    var newPublication by remember { mutableStateOf("") }
    var newDoi by remember { mutableStateOf("") }
    var newNote by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoStories,
                            contentDescription = "Zotero",
                            tint = Color(0xFFC92A2A),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Zotero Reference & Citation Manager",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Active CSL Style: ${cslStyle.displayName} • ${items.size} items in library",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                if (!statusMsg.isNullOrBlank()) {
                    Surface(
                        color = WordCraftPrimary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text(
                            text = statusMsg!!,
                            color = WordCraftPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Add/Edit Citation") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Bibliography & Refresh") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Doc Prefs & Add Item") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                when (selectedTab) {
                    0 -> {
                        // Add/Edit Citation & CrossRef Search
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = crossRefQuery,
                                    onValueChange = { crossRefQuery = it },
                                    label = { Text("Search Zotero / CrossRef by DOI, Title, or Author") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                Button(
                                    onClick = { viewModel.searchZoteroCrossRef(crossRefQuery) },
                                    colors = ButtonDefaults.buttonColors(containerColor = WordCraftPrimary)
                                ) {
                                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Search")
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = locatorPage,
                                    onValueChange = { locatorPage = it },
                                    label = { Text("Optional Page / Locator (e.g. 42-45)") },
                                    singleLine = true,
                                    modifier = Modifier.width(240.dp)
                                )
                                OutlinedButton(onClick = { viewModel.movePastZoteroCitation() }) {
                                    Text("Move Past Citation", fontSize = 12.sp)
                                }
                            }

                            if (isSearching) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Querying CrossRef DOI Registry...", fontSize = 12.sp)
                                }
                            }

                            LazyColumn(
                                modifier = Modifier.weight(1f).padding(top = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (searchResults.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "CrossRef Scholarly Search Results (Tap + to Save & Cite):",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WordCraftPrimary
                                        )
                                    }
                                    itemsIndexed(searchResults) { idx, item ->
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F5FF)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(item.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                    Text("${item.author} (${item.year}) • ${item.publication}", fontSize = 11.sp)
                                                    if (item.doi.isNotBlank()) {
                                                        Text("DOI: ${item.doi}", fontSize = 10.sp, color = Color(0xFF495057))
                                                    }
                                                }
                                                Button(
                                                    onClick = {
                                                        viewModel.addZoteroItem(item)
                                                        viewModel.insertZoteroCitation(item, locatorPage)
                                                        onDismiss()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = WordCraftPrimary)
                                                ) {
                                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("Add & Cite", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }

                                item {
                                    Text(
                                        text = "My Zotero Library (${items.size} items):",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                itemsIndexed(items) { index, item ->
                                    val previewCite = item.formatInTextCitation(cslStyle, index + 1, locatorPage)
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = previewCite,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WordCraftPrimary
                                                )
                                                Text(
                                                    text = item.itemType,
                                                    fontSize = 10.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(item.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                            Text(
                                                text = "${item.author} (${item.year}) • ${item.publication}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = {
                                                        viewModel.insertZoteroCitation(item, locatorPage)
                                                        onDismiss()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = WordCraftPrimary),
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text("Insert Citation", fontSize = 11.sp)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        viewModel.insertZoteroNote(item)
                                                        onDismiss()
                                                    },
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text("Add Note", fontSize = 11.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Add/Edit Bibliography, Refresh, Unlink Citations
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.insertOrUpdateZoteroBibliography()
                                        onDismiss()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WordCraftPrimary)
                                ) {
                                    Text("Add/Edit Bibliography")
                                }
                                OutlinedButton(onClick = { viewModel.refreshZoteroCitations() }) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Refresh")
                                }
                                OutlinedButton(onClick = { viewModel.unlinkZoteroCitations() }) {
                                    Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Unlink Citations")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Formatted Bibliography Preview (${cslStyle.displayName}):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8F9FA), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                itemsIndexed(items) { idx, item ->
                                    Text(
                                        text = item.formatBibliographyEntry(cslStyle, idx + 1),
                                        fontSize = 13.sp,
                                        color = Color(0xFF212529)
                                    )
                                    Divider(color = Color(0xFFDEE2E6))
                                }
                            }
                        }
                    }

                    2 -> {
                        // Zotero Document Preferences & Custom Item Entry
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Text("Zotero Document Preferences (CSL Citation Style)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    CslStyle.entries.forEach { style ->
                                        FilterChip(
                                            selected = cslStyle == style,
                                            onClick = { viewModel.setZoteroCslStyle(style) },
                                            label = { Text(style.displayName) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }

                            item {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = storeRefs,
                                        onCheckedChange = { viewModel.toggleZoteroStoreRefs() }
                                    )
                                    Text("Store Zotero reference metadata inside document (.docx)", fontSize = 13.sp)
                                }
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                            }

                            item {
                                Text("Add New Source to Zotero Library", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                OutlinedTextField(
                                    value = newAuthor,
                                    onValueChange = { newAuthor = it },
                                    label = { Text("Author (e.g. Turing, Alan M.)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = newTitle,
                                    onValueChange = { newTitle = it },
                                    label = { Text("Title of Work") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = newYear,
                                        onValueChange = { newYear = it },
                                        label = { Text("Year") },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = newDoi,
                                        onValueChange = { newDoi = it },
                                        label = { Text("DOI (Optional)") },
                                        singleLine = true,
                                        modifier = Modifier.weight(2f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = newPublication,
                                    onValueChange = { newPublication = it },
                                    label = { Text("Journal / Publisher") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = newNote,
                                    onValueChange = { newNote = it },
                                    label = { Text("Research Note (for Zotero Add Note)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = {
                                        if (newAuthor.isNotBlank() && newTitle.isNotBlank()) {
                                            val key = newAuthor.substringBefore(",").replace("[^A-Za-z]".toRegex(), "") + newYear
                                            viewModel.addZoteroItem(
                                                ZoteroItem(
                                                    citeKey = key,
                                                    author = newAuthor.trim(),
                                                    title = newTitle.trim(),
                                                    year = newYear.trim().ifBlank { "2025" },
                                                    publication = newPublication.trim().ifBlank { "Academic Press" },
                                                    doi = newDoi.trim(),
                                                    note = newNote.trim()
                                                )
                                            )
                                            newAuthor = ""
                                            newTitle = ""
                                            newPublication = ""
                                            newDoi = ""
                                            newNote = ""
                                            selectedTab = 0
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = WordCraftPrimary)
                                ) {
                                    Text("Save Item to Zotero Library")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

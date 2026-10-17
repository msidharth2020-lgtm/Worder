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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.export.TranslationService
import com.example.model.UnicodeCatalog
import com.example.ui.theme.WordCraftPrimary
import com.example.viewmodel.WordCraftViewModel

@Composable
fun WordCraftUnicodeDialog(
    viewModel: WordCraftViewModel,
    onDismiss: () -> Unit
) {
    val doc by viewModel.document.collectAsState()
    val activeBlockId by viewModel.activeBlockId.collectAsState()
    val activeBlock = remember(doc, activeBlockId) {
        doc.blocks.find { it.id == activeBlockId } ?: doc.blocks.firstOrNull()
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var hexInput by remember { mutableStateOf("03BB") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.86f),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WordCraftPrimary)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Unicode",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Unicode & Symbol Studio",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Insert symbols, convert U+XXXX codepoints (Alt+X), and inspect UTF-8",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = WordCraftPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Symbol Map", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("U+Hex / Alt+X", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("UTF-8 Inspector", fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    when (selectedTab) {
                        0 -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search symbol name or U+XXXX (e.g. lambda, infinity, 221E)...", fontSize = 13.sp) },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("unicode_search_input")
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    UnicodeCatalog.categories.forEach { cat ->
                                        FilterChip(
                                            selected = selectedCategory == cat,
                                            onClick = { selectedCategory = cat },
                                            label = { Text(cat, fontSize = 12.sp) }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val filtered = remember(selectedCategory, searchQuery) {
                                    UnicodeCatalog.symbols.filter { sym ->
                                        val matchCat = selectedCategory == "All" || sym.category == selectedCategory
                                        val matchQuery = searchQuery.isBlank() ||
                                            sym.name.contains(searchQuery, ignoreCase = true) ||
                                            sym.codePointHex.contains(searchQuery, ignoreCase = true) ||
                                            sym.symbol.contains(searchQuery, ignoreCase = true)
                                        matchCat && matchQuery
                                    }
                                }

                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(filtered) { sym ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    viewModel.insertUnicodeSymbol(sym.symbol)
                                                    onDismiss()
                                                }
                                                .testTag("unicode_item_${sym.codePointHex}"),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Surface(
                                                        modifier = Modifier.size(40.dp),
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = WordCraftPrimary.copy(alpha = 0.12f)
                                                    ) {
                                                        Box(contentAlignment = Alignment.Center) {
                                                            Text(
                                                                text = sym.symbol,
                                                                fontSize = 20.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = WordCraftPrimary
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(
                                                            text = sym.name,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.SemiBold
                                                        )
                                                        Text(
                                                            text = "${sym.codePointHex} • ${sym.category}",
                                                            fontSize = 11.sp,
                                                            fontFamily = FontFamily.Monospace,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                Button(
                                                    onClick = {
                                                        viewModel.insertUnicodeSymbol(sym.symbol)
                                                        onDismiss()
                                                    }
                                                ) {
                                                    Text("Insert", fontSize = 12.sp)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        1 -> {
                            val decoded = remember(hexInput) { UnicodeCatalog.decodeHexCodePoint(hexInput) }

                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Direct Unicode Hex Codepoint (U+XXXX)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Enter any Unicode hexadecimal codepoint (e.g., 03BB for λ, 221E for ∞, 20AC for €, 1F680 for 🚀):",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = hexInput,
                                    onValueChange = { hexInput = it },
                                    label = { Text("Hex Codepoint (U+XXXX)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("unicode_hex_input")
                                )

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if (decoded != null) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    modifier = Modifier.size(56.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = WordCraftPrimary.copy(alpha = 0.15f)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Text(
                                                            text = decoded.first,
                                                            fontSize = 28.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = WordCraftPrimary
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(16.dp))
                                                Column {
                                                    Text(
                                                        text = decoded.second,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    Text(
                                                        text = "Character: \"${decoded.first}\"",
                                                        fontSize = 12.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            Button(
                                                onClick = {
                                                    viewModel.insertUnicodeSymbol(decoded.first)
                                                    onDismiss()
                                                },
                                                modifier = Modifier.testTag("insert_hex_unicode_button")
                                            ) {
                                                Text("Insert")
                                            }
                                        } else {
                                            Text(
                                                text = "Enter a valid hex codepoint (0001 – 10FFFF)",
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 4.dp))

                                Text(
                                    text = "WordCraft Alt+X Inline Codepoint Toggle",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Converts a trailing hex code (such as 221E or U+03C0) at the end of the active block into its Unicode character, or converts the last character into U+XXXX format.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = {
                                        viewModel.convertTrailingHexInActiveBlock()
                                        onDismiss()
                                    },
                                    modifier = Modifier.testTag("alt_x_toggle_button")
                                ) {
                                    Icon(Icons.Default.SwapHoriz, contentDescription = "Alt+X")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Toggle Last Character / U+Hex in Active Block (Alt+X)")
                                }
                            }
                        }

                        2 -> {
                            val blockText = activeBlock?.text.orEmpty()
                            val inspected = remember(blockText) { UnicodeCatalog.inspectUnicodeString(blockText, 40) }

                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "Unicode Normalization (Active Block)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("NFC", "NFD", "NFKC", "NFKD").forEach { form ->
                                        OutlinedButton(
                                            onClick = { viewModel.normalizeActiveBlockUnicode(form) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(form, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Codepoint Breakdown (First 40 characters of active block):",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(inspected) { info ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                                    RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = info.charDisplay,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = WordCraftPrimary,
                                                    modifier = Modifier.width(28.dp)
                                                )
                                                Text(
                                                    text = info.codePointHex,
                                                    fontSize = 12.sp,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontWeight = FontWeight.SemiBold,
                                                    modifier = Modifier.width(68.dp)
                                                )
                                                Text(
                                                    text = info.characterName,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Text(
                                                text = "UTF-8: ${info.utf8Hex}",
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    }
}

@Composable
fun WordCraftTranslateDialog(
    viewModel: WordCraftViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val doc by viewModel.document.collectAsState()
    val activeBlockId by viewModel.activeBlockId.collectAsState()
    val isTranslating by viewModel.isTranslating.collectAsState()
    val translationPreview by viewModel.translationPreview.collectAsState()
    val translationStatus by viewModel.translationStatus.collectAsState()

    val activeBlock = remember(doc, activeBlockId) {
        doc.blocks.find { it.id == activeBlockId } ?: doc.blocks.firstOrNull()
    }

    var sourceText by remember(activeBlock) { mutableStateOf(activeBlock?.text.orEmpty()) }
    var selectedTargetLang by remember { mutableStateOf(TranslationService.supportedLanguages.first()) }
    var showLangMenu by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WordCraftPrimary)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Translate",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "WordCraft Neural & Google Translate",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Translate active paragraph, full document, or open in Google Translate",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Target Language Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Target Language:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Box {
                            OutlinedButton(
                                onClick = { showLangMenu = true },
                                modifier = Modifier.testTag("target_language_selector")
                            ) {
                                Icon(Icons.Default.Language, contentDescription = "Language", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("${selectedTargetLang.name} (${selectedTargetLang.nativeName})")
                            }
                            DropdownMenu(
                                expanded = showLangMenu,
                                onDismissRequest = { showLangMenu = false }
                            ) {
                                TranslationService.supportedLanguages.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text("${lang.name} — ${lang.nativeName}") },
                                        onClick = {
                                            selectedTargetLang = lang
                                            showLangMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Source Text Input
                    OutlinedTextField(
                        value = sourceText,
                        onValueChange = { sourceText = it },
                        label = { Text("Source Text (Selected Block)") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth().testTag("translate_source_input")
                    )

                    // Action row: Translate Selection vs Open Google Translate
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.previewBlockTranslation(
                                    text = sourceText,
                                    sourceLang = "auto",
                                    targetLang = selectedTargetLang.code
                                )
                            },
                            enabled = !isTranslating && sourceText.isNotBlank(),
                            modifier = Modifier.weight(1f).testTag("run_translate_button")
                        ) {
                            if (isTranslating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Translate Block")
                        }

                        OutlinedButton(
                            onClick = {
                                TranslationService.openInGoogleTranslate(
                                    context = context,
                                    text = sourceText.ifBlank { doc.title },
                                    targetLangCode = selectedTargetLang.code
                                )
                            },
                            modifier = Modifier.testTag("open_google_translate_button")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = "Google Translate", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Google Translate")
                        }
                    }

                    if (!translationStatus.isNullOrBlank()) {
                        Text(
                            text = translationStatus!!,
                            fontSize = 12.sp,
                            color = WordCraftPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Translation result preview
                    if (translationPreview.isNotBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Translated (${selectedTargetLang.name}):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WordCraftPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = translationPreview,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.applyTranslationToActiveBlock(translationPreview, insertBelow = false)
                                            onDismiss()
                                        },
                                        modifier = Modifier.weight(1f).testTag("replace_with_translation_button")
                                    ) {
                                        Text("Replace Block", fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.applyTranslationToActiveBlock(translationPreview, insertBelow = true)
                                            onDismiss()
                                        },
                                        modifier = Modifier.weight(1f).testTag("insert_translation_below_button")
                                    ) {
                                        Text("Insert Below", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    // Full Document Translation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Translate Entire Document",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Translates all headings, paragraphs, and table cells into ${selectedTargetLang.name}.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.translateEntireDocument("auto", selectedTargetLang.code)
                            },
                            enabled = !isTranslating,
                            colors = ButtonDefaults.buttonColors(containerColor = WordCraftPrimary),
                            modifier = Modifier.testTag("translate_full_doc_button")
                        ) {
                            Text("Translate All", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.DocumentStats
import com.example.model.WordCraftDoc
import com.example.model.WordCraftTemplates
import com.example.ui.theme.WordCraftPrimary
import com.example.ui.theme.WordCraftPrimaryDark
import com.example.viewmodel.WordCraftViewModel

enum class BackstagePage {
    NEW,
    OPEN,
    INFO,
    SAVE,
    PRINT,
    EXPORT
}

@Composable
fun WordCraftBackstageDialog(
    viewModel: WordCraftViewModel,
    onImportFileClick: () -> Unit,
    onPrintClick: () -> Unit,
    onExportFormat: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var currentPage by remember { mutableStateOf(BackstagePage.NEW) }
    val doc by viewModel.document.collectAsState()
    val stats = remember(doc) { DocumentStats.calculate(doc) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Backstage Left Navigation Rail
                Surface(
                    modifier = Modifier
                        .width(210.dp)
                        .fillMaxHeight(),
                    color = WordCraftPrimaryDark
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 16.dp, horizontal = 12.dp)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .testTag("backstage_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Return to Document",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "WordCraft",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text(
                            text = "Studio v0.7.0",
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        BackstageNavItem(
                            icon = Icons.Default.NoteAdd,
                            label = "New",
                            isSelected = currentPage == BackstagePage.NEW,
                            onClick = { currentPage = BackstagePage.NEW },
                            testTag = "backstage_nav_new"
                        )

                        BackstageNavItem(
                            icon = Icons.Default.FolderOpen,
                            label = "Open / Import",
                            isSelected = currentPage == BackstagePage.OPEN,
                            onClick = { currentPage = BackstagePage.OPEN },
                            testTag = "backstage_nav_open"
                        )

                        BackstageNavItem(
                            icon = Icons.Default.Info,
                            label = "Info & Inspect",
                            isSelected = currentPage == BackstagePage.INFO,
                            onClick = { currentPage = BackstagePage.INFO },
                            testTag = "backstage_nav_info"
                        )

                        BackstageNavItem(
                            icon = Icons.Default.Save,
                            label = "Save",
                            isSelected = currentPage == BackstagePage.SAVE,
                            onClick = {
                                viewModel.saveCurrentDocument()
                                currentPage = BackstagePage.SAVE
                            },
                            testTag = "backstage_nav_save"
                        )

                        BackstageNavItem(
                            icon = Icons.Default.Print,
                            label = "Print & PDF",
                            isSelected = currentPage == BackstagePage.PRINT,
                            onClick = { currentPage = BackstagePage.PRINT },
                            testTag = "backstage_nav_print"
                        )

                        BackstageNavItem(
                            icon = Icons.Default.FileDownload,
                            label = "Export",
                            isSelected = currentPage == BackstagePage.EXPORT,
                            onClick = { currentPage = BackstagePage.EXPORT },
                            testTag = "backstage_nav_export"
                        )
                    }
                }

                // Right Content Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(24.dp)
                ) {
                    when (currentPage) {
                        BackstagePage.NEW -> NewTemplatesPage(viewModel, onDismiss)
                        BackstagePage.OPEN -> OpenDocPage(doc, onImportFileClick, onDismiss)
                        BackstagePage.INFO -> InfoPropertiesPage(viewModel, doc, stats, onDismiss)
                        BackstagePage.SAVE -> SaveStatusPage(doc, onExportFormat, onDismiss)
                        BackstagePage.PRINT -> PrintDocPage(doc, onPrintClick, onExportFormat)
                        BackstagePage.EXPORT -> ExportDocPage(onExportFormat)
                    }
                }
            }
        }
    }
}

@Composable
private fun BackstageNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        color = if (isSelected) WordCraftPrimary else Color.Transparent,
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun NewTemplatesPage(
    viewModel: WordCraftViewModel,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "New Document",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Select a built-in WordCraft template or start with a blank canvas",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        val templates = listOf(
            TemplateItem(
                title = "The Open Studio Handbook",
                desc = "WordCraft's showcase document with headings, tables, quotes, and closing checklists.",
                tag = "Showcase",
                creator = { WordCraftTemplates.createSampleDocument() }
            ),
            TemplateItem(
                title = "Executive Resume",
                desc = "Professional single-column curriculum vitae with executive summary, experience, and education.",
                tag = "Career",
                creator = { WordCraftTemplates.createResumeTemplate() }
            ),
            TemplateItem(
                title = "Research & Analysis Report",
                desc = "Structured report with executive summary, performance comparison matrix, and findings callout.",
                tag = "Business",
                creator = { WordCraftTemplates.createReportTemplate() }
            ),
            TemplateItem(
                title = "Formal Correspondence Letter",
                desc = "Clean correspondence letter with headers, sender/recipient addresses, and formal signoff.",
                tag = "Formal",
                creator = { WordCraftTemplates.createLetterTemplate() }
            ),
            TemplateItem(
                title = "Blank Document",
                desc = "A clean empty page ready for your thoughts, notes, and drafts.",
                tag = "Blank",
                creator = { WordCraftTemplates.createBlankDocument() }
            )
        )

        templates.forEach { tmpl ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable {
                        viewModel.loadDocument(tmpl.creator())
                        onDismiss()
                    }
                    .testTag("template_${tmpl.tag.lowercase()}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = WordCraftPrimary.copy(alpha = 0.1f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = tmpl.title,
                                tint = WordCraftPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tmpl.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = WordCraftPrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = tmpl.tag,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = WordCraftPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = tmpl.desc,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class TemplateItem(
    val title: String,
    val desc: String,
    val tag: String,
    val creator: () -> WordCraftDoc
)

@Composable
private fun OpenDocPage(
    currentDoc: WordCraftDoc,
    onImportFileClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Open & Import Document", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Import DOCX, Markdown (.md), HTML, or Text files from your device storage",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Browse Device Storage", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Supports .docx (OpenXML), .md (Markdown), .html, and .txt files.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onImportFileClick,
                    colors = ButtonDefaults.buttonColors(containerColor = WordCraftPrimary),
                    modifier = Modifier.testTag("import_file_button")
                ) {
                    Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose File from Device...")
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Currently Active Document:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(currentDoc.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = WordCraftPrimary)
                Text("Author: ${currentDoc.author} • ${currentDoc.blocks.size} blocks", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onDismiss) {
                    Text("Continue Editing")
                }
            }
        }
    }
}

@Composable
private fun InfoPropertiesPage(
    viewModel: WordCraftViewModel,
    doc: WordCraftDoc,
    stats: DocumentStats,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Document Properties & Inspector", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    onDismiss()
                    viewModel.setShowAccessibilityDialog(true)
                },
                colors = ButtonDefaults.buttonColors(containerColor = WordCraftPrimary)
            ) {
                Text("Run Accessibility Checker")
            }
            OutlinedButton(
                onClick = { viewModel.toggleReadOnlyLock() }
            ) {
                Text(if (doc.readOnlyLocked) "Unlock Document" else "Protect Document (Read-Only)")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                PropRow("Title", doc.title)
                PropRow("Author", doc.author)
                PropRow("Theme Preset", doc.themePreset.displayName)
                PropRow("Word Count", "${stats.words} words")
                PropRow("Character Count", "${stats.characters} (${stats.charactersNoSpaces} without spaces)")
                PropRow("Paragraphs", "${stats.paragraphs} paragraphs")
                PropRow("Estimated Read Time", String.format("%.1f minutes", stats.readingTimeMinutes))
                PropRow("Page Layout", "${doc.pageSize.displayName} • ${doc.orientation.name}")
                PropRow("Margins & Columns", "${doc.margins.displayName} • ${doc.columns} col(s)")
                PropRow("Track Changes", if (doc.trackChanges) "Enabled" else "Off")
                PropRow("Protection Status", if (doc.readOnlyLocked) "Locked (Read-Only)" else "Editable")
                PropRow("Citation Style", "${doc.citationStyle} (${doc.sources.size} sources)")
                PropRow("Watermark", doc.watermark ?: "None")
            }
        }
    }
}

@Composable
private fun PropRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
}

@Composable
private fun SaveStatusPage(
    doc: WordCraftDoc,
    onExportFormat: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Document Saved", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "\"${doc.title}\" has been automatically saved to your on-device WordCraft storage.",
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = WordCraftPrimary)
            ) {
                Text("Back to Document")
            }
            OutlinedButton(onClick = { onExportFormat("docx") }) {
                Text("Save Copy as .DOCX")
            }
            OutlinedButton(onClick = { onExportFormat("pdf") }) {
                Text("Save Copy as .PDF")
            }
        }
    }
}

@Composable
private fun PrintDocPage(
    doc: WordCraftDoc,
    onPrintClick: () -> Unit,
    onExportFormat: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Print & PDF Studio", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Print directly using Android Print Services or generate a multi-page PDF document",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Android System Print Spooler", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Page Setup: ${doc.pageSize.displayName} • ${doc.orientation.name} • ${doc.margins.displayName}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onPrintClick,
                        colors = ButtonDefaults.buttonColors(containerColor = WordCraftPrimary),
                        modifier = Modifier.testTag("print_system_button")
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Print Document")
                    }
                    OutlinedButton(
                        onClick = { onExportFormat("pdf") },
                        modifier = Modifier.testTag("print_export_pdf_button")
                    ) {
                        Text("Export Multi-Page PDF")
                    }
                }
            }
        }
    }
}

@Composable
private fun ExportDocPage(
    onExportFormat: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Export & Share (WordCraft Web v0.7.0 Formats)", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(
            text = "Export your document in any of the 8 supported WordCraft formats",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        val formats = listOf(
            Triple("PDF Document (.pdf)", "Paginated vector PDF with headers, footers, watermarks, tables, and charts.", "pdf"),
            Triple("Word OpenXML (.docx)", "Microsoft Word format with custom fonts, styles, colors, and tables.", "docx"),
            Triple("OpenDocument Text (.odt)", "OASIS OpenDocument format for LibreOffice and OpenOffice.", "odt"),
            Triple("Rich Text Format (.rtf)", "Universal cross-platform rich text with color tables and formatting.", "rtf"),
            Triple("LaTeX Source (.tex)", "Academic LaTeX article source with equations, sections, and tables.", "tex"),
            Triple("Markdown (.md)", "Clean Markdown text with headings, checklists, tables, and quotes.", "md"),
            Triple("Web Page (.html)", "Self-contained HTML5 document with Google Fonts and embedded styles.", "html"),
            Triple("Plain Text (.txt)", "Simple UTF-8 plain text without markup.", "txt")
        )

        formats.forEach { (name, desc, ext) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable { onExportFormat(ext) }
                    .testTag("export_$ext"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = name,
                        tint = WordCraftPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(name, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    OutlinedButton(onClick = { onExportFormat(ext) }) {
                        Text("Export")
                    }
                }
            }
        }
    }
}

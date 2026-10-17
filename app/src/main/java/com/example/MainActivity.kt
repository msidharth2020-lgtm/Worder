package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.export.DocxExporter
import com.example.model.DocumentStats
import com.example.model.WordCraftDoc
import com.example.ui.WordCraftAccessibilityDialog
import com.example.ui.WordCraftBackstageDialog
import com.example.ui.WordCraftCanvas
import com.example.ui.WordCraftChartSmartArtDialog
import com.example.ui.WordCraftCommentsPane
import com.example.ui.WordCraftDrawingDialog
import com.example.ui.WordCraftEquationDialog
import com.example.ui.WordCraftFindReplaceBar
import com.example.ui.WordCraftMailingsDialog
import com.example.ui.WordCraftNavigationPane
import com.example.ui.WordCraftReferencesDialog
import com.example.ui.WordCraftRibbon
import com.example.ui.WordCraftStatsModal
import com.example.ui.WordCraftStatusBar
import com.example.ui.WordCraftTopBar
import com.example.ui.WordCraftTranslateDialog
import com.example.ui.WordCraftUnicodeDialog
import com.example.ui.WordCraftZoteroDialog
import com.example.ui.theme.WordCraftTheme
import com.example.viewmodel.WordCraftViewModel
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {

    private val viewModel: WordCraftViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            WordCraftTheme {
                val showBackstage by viewModel.showBackstage.collectAsState()
                val showNav by viewModel.showNavPane.collectAsState()
                val showComments by viewModel.showCommentsPane.collectAsState()
                val showStats by viewModel.showStatsDialog.collectAsState()
                val showUnicode by viewModel.showUnicodeDialog.collectAsState()
                val showTranslate by viewModel.showTranslateDialog.collectAsState()
                val showEquation by viewModel.showEquationDialog.collectAsState()
                val showDrawing by viewModel.showDrawingDialog.collectAsState()
                val showChart by viewModel.showChartSmartArtDialog.collectAsState()
                val showReferences by viewModel.showReferencesDialog.collectAsState()
                val showMailings by viewModel.showMailingsDialog.collectAsState()
                val showAccessibility by viewModel.showAccessibilityDialog.collectAsState()
                val showZotero by viewModel.showZoteroDialog.collectAsState()
                val showFindReplace by viewModel.showFindReplace.collectAsState()
                val doc by viewModel.document.collectAsState()

                val importFileLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.OpenDocument()
                ) { uri: Uri? ->
                    if (uri != null) {
                        val imported = DocxExporter.importDocumentFromUri(this@MainActivity, uri, null)
                        if (imported != null) {
                            viewModel.loadDocument(imported)
                            viewModel.setShowBackstage(false)
                            Toast.makeText(
                                this@MainActivity,
                                "Imported \"${imported.title}\"",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this@MainActivity,
                                "Could not parse selected file",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        WordCraftTopBar(
                            viewModel = viewModel,
                            onExportClick = { viewModel.setShowBackstage(true) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .imePadding()
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Find and replace bar
                            if (showFindReplace) {
                                WordCraftFindReplaceBar(
                                    viewModel = viewModel,
                                    onClose = { viewModel.toggleFindReplace() }
                                )
                            }

                            // 12-Tab WordCraft Ribbon Toolbar
                            WordCraftRibbon(viewModel = viewModel)

                            // Document Writing Canvas
                            WordCraftCanvas(
                                viewModel = viewModel,
                                modifier = Modifier.weight(1f)
                            )

                            // Microsoft Word Bottom Status Bar (Page, Word count, Proofing, View Modes, Zoom)
                            WordCraftStatusBar(viewModel = viewModel)
                        }

                        // Floating side panes (Navigation Outline & Comments)
                        if (showNav) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(top = 70.dp, start = 8.dp)
                            ) {
                                WordCraftNavigationPane(
                                    viewModel = viewModel,
                                    onClose = { viewModel.toggleNavPane() }
                                )
                            }
                        }

                        if (showComments) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 70.dp, end = 8.dp)
                            ) {
                                WordCraftCommentsPane(
                                    viewModel = viewModel,
                                    onClose = { viewModel.toggleCommentsPane() }
                                )
                            }
                        }

                        // Statistics Modal
                        if (showStats) {
                            WordCraftStatsModal(
                                docStats = DocumentStats.calculate(doc),
                                onDismiss = { viewModel.setShowStatsDialog(false) }
                            )
                        }

                        // Unicode & Symbol Studio Dialog
                        if (showUnicode) {
                            WordCraftUnicodeDialog(
                                viewModel = viewModel,
                                onDismiss = { viewModel.setShowUnicodeDialog(false) }
                            )
                        }

                        // Google Translate & Neural Translation Dialog
                        if (showTranslate) {
                            WordCraftTranslateDialog(
                                viewModel = viewModel,
                                onDismiss = { viewModel.setShowTranslateDialog(false) }
                            )
                        }

                        // Equation Gallery & Math Builder Dialog (WordCraft Web v0.7.0)
                        if (showEquation) {
                            WordCraftEquationDialog(
                                viewModel = viewModel,
                                onDismiss = { viewModel.setShowEquationDialog(false) }
                            )
                        }

                        // Freehand Ink Drawing, Ink-to-Shape, Ink-to-Math & Replay Studio (WordCraft Web v0.7.0)
                        if (showDrawing) {
                            WordCraftDrawingDialog(
                                viewModel = viewModel,
                                onDismiss = { viewModel.setShowDrawingDialog(false) }
                            )
                        }

                        // Chart & SmartArt Visual Builder Dialog (WordCraft Web v0.7.0)
                        if (showChart) {
                            WordCraftChartSmartArtDialog(
                                viewModel = viewModel,
                                onDismiss = { viewModel.setShowChartSmartArtDialog(false) }
                            )
                        }

                        // Citations, Bibliography (APA/MLA/Chicago/IEEE), Footnotes & TOC Dialog (WordCraft Web v0.7.0)
                        if (showReferences) {
                            WordCraftReferencesDialog(
                                viewModel = viewModel,
                                onDismiss = { viewModel.setShowReferencesDialog(false) }
                            )
                        }

                        // Mail Merge & Envelopes/Labels Studio Dialog (WordCraft Web v0.7.0)
                        if (showMailings) {
                            WordCraftMailingsDialog(
                                viewModel = viewModel,
                                onDismiss = { viewModel.setShowMailingsDialog(false) }
                            )
                        }

                        // Accessibility Checker & Document Inspector Dialog (WordCraft Web v0.7.0)
                        if (showAccessibility) {
                            WordCraftAccessibilityDialog(
                                viewModel = viewModel,
                                onDismiss = { viewModel.setShowAccessibilityDialog(false) }
                            )
                        }

                        // Zotero Citations, Bibliography & CSL Preferences Studio Dialog (WordCraft Web v0.7.0)
                        if (showZotero) {
                            WordCraftZoteroDialog(
                                viewModel = viewModel,
                                onDismiss = { viewModel.setShowZoteroDialog(false) }
                            )
                        }

                        // Backstage (File menu) Dialog
                        if (showBackstage) {
                            WordCraftBackstageDialog(
                                viewModel = viewModel,
                                onImportFileClick = {
                                    importFileLauncher.launch(
                                        arrayOf(
                                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                            "text/markdown",
                                            "text/html",
                                            "text/plain",
                                            "*/*"
                                        )
                                    )
                                },
                                onPrintClick = {
                                    viewModel.setShowBackstage(false)
                                    DocxExporter.printDocument(this@MainActivity, doc)
                                },
                                onExportFormat = { format ->
                                    handleExport(doc, format)
                                },
                                onDismiss = { viewModel.setShowBackstage(false) }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun handleExport(doc: WordCraftDoc, format: String) {
        try {
            when (format.lowercase()) {
                "pdf" -> {
                    val file = DocxExporter.exportToPdf(this, doc)
                    shareFile(file, "application/pdf", "Share WordCraft PDF")
                }
                "docx" -> {
                    val file = DocxExporter.exportToDocx(this, doc)
                    shareFile(
                        file,
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "Share WordCraft DOCX"
                    )
                }
                "odt" -> {
                    val file = DocxExporter.exportToOdt(this, doc)
                    shareFile(
                        file,
                        "application/vnd.oasis.opendocument.text",
                        "Share WordCraft ODT"
                    )
                }
                "rtf" -> {
                    val rtfText = DocxExporter.exportToRtf(doc)
                    val file = writeTempFile(doc.title, "rtf", rtfText)
                    shareFile(file, "application/rtf", "Share WordCraft RTF")
                }
                "tex" -> {
                    val texText = DocxExporter.exportToLaTeX(doc)
                    val file = writeTempFile(doc.title, "tex", texText)
                    shareFile(file, "application/x-tex", "Share WordCraft LaTeX")
                }
                "md" -> {
                    val mdText = DocxExporter.exportToMarkdown(doc)
                    val file = writeTempFile(doc.title, "md", mdText)
                    shareFile(file, "text/markdown", "Share WordCraft Markdown")
                }
                "html" -> {
                    val htmlText = DocxExporter.exportToHtml(doc)
                    val file = writeTempFile(doc.title, "html", htmlText)
                    shareFile(file, "text/html", "Share WordCraft HTML")
                }
                "txt" -> {
                    val plainText = DocxExporter.exportToPlainText(doc)
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TITLE, doc.title)
                        putExtra(Intent.EXTRA_TEXT, plainText)
                        type = "text/plain"
                    }
                    startActivity(Intent.createChooser(sendIntent, "Share Document Text"))
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun writeTempFile(title: String, ext: String, content: String): File {
        val docsDir = File(cacheDir, "docs").apply { if (!exists()) mkdirs() }
        val safeTitle = title.replace("[^a-zA-Z0-9_-]".toRegex(), "_")
        val file = File(docsDir, "$safeTitle.$ext")
        FileOutputStream(file).use { it.write(content.toByteArray(StandardCharsets.UTF_8)) }
        return file
    }

    private fun shareFile(file: File, mimeType: String, chooserTitle: String) {
        val uri: Uri = FileProvider.getUriForFile(
            this,
            "${applicationContext.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, chooserTitle))
    }
}

package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.export.DocxExporter
import com.example.model.DocumentStats
import com.example.model.WordCraftDoc
import com.example.ui.WordCraftBackstageDialog
import com.example.ui.WordCraftCanvas
import com.example.ui.WordCraftCommentsPane
import com.example.ui.WordCraftFindReplaceBar
import com.example.ui.WordCraftNavigationPane
import com.example.ui.WordCraftRibbon
import com.example.ui.WordCraftStatsModal
import com.example.ui.WordCraftTopBar
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
                val showFindReplace by viewModel.showFindReplace.collectAsState()
                val doc by viewModel.document.collectAsState()

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

                            // The Ribbon Toolbar
                            WordCraftRibbon(viewModel = viewModel)

                            // Document Writing Canvas
                            WordCraftCanvas(
                                viewModel = viewModel,
                                modifier = Modifier.weight(1f)
                            )
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

                        // Backstage (File menu) Dialog
                        if (showBackstage) {
                            WordCraftBackstageDialog(
                                viewModel = viewModel,
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
                "docx" -> {
                    val file = DocxExporter.exportToDocx(this, doc)
                    shareFile(file, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Share WordCraft DOCX")
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

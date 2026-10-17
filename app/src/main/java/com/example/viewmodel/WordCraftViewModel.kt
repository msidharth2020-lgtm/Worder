package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.export.CslStyle
import com.example.export.DictionaryAndZoteroService
import com.example.export.TranslationService
import com.example.export.WordDefinitionResult
import com.example.export.ZoteroItem
import com.example.model.BlockType
import com.example.model.CalloutType
import com.example.model.ChartData
import com.example.model.CitationSource
import com.example.model.Comment
import com.example.model.DocBlock
import com.example.model.DocumentThemePreset
import com.example.model.InkStroke
import com.example.model.MailMergeRecipient
import com.example.model.PageMargins
import com.example.model.PageOrientation
import com.example.model.PageSize
import com.example.model.SmartArtData
import com.example.model.TableData
import com.example.model.TableStyle
import com.example.model.TextAlignment
import com.example.model.TextSpan
import com.example.model.UnicodeCatalog
import com.example.model.WordCraftDoc
import com.example.model.WordCraftTemplates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class RibbonTab(val label: String) {
    HOME("Home"),
    INSERT("Insert"),
    DRAW("Draw"),
    DESIGN("Design"),
    LAYOUT("Layout"),
    REFERENCES("References"),
    MAILINGS("Mailings"),
    ZOTERO("Zotero"),
    REVIEW("Review"),
    VIEW("View"),
    DEVELOPER("Developer"),
    HELP("Help"),
    TABLE_TOOLS("Table")
}

enum class ViewMode(val label: String) {
    PRINT_LAYOUT("Print Layout"),
    WEB_LAYOUT("Web Layout"),
    READING_MODE("Read Mode")
}

class WordCraftViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val prefs: SharedPreferences =
        application.getSharedPreferences("wordcraft_docs_prefs", Context.MODE_PRIVATE)

    private val _document = MutableStateFlow<WordCraftDoc>(WordCraftTemplates.createSampleDocument())
    val document: StateFlow<WordCraftDoc> = _document.asStateFlow()

    private val undoStack = mutableListOf<WordCraftDoc>()
    private val redoStack = mutableListOf<WordCraftDoc>()

    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()

    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()

    private val _activeRibbonTab = MutableStateFlow(RibbonTab.HOME)
    val activeRibbonTab: StateFlow<RibbonTab> = _activeRibbonTab.asStateFlow()

    private val _activeBlockId = MutableStateFlow<String?>(null)
    val activeBlockId: StateFlow<String?> = _activeBlockId.asStateFlow()

    private val _showBackstage = MutableStateFlow(false)
    val showBackstage: StateFlow<Boolean> = _showBackstage.asStateFlow()

    private val _showNavPane = MutableStateFlow(false)
    val showNavPane: StateFlow<Boolean> = _showNavPane.asStateFlow()

    private val _showCommentsPane = MutableStateFlow(false)
    val showCommentsPane: StateFlow<Boolean> = _showCommentsPane.asStateFlow()

    private val _showStatsDialog = MutableStateFlow(false)
    val showStatsDialog: StateFlow<Boolean> = _showStatsDialog.asStateFlow()

    private val _showUnicodeDialog = MutableStateFlow(false)
    val showUnicodeDialog: StateFlow<Boolean> = _showUnicodeDialog.asStateFlow()

    private val _showTranslateDialog = MutableStateFlow(false)
    val showTranslateDialog: StateFlow<Boolean> = _showTranslateDialog.asStateFlow()

    // WordCraft Web v0.7.0 Feature Dialog States
    private val _showEquationDialog = MutableStateFlow(false)
    val showEquationDialog: StateFlow<Boolean> = _showEquationDialog.asStateFlow()

    private val _showDrawingDialog = MutableStateFlow(false)
    val showDrawingDialog: StateFlow<Boolean> = _showDrawingDialog.asStateFlow()

    private val _showChartSmartArtDialog = MutableStateFlow(false)
    val showChartSmartArtDialog: StateFlow<Boolean> = _showChartSmartArtDialog.asStateFlow()

    private val _showReferencesDialog = MutableStateFlow(false)
    val showReferencesDialog: StateFlow<Boolean> = _showReferencesDialog.asStateFlow()

    private val _showMailingsDialog = MutableStateFlow(false)
    val showMailingsDialog: StateFlow<Boolean> = _showMailingsDialog.asStateFlow()

    private val _showAccessibilityDialog = MutableStateFlow(false)
    val showAccessibilityDialog: StateFlow<Boolean> = _showAccessibilityDialog.asStateFlow()

    // Zotero Add-on State (wordcraft-web-0.7.0 Zotero Tab & Dialog)
    private val _showZoteroDialog = MutableStateFlow(false)
    val showZoteroDialog: StateFlow<Boolean> = _showZoteroDialog.asStateFlow()

    private val _zoteroItems = MutableStateFlow(DictionaryAndZoteroService.defaultZoteroItems)
    val zoteroItems: StateFlow<List<ZoteroItem>> = _zoteroItems.asStateFlow()

    private val _zoteroCslStyle = MutableStateFlow(CslStyle.APA_7)
    val zoteroCslStyle: StateFlow<CslStyle> = _zoteroCslStyle.asStateFlow()

    private val _zoteroStoreRefsInDoc = MutableStateFlow(true)
    val zoteroStoreRefsInDoc: StateFlow<Boolean> = _zoteroStoreRefsInDoc.asStateFlow()

    private val _zoteroSearchResults = MutableStateFlow<List<ZoteroItem>>(emptyList())
    val zoteroSearchResults: StateFlow<List<ZoteroItem>> = _zoteroSearchResults.asStateFlow()

    private val _isSearchingZotero = MutableStateFlow(false)
    val isSearchingZotero: StateFlow<Boolean> = _isSearchingZotero.asStateFlow()

    private val _zoteroStatusMessage = MutableStateFlow<String?>(null)
    val zoteroStatusMessage: StateFlow<String?> = _zoteroStatusMessage.asStateFlow()

    // Text Selection & Word Hover Inspector State
    private val _selectedTextRange = MutableStateFlow<Pair<Int, Int>?>(null)
    val selectedTextRange: StateFlow<Pair<Int, Int>?> = _selectedTextRange.asStateFlow()

    private val _selectedOrHoveredWord = MutableStateFlow("")
    val selectedOrHoveredWord: StateFlow<String> = _selectedOrHoveredWord.asStateFlow()

    private val _hoverToolbarExpandedTab = MutableStateFlow<String?>(null) // null, "DEFINE", "TRANSLATE", "HIGHLIGHT", "FONT"
    val hoverToolbarExpandedTab: StateFlow<String?> = _hoverToolbarExpandedTab.asStateFlow()

    private val _wordDefinition = MutableStateFlow<WordDefinitionResult?>(null)
    val wordDefinition: StateFlow<WordDefinitionResult?> = _wordDefinition.asStateFlow()

    private val _isLoadingDefinition = MutableStateFlow(false)
    val isLoadingDefinition: StateFlow<Boolean> = _isLoadingDefinition.asStateFlow()

    private val _inlineTranslationResult = MutableStateFlow<String?>(null)
    val inlineTranslationResult: StateFlow<String?> = _inlineTranslationResult.asStateFlow()

    private val _isInlineTranslating = MutableStateFlow(false)
    val isInlineTranslating: StateFlow<Boolean> = _isInlineTranslating.asStateFlow()

    private val _inlineTargetLang = MutableStateFlow("es")
    val inlineTargetLang: StateFlow<String> = _inlineTargetLang.asStateFlow()

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    private val _translationPreview = MutableStateFlow("")
    val translationPreview: StateFlow<String> = _translationPreview.asStateFlow()

    private val _translationStatus = MutableStateFlow<String?>(null)
    val translationStatus: StateFlow<String?> = _translationStatus.asStateFlow()

    private val _showFindReplace = MutableStateFlow(false)
    val showFindReplace: StateFlow<Boolean> = _showFindReplace.asStateFlow()

    private val _findQuery = MutableStateFlow("")
    val findQuery: StateFlow<String> = _findQuery.asStateFlow()

    private val _replaceText = MutableStateFlow("")
    val replaceText: StateFlow<String> = _replaceText.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.PRINT_LAYOUT)
    val viewMode: StateFlow<ViewMode> = _viewMode.asStateFlow()

    private val _isDarkCanvas = MutableStateFlow(false)
    val isDarkCanvas: StateFlow<Boolean> = _isDarkCanvas.asStateFlow()

    private val _zoomPercent = MutableStateFlow(100)
    val zoomPercent: StateFlow<Int> = _zoomPercent.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    val sampleRecipients = listOf(
        MailMergeRecipient("Ada", "Lovelace", "Analytical Engine Co.", "ada@analytical.org", "London"),
        MailMergeRecipient("Grace", "Hopper", "Compiler Systems", "grace@navy.mil", "New York"),
        MailMergeRecipient("Donald", "Knuth", "TeX Publishing", "knuth@stanford.edu", "Palo Alto")
    )

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        tts = TextToSpeech(application, this)
        loadLastSavedDoc()
        if (_activeBlockId.value == null) {
            _activeBlockId.value = _document.value.blocks.firstOrNull()?.id
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            ttsReady = true
        }
    }

    private fun pushUndo() {
        undoStack.add(_document.value)
        if (undoStack.size > 40) undoStack.removeAt(0)
        redoStack.clear()
        _canUndo.value = true
        _canRedo.value = false
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val prev = undoStack.removeAt(undoStack.size - 1)
            redoStack.add(_document.value)
            _document.value = prev
            _canUndo.value = undoStack.isNotEmpty()
            _canRedo.value = true
            saveCurrentDocument()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.size - 1)
            undoStack.add(_document.value)
            _document.value = next
            _canUndo.value = true
            _canRedo.value = redoStack.isNotEmpty()
            saveCurrentDocument()
        }
    }

    fun getActiveBlock(): DocBlock? {
        val activeId = _activeBlockId.value
        return _document.value.blocks.find { it.id == activeId } ?: _document.value.blocks.firstOrNull()
    }

    fun stepBlockFontSize(deltaSp: Int) {
        _activeBlockId.value?.let { stepBlockFontSize(it, deltaSp) }
    }

    fun updateBlockFontFamily(fontFamily: String) {
        _activeBlockId.value?.let { updateBlockFontFamily(it, fontFamily) }
    }

    fun updateBlockFontSize(fontSizeSp: Int) {
        _activeBlockId.value?.let { updateBlockFontSize(it, fontSizeSp) }
    }

    fun updateBlockAlignment(alignment: TextAlignment) {
        _activeBlockId.value?.let { updateBlockAlignment(it, alignment) }
    }

    fun clearBlockFormatting() {
        _activeBlockId.value?.let { clearBlockFormatting(it) }
    }

    fun toggleBlockBold() {
        _activeBlockId.value?.let { toggleBlockBold(it) }
    }

    fun toggleBlockItalic() {
        _activeBlockId.value?.let { toggleBlockItalic(it) }
    }

    fun toggleBlockUnderline() {
        _activeBlockId.value?.let { toggleBlockUnderline(it) }
    }

    fun toggleBlockStrikethrough() {
        _activeBlockId.value?.let { toggleBlockStrikethrough(it) }
    }

    fun updateBlockHighlightColor(highlightHex: String?) {
        _activeBlockId.value?.let { updateBlockHighlightColor(it, highlightHex) }
    }

    fun updateBlockTextColor(colorHex: String?) {
        _activeBlockId.value?.let { updateBlockTextColor(it, colorHex) }
    }

    fun setActiveRibbonTab(tab: RibbonTab) {
        _activeRibbonTab.value = tab
    }

    private var autoSaveJob: kotlinx.coroutines.Job? = null

    private fun scheduleAutoSave() {
        autoSaveJob?.cancel()
        autoSaveJob = viewModelScope.launch {
            kotlinx.coroutines.delay(800)
            saveCurrentDocument()
        }
    }

    fun setActiveBlockId(id: String?) {
        if (_activeBlockId.value != id) {
            pushUndo()
            _activeBlockId.value = id
            val block = _document.value.blocks.find { it.id == id }
            if (block?.type == BlockType.TABLE) {
                _activeRibbonTab.value = RibbonTab.TABLE_TOOLS
            }
        }
    }

    fun setShowBackstage(show: Boolean) { _showBackstage.value = show }
    fun toggleNavPane() { _showNavPane.value = !_showNavPane.value }
    fun toggleCommentsPane() { _showCommentsPane.value = !_showCommentsPane.value }
    fun setShowStatsDialog(show: Boolean) { _showStatsDialog.value = show }
    fun setShowUnicodeDialog(show: Boolean) { _showUnicodeDialog.value = show }
    fun setShowEquationDialog(show: Boolean) { _showEquationDialog.value = show }
    fun setShowDrawingDialog(show: Boolean) { _showDrawingDialog.value = show }
    fun setShowChartSmartArtDialog(show: Boolean) { _showChartSmartArtDialog.value = show }
    fun setShowReferencesDialog(show: Boolean) { _showReferencesDialog.value = show }
    fun setShowMailingsDialog(show: Boolean) { _showMailingsDialog.value = show }
    fun setShowAccessibilityDialog(show: Boolean) { _showAccessibilityDialog.value = show }

    fun setShowTranslateDialog(show: Boolean) {
        _showTranslateDialog.value = show
        if (show) {
            _translationStatus.value = null
            _translationPreview.value = ""
        }
    }

    fun toggleFindReplace() { _showFindReplace.value = !_showFindReplace.value }
    fun setFindQuery(q: String) { _findQuery.value = q }
    fun setReplaceText(t: String) { _replaceText.value = t }
    fun setViewMode(mode: ViewMode) { _viewMode.value = mode }
    fun toggleDarkCanvas() { _isDarkCanvas.value = !_isDarkCanvas.value }
    fun setZoomPercent(percent: Int) { _zoomPercent.value = percent.coerceIn(50, 200) }

    fun updateTitle(newTitle: String) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        _document.value = _document.value.copy(
            title = newTitle,
            lastModified = System.currentTimeMillis()
        )
        saveCurrentDocument()
    }

    fun updateBlockText(blockId: String, newText: String) {
        if (_document.value.readOnlyLocked) return
        val currentBlock = _document.value.blocks.find { it.id == blockId }
        if (currentBlock?.text == newText) return
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(text = newText) else it
        }
        _document.value = _document.value.copy(
            blocks = updated,
            lastModified = System.currentTimeMillis()
        )
        scheduleAutoSave()
    }

    fun updateBlockType(blockId: String, newType: BlockType) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(type = newType) else it
        }
        _document.value = _document.value.copy(
            blocks = updated,
            lastModified = System.currentTimeMillis()
        )
        saveCurrentDocument()
    }

    fun updateBlockAlignment(blockId: String, alignment: TextAlignment) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(alignment = alignment) else it
        }
        _document.value = _document.value.copy(
            blocks = updated,
            lastModified = System.currentTimeMillis()
        )
        saveCurrentDocument()
    }

    // --- Font Family, Font Size & Inline Styling Functions ---

    fun updateBlockFontFamily(blockId: String, fontFamily: String) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(fontFamily = fontFamily) else it
        }
        _document.value = _document.value.copy(
            blocks = updated,
            lastModified = System.currentTimeMillis()
        )
        saveCurrentDocument()
    }

    fun updateAllBlocksFontFamily(fontFamily: String) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updated = _document.value.blocks.map {
            it.copy(fontFamily = fontFamily)
        }
        _document.value = _document.value.copy(
            blocks = updated,
            lastModified = System.currentTimeMillis()
        )
        saveCurrentDocument()
    }

    fun updateBlockFontSize(blockId: String, fontSizeSp: Int) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val clamped = fontSizeSp.coerceIn(8, 72)
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(fontSizeSp = clamped) else it
        }
        _document.value = _document.value.copy(
            blocks = updated,
            lastModified = System.currentTimeMillis()
        )
        saveCurrentDocument()
    }

    fun stepBlockFontSize(blockId: String, deltaSp: Int) {
        val block = _document.value.blocks.find { it.id == blockId } ?: return
        val nextSize = (block.effectiveFontSizeSp + deltaSp).coerceIn(8, 72)
        updateBlockFontSize(blockId, nextSize)
    }

    fun toggleBlockBold(blockId: String) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(bold = !it.bold) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun toggleBlockItalic(blockId: String) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(italic = !it.italic) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun toggleBlockUnderline(blockId: String) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(underline = !it.underline) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun toggleBlockStrikethrough(blockId: String) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(strikethrough = !it.strikethrough) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun updateBlockTextColor(blockId: String, colorHex: String?) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(textColorHex = colorHex) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun updateBlockHighlightColor(blockId: String, highlightHex: String?) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(highlightColorHex = highlightHex) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun updateBlockLineSpacing(blockId: String, spacing: Float) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(lineSpacing = spacing) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun changeBlockIndent(blockId: String, delta: Int) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(indentLevel = (it.indentLevel + delta).coerceIn(0, 5)) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun changeBlockCase(blockId: String, mode: String) {
        if (_document.value.readOnlyLocked) return
        val block = _document.value.blocks.find { it.id == blockId } ?: return
        pushUndo()
        val transformed = when (mode) {
            "UPPER" -> block.text.uppercase()
            "LOWER" -> block.text.lowercase()
            "TITLE" -> block.text.split(" ").joinToString(" ") { w ->
                w.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
            "TOGGLE" -> block.text.map { c ->
                if (c.isUpperCase()) c.lowercaseChar() else c.uppercaseChar()
            }.joinToString("")
            else -> block.text.lowercase().replaceFirstChar { it.uppercase() }
        }
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(text = transformed) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun clearBlockFormatting(blockId: String) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updated = _document.value.blocks.map {
            if (it.id == blockId) {
                it.copy(
                    type = BlockType.PARAGRAPH,
                    bold = false,
                    italic = false,
                    underline = false,
                    strikethrough = false,
                    fontSizeSp = null,
                    textColorHex = null,
                    highlightColorHex = null,
                    dropCap = false,
                    indentLevel = 0
                )
            } else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun toggleBlockDropCap(blockId: String) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(dropCap = !it.dropCap) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    // --- WordCraft Web v0.7.0 Insert, Draw, Design, References, Mailings, Developer Operations ---

    fun insertCoverPage() {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val dateStr = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date())
        val coverBlocks = listOf(
            DocBlock(
                type = BlockType.TITLE,
                text = _document.value.title,
                alignment = TextAlignment.CENTER,
                fontSizeSp = 32,
                bold = true
            ),
            DocBlock(
                type = BlockType.SUBTITLE,
                text = "Prepared by ${_document.value.author}  •  $dateStr",
                alignment = TextAlignment.CENTER
            ),
            DocBlock(type = BlockType.PAGE_BREAK)
        )
        _document.value = _document.value.copy(
            blocks = coverBlocks + _document.value.blocks,
            lastModified = System.currentTimeMillis()
        )
        saveCurrentDocument()
    }

    fun insertEquationBlock(equationText: String) {
        if (_document.value.readOnlyLocked) return
        insertBlock(_activeBlockId.value, BlockType.EQUATION, equationText)
    }

    fun insertDrawingBlock(strokes: List<InkStroke>, caption: String = "Freehand Ink Canvas") {
        if (_document.value.readOnlyLocked || strokes.isEmpty()) return
        pushUndo()
        val newBlock = DocBlock(
            id = UUID.randomUUID().toString(),
            type = BlockType.DRAWING,
            text = caption,
            inkStrokes = strokes
        )
        insertCustomBlockAfter(_activeBlockId.value, newBlock)
    }

    fun insertChartBlock(chartData: ChartData) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val newBlock = DocBlock(
            id = UUID.randomUUID().toString(),
            type = BlockType.CHART,
            text = chartData.title,
            chartData = chartData
        )
        insertCustomBlockAfter(_activeBlockId.value, newBlock)
    }

    fun insertSmartArtBlock(smartArtData: SmartArtData) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val newBlock = DocBlock(
            id = UUID.randomUUID().toString(),
            type = BlockType.SMART_ART,
            text = smartArtData.nodes.joinToString(" → "),
            smartArtData = smartArtData
        )
        insertCustomBlockAfter(_activeBlockId.value, newBlock)
    }

    fun insertShapeBlock(shapeKind: String, labelText: String) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val newBlock = DocBlock(
            id = UUID.randomUUID().toString(),
            type = BlockType.SHAPE,
            text = labelText,
            shapeKind = shapeKind,
            alignment = TextAlignment.CENTER
        )
        insertCustomBlockAfter(_activeBlockId.value, newBlock)
    }

    fun insertSignatureLineBlock(signerName: String = _document.value.author, signerTitle: String = "Authorized Signatory") {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val newBlock = DocBlock(
            id = UUID.randomUUID().toString(),
            type = BlockType.SIGNATURE_LINE,
            text = "X __________________________\n$signerName — $signerTitle"
        )
        insertCustomBlockAfter(_activeBlockId.value, newBlock)
    }

    fun insertDateTimeStamp() {
        val stamp = SimpleDateFormat("MMMM d, yyyy 'at' h:mm a", Locale.getDefault()).format(Date())
        insertUnicodeSymbol(" $stamp ")
    }

    fun applyDocumentTheme(preset: DocumentThemePreset) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updatedBlocks = _document.value.blocks.map { b ->
            val isHeading = b.type in listOf(BlockType.TITLE, BlockType.SUBTITLE, BlockType.HEADING_1, BlockType.HEADING_2, BlockType.HEADING_3)
            b.copy(fontFamily = if (isHeading) preset.headingFont else preset.bodyFont)
        }
        _document.value = _document.value.copy(
            themePreset = preset,
            blocks = updatedBlocks,
            lastModified = System.currentTimeMillis()
        )
        saveCurrentDocument()
    }

    fun setPageColor(colorHex: String?) {
        pushUndo()
        _document.value = _document.value.copy(pageColorHex = colorHex)
        saveCurrentDocument()
    }

    fun togglePageBorder() {
        pushUndo()
        _document.value = _document.value.copy(pageBorder = !_document.value.pageBorder)
        saveCurrentDocument()
    }

    fun setDocumentParagraphSpacing(spacing: Float) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updated = _document.value.blocks.map { it.copy(lineSpacing = spacing) }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun setPageColumns(cols: Int) {
        pushUndo()
        _document.value = _document.value.copy(columns = cols.coerceIn(1, 3))
        saveCurrentDocument()
    }

    fun toggleLineNumbers() {
        pushUndo()
        _document.value = _document.value.copy(showLineNumbers = !_document.value.showLineNumbers)
        saveCurrentDocument()
    }

    fun toggleRuler() {
        _document.value = _document.value.copy(showRuler = !_document.value.showRuler)
    }

    fun toggleGridlines() {
        _document.value = _document.value.copy(showGridlines = !_document.value.showGridlines)
    }

    fun toggleTrackChanges() {
        _document.value = _document.value.copy(trackChanges = !_document.value.trackChanges)
    }

    fun toggleReadOnlyLock() {
        _document.value = _document.value.copy(readOnlyLocked = !_document.value.readOnlyLocked)
    }

    // --- References Tab Operations (TOC, Citations, Bibliography, Footnotes) ---

    fun insertTableOfContents() {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val headings = _document.value.blocks.filter {
            it.type in listOf(BlockType.TITLE, BlockType.HEADING_1, BlockType.HEADING_2, BlockType.HEADING_3)
        }
        val tocLines = if (headings.isEmpty()) {
            "Table of Contents\n1. Introduction ...................... 1"
        } else {
            "TABLE OF CONTENTS\n" + headings.mapIndexed { idx, h ->
                val indent = when (h.type) {
                    BlockType.HEADING_2 -> "   "
                    BlockType.HEADING_3 -> "      "
                    else -> ""
                }
                "$indent${idx + 1}. ${h.text} ...................... p. 1"
            }.joinToString("\n")
        }
        val tocBlock = DocBlock(
            id = UUID.randomUUID().toString(),
            type = BlockType.TOC,
            text = tocLines
        )
        insertCustomBlockAfter(_activeBlockId.value, tocBlock)
    }

    fun addCitationSource(tag: String, author: String, title: String, year: String, publisher: String) {
        pushUndo()
        val newSrc = CitationSource(tag = tag, author = author, title = title, year = year, publisher = publisher)
        _document.value = _document.value.copy(sources = _document.value.sources + newSrc)
        saveCurrentDocument()
    }

    fun setCitationStyle(style: String) {
        _document.value = _document.value.copy(citationStyle = style)
        saveCurrentDocument()
    }

    fun insertCitationInActiveBlock(source: CitationSource) {
        val style = _document.value.citationStyle
        val formatted = when (style) {
            "MLA" -> " (${source.author.substringBefore(",")} ${source.year})"
            "IEEE" -> " [${_document.value.sources.indexOfFirst { it.id == source.id }.coerceAtLeast(0) + 1}]"
            else -> " (${source.author.substringBefore(",")}, ${source.year})"
        }
        insertUnicodeSymbol(formatted)
    }

    fun insertBibliographyBlock() {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val style = _document.value.citationStyle
        val entries = _document.value.sources.mapIndexed { idx, s ->
            when (style) {
                "IEEE" -> "[${idx + 1}] ${s.author}, \"${s.title},\" ${s.publisher}, ${s.year}."
                "MLA" -> "${s.author}. *${s.title}*. ${s.publisher}, ${s.year}."
                else -> "${s.author} (${s.year}). ${s.title}. ${s.publisher}."
            }
        }
        val bibText = "REFERENCES ($style)\n" + entries.joinToString("\n")
        val bibBlock = DocBlock(
            id = UUID.randomUUID().toString(),
            type = BlockType.BIBLIOGRAPHY,
            text = bibText
        )
        insertCustomBlockAfter(_activeBlockId.value, bibBlock)
    }

    fun addFootnoteToActiveBlock(noteText: String) {
        if (noteText.isBlank() || _document.value.readOnlyLocked) return
        pushUndo()
        val fnIndex = _document.value.footnotes.size + 1
        val targetId = _activeBlockId.value ?: _document.value.blocks.lastOrNull()?.id ?: return
        val updatedBlocks = _document.value.blocks.map {
            if (it.id == targetId) it.copy(text = "${it.text}[$fnIndex]", footnote = noteText) else it
        }
        _document.value = _document.value.copy(
            blocks = updatedBlocks,
            footnotes = _document.value.footnotes + "[$fnIndex] $noteText",
            lastModified = System.currentTimeMillis()
        )
        saveCurrentDocument()
    }

    // --- Mailings & Developer Content Controls ---

    fun insertMergeField(fieldToken: String) {
        insertUnicodeSymbol(" «$fieldToken» ")
    }

    fun applyMailMergeRecipient(recipient: MailMergeRecipient) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updatedBlocks = _document.value.blocks.map { b ->
            val merged = b.text
                .replace("«First_Name»", recipient.firstName)
                .replace("«Last_Name»", recipient.lastName)
                .replace("«Company»", recipient.company)
                .replace("«Email»", recipient.email)
                .replace("«City»", recipient.city)
                .replace("«AddressBlock»", "${recipient.firstName} ${recipient.lastName}\n${recipient.company} • ${recipient.city}")
                .replace("«GreetingLine»", "Dear ${recipient.firstName} ${recipient.lastName},")
            b.copy(text = merged)
        }
        _document.value = _document.value.copy(blocks = updatedBlocks, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun insertContentControl(kind: String) {
        val snippet = when (kind) {
            "TEXT" -> "[Click or tap here to enter text]"
            "DATE" -> "[Date: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}]"
            "DROPDOWN" -> "[Choose an item: Approved | Pending | In Review]"
            "CHECKBOX" -> "☐ [Interactive Check Box Control]"
            else -> "[Content Control]"
        }
        insertUnicodeSymbol(" $snippet ")
    }

    private fun insertCustomBlockAfter(afterBlockId: String?, newBlock: DocBlock) {
        val currentBlocks = _document.value.blocks.toMutableList()
        val insertIndex = if (afterBlockId == null) {
            currentBlocks.size
        } else {
            val idx = currentBlocks.indexOfFirst { it.id == afterBlockId }
            if (idx >= 0) idx + 1 else currentBlocks.size
        }
        currentBlocks.add(insertIndex, newBlock)
        _document.value = _document.value.copy(
            blocks = currentBlocks,
            lastModified = System.currentTimeMillis()
        )
        _activeBlockId.value = newBlock.id
        saveCurrentDocument()
    }

    // --- Unicode & Symbol Functions ---

    fun insertUnicodeSymbol(symbol: String) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val targetId = _activeBlockId.value ?: _document.value.blocks.lastOrNull()?.id
        if (targetId == null) {
            insertBlock(null, BlockType.PARAGRAPH, symbol)
            return
        }
        val updated = _document.value.blocks.map {
            if (it.id == targetId) it.copy(text = it.text + symbol) else it
        }
        _document.value = _document.value.copy(
            blocks = updated,
            lastModified = System.currentTimeMillis()
        )
        saveCurrentDocument()
    }

    fun convertTrailingHexInActiveBlock() {
        if (_document.value.readOnlyLocked) return
        val targetId = _activeBlockId.value ?: _document.value.blocks.firstOrNull()?.id ?: return
        val block = _document.value.blocks.find { it.id == targetId } ?: return
        if (block.text.isEmpty()) return
        pushUndo()
        val converted = UnicodeCatalog.toggleTrailingUnicodeHex(block.text)
        val updated = _document.value.blocks.map {
            if (it.id == targetId) it.copy(text = converted) else it
        }
        _document.value = _document.value.copy(
            blocks = updated,
            lastModified = System.currentTimeMillis()
        )
        saveCurrentDocument()
    }

    fun normalizeActiveBlockUnicode(formName: String) {
        if (_document.value.readOnlyLocked) return
        val targetId = _activeBlockId.value ?: _document.value.blocks.firstOrNull()?.id ?: return
        val block = _document.value.blocks.find { it.id == targetId } ?: return
        pushUndo()
        val normalized = UnicodeCatalog.normalize(block.text, formName)
        val updated = _document.value.blocks.map {
            if (it.id == targetId) it.copy(text = normalized) else it
        }
        _document.value = _document.value.copy(
            blocks = updated,
            lastModified = System.currentTimeMillis()
        )
        saveCurrentDocument()
    }

    // --- Google Translate / Neural Translation Functions ---

    fun previewBlockTranslation(text: String, sourceLang: String, targetLang: String) {
        if (text.isBlank()) {
            _translationStatus.value = "Selected block is empty."
            return
        }
        viewModelScope.launch {
            _isTranslating.value = true
            _translationStatus.value = "Translating..."
            val result = TranslationService.translateText(text, sourceLang, targetLang)
            _isTranslating.value = false
            result.onSuccess { translated ->
                _translationPreview.value = translated
                _translationStatus.value = "Translation complete."
            }.onFailure { err ->
                _translationStatus.value = "Translation error: ${err.message}"
            }
        }
    }

    fun applyTranslationToActiveBlock(translatedText: String, insertBelow: Boolean) {
        if (translatedText.isBlank() || _document.value.readOnlyLocked) return
        val targetId = _activeBlockId.value ?: _document.value.blocks.firstOrNull()?.id
        if (targetId == null) {
            insertBlock(null, BlockType.PARAGRAPH, translatedText)
            return
        }
        if (insertBelow) {
            val activeBlock = _document.value.blocks.find { it.id == targetId }
            insertBlock(targetId, activeBlock?.type ?: BlockType.PARAGRAPH, translatedText)
        } else {
            pushUndo()
            val updated = _document.value.blocks.map {
                if (it.id == targetId) it.copy(text = translatedText) else it
            }
            _document.value = _document.value.copy(
                blocks = updated,
                lastModified = System.currentTimeMillis()
            )
            saveCurrentDocument()
        }
    }

    fun translateEntireDocument(sourceLang: String, targetLang: String) {
        if (_document.value.readOnlyLocked) return
        viewModelScope.launch {
            _isTranslating.value = true
            _translationStatus.value = "Translating entire document..."
            pushUndo()

            val currentDoc = _document.value
            val translatedTitle = TranslationService.translateText(currentDoc.title, sourceLang, targetLang)
                .getOrDefault(currentDoc.title)

            val newBlocks = mutableListOf<DocBlock>()
            for ((index, block) in currentDoc.blocks.withIndex()) {
                _translationStatus.value = "Translating block ${index + 1} of ${currentDoc.blocks.size}..."
                if (block.type == BlockType.TABLE && block.tableData != null) {
                    val t = block.tableData
                    val newHeaders = t.headers.map { h ->
                        if (h.isNotBlank()) TranslationService.translateText(h, sourceLang, targetLang).getOrDefault(h) else h
                    }
                    val newRows = t.rows.map { row ->
                        row.map { cell ->
                            if (cell.isNotBlank()) TranslationService.translateText(cell, sourceLang, targetLang).getOrDefault(cell) else cell
                        }
                    }
                    newBlocks.add(block.copy(tableData = t.copy(headers = newHeaders, rows = newRows)))
                } else if (block.text.isNotBlank()) {
                    val trText = TranslationService.translateText(block.text, sourceLang, targetLang)
                        .getOrDefault(block.text)
                    newBlocks.add(block.copy(text = trText))
                } else {
                    newBlocks.add(block)
                }
            }

            _document.value = currentDoc.copy(
                title = translatedTitle,
                blocks = newBlocks,
                lastModified = System.currentTimeMillis()
            )
            _isTranslating.value = false
            _translationStatus.value = "Full document translated!"
            saveCurrentDocument()
        }
    }

    // --- Existing Document Operations ---

    fun toggleChecklist(blockId: String) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(isChecked = !it.isChecked) else it
        }
        _document.value = _document.value.copy(
            blocks = updated,
            lastModified = System.currentTimeMillis()
        )
        saveCurrentDocument()
    }

    fun insertBlock(afterBlockId: String?, type: BlockType = BlockType.PARAGRAPH, text: String = "") {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val activeFont = _document.value.blocks.find { it.id == afterBlockId }?.fontFamily ?: "Inter"
        val newBlock = DocBlock(
            id = UUID.randomUUID().toString(),
            type = type,
            text = text,
            fontFamily = activeFont
        )
        insertCustomBlockAfter(afterBlockId, newBlock)
    }

    fun deleteBlock(blockId: String) {
        if (_document.value.readOnlyLocked || _document.value.blocks.size <= 1) return
        pushUndo()
        val currentBlocks = _document.value.blocks.filterNot { it.id == blockId }
        _document.value = _document.value.copy(
            blocks = currentBlocks,
            lastModified = System.currentTimeMillis()
        )
        _activeBlockId.value = currentBlocks.lastOrNull()?.id
        saveCurrentDocument()
    }

    fun insertTable(rows: Int = 3, cols: Int = 3, afterBlockId: String? = null) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val headers = (1..cols).map { "Column $it" }
        val rowData = (1..rows).map { r -> (1..cols).map { c -> "Cell $r,$c" } }
        val tableBlock = DocBlock(
            id = UUID.randomUUID().toString(),
            type = BlockType.TABLE,
            tableData = TableData(headers = headers, rows = rowData, style = TableStyle.BANDED_BLUE)
        )
        val list = _document.value.blocks.toMutableList()
        val idx = if (afterBlockId != null) {
            val i = list.indexOfFirst { it.id == afterBlockId }
            if (i >= 0) i + 1 else list.size
        } else list.size
        list.add(idx, tableBlock)
        _document.value = _document.value.copy(blocks = list, lastModified = System.currentTimeMillis())
        _activeBlockId.value = tableBlock.id
        _activeRibbonTab.value = RibbonTab.TABLE_TOOLS
        saveCurrentDocument()
    }

    fun updateTableCell(blockId: String, rowIndex: Int, colIndex: Int, text: String) {
        if (_document.value.readOnlyLocked) return
        val block = _document.value.blocks.find { it.id == blockId } ?: return
        val table = block.tableData ?: return

        val newTable = if (rowIndex == -1) {
            val newHeaders = table.headers.toMutableList()
            if (colIndex in newHeaders.indices) {
                if (newHeaders[colIndex] == text) return
                newHeaders[colIndex] = text
            }
            table.copy(headers = newHeaders)
        } else {
            val newRows = table.rows.mapIndexed { rIdx, row ->
                if (rIdx == rowIndex) {
                    val cells = row.toMutableList()
                    if (colIndex in cells.indices) {
                        if (cells[colIndex] == text) return@updateTableCell
                        cells[colIndex] = text
                    }
                    cells
                } else row
            }
            table.copy(rows = newRows)
        }

        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(tableData = newTable) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        scheduleAutoSave()
    }

    fun addTableRow(blockId: String) {
        if (_document.value.readOnlyLocked) return
        val block = _document.value.blocks.find { it.id == blockId } ?: return
        val table = block.tableData ?: return
        pushUndo()
        val colCount = if (table.headers.isNotEmpty()) table.headers.size else (table.rows.firstOrNull()?.size ?: 3)
        val newRow = (1..colCount).map { "" }
        val newRows = table.rows + listOf(newRow)
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(tableData = table.copy(rows = newRows)) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun addTableCol(blockId: String) {
        if (_document.value.readOnlyLocked) return
        val block = _document.value.blocks.find { it.id == blockId } ?: return
        val table = block.tableData ?: return
        pushUndo()
        val newHeaders = table.headers + listOf("Column ${table.headers.size + 1}")
        val newRows = table.rows.map { it + listOf("") }
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(tableData = table.copy(headers = newHeaders, rows = newRows)) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun deleteTableRow(blockId: String, rowIndex: Int) {
        if (_document.value.readOnlyLocked) return
        val block = _document.value.blocks.find { it.id == blockId } ?: return
        val table = block.tableData ?: return
        if (table.rows.size <= 1) return
        pushUndo()
        val newRows = table.rows.filterIndexed { idx, _ -> idx != rowIndex }
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(tableData = table.copy(rows = newRows)) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun updateTableStyle(blockId: String, style: TableStyle) {
        val block = _document.value.blocks.find { it.id == blockId } ?: return
        val table = block.tableData ?: return
        pushUndo()
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(tableData = table.copy(style = style)) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun appendTableFormulaSummaryRow(blockId: String, formulaType: String) {
        val block = _document.value.blocks.find { it.id == blockId } ?: return
        val table = block.tableData ?: return
        pushUndo()
        val colCount = table.headers.size.coerceAtLeast(1)
        val summaryRow = (0 until colCount).map { colIdx ->
            if (colIdx == 0) {
                if (formulaType == "AVG") "Average" else "Total (SUM)"
            } else {
                val nums = table.rows.mapNotNull { r ->
                    r.getOrNull(colIdx)?.replace("[^0-9.-]".toRegex(), "")?.toDoubleOrNull()
                }
                if (nums.isEmpty()) "-"
                else {
                    val v = if (formulaType == "AVG") nums.average() else nums.sum()
                    String.format(Locale.US, "%.1f", v)
                }
            }
        }
        val updated = _document.value.blocks.map {
            if (it.id == blockId) it.copy(tableData = table.copy(rows = table.rows + listOf(summaryRow))) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun insertCallout(afterBlockId: String?, calloutType: CalloutType = CalloutType.INFO, text: String = "Note:") {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val newBlock = DocBlock(
            id = UUID.randomUUID().toString(),
            type = BlockType.CALLOUT,
            text = text,
            calloutType = calloutType
        )
        insertCustomBlockAfter(afterBlockId, newBlock)
    }

    fun addComment(blockId: String, author: String = "WordCraft User", text: String) {
        if (text.isBlank()) return
        pushUndo()
        val newComment = Comment(
            id = UUID.randomUUID().toString(),
            blockId = blockId,
            author = author,
            text = text,
            timestamp = System.currentTimeMillis(),
            resolved = false
        )
        val comments = _document.value.comments + newComment
        _document.value = _document.value.copy(comments = comments)
        saveCurrentDocument()
    }

    fun resolveComment(commentId: String) {
        pushUndo()
        val comments = _document.value.comments.map {
            if (it.id == commentId) it.copy(resolved = !it.resolved) else it
        }
        _document.value = _document.value.copy(comments = comments)
        saveCurrentDocument()
    }

    fun setPageMargins(margins: PageMargins) {
        pushUndo()
        _document.value = _document.value.copy(margins = margins)
        saveCurrentDocument()
    }

    fun setPageOrientation(orientation: PageOrientation) {
        pushUndo()
        _document.value = _document.value.copy(orientation = orientation)
        saveCurrentDocument()
    }

    fun setPageSize(pageSize: PageSize) {
        pushUndo()
        _document.value = _document.value.copy(pageSize = pageSize)
        saveCurrentDocument()
    }

    fun setWatermark(watermarkText: String?) {
        pushUndo()
        _document.value = _document.value.copy(watermark = watermarkText?.takeIf { it.isNotBlank() })
        saveCurrentDocument()
    }

    fun updateHeaderFooter(header: String?, footer: String?) {
        pushUndo()
        _document.value = _document.value.copy(headerText = header, footerText = footer)
        saveCurrentDocument()
    }

    fun replaceAll(find: String, replace: String) {
        if (find.isEmpty() || _document.value.readOnlyLocked) return
        pushUndo()
        val updated = _document.value.blocks.map { block ->
            if (block.text.contains(find, ignoreCase = true)) {
                block.copy(text = block.text.replace(find, replace, ignoreCase = true))
            } else block
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun loadDocument(doc: WordCraftDoc) {
        undoStack.clear()
        redoStack.clear()
        _canUndo.value = false
        _canRedo.value = false
        _document.value = doc
        _activeBlockId.value = doc.blocks.firstOrNull()?.id
        _showBackstage.value = false
        saveCurrentDocument()
    }

    fun toggleSpeech() {
        if (_isSpeaking.value) {
            tts?.stop()
            _isSpeaking.value = false
        } else {
            if (ttsReady) {
                val fullText = _document.value.blocks.joinToString("\n") { it.text }
                tts?.speak(fullText, TextToSpeech.QUEUE_FLUSH, null, "doc_tts")
                _isSpeaking.value = true
            }
        }
    }

    // =========================================================================
    // Zotero Add-on Operations (wordcraft-web-0.7.0 `fn zotero`)
    // =========================================================================

    fun setShowZoteroDialog(show: Boolean) {
        _showZoteroDialog.value = show
    }

    fun setZoteroCslStyle(style: CslStyle) {
        _zoteroCslStyle.value = style
        _document.value = _document.value.copy(citationStyle = style.displayName)
        refreshZoteroCitations()
    }

    fun toggleZoteroStoreRefs() {
        _zoteroStoreRefsInDoc.value = !_zoteroStoreRefsInDoc.value
        _zoteroStatusMessage.value = if (_zoteroStoreRefsInDoc.value) {
            "Zotero references stored inside document metadata"
        } else {
            "Zotero references linked externally"
        }
    }

    fun addZoteroItem(item: ZoteroItem) {
        _zoteroItems.value = listOf(item) + _zoteroItems.value
        _zoteroStatusMessage.value = "Added \"${item.title}\" (${item.year}) to Zotero library"
    }

    fun searchZoteroCrossRef(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isSearchingZotero.value = true
            _zoteroStatusMessage.value = "Searching CrossRef / Zotero catalog for \"$query\"..."
            val results = DictionaryAndZoteroService.searchZoteroCrossRef(query)
            _zoteroSearchResults.value = results
            _isSearchingZotero.value = false
            _zoteroStatusMessage.value = if (results.isNotEmpty()) {
                "Found ${results.size} scholarly works via CrossRef DOI catalog"
            } else {
                "No external matches found; you can add a custom Zotero item below."
            }
        }
    }

    fun insertZoteroCitation(item: ZoteroItem, locatorPage: String = "") {
        if (_document.value.readOnlyLocked) return
        val style = _zoteroCslStyle.value
        val idx = (_zoteroItems.value.indexOfFirst { it.id == item.id }.takeIf { it >= 0 } ?: 0) + 1
        val formatted = item.formatInTextCitation(style, idx, locatorPage)
        insertUnicodeSymbol(" $formatted")
        _zoteroStatusMessage.value = "Inserted Zotero citation: $formatted"
    }

    fun insertZoteroNote(item: ZoteroItem) {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val noteText = if (item.note.isNotBlank()) {
            "Zotero Note (${item.author}, ${item.year}): ${item.note}"
        } else {
            "Zotero Research Note on \"${item.title}\" (${item.author}, ${item.year}): Key findings and methodology."
        }
        val currentBlocks = _document.value.blocks.toMutableList()
        val activeIndex = currentBlocks.indexOfFirst { it.id == _activeBlockId.value }
        val newBlock = DocBlock(
            type = BlockType.CALLOUT,
            calloutType = CalloutType.INFO,
            text = noteText
        )
        val insertIdx = if (activeIndex >= 0) activeIndex + 1 else currentBlocks.size
        currentBlocks.add(insertIdx, newBlock)
        _document.value = _document.value.copy(blocks = currentBlocks, lastModified = System.currentTimeMillis())
        _activeBlockId.value = newBlock.id
        _zoteroStatusMessage.value = "Inserted Zotero Note block"
        saveCurrentDocument()
    }

    fun movePastZoteroCitation() {
        val active = getActiveBlock() ?: return
        val text = active.text
        val curEnd = _selectedTextRange.value?.second ?: text.length
        val closeParen = text.indexOf(')', curEnd)
        val closeBracket = text.indexOf(']', curEnd)
        val targetPos = listOf(closeParen, closeBracket).filter { it >= 0 }.minOrNull()?.plus(1) ?: text.length
        _selectedTextRange.value = targetPos to targetPos
        _zoteroStatusMessage.value = "Caret moved past citation"
    }

    fun insertOrUpdateZoteroBibliography() {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val style = _zoteroCslStyle.value
        val entries = _zoteroItems.value.mapIndexed { idx, item ->
            item.formatBibliographyEntry(style, idx + 1)
        }.joinToString("\n\n")

        val currentBlocks = _document.value.blocks.toMutableList()
        val existingBibIdx = currentBlocks.indexOfFirst { it.type == BlockType.BIBLIOGRAPHY }
        if (existingBibIdx >= 0) {
            val updated = currentBlocks[existingBibIdx].copy(text = entries)
            currentBlocks[existingBibIdx] = updated
            _activeBlockId.value = updated.id
        } else {
            val newBlock = DocBlock(
                type = BlockType.BIBLIOGRAPHY,
                text = entries
            )
            currentBlocks.add(newBlock)
            _activeBlockId.value = newBlock.id
        }
        _document.value = _document.value.copy(
            blocks = currentBlocks,
            citationStyle = style.displayName,
            lastModified = System.currentTimeMillis()
        )
        _zoteroStatusMessage.value = "Updated Zotero Bibliography (${style.displayName})"
        saveCurrentDocument()
    }

    fun refreshZoteroCitations() {
        if (_document.value.readOnlyLocked) return
        val style = _zoteroCslStyle.value
        val items = _zoteroItems.value
        val updatedBlocks = _document.value.blocks.map { block ->
            if (block.type == BlockType.BIBLIOGRAPHY) {
                val entries = items.mapIndexed { idx, item ->
                    item.formatBibliographyEntry(style, idx + 1)
                }.joinToString("\n\n")
                block.copy(text = entries)
            } else {
                var newText = block.text
                items.forEachIndexed { idx, item ->
                    val lastName = item.author.substringBefore(",").trim()
                    val targetCite = item.formatInTextCitation(style, idx + 1)
                    // Replace older APA/MLA/Chicago/IEEE variants for this author+year
                    val patterns = listOf(
                        "($lastName, ${item.year})",
                        "($lastName ${item.year})",
                        "($lastName)"
                    )
                    for (pat in patterns) {
                        if (newText.contains(pat) && pat != targetCite) {
                            newText = newText.replace(pat, targetCite)
                        }
                    }
                }
                if (newText != block.text) block.copy(text = newText) else block
            }
        }
        _document.value = _document.value.copy(
            blocks = updatedBlocks,
            citationStyle = style.displayName,
            lastModified = System.currentTimeMillis()
        )
        _zoteroStatusMessage.value = "Refreshed all Zotero citations & bibliography (${style.displayName})"
        saveCurrentDocument()
    }

    fun unlinkZoteroCitations() {
        if (_document.value.readOnlyLocked) return
        pushUndo()
        val updatedBlocks = _document.value.blocks.map { block ->
            if (block.type == BlockType.BIBLIOGRAPHY) {
                block.copy(type = BlockType.PARAGRAPH)
            } else {
                block.copy(text = block.text.replace("ADDIN ZOTERO_ITEM ", ""))
            }
        }
        _document.value = _document.value.copy(blocks = updatedBlocks, lastModified = System.currentTimeMillis())
        _zoteroStatusMessage.value = "Unlinked Zotero field codes; citations converted to plain text"
        saveCurrentDocument()
    }

    // =========================================================================
    // Text Selection & Word Hover Mini-Toolbar Operations
    // =========================================================================

    fun setHoverToolbarExpandedTab(tab: String?) {
        _hoverToolbarExpandedTab.value = if (_hoverToolbarExpandedTab.value == tab) null else tab
        if (_hoverToolbarExpandedTab.value == "DEFINE") {
            lookupActiveSelectionDefinition()
        } else if (_hoverToolbarExpandedTab.value == "TRANSLATE") {
            translateActiveSelection(_inlineTargetLang.value)
        }
    }

    fun setInlineTargetLang(langCode: String) {
        _inlineTargetLang.value = langCode
        translateActiveSelection(langCode)
    }

    fun clearSelectionOrHover() {
        _selectedTextRange.value = null
        _selectedOrHoveredWord.value = ""
        _hoverToolbarExpandedTab.value = null
    }

    /**
     * Called whenever the user moves the cursor, taps a word, or selects a text range in a block.
     * Automatically resolves either the selected range [selStart, selEnd) or the hovered word under the cursor.
     */
    fun updateSelectionOrHover(blockId: String, fullText: String, rawStart: Int, rawEnd: Int) {
        if (_activeBlockId.value != blockId) {
            _activeBlockId.value = blockId
        }
        if (fullText.isBlank()) {
            _selectedTextRange.value = null
            _selectedOrHoveredWord.value = ""
            return
        }
        val start = rawStart.coerceIn(0, fullText.length)
        val end = rawEnd.coerceIn(0, fullText.length)
        val minPos = minOf(start, end)
        val maxPos = maxOf(start, end)

        if (minPos != maxPos) {
            val sub = fullText.substring(minPos, maxPos).trim()
            _selectedTextRange.value = minPos to maxPos
            if (sub.isNotBlank() && sub != _selectedOrHoveredWord.value) {
                _selectedOrHoveredWord.value = sub
                _wordDefinition.value = null
                _inlineTranslationResult.value = null
                if (_hoverToolbarExpandedTab.value == "DEFINE") lookupActiveSelectionDefinition()
                if (_hoverToolbarExpandedTab.value == "TRANSLATE") translateActiveSelection(_inlineTargetLang.value)
            }
        } else {
            // Cursor / hover over a single point: find the word boundaries around `minPos`
            val wordBounds = findWordBoundsAt(fullText, minPos)
            if (wordBounds != null) {
                val hoveredWord = fullText.substring(wordBounds.first, wordBounds.second)
                _selectedTextRange.value = wordBounds
                if (hoveredWord.isNotBlank() && hoveredWord != _selectedOrHoveredWord.value) {
                    _selectedOrHoveredWord.value = hoveredWord
                    _wordDefinition.value = null
                    _inlineTranslationResult.value = null
                    if (_hoverToolbarExpandedTab.value == "DEFINE") lookupActiveSelectionDefinition()
                    if (_hoverToolbarExpandedTab.value == "TRANSLATE") translateActiveSelection(_inlineTargetLang.value)
                }
            }
        }
    }

    private fun findWordBoundsAt(text: String, offset: Int): Pair<Int, Int>? {
        if (text.isEmpty()) return null
        val safeIdx = offset.coerceIn(0, (text.length - 1).coerceAtLeast(0))
        var s = safeIdx
        var e = safeIdx
        if (!text[s].isLetterOrDigit() && s > 0 && text[s - 1].isLetterOrDigit()) {
            s -= 1
            e = s
        }
        if (!text[s].isLetterOrDigit()) return null
        while (s > 0 && (text[s - 1].isLetterOrDigit() || text[s - 1] == '\'' || text[s - 1] == '-')) {
            s--
        }
        while (e < text.length && (text[e].isLetterOrDigit() || text[e] == '\'' || text[e] == '-')) {
            e++
        }
        return if (e > s) s to e else null
    }

    private fun updateActiveBlockSpan(transform: (TextSpan) -> TextSpan) {
        val active = getActiveBlock() ?: return
        val range = _selectedTextRange.value ?: findWordBoundsAt(active.text, active.text.length / 2) ?: (0 to active.text.length)
        val s = range.first.coerceIn(0, active.text.length)
        val e = range.second.coerceIn(0, active.text.length)
        if (e <= s) return

        pushUndo()
        val existingIdx = active.spans.indexOfFirst { it.start == s && it.end == e }
        val newSpans = active.spans.toMutableList()
        if (existingIdx >= 0) {
            newSpans[existingIdx] = transform(newSpans[existingIdx])
        } else {
            newSpans.add(transform(TextSpan(start = s, end = e)))
        }
        val updated = _document.value.blocks.map {
            if (it.id == active.id) it.copy(spans = newSpans) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun applySpanHighlight(highlightHex: String?) {
        val active = getActiveBlock() ?: return
        val range = _selectedTextRange.value
        if (range != null && range.second > range.first) {
            updateActiveBlockSpan { it.copy(highlightHex = highlightHex) }
        } else {
            updateBlockHighlightColor(highlightHex)
        }
    }

    fun toggleSpanUnderline() {
        val active = getActiveBlock() ?: return
        val range = _selectedTextRange.value
        if (range != null && range.second > range.first) {
            updateActiveBlockSpan { it.copy(underline = !it.underline) }
        } else {
            toggleBlockUnderline()
        }
    }

    fun toggleSpanBold() {
        val active = getActiveBlock() ?: return
        val range = _selectedTextRange.value
        if (range != null && range.second > range.first) {
            updateActiveBlockSpan { it.copy(bold = !it.bold) }
        } else {
            toggleBlockBold()
        }
    }

    fun toggleSpanItalic() {
        val active = getActiveBlock() ?: return
        val range = _selectedTextRange.value
        if (range != null && range.second > range.first) {
            updateActiveBlockSpan { it.copy(italic = !it.italic) }
        } else {
            toggleBlockItalic()
        }
    }

    fun toggleSpanStrikethrough() {
        val active = getActiveBlock() ?: return
        val range = _selectedTextRange.value
        if (range != null && range.second > range.first) {
            updateActiveBlockSpan { it.copy(strikethrough = !it.strikethrough) }
        } else {
            toggleBlockStrikethrough()
        }
    }

    fun applySpanTextColor(colorHex: String?) {
        val active = getActiveBlock() ?: return
        val range = _selectedTextRange.value
        if (range != null && range.second > range.first) {
            updateActiveBlockSpan { it.copy(colorHex = colorHex) }
        } else {
            updateBlockTextColor(colorHex)
        }
    }

    fun lookupActiveSelectionDefinition() {
        val word = _selectedOrHoveredWord.value.ifBlank {
            getActiveBlock()?.text?.trim()?.split("\\s+".toRegex())?.firstOrNull().orEmpty()
        }
        if (word.isBlank()) return
        viewModelScope.launch {
            _isLoadingDefinition.value = true
            val def = DictionaryAndZoteroService.lookupWordDefinition(word)
            _wordDefinition.value = def
            _isLoadingDefinition.value = false
        }
    }

    fun translateActiveSelection(targetLangCode: String = _inlineTargetLang.value) {
        val selection = _selectedOrHoveredWord.value.ifBlank {
            getActiveBlock()?.text.orEmpty()
        }
        if (selection.isBlank()) return
        viewModelScope.launch {
            _isInlineTranslating.value = true
            val res = TranslationService.translateText(
                text = selection,
                sourceLang = "auto",
                targetLang = targetLangCode
            )
            _inlineTranslationResult.value = res.getOrElse { "Translation unavailable: ${it.message}" }
            _isInlineTranslating.value = false
        }
    }

    fun replaceSelectionWithText(replacement: String) {
        if (_document.value.readOnlyLocked || replacement.isBlank()) return
        val active = getActiveBlock() ?: return
        val range = _selectedTextRange.value
        pushUndo()
        val newText = if (range != null && range.first >= 0 && range.second <= active.text.length && range.second > range.first) {
            active.text.substring(0, range.first) + replacement + active.text.substring(range.second)
        } else {
            replacement
        }
        val updated = _document.value.blocks.map {
            if (it.id == active.id) it.copy(text = newText) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        _selectedOrHoveredWord.value = replacement
        saveCurrentDocument()
    }

    fun appendTranslationToSelection(translated: String) {
        if (_document.value.readOnlyLocked || translated.isBlank()) return
        val active = getActiveBlock() ?: return
        val range = _selectedTextRange.value
        pushUndo()
        val newText = if (range != null && range.first >= 0 && range.second <= active.text.length && range.second > range.first) {
            val original = active.text.substring(range.first, range.second)
            active.text.substring(0, range.first) + "$original ($translated)" + active.text.substring(range.second)
        } else {
            "${active.text} ($translated)"
        }
        val updated = _document.value.blocks.map {
            if (it.id == active.id) it.copy(text = newText) else it
        }
        _document.value = _document.value.copy(blocks = updated, lastModified = System.currentTimeMillis())
        saveCurrentDocument()
    }

    fun transformSelectionCase(mode: String) {
        val current = _selectedOrHoveredWord.value
        if (current.isBlank()) return
        val transformed = when (mode) {
            "UPPER" -> current.uppercase()
            "LOWER" -> current.lowercase()
            "TITLE" -> current.split(" ").joinToString(" ") { w ->
                w.lowercase().replaceFirstChar { it.titlecase() }
            }
            else -> current
        }
        replaceSelectionWithText(transformed)
    }

    fun speakSelection() {
        val textToRead = _selectedOrHoveredWord.value.ifBlank {
            getActiveBlock()?.text.orEmpty()
        }
        if (textToRead.isNotBlank() && ttsReady) {
            tts?.speak(textToRead, TextToSpeech.QUEUE_FLUSH, null, "selection_tts")
        }
    }

    fun copySelectedText(context: Context) {
        val text = _selectedOrHoveredWord.value.ifBlank { getActiveBlock()?.text.orEmpty() }
        if (text.isNotBlank()) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("WordCraft Selection", text)
            clipboard.setPrimaryClip(clip)
            android.widget.Toast.makeText(context, "Copied \"${text.take(20)}\"", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun cutSelectedText(context: Context) {
        copySelectedText(context)
        replaceSelectionWithText("")
    }

    fun pasteFromClipboard(context: Context, mode: String = "MERGE") {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val item = clipboard.primaryClip?.getItemAt(0)
            val text = item?.text?.toString().orEmpty()
            if (text.isNotBlank()) {
                val toInsert = when (mode) {
                    "TEXT_ONLY" -> text.replace("\\s+".toRegex(), " ")
                    "UPPER" -> text.uppercase()
                    "LOWER" -> text.lowercase()
                    else -> text
                }
                insertUnicodeSymbol(toInsert)
                android.widget.Toast.makeText(context, "Pasted into document", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.widget.Toast.makeText(context, "Clipboard empty or inaccessible", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    fun saveCurrentDocument() {
        viewModelScope.launch {
            try {
                val doc = _document.value
                val json = JSONObject().apply {
                    put("id", doc.id)
                    put("title", doc.title)
                    put("author", doc.author)
                    put("createdDate", doc.createdDate)
                    put("lastModified", doc.lastModified)
                    put("margins", doc.margins.name)
                    put("pageSize", doc.pageSize.name)
                    put("orientation", doc.orientation.name)
                    put("watermark", doc.watermark ?: "")
                    put("headerText", doc.headerText ?: "")
                    put("footerText", doc.footerText ?: "")
                    put("showPageNumbers", doc.showPageNumbers)
                    put("pageColorHex", doc.pageColorHex ?: "")
                    put("pageBorder", doc.pageBorder)
                    put("columns", doc.columns)
                    put("showLineNumbers", doc.showLineNumbers)

                    val blocksArr = JSONArray()
                    for (b in doc.blocks) {
                        val bObj = JSONObject().apply {
                            put("id", b.id)
                            put("type", b.type.name)
                            put("text", b.text)
                            put("alignment", b.alignment.name)
                            put("fontFamily", b.fontFamily)
                            if (b.fontSizeSp != null) put("fontSizeSp", b.fontSizeSp)
                            put("bold", b.bold)
                            put("italic", b.italic)
                            put("underline", b.underline)
                            put("strikethrough", b.strikethrough)
                            put("dropCap", b.dropCap)
                            put("textColorHex", b.textColorHex ?: "")
                            put("highlightColorHex", b.highlightColorHex ?: "")
                            put("isChecked", b.isChecked)
                            put("calloutType", b.calloutType?.name ?: "")
                            put("shapeKind", b.shapeKind ?: "")

                            if (b.spans.isNotEmpty()) {
                                val spansArr = JSONArray()
                                for (sp in b.spans) {
                                    spansArr.put(
                                        JSONObject().apply {
                                            put("start", sp.start)
                                            put("end", sp.end)
                                            put("bold", sp.bold)
                                            put("italic", sp.italic)
                                            put("underline", sp.underline)
                                            put("strikethrough", sp.strikethrough)
                                            put("colorHex", sp.colorHex ?: "")
                                            put("highlightHex", sp.highlightHex ?: "")
                                        }
                                    )
                                }
                                put("spans", spansArr)
                            }

                            b.tableData?.let { t ->
                                val tObj = JSONObject().apply {
                                    put("headers", JSONArray(t.headers))
                                    val rowsArr = JSONArray()
                                    for (r in t.rows) rowsArr.put(JSONArray(r))
                                    put("rows", rowsArr)
                                    put("style", t.style.name)
                                }
                                put("tableData", tObj)
                            }
                        }
                        blocksArr.put(bObj)
                    }
                    put("blocks", blocksArr)
                }
                prefs.edit().putString("saved_doc_${doc.id}", json.toString()).apply()
                prefs.edit().putString("last_active_doc_id", doc.id).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun loadLastSavedDoc() {
        val lastId = prefs.getString("last_active_doc_id", null) ?: return
        val raw = prefs.getString("saved_doc_$lastId", null) ?: return
        try {
            val json = JSONObject(raw)
            val blocks = mutableListOf<DocBlock>()
            val blocksArr = json.optJSONArray("blocks") ?: JSONArray()
            for (i in 0 until blocksArr.length()) {
                val bObj = blocksArr.getJSONObject(i)
                val type = runCatching { BlockType.valueOf(bObj.getString("type")) }.getOrDefault(BlockType.PARAGRAPH)
                val align = runCatching { TextAlignment.valueOf(bObj.getString("alignment")) }.getOrDefault(TextAlignment.LEFT)
                val callout = runCatching { CalloutType.valueOf(bObj.getString("calloutType")) }.getOrNull()
                val fontSize = if (bObj.has("fontSizeSp")) bObj.optInt("fontSizeSp", type.defaultFontSizeSp()) else null

                val spans = mutableListOf<TextSpan>()
                bObj.optJSONArray("spans")?.let { sArr ->
                    for (sIdx in 0 until sArr.length()) {
                        val sObj = sArr.getJSONObject(sIdx)
                        spans.add(
                            TextSpan(
                                start = sObj.optInt("start", 0),
                                end = sObj.optInt("end", 0),
                                bold = sObj.optBoolean("bold", false),
                                italic = sObj.optBoolean("italic", false),
                                underline = sObj.optBoolean("underline", false),
                                strikethrough = sObj.optBoolean("strikethrough", false),
                                colorHex = sObj.optString("colorHex").takeIf { it.isNotBlank() },
                                highlightHex = sObj.optString("highlightHex").takeIf { it.isNotBlank() }
                            )
                        )
                    }
                }

                var tableData: TableData? = null
                if (bObj.has("tableData")) {
                    val tObj = bObj.getJSONObject("tableData")
                    val hArr = tObj.getJSONArray("headers")
                    val headers = (0 until hArr.length()).map { hArr.getString(it) }
                    val rArr = tObj.getJSONArray("rows")
                    val rows = (0 until rArr.length()).map { rIdx ->
                        val row = rArr.getJSONArray(rIdx)
                        (0 until row.length()).map { row.getString(it) }
                    }
                    val style = runCatching { TableStyle.valueOf(tObj.getString("style")) }.getOrDefault(TableStyle.BANDED_BLUE)
                    tableData = TableData(headers = headers, rows = rows, style = style)
                }

                blocks.add(
                    DocBlock(
                        id = bObj.getString("id"),
                        type = type,
                        text = bObj.getString("text"),
                        spans = spans,
                        alignment = align,
                        fontFamily = bObj.optString("fontFamily", "Inter").ifBlank { "Inter" },
                        fontSizeSp = fontSize,
                        bold = bObj.optBoolean("bold", false),
                        italic = bObj.optBoolean("italic", false),
                        underline = bObj.optBoolean("underline", false),
                        strikethrough = bObj.optBoolean("strikethrough", false),
                        dropCap = bObj.optBoolean("dropCap", false),
                        textColorHex = bObj.optString("textColorHex").takeIf { it.isNotBlank() },
                        highlightColorHex = bObj.optString("highlightColorHex").takeIf { it.isNotBlank() },
                        isChecked = bObj.optBoolean("isChecked", false),
                        calloutType = callout,
                        tableData = tableData,
                        shapeKind = bObj.optString("shapeKind").takeIf { it.isNotBlank() }
                    )
                )
            }

            _document.value = WordCraftDoc(
                id = json.getString("id"),
                title = json.getString("title"),
                author = json.optString("author", "WordCraft User"),
                createdDate = json.optLong("createdDate", System.currentTimeMillis()),
                lastModified = json.optLong("lastModified", System.currentTimeMillis()),
                blocks = blocks,
                margins = runCatching { PageMargins.valueOf(json.getString("margins")) }.getOrDefault(PageMargins.NORMAL),
                pageSize = runCatching { PageSize.valueOf(json.getString("pageSize")) }.getOrDefault(PageSize.A4),
                orientation = runCatching { PageOrientation.valueOf(json.getString("orientation")) }.getOrDefault(PageOrientation.PORTRAIT),
                watermark = json.optString("watermark").takeIf { it.isNotBlank() },
                headerText = json.optString("headerText", "WordCraft Document"),
                footerText = json.optString("footerText", "Page 1"),
                showPageNumbers = json.optBoolean("showPageNumbers", true),
                pageColorHex = json.optString("pageColorHex").takeIf { it.isNotBlank() },
                pageBorder = json.optBoolean("pageBorder", false),
                columns = json.optInt("columns", 1),
                showLineNumbers = json.optBoolean("showLineNumbers", false)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}

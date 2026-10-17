package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.BlockType
import com.example.model.CalloutType
import com.example.model.Comment
import com.example.model.DocBlock
import com.example.model.DocumentStats
import com.example.model.PageMargins
import com.example.model.PageOrientation
import com.example.model.PageSize
import com.example.model.TableData
import com.example.model.TableStyle
import com.example.model.TextAlignment
import com.example.model.WordCraftDoc
import com.example.model.WordCraftTemplates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.UUID

enum class RibbonTab(val label: String) {
    HOME("Home"),
    INSERT("Insert"),
    LAYOUT("Layout"),
    REVIEW("Review"),
    VIEW("View"),
    TABLE_TOOLS("Table")
}

enum class ViewMode(val label: String) {
    PRINT_LAYOUT("Print Layout"),
    WEB_LAYOUT("Continuous"),
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

    private var tts: TextToSpeech? = null
    private var ttsReady = false

    init {
        tts = TextToSpeech(application, this)
        loadLastSavedDoc()
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
            // Auto-switch to table tab if active block is table
            val block = _document.value.blocks.find { it.id == id }
            if (block?.type == BlockType.TABLE) {
                _activeRibbonTab.value = RibbonTab.TABLE_TOOLS
            }
        }
    }

    fun setShowBackstage(show: Boolean) {
        _showBackstage.value = show
    }

    fun toggleNavPane() {
        _showNavPane.value = !_showNavPane.value
    }

    fun toggleCommentsPane() {
        _showCommentsPane.value = !_showCommentsPane.value
    }

    fun setShowStatsDialog(show: Boolean) {
        _showStatsDialog.value = show
    }

    fun toggleFindReplace() {
        _showFindReplace.value = !_showFindReplace.value
    }

    fun setFindQuery(q: String) {
        _findQuery.value = q
    }

    fun setReplaceText(t: String) {
        _replaceText.value = t
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun toggleDarkCanvas() {
        _isDarkCanvas.value = !_isDarkCanvas.value
    }

    fun setZoomPercent(percent: Int) {
        _zoomPercent.value = percent.coerceIn(50, 200)
    }

    fun updateTitle(newTitle: String) {
        pushUndo()
        _document.value = _document.value.copy(
            title = newTitle,
            lastModified = System.currentTimeMillis()
        )
        saveCurrentDocument()
    }

    fun updateBlockText(blockId: String, newText: String) {
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

    fun toggleChecklist(blockId: String) {
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
        pushUndo()
        val newBlock = DocBlock(
            id = UUID.randomUUID().toString(),
            type = type,
            text = text
        )
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

    fun deleteBlock(blockId: String) {
        if (_document.value.blocks.size <= 1) return
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
        val block = _document.value.blocks.find { it.id == blockId } ?: return
        val table = block.tableData ?: return

        val newTable = if (rowIndex == -1) {
            // Header row
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

    fun insertCallout(afterBlockId: String?, calloutType: CalloutType = CalloutType.INFO, text: String = "Note:") {
        pushUndo()
        val newBlock = DocBlock(
            id = UUID.randomUUID().toString(),
            type = BlockType.CALLOUT,
            text = text,
            calloutType = calloutType
        )
        val list = _document.value.blocks.toMutableList()
        val idx = if (afterBlockId != null) {
            val i = list.indexOfFirst { it.id == afterBlockId }
            if (i >= 0) i + 1 else list.size
        } else list.size
        list.add(idx, newBlock)
        _document.value = _document.value.copy(blocks = list, lastModified = System.currentTimeMillis())
        _activeBlockId.value = newBlock.id
        saveCurrentDocument()
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

    fun replaceAll(find: String, replace: String) {
        if (find.isEmpty()) return
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

                    val blocksArr = JSONArray()
                    for (b in doc.blocks) {
                        val bObj = JSONObject().apply {
                            put("id", b.id)
                            put("type", b.type.name)
                            put("text", b.text)
                            put("alignment", b.alignment.name)
                            put("isChecked", b.isChecked)
                            put("calloutType", b.calloutType?.name ?: "")

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
                        alignment = align,
                        isChecked = bObj.optBoolean("isChecked", false),
                        calloutType = callout,
                        tableData = tableData
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
                showPageNumbers = json.optBoolean("showPageNumbers", true)
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

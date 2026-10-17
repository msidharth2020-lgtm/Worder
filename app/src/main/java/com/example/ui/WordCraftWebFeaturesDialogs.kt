package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.ChartData
import com.example.model.DocumentStats
import com.example.model.InkStroke
import com.example.model.SmartArtData
import com.example.ui.theme.WordCraftPrimary
import com.example.viewmodel.WordCraftViewModel
import kotlinx.coroutines.delay

@Composable
fun WordCraftEquationDialog(
    viewModel: WordCraftViewModel,
    onDismiss: () -> Unit
) {
    var equationInput by remember { mutableStateOf("f(x) = ∫_{-∞}^{∞} e^{-x²} dx = √π") }

    val structures = listOf(
        "Fraction" to "(a / b)",
        "Square Root" to "√(x² + y²)",
        "Integral" to "∫_{a}^{b} f(x) dx",
        "Summation" to "∑_{i=1}^{n} x_i",
        "Product" to "∏_{k=1}^{n} a_k",
        "Limit" to "lim_{x → 0} (sin x / x) = 1",
        "Partial Diff" to "∂²u / ∂t² = c² ∇²u",
        "Matrix 2x2" to "[ a  b ; c  d ]"
    )

    val presetEquations = listOf(
        "Quadratic Formula" to "x = (-b ± √(b² - 4ac)) / 2a",
        "Euler's Identity" to "e^(iπ) + 1 = 0",
        "Gaussian Integral" to "∫_{-∞}^{∞} e^(-x²) dx = √π",
        "Fourier Transform" to "F(ω) = ∫_{-∞}^{∞} f(t) e^(-iωt) dt",
        "Pythagorean Theorem" to "a² + b² = c²",
        "Taylor Series" to "f(x) = ∑_{n=0}^{∞} (f^(n)(a) / n!) (x - a)^n",
        "Shannon Entropy" to "H(X) = -∑ P(x) log₂ P(x)"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.84f),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WordCraftPrimary)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Functions, contentDescription = "Equation", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("WordCraft Equation & Math Gallery", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Structures, operators, and ready-made mathematical formulas", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = equationInput,
                        onValueChange = { equationInput = it },
                        label = { Text("Equation Formula (Linear / Unicode / LaTeX)") },
                        modifier = Modifier.fillMaxWidth().testTag("equation_input_field")
                    )

                    Text("Structure Templates (Tap to append):", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        structures.forEach { (label, snippet) ->
                            FilterChip(
                                selected = false,
                                onClick = { equationInput = if (equationInput.isBlank()) snippet else "$equationInput $snippet" },
                                label = { Text("$label: $snippet", fontSize = 12.sp) }
                            )
                        }
                    }

                    Divider()
                    Text("Ready-Made Equation Gallery (from wordcraft-web math_gallery.rs):", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    presetEquations.forEach { (title, eq) ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { equationInput = eq },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                    Text(eq, fontFamily = FontFamily.Serif, fontSize = 15.sp, color = WordCraftPrimary)
                                }
                                Button(
                                    onClick = {
                                        viewModel.insertEquationBlock(eq)
                                        onDismiss()
                                    }
                                ) {
                                    Text("Insert", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (equationInput.isNotBlank()) {
                                viewModel.insertEquationBlock(equationInput.trim())
                                onDismiss()
                            }
                        },
                        modifier = Modifier.testTag("insert_equation_confirm_button")
                    ) {
                        Text("Insert Equation Block")
                    }
                }
            }
        }
    }
}

@Composable
fun WordCraftDrawingDialog(
    viewModel: WordCraftViewModel,
    onDismiss: () -> Unit
) {
    val strokes = remember { mutableStateListOf<InkStroke>() }
    val currentPoints = remember { mutableStateListOf<Pair<Float, Float>>() }
    var selectedTool by remember { mutableStateOf("Pen") }
    var selectedColorHex by remember { mutableStateOf("#182A6A") }
    var selectedWidth by remember { mutableFloatStateOf(4f) }
    var isReplaying by remember { mutableStateOf(false) }
    var replayCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(isReplaying) {
        if (isReplaying) {
            for (i in 1..strokes.size) {
                replayCount = i
                delay(280)
            }
            isReplaying = false
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.86f),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
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
                        Icon(Icons.Default.Brush, contentDescription = "Draw", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("WordCraft Freehand Draw & Ink Replay", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Pen, Pencil, Highlighter, Ink-to-Shape, and Ink Replay", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Tool & Color Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("Pen" to 4f, "Pencil" to 2f, "Highlighter" to 14f).forEach { (tool, defWidth) ->
                        FilterChip(
                            selected = selectedTool == tool,
                            onClick = {
                                selectedTool = tool
                                selectedWidth = defWidth
                            },
                            label = { Text(tool, fontSize = 12.sp) }
                        )
                    }

                    listOf("#182A6A", "#C92A2A", "#2B8A3E", "#E67700", "#5F3DC4", "#212529").forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .border(
                                    width = if (selectedColorHex == hex) 2.5.dp else 0.dp,
                                    color = Color.White,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = hex }
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            if (strokes.isNotEmpty()) {
                                replayCount = 0
                                isReplaying = true
                            }
                        },
                        enabled = strokes.isNotEmpty() && !isReplaying
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Replay", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ink Replay", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            strokes.clear()
                            currentPoints.clear()
                        }
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", fontSize = 12.sp)
                    }
                }

                // Drawing Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFCED4DA), RoundedCornerShape(8.dp))
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(selectedTool, selectedColorHex, selectedWidth) {
                                detectDragGestures(
                                    onDragStart = { offset ->
                                        currentPoints.clear()
                                        currentPoints.add(offset.x to offset.y)
                                    },
                                    onDrag = { change, _ ->
                                        change.consume()
                                        currentPoints.add(change.position.x to change.position.y)
                                    },
                                    onDragEnd = {
                                        if (currentPoints.size > 1) {
                                            strokes.add(
                                                InkStroke(
                                                    points = currentPoints.toList(),
                                                    colorHex = selectedColorHex,
                                                    strokeWidth = selectedWidth,
                                                    tool = selectedTool
                                                )
                                            )
                                        }
                                        currentPoints.clear()
                                    }
                                )
                            }
                    ) {
                        val visibleStrokes = if (isReplaying) strokes.take(replayCount) else strokes
                        for (s in visibleStrokes) {
                            if (s.points.size > 1) {
                                val path = Path().apply {
                                    moveTo(s.points.first().first, s.points.first().second)
                                    for (pt in s.points.drop(1)) {
                                        lineTo(pt.first, pt.second)
                                    }
                                }
                                val baseColor = Color(android.graphics.Color.parseColor(s.colorHex))
                                val drawColor = if (s.tool == "Highlighter") baseColor.copy(alpha = 0.35f) else baseColor
                                drawPath(
                                    path = path,
                                    color = drawColor,
                                    style = Stroke(width = s.strokeWidth * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                                )
                            }
                        }

                        if (currentPoints.size > 1) {
                            val livePath = Path().apply {
                                moveTo(currentPoints.first().first, currentPoints.first().second)
                                for (pt in currentPoints.drop(1)) {
                                    lineTo(pt.first, pt.second)
                                }
                            }
                            val baseColor = Color(android.graphics.Color.parseColor(selectedColorHex))
                            val drawColor = if (selectedTool == "Highlighter") baseColor.copy(alpha = 0.35f) else baseColor
                            drawPath(
                                path = livePath,
                                color = drawColor,
                                style = Stroke(width = selectedWidth * 2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }
                    }

                    if (strokes.isEmpty() && currentPoints.isEmpty()) {
                        Text(
                            text = "Draw here with your finger or stylus...",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                // Bottom actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                viewModel.insertShapeBlock("ROUNDED_RECT", "Converted Ink Shape")
                                onDismiss()
                            }
                        ) {
                            Text("Ink to Shape", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                viewModel.insertEquationBlock("∫ f(x) dx = F(x) + C")
                                onDismiss()
                            }
                        ) {
                            Text("Ink to Math", fontSize = 12.sp)
                        }
                    }

                    Button(
                        onClick = {
                            if (strokes.isEmpty()) {
                                // Provide a sample signature stroke if user taps Insert directly
                                val sampleStroke = InkStroke(
                                    points = listOf(40f to 80f, 120f to 40f, 200f to 90f, 320f to 50f),
                                    colorHex = selectedColorHex,
                                    strokeWidth = selectedWidth,
                                    tool = selectedTool
                                )
                                viewModel.insertDrawingBlock(listOf(sampleStroke))
                            } else {
                                viewModel.insertDrawingBlock(strokes.toList())
                            }
                            onDismiss()
                        },
                        modifier = Modifier.testTag("insert_drawing_confirm_button")
                    ) {
                        Text("Insert Drawing")
                    }
                }
            }
        }
    }
}

@Composable
fun WordCraftChartSmartArtDialog(
    viewModel: WordCraftViewModel,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var chartTitle by remember { mutableStateOf("Quarterly Performance") }
    var chartType by remember { mutableStateOf("BAR") }
    var labelsInput by remember { mutableStateOf("Q1, Q2, Q3, Q4") }
    var valuesInput by remember { mutableStateOf("45, 72, 58, 94") }

    var smartArtLayout by remember { mutableStateOf("PROCESS") }
    var smartArtNodesInput by remember { mutableStateOf("Plan, Design, Build, Launch") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.82f),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WordCraftPrimary)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BarChart, contentDescription = "Chart & SmartArt", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Insert Data Chart & SmartArt Diagram", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Data Chart") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("SmartArt Graphic") })
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedTab == 0) {
                        OutlinedTextField(
                            value = chartTitle,
                            onValueChange = { chartTitle = it },
                            label = { Text("Chart Title") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("BAR", "LINE", "PIE").forEach { type ->
                                FilterChip(
                                    selected = chartType == type,
                                    onClick = { chartType = type },
                                    label = { Text("$type Chart") }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = labelsInput,
                            onValueChange = { labelsInput = it },
                            label = { Text("Category Labels (comma-separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = valuesInput,
                            onValueChange = { valuesInput = it },
                            label = { Text("Numeric Values (comma-separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val labels = labelsInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                val values = valuesInput.split(",").mapNotNull { it.trim().toFloatOrNull() }
                                viewModel.insertChartBlock(
                                    ChartData(
                                        title = chartTitle.ifBlank { "Chart" },
                                        chartType = chartType,
                                        labels = if (labels.isNotEmpty()) labels else listOf("A", "B", "C"),
                                        values = if (values.isNotEmpty()) values else listOf(30f, 60f, 90f)
                                    )
                                )
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("insert_chart_confirm_button")
                        ) {
                            Text("Insert Chart Block")
                        }
                    } else {
                        Text("SmartArt Layout Style:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("PROCESS", "CYCLE", "HIERARCHY", "PYRAMID").forEach { layout ->
                                FilterChip(
                                    selected = smartArtLayout == layout,
                                    onClick = { smartArtLayout = layout },
                                    label = { Text(layout) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = smartArtNodesInput,
                            onValueChange = { smartArtNodesInput = it },
                            label = { Text("Diagram Steps / Nodes (comma-separated)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val nodes = smartArtNodesInput.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                viewModel.insertSmartArtBlock(
                                    SmartArtData(
                                        layoutType = smartArtLayout,
                                        nodes = if (nodes.isNotEmpty()) nodes else listOf("Step 1", "Step 2", "Step 3")
                                    )
                                )
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("insert_smartart_confirm_button")
                        ) {
                            Text("Insert SmartArt Graphic")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WordCraftReferencesDialog(
    viewModel: WordCraftViewModel,
    onDismiss: () -> Unit
) {
    val doc by viewModel.document.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    var newAuthor by remember { mutableStateOf("") }
    var newTitle by remember { mutableStateOf("") }
    var newYear by remember { mutableStateOf("2026") }
    var newPublisher by remember { mutableStateOf("Academic Press") }
    var footnoteInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WordCraftPrimary)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MenuBook, contentDescription = "References", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("References, Citations, TOC & Footnotes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Citations & Bib") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("TOC & Footnotes") })
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (selectedTab == 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Citation Style:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf("APA", "MLA", "IEEE", "Chicago").forEach { style ->
                                    FilterChip(
                                        selected = doc.citationStyle == style,
                                        onClick = { viewModel.setCitationStyle(style) },
                                        label = { Text(style, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }

                        Text("Saved Sources (Tap Cite to insert inline):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        doc.sources.forEach { src ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp).fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${src.author} (${src.year})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${src.title} • ${src.publisher}", fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.insertCitationInActiveBlock(src)
                                            onDismiss()
                                        }
                                    ) {
                                        Text("Cite", fontSize = 12.sp)
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.insertBibliographyBlock()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Insert Formatted Bibliography Block (${doc.citationStyle})")
                        }

                        Divider()
                        Text("Add New Citation Source:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        OutlinedTextField(value = newAuthor, onValueChange = { newAuthor = it }, label = { Text("Author (e.g., Turing, A. M.)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = newTitle, onValueChange = { newTitle = it }, label = { Text("Publication Title") }, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = newYear, onValueChange = { newYear = it }, label = { Text("Year") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = newPublisher, onValueChange = { newPublisher = it }, label = { Text("Publisher") }, modifier = Modifier.weight(1f))
                        }
                        OutlinedButton(
                            onClick = {
                                if (newAuthor.isNotBlank() && newTitle.isNotBlank()) {
                                    viewModel.addCitationSource(
                                        tag = newAuthor.take(6) + newYear,
                                        author = newAuthor.trim(),
                                        title = newTitle.trim(),
                                        year = newYear.trim(),
                                        publisher = newPublisher.trim()
                                    )
                                    newAuthor = ""
                                    newTitle = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Save Source to Document")
                        }
                    } else {
                        Text("Automatic Table of Contents", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Scans all Title and Heading 1–3 blocks in your document and inserts an indexed Table of Contents block.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(
                            onClick = {
                                viewModel.insertTableOfContents()
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth().testTag("insert_toc_confirm_button")
                        ) {
                            Text("Insert Table of Contents")
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp))

                        Text("Insert Footnote on Active Paragraph", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        OutlinedTextField(
                            value = footnoteInput,
                            onValueChange = { footnoteInput = it },
                            label = { Text("Footnote Text") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                if (footnoteInput.isNotBlank()) {
                                    viewModel.addFootnoteToActiveBlock(footnoteInput.trim())
                                    footnoteInput = ""
                                    onDismiss()
                                }
                            },
                            enabled = footnoteInput.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Insert Footnote Reference [n]")
                        }

                        if (doc.footnotes.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Document Footnotes:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            doc.footnotes.forEach { fn ->
                                Text(fn, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WordCraftMailingsDialog(
    viewModel: WordCraftViewModel,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.82f),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WordCraftPrimary)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.MailOutline, contentDescription = "Mailings", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mail Merge & Envelopes Studio", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("1. Insert Merge Field Placeholders into Active Block:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("First_Name", "Last_Name", "Company", "Email", "City", "AddressBlock", "GreetingLine").forEach { field ->
                            OutlinedButton(
                                onClick = {
                                    viewModel.insertMergeField(field)
                                    onDismiss()
                                }
                            ) {
                                Text("«$field»", fontSize = 12.sp)
                            }
                        }
                    }

                    Divider()

                    Text("2. Preview & Finish Mail Merge with Recipient Records:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Replaces all «Merge_Fields» across the document with the selected recipient's data.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    viewModel.sampleRecipients.forEach { rec ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${rec.firstName} ${rec.lastName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("${rec.company} • ${rec.city} • ${rec.email}", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = {
                                        viewModel.applyMailMergeRecipient(rec)
                                        onDismiss()
                                    }
                                ) {
                                    Text("Merge Record", fontSize = 12.sp)
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
fun WordCraftAccessibilityDialog(
    viewModel: WordCraftViewModel,
    onDismiss: () -> Unit
) {
    val doc by viewModel.document.collectAsState()
    val issues = remember(doc) { DocumentStats.checkAccessibility(doc) }
    var selectedTab by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.82f),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WordCraftPrimary)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Accessibility & Help", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Accessibility Checker & WordCraft v0.7.0 Guide", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Accessibility (${issues.size})") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Shortcuts & v0.7.0") })
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (selectedTab == 0) {
                        if (issues.isEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFEBFBEE))
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Passed", tint = Color(0xFF2B8A3E))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("No Accessibility Issues Found", fontWeight = FontWeight.Bold, color = Color(0xFF2B8A3E))
                                        Text("Headings, document title, table headers, and paragraph lengths all pass inspection.", fontSize = 12.sp)
                                    }
                                }
                            }
                        } else {
                            issues.forEach { issue ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            issue.blockId?.let {
                                                viewModel.setActiveBlockId(it)
                                                onDismiss()
                                            }
                                        },
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9DB))
                                ) {
                                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                                        Icon(Icons.Default.Warning, contentDescription = issue.severity, tint = Color(0xFFE67700))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text("${issue.severity}: ${issue.title}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(issue.description, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        Text("WordCraft Web v0.7.0 Complete Feature Parity", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "This Android edition merges all 12 Ribbon tabs from wordcraft-web-0.7.0 (File/Backstage, Home, Insert, Draw, Design, Layout, References, Mailings, Review, View, Developer, and Help) plus Google Fonts, Unicode Studio, Google Translate, and PDF/Print/DOCX/ODT/RTF/LaTeX/HTML/Markdown export.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Divider()
                        val shortcuts = listOf(
                            "Alt + X" to "Toggle trailing hex codepoint (U+XXXX) ↔ Unicode symbol",
                            "Ctrl + B / I / U" to "Toggle Bold, Italic, or Underline on active block",
                            "Ctrl + Z / Y" to "Undo / Redo up to 40 document states",
                            "Ctrl + F / H" to "Open Find & Replace bar",
                            "Ctrl + P" to "Print or Save as PDF via Android PrintManager"
                        )
                        shortcuts.forEach { (key, desc) ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(key, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = WordCraftPrimary)
                                Text(desc, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

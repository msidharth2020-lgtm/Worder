package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BlockType
import com.example.model.DocumentStats
import com.example.ui.theme.WordCraftPrimary
import com.example.viewmodel.WordCraftViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WordCraftNavigationPane(
    viewModel: WordCraftViewModel,
    onClose: () -> Unit
) {
    val doc by viewModel.document.collectAsState()
    val headings = remember(doc) {
        doc.blocks.filter {
            it.type in listOf(BlockType.TITLE, BlockType.HEADING_1, BlockType.HEADING_2, BlockType.HEADING_3)
        }
    }

    Surface(
        modifier = Modifier
            .width(260.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatListBulleted,
                        contentDescription = "Headings",
                        tint = WordCraftPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Document Outline", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            if (headings.isEmpty()) {
                Text(
                    "No headings found. Add Title or Heading blocks to see them here.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn {
                    items(headings) { h ->
                        val indent = when (h.type) {
                            BlockType.TITLE -> 0.dp
                            BlockType.HEADING_1 -> 8.dp
                            BlockType.HEADING_2 -> 16.dp
                            BlockType.HEADING_3 -> 24.dp
                            else -> 0.dp
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    viewModel.setActiveBlockId(h.id)
                                    onClose()
                                }
                                .padding(vertical = 6.dp, horizontal = 4.dp)
                                .padding(start = indent),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (h.text.isBlank()) "(Untitled heading)" else h.text,
                                fontSize = if (h.type == BlockType.TITLE || h.type == BlockType.HEADING_1) 13.sp else 12.sp,
                                fontWeight = if (h.type == BlockType.TITLE || h.type == BlockType.HEADING_1) FontWeight.Bold else FontWeight.Normal,
                                color = if (h.type == BlockType.TITLE) WordCraftPrimary else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WordCraftCommentsPane(
    viewModel: WordCraftViewModel,
    onClose: () -> Unit
) {
    val doc by viewModel.document.collectAsState()
    val activeBlockId by viewModel.activeBlockId.collectAsState()
    var newCommentText by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .width(280.dp)
            .padding(8.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comments",
                        tint = WordCraftPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Comments & Review", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Add new comment box
            if (activeBlockId != null) {
                OutlinedTextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    placeholder = { Text("Add comment to selected block...", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = {
                        if (newCommentText.isNotBlank()) {
                            viewModel.addComment(activeBlockId!!, "Reviewer", newCommentText.trim())
                            newCommentText = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End),
                    enabled = newCommentText.isNotBlank()
                ) {
                    Text("Post Comment", fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Text(
                    "Click any paragraph or block to add a comment.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Comments list
            if (doc.comments.isEmpty()) {
                Text(
                    "No comments on this document yet.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
                LazyColumn {
                    items(doc.comments) { c ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (c.resolved) Color(0xFFF1F3F5) else Color(0xFFE7F5FF)
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(c.author, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text(
                                        dateFormat.format(Date(c.timestamp)),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    c.text,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { viewModel.resolveComment(c.id) }
                                    ) {
                                        Text(
                                            if (c.resolved) "Re-open" else "Resolve",
                                            fontSize = 11.sp,
                                            color = if (c.resolved) Color.Gray else WordCraftPrimary
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

@Composable
fun WordCraftFindReplaceBar(
    viewModel: WordCraftViewModel,
    onClose: () -> Unit
) {
    val findQuery by viewModel.findQuery.collectAsState()
    val replaceText by viewModel.replaceText.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = WordCraftPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = findQuery,
                    onValueChange = { viewModel.setFindQuery(it) },
                    placeholder = { Text("Find text in document...", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.FindReplace,
                    contentDescription = "Replace",
                    tint = WordCraftPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = replaceText,
                    onValueChange = { viewModel.setReplaceText(it) },
                    placeholder = { Text("Replace with...", fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.replaceAll(findQuery, replaceText) },
                    enabled = findQuery.isNotBlank()
                ) {
                    Text("Replace All", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun WordCraftStatsModal(
    docStats: DocumentStats,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Stats", tint = WordCraftPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Document Statistics")
            }
        },
        text = {
            Column {
                StatItem("Words", "${docStats.words}")
                StatItem("Characters (with spaces)", "${docStats.characters}")
                StatItem("Characters (no spaces)", "${docStats.charactersNoSpaces}")
                StatItem("Paragraphs", "${docStats.paragraphs}")
                StatItem("Estimated Reading Time", String.format("%.1f minutes", docStats.readingTimeMinutes))
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun StatItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

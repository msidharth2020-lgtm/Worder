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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Pageview
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DocumentStats
import com.example.ui.theme.WordCraftPrimary
import com.example.viewmodel.ViewMode
import com.example.viewmodel.WordCraftViewModel

/**
 * WordCraft Status Bar matching Microsoft Word and the Web Deployment UI seen in the video:
 * Displays Page count, Word count, Proofing Language, View Mode switchers, and Zoom Controls.
 */
@Composable
fun WordCraftStatusBar(
    viewModel: WordCraftViewModel,
    modifier: Modifier = Modifier
) {
    val doc by viewModel.document.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val zoomPercent by viewModel.zoomPercent.collectAsState()
    val isDark by viewModel.isDarkCanvas.collectAsState()

    val stats = remember(doc) { DocumentStats.calculate(doc) }

    val barBg = if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val barBorder = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)
    val textColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .testTag("wordcraft_status_bar"),
        color = barBg
    ) {
        Column {
            Divider(color = barBorder, thickness = 1.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(33.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Document Indicators (Page, Words, Language, Proofing)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Page indicator
                    Text(
                        text = "Page 1 of 1",
                        fontSize = 11.sp,
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { viewModel.toggleNavPane() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    // Word count indicator (opens Stats modal on tap)
                    Text(
                        text = "${stats.words} words",
                        fontSize = 11.sp,
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { viewModel.setShowStatsDialog(true) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .testTag("status_bar_word_count")
                    )

                    // Language indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { viewModel.setShowTranslateDialog(true) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Proofing OK",
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "English (United States)",
                            fontSize = 11.sp,
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Right: View Mode toggles & Zoom Stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Read Mode
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (viewMode == ViewMode.READING_MODE) WordCraftPrimary.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { viewModel.setViewMode(ViewMode.READING_MODE) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Read Mode",
                            tint = if (viewMode == ViewMode.READING_MODE) WordCraftPrimary else textColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Print Layout Mode
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (viewMode == ViewMode.PRINT_LAYOUT) WordCraftPrimary.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { viewModel.setViewMode(ViewMode.PRINT_LAYOUT) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Print Layout",
                            tint = if (viewMode == ViewMode.PRINT_LAYOUT) WordCraftPrimary else textColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    // Web Layout Mode
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (viewMode == ViewMode.WEB_LAYOUT) WordCraftPrimary.copy(alpha = 0.2f) else Color.Transparent)
                            .clickable { viewModel.setViewMode(ViewMode.WEB_LAYOUT) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pageview,
                            contentDescription = "Web Layout",
                            tint = if (viewMode == ViewMode.WEB_LAYOUT) WordCraftPrimary else textColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Zoom Out button (-)
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .clickable { viewModel.setZoomPercent((zoomPercent - 10).coerceAtLeast(50)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Zoom Out",
                            tint = textColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }

                    // Zoom Percentage badge
                    Text(
                        text = "$zoomPercent%",
                        fontSize = 11.sp,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { viewModel.setZoomPercent(100) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )

                    // Zoom In button (+)
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .clickable { viewModel.setZoomPercent((zoomPercent + 10).coerceAtMost(200)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zoom In",
                            tint = textColor,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

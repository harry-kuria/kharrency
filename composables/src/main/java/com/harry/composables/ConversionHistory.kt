package com.harry.composables

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.harry.model.ConversionRecord
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ConversionHistory(
    conversions: List<ConversionRecord>,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "History",
                tint = Color(0xFF8B5CF6),
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Recent Conversions",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = if (isDarkMode) Color.White else Color(0xFF374151)
                )
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (conversions.isEmpty()) {
            // Empty State
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF1F2937) else Color(0xFFF3F4F6)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "No conversions",
                        tint = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color(0xFF6B7280),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No conversion history yet",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (isDarkMode) Color.White else Color(0xFF374151)
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Start converting currencies to see your history here",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color(0xFF6B7280)
                        )
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(conversions) { conversion ->
                    ConversionHistoryItem(conversion, isDarkMode)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ConversionHistoryItem(conversion: ConversionRecord, isDarkMode: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF1F2937) else Color(0xFFF3F4F6)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Currency pair and amount
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Currency pair badge
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isDarkMode) Color(0xFF2D1B69) else Color(0xFFEDE9FE),
                                shape = RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${conversion.fromCurrency}→${conversion.toCurrency}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color(0xFF8B5CF6)
                            )
                        )
                    }
                }
                
                Text(
                    text = "${String.format("%.2f", conversion.amount)} ${conversion.fromCurrency}",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (isDarkMode) Color.White else Color(0xFF374151)
                    )
                )
                
                Text(
                    text = conversion.timestamp.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm")),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color(0xFF6B7280)
                    )
                )
            }
            
            // Right side - Result
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${String.format("%.2f", conversion.result)} ${conversion.toCurrency}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF10B981)
                    )
                )
                
                Text(
                    text = "Rate: ${String.format("%.4f", conversion.result / conversion.amount)}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color(0xFF6B7280)
                    )
                )
            }
        }
    }
} 
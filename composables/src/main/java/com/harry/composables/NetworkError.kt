package com.harry.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun NetworkError(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF7F1D1D) else Color(0xFFFEF2F2)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = "Error",
                tint = if (isDarkMode) Color(0xFFFCA5A5) else Color(0xFFDC2626),
                modifier = Modifier.size(24.dp)
            )
            
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Network Error",
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = if (isDarkMode) Color(0xFFFCA5A5) else Color(0xFFDC2626)
            )
                )
            Text(
                text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isDarkMode) Color.White.copy(alpha = 0.9f) else Color(0xFF7F1D1D)
                    )
            )
            }
            
            FilledTonalButton(
                onClick = onRetry,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFFDC2626),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Retry",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "Retry",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = Color.White
                    )
                )
            }
        }
    }
} 
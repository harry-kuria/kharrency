package com.harry.composables

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.harry.model.UpdateInfo

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    isDarkMode: Boolean = false
) {
    val context = LocalContext.current
    
    Dialog(
        onDismissRequest = { if (!updateInfo.isForceUpdate) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !updateInfo.isForceUpdate,
            dismissOnClickOutside = !updateInfo.isForceUpdate
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDarkMode) Color(0xFF1A1B3A) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (updateInfo.isForceUpdate) Icons.Default.Info else Icons.Default.Refresh,
                        contentDescription = "Update",
                        tint = if (updateInfo.isForceUpdate) Color(0xFFEF4444) else Color(0xFF8B5CF6),
                        modifier = Modifier.size(32.dp)
                    )
                    
                    Column {
                        Text(
                            text = if (updateInfo.isForceUpdate) "Required Update" else "Update Available",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                color = if (isDarkMode) Color.White else Color(0xFF374151),
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "Version ${updateInfo.latestVersion}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color(0xFF6B7280)
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Update Info
                UpdateInfoCard(updateInfo, isDarkMode)
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Release Notes
                if (updateInfo.releaseNotes.isNotEmpty()) {
                    Text(
                        text = "What's New:",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (isDarkMode) Color.White else Color(0xFF374151),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkMode) Color(0xFF1F2937) else Color(0xFFF9FAFB)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        ) {
                            Text(
                                text = updateInfo.releaseNotes,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = if (isDarkMode) Color.White.copy(alpha = 0.9f) else Color(0xFF374151)
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                }
                
                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (updateInfo.isForceUpdate) {
                        Arrangement.Center
                    } else {
                        Arrangement.spacedBy(12.dp)
                    }
                ) {
                    if (!updateInfo.isForceUpdate) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (isDarkMode) Color.White else Color(0xFF374151)
                            ),
                            border = BorderStroke(
                                1.dp, 
                                if (isDarkMode) Color.White.copy(alpha = 0.3f) else Color(0xFFE5E7EB)
                            )
                        ) {
                            Text("Later")
                        }
                    }
                    
                    Button(
                        onClick = {
                            downloadUpdate(context, updateInfo.downloadUrl)
                            onDownload()
                        },
                        modifier = if (updateInfo.isForceUpdate) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier.weight(1f)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (updateInfo.isForceUpdate) Color(0xFFEF4444) else Color(0xFF8B5CF6),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (updateInfo.isForceUpdate) "Update Now" else "Download",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
                
                if (updateInfo.isForceUpdate) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "This update is required to continue using the app.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFEF4444)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun UpdateInfoCard(updateInfo: UpdateInfo, isDarkMode: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) Color(0xFF2D1B69) else Color(0xFFEDE9FE)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UpdateInfoItem(
                label = "Version",
                value = updateInfo.latestVersion,
                isDarkMode = isDarkMode
            )
            UpdateInfoItem(
                label = "Release Date",
                value = updateInfo.releaseDate,
                isDarkMode = isDarkMode
            )
            UpdateInfoItem(
                label = "File Size",
                value = updateInfo.fileSize,
                isDarkMode = isDarkMode
            )
        }
    }
}

@Composable
private fun UpdateInfoItem(label: String, value: String, isDarkMode: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color(0xFF6B46C1)
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (isDarkMode) Color.White else Color(0xFF4C1D95),
                fontWeight = FontWeight.Medium
            )
        )
    }
}

private fun downloadUpdate(context: Context, downloadUrl: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        // Handle error - could show a toast or fallback
    }
} 
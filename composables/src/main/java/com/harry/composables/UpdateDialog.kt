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
import androidx.hilt.navigation.compose.hiltViewModel

import com.harry.model.UpdateInfo
import com.harry.repository.ApkDownloadService
import com.harry.repository.DownloadProgress
import com.harry.repository.InstallationResult
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class InstallationState {
    object Installing : InstallationState()
    object Success : InstallationState()
    data class Error(val message: String) : InstallationState()
    data class ConflictDetected(val message: String) : InstallationState()
}

@Composable
fun UpdateDialog(
    updateInfo: UpdateInfo,
    onDismiss: () -> Unit,
    isDarkMode: Boolean,
    downloadService: ApkDownloadService
) {
    var downloadProgress by remember { mutableStateOf<DownloadProgress?>(null) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadComplete by remember { mutableStateOf(false) }
    var installationState by remember { mutableStateOf<InstallationState?>(null) }
    var shouldStartInstallation by remember { mutableStateOf(false) }
    
    val startDownload = {
        isDownloading = true
        downloadComplete = false
        downloadProgress = null
        installationState = null
    }
    
    // Handle installation when triggered
    if (shouldStartInstallation) {
        LaunchedEffect(shouldStartInstallation) {
            val fileName = "Kharrency-v${updateInfo.latestVersion}.apk"
            downloadService.installWithConflictResolution(fileName).collect { result ->
                installationState = when (result) {
                    is InstallationResult.Installing -> InstallationState.Installing
                    is InstallationResult.Success -> InstallationState.Success
                    is InstallationResult.Error -> InstallationState.Error(result.message)
                    is InstallationResult.ConflictDetected -> InstallationState.ConflictDetected(result.message)
                }
            }
            shouldStartInstallation = false
        }
    }
    
    // Collect download progress
    if (isDownloading && !downloadComplete) {
        LaunchedEffect(updateInfo.downloadUrl) {
            val fileName = "Kharrency-v${updateInfo.latestVersion}.apk"
            downloadService.downloadApk(updateInfo.downloadUrl, fileName).collectLatest { progress ->
                downloadProgress = progress
                if (progress.isComplete && progress.error == null) {
                    downloadComplete = true
                    isDownloading = false
                }
                if (progress.error != null) {
                    isDownloading = false
                }
            }
        }
    }
    
    Dialog(
        onDismissRequest = { if (!updateInfo.isForceUpdate && !isDownloading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !updateInfo.isForceUpdate && !isDownloading,
            dismissOnClickOutside = !updateInfo.isForceUpdate && !isDownloading
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
                
                // Download Progress Section
                if (isDownloading || downloadComplete) {
                    DownloadProgressSection(
                        downloadProgress = downloadProgress,
                        isDownloading = isDownloading,
                        downloadComplete = downloadComplete,
                        isDarkMode = isDarkMode,
                        downloadService = downloadService,
                        updateInfo = updateInfo,
                        installationState = installationState,
                        onStartInstallation = { shouldStartInstallation = true }
                    )
                } else {
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
                                startDownload()
                        },
                            enabled = !isDownloading,
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
                                text = if (updateInfo.isForceUpdate) "📥 Update Now" else "📥 Download",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        }
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

@Composable
private fun DownloadProgressSection(
    downloadProgress: DownloadProgress?,
    isDownloading: Boolean,
    downloadComplete: Boolean,
    isDarkMode: Boolean,
    downloadService: ApkDownloadService,
    updateInfo: UpdateInfo,
    installationState: InstallationState?,
    onStartInstallation: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (downloadProgress?.error != null) {
            // Error state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Download failed: ${downloadProgress.error}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFEF4444)
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else if (downloadComplete) {
            // Download complete - Show install button
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "✅ Download Complete!",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color(0xFF10B981),
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onStartInstallation,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = installationState !is InstallationState.Installing
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Install",
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = when (installationState) {
                                        is InstallationState.Installing -> "Installing..."
                                        else -> "Install Update"
                                    },
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            }
                        }
                        
                        // Helpful info card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFF3B82F6).copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "💡 If installation fails with 'package conflicts', uninstall the current app first from Settings → Apps → Kharrency → Uninstall",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color(0xFF1E40AF)
                                ),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        
                        // Installation status and conflict resolution
                        installationState?.let { state ->
                            when (state) {
                                is InstallationState.ConflictDetected -> {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = "⚠️ Package Conflict Detected",
                                                style = MaterialTheme.typography.titleSmall.copy(
                                                    color = Color(0xFFEF4444),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                            Text(
                                                text = state.message,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFFEF4444)
                                                )
                                            )
                                        }
                                    }
                                    
                                    OutlinedButton(
                                        onClick = { 
                                            downloadService.uninstallCurrentApp()
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color(0xFFEF4444)
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = "🗑️ Uninstall Current App",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                                is InstallationState.Error -> {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFFEF4444).copy(alpha = 0.1f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "❌ Installation failed: ${state.message}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFFEF4444)
                                            ),
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                                is InstallationState.Success -> {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = "🎉 Installation successful! The app will restart with the new version.",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF10B981)
                                            ),
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                                is InstallationState.Installing -> {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFF3B82F6).copy(alpha = 0.1f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            androidx.compose.material3.CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = Color(0xFF3B82F6)
                                            )
                                            Text(
                                                text = "Installing update...",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = Color(0xFF3B82F6)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Quick uninstall option (only show if no specific conflict detected)
                        if (installationState !is InstallationState.ConflictDetected) {
                            OutlinedButton(
                                onClick = { 
                                    downloadService.uninstallCurrentApp()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFEF4444)
                                ),
                                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "🗑️ Quick Uninstall (if conflicts occur)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } else if (isDownloading && downloadProgress != null) {
            // Download in progress
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF1F2937) else Color(0xFFF9FAFB)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Downloading...",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = if (isDarkMode) Color.White else Color(0xFF374151),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = "${downloadProgress.percentage}%",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color(0xFF6B7280)
                            )
                        )
                    }
                    
                    LinearProgressIndicator(
                        progress = downloadProgress.percentage / 100f,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF8B5CF6),
                        trackColor = if (isDarkMode) Color.White.copy(alpha = 0.1f) else Color(0xFFE5E7EB)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = downloadProgress.bytesDownloaded.let { bytes ->
                                when {
                                    bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
                                    bytes >= 1024 -> "${bytes / 1024} KB"
                                    else -> "$bytes bytes"
                                }
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color(0xFF6B7280)
                            )
                        )
                        Text(
                            text = downloadProgress.totalBytes.let { bytes ->
                                when {
                                    bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
                                    bytes >= 1024 -> "${bytes / 1024} KB"
                                    else -> "$bytes bytes"
                                }
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isDarkMode) Color.White.copy(alpha = 0.7f) else Color(0xFF6B7280)
                            )
                        )
                    }
                }
            }
        }
    }
} 
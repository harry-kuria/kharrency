package com.harry.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class DownloadProgress(
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val percentage: Int,
    val isComplete: Boolean = false,
    val error: String? = null
)

sealed class InstallationResult {
    object Installing : InstallationResult()
    object Success : InstallationResult()
    data class Error(val message: String) : InstallationResult()
    data class ConflictDetected(val message: String) : InstallationResult()
}

@Singleton
class ApkDownloadService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun downloadApk(downloadUrl: String, fileName: String): Flow<DownloadProgress> = flow {
        try {
            android.util.Log.d("ApkDownloadService", "Starting download: $downloadUrl")
            
            val url = URL(downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.setRequestProperty("User-Agent", "Kharrency-UpdateClient/1.0")
            
            val responseCode = connection.responseCode
            android.util.Log.d("ApkDownloadService", "Download response code: $responseCode")
            
            if (responseCode != HttpURLConnection.HTTP_OK) {
                emit(DownloadProgress(0, 0, 0, false, "Download failed: HTTP $responseCode"))
                return@flow
            }
            
            val totalBytes = connection.contentLength.toLong()
            val inputStream = connection.inputStream
            
            // Create updates directory in app's private storage
            val updatesDir = File(context.filesDir, "updates")
            if (!updatesDir.exists()) {
                if (!updatesDir.mkdirs()) {
                    emit(DownloadProgress(0, 0, 0, false, "Failed to create updates directory"))
                    return@flow
                }
            }
            
            val apkFile = File(updatesDir, fileName)
            
            // Remove existing file if it exists
            if (apkFile.exists()) {
                if (!apkFile.delete()) {
                    android.util.Log.w("ApkDownloadService", "Could not delete existing APK file")
                }
            }
            
            val outputStream = FileOutputStream(apkFile)
            
            val buffer = ByteArray(16384) // 16KB buffer for better performance
            var bytesDownloaded = 0L
            var bytesRead: Int
            var lastProgressTime = System.currentTimeMillis()
            
            android.util.Log.d("ApkDownloadService", "Starting download to: ${apkFile.absolutePath}, size: $totalBytes bytes")
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                bytesDownloaded += bytesRead
                
                val percentage = if (totalBytes > 0) {
                    ((bytesDownloaded * 100) / totalBytes).toInt()
                } else {
                    0
                }
                
                // Emit progress updates every 500ms to avoid overwhelming the UI
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastProgressTime > 500 || bytesRead == -1) {
                    emit(DownloadProgress(bytesDownloaded, totalBytes, percentage, false))
                    lastProgressTime = currentTime
                }
            }
            
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            connection.disconnect()
            
            // Verify download completed successfully
            if (apkFile.length() != totalBytes && totalBytes > 0) {
                android.util.Log.e("ApkDownloadService", "Download size mismatch: ${apkFile.length()} vs $totalBytes")
                apkFile.delete()
                emit(DownloadProgress(0, 0, 0, false, "Download incomplete: file size mismatch"))
                return@flow
            }
            
            android.util.Log.d("ApkDownloadService", "Download completed successfully: ${apkFile.length()} bytes")
            
            // Final integrity check
            if (!apkFile.exists() || apkFile.length() == 0L) {
                emit(DownloadProgress(0, 0, 0, false, "Downloaded file is invalid"))
                return@flow
            }
            
            // Download complete
            emit(DownloadProgress(bytesDownloaded, totalBytes, 100, true))
            
        } catch (e: java.io.IOException) {
            android.util.Log.e("ApkDownloadService", "Network error during download", e)
            emit(DownloadProgress(0, 0, 0, false, "Network error: ${e.message}"))
        } catch (e: Exception) {
            android.util.Log.e("ApkDownloadService", "Download error", e)
            emit(DownloadProgress(0, 0, 0, false, "Download error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    suspend fun installApk(fileName: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val updatesDir = File(context.filesDir, "updates")
                val apkFile = File(updatesDir, fileName)
                
                if (!apkFile.exists()) {
                    android.util.Log.e("ApkDownloadService", "APK file not found: ${apkFile.absolutePath}")
                    return@withContext false
                }
                
                // Verify APK integrity
                if (!verifyApkIntegrity(apkFile)) {
                    android.util.Log.e("ApkDownloadService", "APK integrity check failed")
                    return@withContext false
                }
                
                // Check if we can install unknown apps (Android 8.0+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val packageManager = context.packageManager
                    if (!packageManager.canRequestPackageInstalls()) {
                        android.util.Log.w("ApkDownloadService", "No permission to install unknown apps")
                        withContext(Dispatchers.Main) {
                            requestInstallPermission()
                        }
                        return@withContext false
                    }
                }
                
                // Use proper installation method based on Android version
                withContext(Dispatchers.Main) {
                    installApkWithIntent(apkFile)
                }
                
                return@withContext true
                
            } catch (e: Exception) {
                android.util.Log.e("ApkDownloadService", "Installation failed", e)
                return@withContext false
            }
        }
    }
    
    private fun verifyApkIntegrity(apkFile: File): Boolean {
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageArchiveInfo(
                apkFile.absolutePath, 
                android.content.pm.PackageManager.GET_META_DATA
            )
            
            // Basic checks
            if (packageInfo == null) {
                android.util.Log.e("ApkDownloadService", "Invalid APK: Cannot parse package info")
                return false
            }
            
            if (packageInfo.packageName != context.packageName) {
                android.util.Log.e("ApkDownloadService", "Package name mismatch: ${packageInfo.packageName} vs ${context.packageName}")
                return false
            }
            
            // Check file size (should be reasonable for an app update)
            val fileSize = apkFile.length()
            if (fileSize < 1024 * 1024 || fileSize > 500 * 1024 * 1024) { // 1MB to 500MB
                android.util.Log.e("ApkDownloadService", "Suspicious file size: $fileSize bytes")
                return false
            }
            
            android.util.Log.d("ApkDownloadService", "APK integrity verified: ${packageInfo.packageName} v${packageInfo.versionName}")
            true
        } catch (e: Exception) {
            android.util.Log.e("ApkDownloadService", "APK integrity check failed", e)
            false
        }
    }
    
    private fun requestInstallPermission() {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("ApkDownloadService", "Failed to request install permission", e)
        }
    }
    
    private fun installApkWithIntent(apkFile: File) {
        try {
            // Create URI for the APK file using FileProvider
            val apkUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            
            // Create install intent with proper flags
            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                
                // Add additional flags for better compatibility
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                }
            }
            
            // Verify intent can be handled
            val packageManager = context.packageManager
            val resolveInfo = packageManager.resolveActivity(installIntent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
            
            if (resolveInfo != null) {
                android.util.Log.d("ApkDownloadService", "Starting APK installation")
                context.startActivity(installIntent)
            } else {
                android.util.Log.e("ApkDownloadService", "No app can handle APK installation intent")
                // Fallback: try with package installer
                installWithPackageInstaller(apkUri)
            }
            
        } catch (e: Exception) {
            android.util.Log.e("ApkDownloadService", "Failed to install APK with intent", e)
        }
    }
    
    private fun installWithPackageInstaller(apkUri: Uri) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val intent = Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                    data = apkUri
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                           Intent.FLAG_GRANT_READ_URI_PERMISSION
                    putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true)
                    putExtra(Intent.EXTRA_RETURN_RESULT, true)
                    putExtra(Intent.EXTRA_INSTALLER_PACKAGE_NAME, context.packageName)
                }
                context.startActivity(intent)
                android.util.Log.d("ApkDownloadService", "Started installation with package installer")
            } else {
                // Fallback for older Android versions
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            android.util.Log.e("ApkDownloadService", "Package installer fallback failed", e)
        }
    }
    
    private fun hasPackageConflict(apkFile: File): Boolean {
        try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageArchiveInfo(apkFile.absolutePath, 0)
            val newPackageName = packageInfo?.packageName ?: return false
            
            // Check if app with same package name but different signature exists
            try {
                val existingInfo = packageManager.getPackageInfo(newPackageName, 0)
                val existingSignature = getPackageSignature(existingInfo)
                val newSignature = getApkSignature(apkFile)
                
                return existingSignature != null && newSignature != null && existingSignature != newSignature
            } catch (e: Exception) {
                // Package not installed, no conflict
                return false
            }
        } catch (e: Exception) {
            return false
        }
    }
    
    private fun getPackageSignature(packageInfo: android.content.pm.PackageInfo): String? {
        return try {
            val packageManager = context.packageManager
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageManager.getPackageInfo(packageInfo.packageName, android.content.pm.PackageManager.GET_SIGNING_CERTIFICATES).signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageInfo.packageName, android.content.pm.PackageManager.GET_SIGNATURES).signatures
            }
            signatures?.firstOrNull()?.toCharsString()
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getApkSignature(apkFile: File): String? {
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageArchiveInfo(apkFile.absolutePath, android.content.pm.PackageManager.GET_SIGNATURES)
            @Suppress("DEPRECATION")
            packageInfo?.signatures?.firstOrNull()?.toCharsString()
        } catch (e: Exception) {
            null
        }
    }
    
    private fun showConflictResolutionDialog() {
        // This will be handled by the UI layer - we'll emit a conflict event
        // The UpdateDialog can then show appropriate options
    }
    
    fun installWithConflictResolution(fileName: String): Flow<InstallationResult> = flow {
        try {
            val updatesDir = File(context.filesDir, "updates")
            val apkFile = File(updatesDir, fileName)
            
            if (!apkFile.exists()) {
                emit(InstallationResult.Error("APK file not found"))
                return@flow
            }
            
            // Check for conflicts
            if (hasPackageConflict(apkFile)) {
                emit(InstallationResult.ConflictDetected("Package signing conflict detected. Please uninstall the current version first."))
                return@flow
            }
            
            // Proceed with installation
            emit(InstallationResult.Installing)
            val success = installApk(fileName)
            
            if (success) {
                emit(InstallationResult.Success)
            } else {
                emit(InstallationResult.Error("Installation failed"))
            }
        } catch (e: Exception) {
            emit(InstallationResult.Error("Installation error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    fun uninstallCurrentApp() {
        try {
            val packageName = context.packageName
            val uninstallIntent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(uninstallIntent)
        } catch (e: Exception) {
            // Handle error
        }
    }
    
    fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            bytes >= 1024 -> "${bytes / 1024} KB"
            else -> "$bytes bytes"
        }
    }
} 
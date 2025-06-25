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

@Singleton
class ApkDownloadService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    fun downloadApk(downloadUrl: String, fileName: String): Flow<DownloadProgress> = flow {
        try {
            val url = URL(downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            
            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                emit(DownloadProgress(0, 0, 0, false, "Download failed: HTTP $responseCode"))
                return@flow
            }
            
            val totalBytes = connection.contentLength.toLong()
            val inputStream = connection.inputStream
            
            // Create updates directory in app's private storage
            val updatesDir = File(context.filesDir, "updates")
            if (!updatesDir.exists()) {
                updatesDir.mkdirs()
            }
            
            val apkFile = File(updatesDir, fileName)
            val outputStream = FileOutputStream(apkFile)
            
            val buffer = ByteArray(8192)
            var bytesDownloaded = 0L
            var bytesRead: Int
            
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                bytesDownloaded += bytesRead
                
                val percentage = if (totalBytes > 0) {
                    ((bytesDownloaded * 100) / totalBytes).toInt()
                } else {
                    0
                }
                
                emit(DownloadProgress(bytesDownloaded, totalBytes, percentage, false))
            }
            
            outputStream.close()
            inputStream.close()
            connection.disconnect()
            
            // Download complete
            emit(DownloadProgress(bytesDownloaded, totalBytes, 100, true))
            
        } catch (e: Exception) {
            emit(DownloadProgress(0, 0, 0, false, "Download error: ${e.message}"))
        }
    }.flowOn(Dispatchers.IO)
    
    suspend fun installApk(fileName: String): Boolean {
        return withContext(Dispatchers.Main) {
            try {
                val updatesDir = File(context.filesDir, "updates")
                val apkFile = File(updatesDir, fileName)
                
                if (!apkFile.exists()) {
                    return@withContext false
                }
                
                // Check if we can install unknown apps (Android 8.0+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val packageManager = context.packageManager
                    if (!packageManager.canRequestPackageInstalls()) {
                        // Request permission to install unknown apps
                        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        intent.data = Uri.parse("package:${context.packageName}")
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        context.startActivity(intent)
                        return@withContext false
                    }
                }
                
                // Create URI for the APK file using FileProvider
                val apkUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    apkFile
                )
                
                // Create install intent
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(apkUri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                
                // For debug builds, add additional flags to help with installation
                if (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0) {
                    installIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                }
                
                context.startActivity(installIntent)
                return@withContext true
                
            } catch (e: Exception) {
                return@withContext false
            }
        }
    }
    
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
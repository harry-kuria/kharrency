package com.harry.repository

import com.harry.model.UpdateInfo
import com.harry.model.UpdateCheckResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateService @Inject constructor() {
    
    private val githubApiUrl = "https://api.github.com/repos/harry-kuria/kharrency/releases/latest"
    
    suspend fun checkForUpdates(currentVersionCode: Int): UpdateCheckResponse {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(githubApiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()
                    
                    parseUpdateResponse(response, currentVersionCode)
                } else {
                    UpdateCheckResponse(
                        hasUpdate = false,
                        error = "Failed to check for updates: HTTP $responseCode"
                    )
                }
            } catch (e: Exception) {
                UpdateCheckResponse(
                    hasUpdate = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }
    
    private fun parseUpdateResponse(response: String, currentVersionCode: Int): UpdateCheckResponse {
        return try {
            val json = JSONObject(response)
            val tagName = json.getString("tag_name") // e.g., "v1.0.0"
            val versionName = tagName.removePrefix("v")
            val versionCode = extractVersionCode(json.getString("body"))
            val downloadUrl = getApkDownloadUrl(json)
            val releaseNotes = extractReleaseNotes(json.getString("body"))
            val releaseDate = json.getString("published_at")
            val fileSize = getApkFileSize(json)
            
            val hasUpdate = versionCode > currentVersionCode
            
            if (hasUpdate) {
                val updateInfo = UpdateInfo(
                    latestVersion = versionName,
                    latestVersionCode = versionCode,
                    downloadUrl = downloadUrl,
                    releaseNotes = releaseNotes,
                    isForceUpdate = isForceUpdate(currentVersionCode, versionCode),
                    minSupportedVersion = 1,
                    releaseDate = formatReleaseDate(releaseDate),
                    fileSize = fileSize
                )
                UpdateCheckResponse(hasUpdate = true, updateInfo = updateInfo)
            } else {
                UpdateCheckResponse(hasUpdate = false)
            }
        } catch (e: Exception) {
            UpdateCheckResponse(
                hasUpdate = false,
                error = "Failed to parse update info: ${e.message}"
            )
        }
    }
    
    private fun extractVersionCode(body: String): Int {
        // Extract version code from release body
        val regex = "Version Code:\\s*(\\d+)".toRegex()
        val match = regex.find(body)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 1
    }
    
    private fun getApkDownloadUrl(json: JSONObject): String {
        val assets = json.getJSONArray("assets")
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.getString("name")
            if (name.endsWith(".apk")) {
                return asset.getString("browser_download_url")
            }
        }
        return ""
    }
    
    private fun extractReleaseNotes(body: String): String {
        // Extract clean release notes (remove technical details)
        val lines = body.split("\n")
        val notesStart = lines.indexOfFirst { it.contains("### Features:") || it.contains("## ") }
        val notesEnd = lines.indexOfFirst { it.contains("### Technical Details:") }
        
        return if (notesStart != -1 && notesEnd != -1) {
            lines.subList(notesStart, notesEnd).joinToString("\n").trim()
        } else {
            body.split("### Technical Details:")[0].trim()
        }
    }
    
    private fun getApkFileSize(json: JSONObject): String {
        val assets = json.getJSONArray("assets")
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val name = asset.getString("name")
            if (name.endsWith(".apk")) {
                val sizeBytes = asset.getLong("size")
                return formatFileSize(sizeBytes)
            }
        }
        return "Unknown"
    }
    
    private fun formatFileSize(bytes: Long): String {
        return when {
            bytes >= 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
            bytes >= 1024 -> "${bytes / 1024} KB"
            else -> "$bytes bytes"
        }
    }
    
    private fun formatReleaseDate(dateString: String): String {
        // Convert ISO date to readable format
        return try {
            val date = java.time.LocalDateTime.parse(dateString.substringBefore("Z"))
            date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"))
        } catch (e: Exception) {
            dateString.substringBefore("T")
        }
    }
    
    private fun isForceUpdate(currentVersionCode: Int, latestVersionCode: Int): Boolean {
        // Force update if the version difference is more than 3 versions
        return (latestVersionCode - currentVersionCode) > 3
    }
} 
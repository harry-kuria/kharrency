package com.harry.repository

import android.util.Log
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
    private val TAG = "UpdateService"
    
    suspend fun checkForUpdates(currentVersionCode: Int): UpdateCheckResponse {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(githubApiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.setRequestProperty("User-Agent", "Kharrency-App")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseCode = connection.responseCode
                Log.d(TAG, "GitHub API response code: $responseCode")
                
                when (responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        val response = reader.readText()
                        reader.close()
                        
                        Log.d(TAG, "GitHub API response length: ${response.length}")
                        parseUpdateResponse(response, currentVersionCode)
                    }
                    HttpURLConnection.HTTP_FORBIDDEN -> {
                        // Rate limit exceeded
                        val rateLimitResetHeader = connection.getHeaderField("X-RateLimit-Reset")
                        val resetTime = rateLimitResetHeader?.toLongOrNull()
                        val resetTimeString = if (resetTime != null) {
                            val resetDate = java.util.Date(resetTime * 1000)
                            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(resetDate)
                        } else {
                            "in a while"
                        }
                        
                        UpdateCheckResponse(
                            hasUpdate = false,
                            error = "Rate limit exceeded. Try again at $resetTimeString"
                        )
                    }
                    HttpURLConnection.HTTP_NOT_FOUND -> {
                        UpdateCheckResponse(
                            hasUpdate = false,
                            error = "Release not found. Check repository settings"
                        )
                    }
                    else -> {
                        UpdateCheckResponse(
                            hasUpdate = false,
                            error = "GitHub API error: HTTP $responseCode"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception in checkForUpdates: ${e.message}")
                UpdateCheckResponse(
                    hasUpdate = false,
                    error = "Network error: ${e.message}"
                )
            }
        }
    }
    
    private fun parseUpdateResponse(response: String, currentVersionCode: Int): UpdateCheckResponse {
        return try {
            Log.d(TAG, "Parsing update response, currentVersionCode: $currentVersionCode")
            val json = JSONObject(response)
            val tagName = json.getString("tag_name") // e.g., "v1.0.0"
            val versionName = tagName.removePrefix("v")
            val body = json.getString("body")
            val versionCode = extractVersionCode(body)
            
            Log.d(TAG, "After extractVersionCode - versionCode: $versionCode")
            
            val downloadUrl = getApkDownloadUrl(json)
            Log.d(TAG, "After getApkDownloadUrl - downloadUrl: $downloadUrl")
            
            val releaseNotes = extractReleaseNotes(body)
            Log.d(TAG, "After extractReleaseNotes - releaseNotes length: ${releaseNotes.length}")
            
            val releaseDate = json.getString("published_at")
            Log.d(TAG, "After getReleaseDate - releaseDate: $releaseDate")
            
            val fileSize = getApkFileSize(json)
            Log.d(TAG, "After getApkFileSize - fileSize: $fileSize")
            
            Log.d(TAG, "Parsed - tagName: $tagName, versionName: $versionName, versionCode: $versionCode")
            Log.d(TAG, "Current vs Latest: $currentVersionCode vs $versionCode")
            
            val hasUpdate = versionCode > currentVersionCode
            Log.d(TAG, "hasUpdate: $hasUpdate")
            
            if (hasUpdate) {
                Log.d(TAG, "Creating UpdateInfo object...")
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
                Log.d(TAG, "UpdateInfo created successfully, returning hasUpdate: true")
                UpdateCheckResponse(hasUpdate = true, updateInfo = updateInfo)
            } else {
                Log.d(TAG, "No update needed, returning hasUpdate: false")
                UpdateCheckResponse(hasUpdate = false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in parseUpdateResponse: ${e.message}")
            e.printStackTrace()
            UpdateCheckResponse(
                hasUpdate = false,
                error = "Failed to parse update info: ${e.message}"
            )
        }
    }
    
    private fun extractVersionCode(body: String): Int {
        // Extract version code from release body - handle markdown formatting
        val regex = "\\*\\*Version Code:\\*\\*\\s*(\\d+)".toRegex()
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
        // Extract "What's New" section for display in update dialog
        val lines = body.split("\n")
        val whatsNewStart = lines.indexOfFirst { it.contains("### What's New:") }
        
        if (whatsNewStart != -1) {
            // Find the end of What's New section (next ### heading or end of content)
            val whatsNewEnd = lines.drop(whatsNewStart + 1).indexOfFirst { 
                it.startsWith("### ") || it.startsWith("## ")
            }
            
            val endIndex = if (whatsNewEnd != -1) {
                whatsNewStart + 1 + whatsNewEnd
            } else {
                lines.size
            }
            
            return lines.subList(whatsNewStart + 1, endIndex)
                .joinToString("\n")
                .trim()
        }
        
        // Fallback: extract features section or first part of body
        val featuresStart = lines.indexOfFirst { it.contains("### Features:") }
        val technicalDetailsStart = lines.indexOfFirst { it.contains("### Technical Details:") }
        
        return if (featuresStart != -1 && technicalDetailsStart != -1) {
            lines.subList(featuresStart, technicalDetailsStart).joinToString("\n").trim()
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
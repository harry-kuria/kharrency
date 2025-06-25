package com.harry.model

data class UpdateInfo(
    val latestVersion: String,
    val latestVersionCode: Int,
    val downloadUrl: String,
    val releaseNotes: String,
    val isForceUpdate: Boolean = false,
    val minSupportedVersion: Int = 1,
    val releaseDate: String,
    val fileSize: String
)

data class UpdateCheckResponse(
    val hasUpdate: Boolean,
    val updateInfo: UpdateInfo? = null,
    val error: String? = null
) 
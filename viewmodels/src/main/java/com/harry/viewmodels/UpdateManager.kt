package com.harry.viewmodels

import android.content.Context
import android.content.pm.PackageManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.harry.model.UpdateCheckResponse
import com.harry.repository.UpdateService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.updateDataStore: DataStore<Preferences> by preferencesDataStore(name = "update_prefs")

@Singleton
class UpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val updateService: UpdateService
) {
    private val dataStore = context.updateDataStore
    
    companion object {
        private val LAST_UPDATE_CHECK = longPreferencesKey("last_update_check")
        private val UPDATE_CHECK_INTERVAL = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
    }
    
    suspend fun checkForUpdates(forceCheck: Boolean = false): UpdateCheckResponse {
        val currentTime = System.currentTimeMillis()
        val lastCheckTime = getLastUpdateCheckTime()
        
        // Check if we should skip the update check (unless forced)
        if (!forceCheck && (currentTime - lastCheckTime) < UPDATE_CHECK_INTERVAL) {
            return UpdateCheckResponse(hasUpdate = false)
        }
        
        val currentVersionCode = getCurrentVersionCode()
        val result = updateService.checkForUpdates(currentVersionCode)
        
        // Save the last check time
        saveLastUpdateCheckTime(currentTime)
        
        return result
    }
    
    private suspend fun getLastUpdateCheckTime(): Long {
        return dataStore.data.map { preferences ->
            preferences[LAST_UPDATE_CHECK] ?: 0L
        }.first()
    }
    
    private suspend fun saveLastUpdateCheckTime(time: Long) {
        dataStore.edit { preferences ->
            preferences[LAST_UPDATE_CHECK] = time
        }
    }
    
    private fun getCurrentVersionCode(): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            1 // Default version code
        }
    }
    
    fun getCurrentVersionName(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0"
        }
    }
    
    // Flow to observe last update check time
    fun getLastUpdateCheckFlow(): Flow<Long> {
        return dataStore.data.map { preferences ->
            preferences[LAST_UPDATE_CHECK] ?: 0L
        }
    }
} 
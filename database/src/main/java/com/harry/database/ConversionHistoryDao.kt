package com.harry.database

import androidx.room.*
import com.harry.model.ConversionHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversionHistoryDao {
    @Query("SELECT * FROM conversion_history ORDER BY timestamp DESC")
    fun getConversionHistory(): Flow<List<ConversionHistory>>

    @Query("SELECT * FROM conversion_history ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentConversions(limit: Int): Flow<List<ConversionHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversion(conversion: ConversionHistory): Long

    @Query("DELETE FROM conversion_history WHERE timestamp < :timestamp")
    suspend fun deleteOldConversions(timestamp: Long): Int

    @Query("DELETE FROM conversion_history")
    suspend fun deleteAllConversions()
} 
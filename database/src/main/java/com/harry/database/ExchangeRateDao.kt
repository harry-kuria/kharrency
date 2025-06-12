package com.harry.database

import androidx.room.*
import com.harry.model.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {
    @Query("SELECT * FROM exchange_rates ORDER BY timestamp DESC")
    fun getExchangeRates(): Flow<List<ExchangeRateEntity>>

    @Query("SELECT * FROM exchange_rates WHERE base = :base ORDER BY timestamp DESC LIMIT 1")
    fun getLatestExchangeRate(base: String): Flow<ExchangeRateEntity?>

    @Query("SELECT * FROM exchange_rates WHERE base = :base AND timestamp BETWEEN :startTime AND :endTime ORDER BY timestamp DESC")
    fun getExchangeRatesByDateRange(base: String, startTime: Long, endTime: Long): Flow<List<ExchangeRateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeRate(exchangeRate: ExchangeRateEntity): Long

    @Query("DELETE FROM exchange_rates WHERE timestamp < :timestamp")
    suspend fun deleteOldRates(timestamp: Long): Int

    @Query("DELETE FROM exchange_rates")
    suspend fun deleteAllRates()
} 
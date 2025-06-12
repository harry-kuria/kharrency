package com.harry.database

import androidx.room.*
import com.harry.model.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface ExchangeRateDao {
    @Query("SELECT * FROM exchange_rates WHERE baseCurrency = :baseCurrency")
    fun getExchangeRates(baseCurrency: String): Flow<ExchangeRateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeRates(exchangeRate: ExchangeRateEntity)

    @Query("DELETE FROM exchange_rates WHERE timestamp < :timestamp")
    suspend fun deleteOldRates(timestamp: LocalDateTime)
} 
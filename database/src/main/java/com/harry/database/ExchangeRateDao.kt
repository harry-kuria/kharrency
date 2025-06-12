package com.harry.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.harry.model.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExchangeRateDao {
    @Query("SELECT * FROM exchange_rates ORDER BY timestamp DESC")
    fun getExchangeRates(): Flow<List<ExchangeRateEntity>>

    @Query("SELECT * FROM exchange_rates WHERE base = :base ORDER BY timestamp DESC LIMIT 1")
    fun getLatestExchangeRate(base: String): Flow<ExchangeRateEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExchangeRate(exchangeRate: ExchangeRateEntity): Long
} 
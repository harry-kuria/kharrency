package com.harry.repository

import com.harry.database.ExchangeRateDao
import com.harry.model.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.ZoneOffset

class FakeExchangeRateDao : ExchangeRateDao {
    private val rates = mutableListOf<ExchangeRateEntity>()
    private val ratesFlow = MutableStateFlow<List<ExchangeRateEntity>>(emptyList())

    override fun getExchangeRates(): Flow<List<ExchangeRateEntity>> = ratesFlow

    override fun getLatestExchangeRate(base: String): Flow<ExchangeRateEntity?> {
        return MutableStateFlow(rates.findLast { it.base == base })
    }

    override suspend fun insertExchangeRate(exchangeRate: ExchangeRateEntity) {
        rates.add(exchangeRate)
        ratesFlow.value = rates.toList()
    }

    override suspend fun deleteOldRates(timestamp: Long) {
        val oldRates = rates.filter { it.timestamp.toEpochSecond(ZoneOffset.UTC) < timestamp }
        rates.removeAll(oldRates)
        ratesFlow.value = rates.toList()
    }
} 
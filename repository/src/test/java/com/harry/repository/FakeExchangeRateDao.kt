package com.harry.repository

import com.harry.database.ExchangeRateDao
import com.harry.model.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeExchangeRateDao : ExchangeRateDao {
    private val rates = mutableListOf<ExchangeRateEntity>()
    private val ratesFlow = MutableStateFlow<List<ExchangeRateEntity>>(emptyList())

    override fun getExchangeRates(): Flow<List<ExchangeRateEntity>> = ratesFlow

    override fun getLatestExchangeRate(base: String): Flow<ExchangeRateEntity?> {
        return MutableStateFlow(rates.findLast { it.base == base })
    }

    override fun getExchangeRatesByDateRange(base: String, startTime: Long, endTime: Long): Flow<List<ExchangeRateEntity>> {
        return MutableStateFlow(rates.filter { it.base == base && it.timestamp in startTime..endTime })
    }

    override suspend fun insertExchangeRate(exchangeRate: ExchangeRateEntity): Long {
        rates.add(exchangeRate)
        ratesFlow.value = rates.toList()
        return rates.size.toLong()
    }

    override suspend fun deleteOldRates(timestamp: Long): Int {
        val oldRates = rates.filter { it.timestamp < timestamp }
        rates.removeAll(oldRates)
        ratesFlow.value = rates.toList()
        return oldRates.size
    }

    override suspend fun deleteAllRates() {
        rates.clear()
        ratesFlow.value = emptyList()
    }
} 
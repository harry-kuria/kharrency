package com.harry.repository

import com.harry.model.ExchangeRateEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

/**
 * Simple test class that only tests the DAO wrapper methods, not the complex API methods
 */
class CurrencyRepositoryTest {
    private lateinit var exchangeRateDao: FakeExchangeRateDao
    private lateinit var conversionHistoryDao: FakeConversionHistoryDao

    @Before
    fun setup() {
        exchangeRateDao = FakeExchangeRateDao()
        conversionHistoryDao = FakeConversionHistoryDao()
    }

    @Test
    fun `getLatestExchangeRate returns latest rate for base currency`() = runBlocking {
        val now = System.currentTimeMillis()
        val rate = ExchangeRateEntity(
            base = "USD",
            timestamp = now,
            rates = mapOf("EUR" to 0.85, "JPY" to 110.0)
        )
        exchangeRateDao.insertExchangeRate(rate)

        val result = exchangeRateDao.getLatestExchangeRate("USD").first()
        assertEquals(rate, result)
    }

    @Test
    fun `getLatestExchangeRate returns null when no rate exists`() = runBlocking {
        val result = exchangeRateDao.getLatestExchangeRate("USD").first()
        assertNull(result)
    }

    @Test
    fun `insertExchangeRate stores rate successfully`() = runBlocking {
        val now = System.currentTimeMillis()
        val rate = ExchangeRateEntity(
            base = "EUR",
            timestamp = now,
            rates = mapOf("USD" to 1.18, "JPY" to 130.0)
        )
        
        exchangeRateDao.insertExchangeRate(rate)
        val result = exchangeRateDao.getLatestExchangeRate("EUR").first()
        assertEquals(rate, result)
    }
} 
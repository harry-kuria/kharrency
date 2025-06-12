package com.harry.repository

import com.harry.model.ExchangeRateEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime

class CurrencyRepositoryTest {
    private lateinit var repository: CurrencyRepository
    private lateinit var dao: FakeExchangeRateDao

    @Before
    fun setup() {
        dao = FakeExchangeRateDao()
        repository = CurrencyRepository(dao)
    }

    @Test
    fun `getLatestExchangeRate returns latest rate for base currency`() = runBlocking {
        val now = LocalDateTime.now()
        val rate = ExchangeRateEntity(
            base = "USD",
            timestamp = now,
            rates = mapOf("EUR" to 0.85, "JPY" to 110.0)
        )
        dao.insertExchangeRate(rate)

        val result = repository.getLatestExchangeRate("USD").first()
        assertEquals(rate, result)
    }

    @Test
    fun `getLatestExchangeRate returns null when no rate exists`() = runBlocking {
        val result = repository.getLatestExchangeRate("USD").first()
        assertNull(result)
    }

    @Test
    fun `deleteOldRates returns number of deleted rows`() = runBlocking {
        val now = LocalDateTime.now()
        val oldRate = ExchangeRateEntity(
            base = "USD",
            timestamp = now.minusDays(2),
            rates = mapOf("EUR" to 0.85)
        )
        dao.insertExchangeRate(oldRate)

        val deletedCount = repository.deleteOldRates(now.minusDays(1))
        assertEquals(1, deletedCount)
    }
} 
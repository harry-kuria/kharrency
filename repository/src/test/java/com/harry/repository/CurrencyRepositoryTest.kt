package com.harry.repository

import com.harry.model.AppDatabase
import com.harry.model.ExchangeRateDao
import com.harry.model.ExchangeRateEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CurrencyRepositoryTest {
    private val database: AppDatabase = mock()
    private val dao: ExchangeRateDao = mock()
    private lateinit var repository: CurrencyRepository

    @Before
    fun setup() {
        whenever(database.exchangeRateDao()).thenReturn(dao)
        repository = CurrencyRepository(database)
    }

    @Test
    fun `convertCurrency uses cached rates when available and fresh`() = runBlocking {
        val cachedRates = mapOf("EUR" to 0.85)
        val cachedEntity = ExchangeRateEntity(
            baseCurrency = "USD",
            rates = cachedRates,
            timestamp = LocalDateTime.now()
        )
        whenever(dao.getExchangeRates("USD")).thenReturn(flowOf(cachedEntity))

        val result = repository.convertCurrency(100.0, "USD", "EUR")
        assertEquals(85.0, result)
    }

    @Test
    fun `convertCurrency throws exception for unsupported currency`() = runBlocking {
        val cachedRates = mapOf("EUR" to 0.85)
        val cachedEntity = ExchangeRateEntity(
            baseCurrency = "USD",
            rates = cachedRates,
            timestamp = LocalDateTime.now()
        )
        whenever(dao.getExchangeRates("USD")).thenReturn(flowOf(cachedEntity))

        assertFailsWith<Exception> {
            repository.convertCurrency(100.0, "USD", "JPY")
        }
    }

    @Test
    fun `cleanupOldRates deletes rates older than one day`() = runBlocking {
        repository.cleanupOldRates()
        verify(dao).deleteOldRates(org.mockito.kotlin.any())
    }
} 
package com.harry.viewmodels

import com.harry.repository.CurrencyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {
    private val testDispatcher = StandardTestDispatcher()
    private val repository: CurrencyRepository = mock()
    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = DashboardViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateAmount updates state correctly`() = runTest {
        viewModel.updateAmount("100.50")
        assertEquals("100.50", viewModel.state.value.amount)
    }

    @Test
    fun `swapCurrencies swaps currencies correctly`() = runTest {
        viewModel.updateFromCurrency("USD")
        viewModel.updateToCurrency("EUR")
        viewModel.swapCurrencies()
        assertEquals("EUR", viewModel.state.value.fromCurrency)
        assertEquals("USD", viewModel.state.value.toCurrency)
    }

    @Test
    fun `convertCurrency shows error for invalid amount`() = runTest {
        viewModel.updateAmount("invalid")
        viewModel.convertCurrency()
        assertEquals("Please enter a valid amount", viewModel.state.value.error)
    }

    @Test
    fun `convertCurrency updates result correctly`() = runTest {
        viewModel.updateAmount("100")
        viewModel.updateFromCurrency("USD")
        viewModel.updateToCurrency("EUR")
        whenever(repository.convertCurrency(100.0, "USD", "EUR")).thenReturn(85.0)
        
        viewModel.convertCurrency()
        testDispatcher.scheduler.advanceUntilIdle()
        
        assertEquals("85.00", viewModel.state.value.result)
        assertNull(viewModel.state.value.error)
    }
} 
package com.harry.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.harry.composables.ConversionRecord
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class DashboardState(
    val amount: String = "",
    val fromCurrency: String = "USD",
    val toCurrency: String = "EUR",
    val result: String = "0.00",
    val isLoading: Boolean = false,
    val error: String? = null,
    val conversionHistory: List<ConversionRecord> = emptyList()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: CurrencyRepository // TODO: Create this repository
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _conversionHistory = MutableSharedFlow<List<ConversionRecord>>()
    val conversionHistory: SharedFlow<List<ConversionRecord>> = _conversionHistory.asSharedFlow()

    fun updateAmount(amount: String) {
        _state.update { it.copy(amount = amount) }
    }

    fun updateFromCurrency(currency: String) {
        _state.update { it.copy(fromCurrency = currency) }
    }

    fun updateToCurrency(currency: String) {
        _state.update { it.copy(toCurrency = currency) }
    }

    fun swapCurrencies() {
        _state.update { currentState ->
            currentState.copy(
                fromCurrency = currentState.toCurrency,
                toCurrency = currentState.fromCurrency
            )
        }
    }

    fun convertCurrency() {
        val amount = _state.value.amount.toDoubleOrNull()
        if (amount == null) {
            _state.update { it.copy(error = "Please enter a valid amount") }
            return
        }

        _state.update { it.copy(
            isLoading = true,
            error = null
        ) }

        viewModelScope.launch {
            try {
                val result = repository.convertCurrency(
                    amount = amount,
                    fromCurrency = _state.value.fromCurrency,
                    toCurrency = _state.value.toCurrency
                )

                // Add to conversion history
                val newRecord = ConversionRecord(
                    fromCurrency = _state.value.fromCurrency,
                    toCurrency = _state.value.toCurrency,
                    amount = amount,
                    result = result,
                    timestamp = LocalDateTime.now()
                )

                val updatedHistory = (_state.value.conversionHistory + newRecord)
                    .takeLast(5) // Keep only last 5 conversions

                _state.update { it.copy(
                    result = String.format("%.2f", result),
                    isLoading = false,
                    conversionHistory = updatedHistory
                ) }

                _conversionHistory.emit(updatedHistory)
            } catch (e: Exception) {
                _state.update { it.copy(
                    error = "Failed to convert currency: ${e.message}",
                    isLoading = false
                ) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 
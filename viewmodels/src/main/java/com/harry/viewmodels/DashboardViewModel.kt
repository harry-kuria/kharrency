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
import javax.inject.Inject


data class DashboardState(
    val amount: String = "",
    val fromCurrency: String = "USD",
    val toCurrency: String = "EUR",
    val result: String = "0.00",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel
@Inject constructor(


) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

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

        // TODO: Implement actual currency conversion using a repository
        // For now, we'll just show a placeholder result
        _state.update { it.copy(
            isLoading = true,
            error = null
        ) }

        // Simulate API call
        kotlinx.coroutines.MainScope().launch {
            try {
                // TODO: Replace with actual conversion logic
                val result = amount * 1.2 // Placeholder conversion rate
                _state.update { it.copy(
                    result = String.format("%.2f", result),
                    isLoading = false
                ) }
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
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
import com.harry.model.ConversionRecord
import com.harry.model.ConversionHistory
import java.time.LocalDateTime
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.harry.repository.CurrencyRepository
import com.harry.repository.ExchangeRateResult
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.collectLatest
import java.time.ZoneOffset

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
    private val repository: CurrencyRepository
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _conversionHistory = MutableSharedFlow<List<ConversionRecord>>()
    val conversionHistory: SharedFlow<List<ConversionRecord>> = _conversionHistory.asSharedFlow()

    init {
        loadConversionHistory()
    }

    private fun loadConversionHistory() {
        viewModelScope.launch {
            repository.getConversionHistory().collectLatest { historyList ->
                val convertedHistory = historyList.map { it.toConversionRecord() }
                _state.update { it.copy(conversionHistory = convertedHistory) }
                _conversionHistory.emit(convertedHistory)
            }
        }
    }

    // Extension functions to convert between models
    private fun ConversionHistory.toConversionRecord(): ConversionRecord {
        return ConversionRecord(
            fromCurrency = fromCurrency,
            toCurrency = toCurrency,
            amount = amount,
            result = convertedAmount,
            timestamp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                LocalDateTime.ofEpochSecond(timestamp, 0, ZoneOffset.UTC)
            } else {
                LocalDateTime.now()
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ConversionRecord.toConversionHistory(): ConversionHistory {
        return ConversionHistory(
            fromCurrency = fromCurrency,
            toCurrency = toCurrency,
            amount = amount,
            convertedAmount = result,
            rate = result / amount,
            timestamp = timestamp.toEpochSecond(ZoneOffset.UTC)
        )
    }

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
                when (val result = repository.convertCurrency(
                    amount = amount,
                    fromCurrency = _state.value.fromCurrency,
                    toCurrency = _state.value.toCurrency
                )) {
                    is ExchangeRateResult.Success -> {
                        val rateResult = result.rates[_state.value.toCurrency] ?: 0.0

                        // Create conversion record
                        val newRecord = ConversionRecord(
                            fromCurrency = _state.value.fromCurrency,
                            toCurrency = _state.value.toCurrency,
                            amount = amount,
                            result = rateResult,
                            timestamp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                LocalDateTime.now()
                            } else {
                                LocalDateTime.now()
                            }
                        )

                        // Save to database
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            repository.insertConversionHistory(newRecord.toConversionHistory())
                        }

                        _state.update { it.copy(
                            result = String.format("%.2f", rateResult),
                            isLoading = false
                        ) }
                        
                        // Note: conversion history will be updated automatically via the Flow in loadConversionHistory()
                    }
                    is ExchangeRateResult.Error -> {
                        _state.update { it.copy(
                            error = result.message,
                            isLoading = false
                        ) }
                    }
                }
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
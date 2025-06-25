package com.harry.viewmodels

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.harry.model.ConversionRecord
import com.harry.model.ConversionHistory
import com.harry.model.UpdateCheckResponse
import com.harry.repository.CurrencyRepository
import com.harry.repository.ExchangeRateResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

data class DashboardState(
    val amount: String = "",
    val fromCurrency: String = "USD",
    val toCurrency: String = "EUR",
    val result: String = "0.00",
    val isLoading: Boolean = false,
    val error: String? = null,
    val conversionHistory: List<ConversionRecord> = emptyList()
)

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: CurrencyRepository,
    private val updateManager: UpdateManager,
    val downloadService: com.harry.repository.ApkDownloadService
) : ViewModel() {
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    private val _conversionHistory = MutableStateFlow<List<ConversionRecord>>(emptyList())
    val conversionHistory: StateFlow<List<ConversionRecord>> = _conversionHistory.asStateFlow()

    init {
        loadConversionHistory()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadConversionHistory() {
        viewModelScope.launch {
            repository.getConversionHistory().collectLatest { historyList ->
                val convertedHistory = historyList.map { it.toConversionRecord() }
                _state.update { it.copy(conversionHistory = convertedHistory) }
                _conversionHistory.value = convertedHistory
            }
        }
    }

    // Extension functions to convert between models
    @RequiresApi(Build.VERSION_CODES.O)
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

    fun checkForUpdates(onResult: (UpdateCheckResponse) -> Unit) {
        viewModelScope.launch {
            try {
                val result = updateManager.checkForUpdates(forceCheck = false)
                onResult(result)
            } catch (e: Exception) {
                onResult(UpdateCheckResponse(hasUpdate = false, error = e.message))
            }
        }
    }
    
    fun forceUpdateCheck(onResult: (UpdateCheckResponse) -> Unit) {
        viewModelScope.launch {
            try {
                val result = updateManager.checkForUpdates(forceCheck = true)
                onResult(result)
            } catch (e: Exception) {
                onResult(UpdateCheckResponse(hasUpdate = false, error = e.message))
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
} 
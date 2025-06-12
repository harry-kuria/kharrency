package com.harry.model

import java.time.LocalDateTime

data class ConversionRecord(
    val fromCurrency: String,
    val toCurrency: String,
    val amount: Double,
    val result: Double,
    val timestamp: LocalDateTime
)
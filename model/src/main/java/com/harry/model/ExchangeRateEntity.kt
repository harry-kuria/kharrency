package com.harry.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey
    val baseCurrency: String,
    val rates: Map<String, Double>,
    val timestamp: LocalDateTime
) 
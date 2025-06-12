package com.harry.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRateEntity(
    @PrimaryKey
    val base: String,
    val timestamp: Long,
    val rates: Map<String, Double>
) 
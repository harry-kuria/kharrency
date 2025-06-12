package com.harry.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDateTime

@Entity(tableName = "exchange_rates")
@TypeConverters(Converters::class)
data class ExchangeRateEntity(
    @PrimaryKey
    val base: String,
    val timestamp: LocalDateTime,
    val rates: Map<String, Double>
) 
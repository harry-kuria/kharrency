package com.harry.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.harry.model.ExchangeRateEntity
import java.time.LocalDateTime

@Database(
    entities = [ExchangeRateEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exchangeRateDao(): ExchangeRateDao
}

@androidx.room.TypeConverters
class Converters {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }

    @androidx.room.TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): String? {
        return date?.toString()
    }

    @androidx.room.TypeConverter
    fun fromString(value: String): Map<String, Double> {
        return value.split(",").associate {
            val (key, value) = it.split(":")
            key to value.toDouble()
        }
    }

    @androidx.room.TypeConverter
    fun fromMap(map: Map<String, Double>): String {
        return map.map { "${it.key}:${it.value}" }.joinToString(",")
    }
} 
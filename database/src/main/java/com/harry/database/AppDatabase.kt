package com.harry.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.harry.model.Converters
import com.harry.model.ExchangeRateEntity
import com.harry.model.ConversionHistory

@Database(
    entities = [ExchangeRateEntity::class, ConversionHistory::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun conversionHistoryDao(): ConversionHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(databaseBuilder: androidx.room.RoomDatabase.Builder<AppDatabase>): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: databaseBuilder.build().also { INSTANCE = it }
            }
        }
    }
} 
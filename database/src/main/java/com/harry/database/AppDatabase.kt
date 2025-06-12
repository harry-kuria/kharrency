package com.harry.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.harry.model.Converters
import com.harry.model.ExchangeRateEntity

@Database(
    entities = [ExchangeRateEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exchangeRateDao(): ExchangeRateDao

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
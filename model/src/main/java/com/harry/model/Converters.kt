package com.harry.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.ZoneOffset

class Converters {
    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, Double>>() {}.type

    @TypeConverter
    fun fromString(value: String?): Map<String, Double>? {
        if (value == null) return null
        return try {
            gson.fromJson(value, mapType)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromMap(map: Map<String, Double>?): String? {
        if (map == null) return null
        return try {
            gson.toJson(map, mapType)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }
}

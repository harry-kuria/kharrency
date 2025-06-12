package com.harry.model

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromString(value: String?): Map<String, Double> {
        if (value == null) return emptyMap()
        return try {
            val type = object : TypeToken<Map<String, Double>>() {}.type
            Gson().fromJson(value, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }

    @TypeConverter
    fun fromMap(map: Map<String, Double>?): String {
        if (map == null) return "{}"
        return try {
            val type = object : TypeToken<Map<String, Double>>() {}.type
            Gson().toJson(map, type)
        } catch (e: Exception) {
            "{}"
        }
    }
} 
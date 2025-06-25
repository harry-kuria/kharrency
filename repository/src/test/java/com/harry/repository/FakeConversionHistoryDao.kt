package com.harry.repository

import com.harry.database.ConversionHistoryDao
import com.harry.model.ConversionHistory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeConversionHistoryDao : ConversionHistoryDao {
    private val conversions = mutableListOf<ConversionHistory>()
    private val conversionsFlow = MutableStateFlow<List<ConversionHistory>>(emptyList())

    override fun getConversionHistory(): Flow<List<ConversionHistory>> = conversionsFlow

    override fun getRecentConversions(limit: Int): Flow<List<ConversionHistory>> {
        return MutableStateFlow(conversions.sortedByDescending { it.timestamp }.take(limit))
    }

    override suspend fun insertConversion(conversion: ConversionHistory): Long {
        conversions.add(conversion)
        conversionsFlow.value = conversions.toList()
        return conversions.size.toLong()
    }

    override suspend fun deleteOldConversions(timestamp: Long): Int {
        val oldConversions = conversions.filter { it.timestamp < timestamp }
        conversions.removeAll(oldConversions)
        conversionsFlow.value = conversions.toList()
        return oldConversions.size
    }

    override suspend fun deleteAllConversions() {
        conversions.clear()
        conversionsFlow.value = emptyList()
    }
} 
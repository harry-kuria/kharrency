package com.harry.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.harry.database.AppDatabase
import com.harry.model.ExchangeRateEntity
import com.harry.database.ExchangeRateDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.IOException
import java.net.SocketTimeoutException

interface ExchangeRateApi {
    @GET("latest")
    suspend fun getLatestRates(
        @Query("base") base: String,
        @Query("symbols") symbols: String? = null
    ): ExchangeRateResponse

    @GET("historical")
    suspend fun getHistoricalRates(
        @Query("base") base: String,
        @Query("date") date: String,
        @Query("symbols") symbols: String? = null
    ): ExchangeRateResponse
}

data class ExchangeRateResponse(
    val base: String,
    val rates: Map<String, Double>,
    val date: String? = null,
    val success: Boolean = true,
    val error: ErrorResponse? = null
)

data class ErrorResponse(
    val code: String,
    val message: String
)

sealed class ExchangeRateResult {
    data class Success(val rates: Map<String, Double>) : ExchangeRateResult()
    data class Error(val message: String) : ExchangeRateResult()
}

@Singleton
class CurrencyRepository @Inject constructor(
    private val database: AppDatabase,
    private val exchangeRateDao: ExchangeRateDao
) {
    private val api = Retrofit.Builder()
        .baseUrl("https://api.exchangerate.host/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ExchangeRateApi::class.java)

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun convertCurrency(
        amount: Double,
        fromCurrency: String,
        toCurrency: String
    ): ExchangeRateResult = withContext(Dispatchers.IO) {
        try {
            // Try to get cached rates first
            val cachedRates = database.exchangeRateDao()
                .getExchangeRates(fromCurrency)
                .firstOrNull()

            val rates = if (cachedRates != null && 
                ChronoUnit.HOURS.between(cachedRates.timestamp, LocalDateTime.now()) < 1) {
                // Use cached rates if they're less than 1 hour old
                cachedRates.rates
            } else {
                // Fetch new rates from API
                val response = api.getLatestRates(fromCurrency, toCurrency)
                if (!response.success) {
                    return@withContext ExchangeRateResult.Error(
                        response.error?.message ?: "Unknown error occurred"
                    )
                }
                // Cache the new rates
                database.exchangeRateDao().insertExchangeRates(
                    ExchangeRateEntity(
                        base = fromCurrency,
                        rates = response.rates,
                        timestamp = LocalDateTime.now()
                    )
                )
                response.rates
            }

            val rate = rates[toCurrency] ?: return@withContext ExchangeRateResult.Error("Currency not supported")
            ExchangeRateResult.Success(mapOf(toCurrency to (amount * rate)))
        } catch (e: SocketTimeoutException) {
            ExchangeRateResult.Error("Network timeout. Please check your connection.")
        } catch (e: IOException) {
            ExchangeRateResult.Error("Network error. Please check your connection.")
        } catch (e: Exception) {
            ExchangeRateResult.Error("Failed to fetch exchange rate: ${e.message}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getHistoricalRates(
        base: String,
        date: String,
        symbols: String? = null
    ): ExchangeRateResult = withContext(Dispatchers.IO) {
        try {
            val response = api.getHistoricalRates(base, date, symbols)
            if (!response.success) {
                return@withContext ExchangeRateResult.Error(
                    response.error?.message ?: "Unknown error occurred"
                )
            }
            ExchangeRateResult.Success(response.rates)
        } catch (e: Exception) {
            ExchangeRateResult.Error("Failed to fetch historical rates: ${e.message}")
        }
    }

    // Clean up old rates periodically
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun cleanupOldRates() {
        withContext(Dispatchers.IO) {
            val oneDayAgo = LocalDateTime.now().minus(1, ChronoUnit.DAYS)
            database.exchangeRateDao().deleteOldRates(oneDayAgo)
        }
    }

    fun getExchangeRates(): Flow<List<ExchangeRateEntity>> {
        return exchangeRateDao.getExchangeRates()
    }

    fun getLatestExchangeRate(base: String): Flow<ExchangeRateEntity?> {
        return exchangeRateDao.getLatestExchangeRate(base)
    }

    suspend fun insertExchangeRate(exchangeRate: ExchangeRateEntity) {
        exchangeRateDao.insertExchangeRate(exchangeRate)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun deleteOldRates(timestamp: LocalDateTime) {
        exchangeRateDao.deleteOldRates(timestamp.toEpochSecond(ZoneOffset.UTC))
    }
} 
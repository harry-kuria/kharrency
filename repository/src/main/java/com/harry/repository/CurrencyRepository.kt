package com.harry.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.harry.database.AppDatabase
import com.harry.model.ExchangeRateEntity
import com.harry.database.ExchangeRateDao
import com.harry.database.ConversionHistoryDao
import com.harry.model.ConversionHistory
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
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

interface ExchangeRateApi {
    @GET("latest/{base}")
    suspend fun getLatestRates(
        @retrofit2.http.Path("base") base: String
    ): ExchangeRateResponse

    @GET("history/{base}/{date}")
    suspend fun getHistoricalRates(
        @retrofit2.http.Path("base") base: String,
        @retrofit2.http.Path("date") date: String
    ): ExchangeRateResponse
}

data class ExchangeRateResponse(
    val base: String,
    val rates: Map<String, Double>?,
    val date: String? = null,
    val success: Boolean? = true
)

sealed class ExchangeRateResult {
    data class Success(val rates: Map<String, Double>) : ExchangeRateResult()
    data class Error(val message: String) : ExchangeRateResult()
}

@Singleton
class CurrencyRepository @Inject constructor(
    private val database: AppDatabase,
    private val exchangeRateDao: ExchangeRateDao,
    private val conversionHistoryDao: ConversionHistoryDao
) {
    private val api: ExchangeRateApi = Retrofit.Builder()
        .baseUrl("https://api.exchangerate-api.com/v4/")
        .client(
            OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        )
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
                .getLatestExchangeRate(fromCurrency)
                .firstOrNull()

            val ratesMap: Map<String, Double> = if (cachedRates != null &&
                ChronoUnit.HOURS.between(
                    LocalDateTime.ofEpochSecond(cachedRates.timestamp, 0, ZoneOffset.UTC),
                    LocalDateTime.now()
                ) < 1) {
                // Use cached rates if they're less than 1 hour old
                cachedRates.rates
            } else {
                // Fetch new rates from API
                val response = api.getLatestRates(fromCurrency)

                val apiRates = response.rates
                if (apiRates == null || apiRates.isEmpty()) {
                    return@withContext ExchangeRateResult.Error("No exchange rates available")
                }
                
                // Cache the new rates
                database.exchangeRateDao().insertExchangeRate(
                    ExchangeRateEntity(
                        base = fromCurrency,
                        rates = apiRates,
                        timestamp = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
                    )
                )
                apiRates
            }

            val rate = ratesMap[toCurrency] ?: return@withContext ExchangeRateResult.Error("Currency '$toCurrency' not supported")
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
        date: String
    ): ExchangeRateResult = withContext(Dispatchers.IO) {
        try {
            val response = api.getHistoricalRates(base, date)
            response.rates?.let { ExchangeRateResult.Success(it) } ?: ExchangeRateResult.Error("No rates returned")
        } catch (e: Exception) {
            ExchangeRateResult.Error("Failed to fetch historical rates: ${e.message}")
        }
    }

    // Clean up old rates periodically
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun cleanupOldRates() {
        withContext(Dispatchers.IO) {
            val oneDayAgo = LocalDateTime.now().minus(1, ChronoUnit.DAYS)
            database.exchangeRateDao().deleteOldRates(oneDayAgo.toEpochSecond(ZoneOffset.UTC))
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

    suspend fun insertConversionHistory(conversionHistory: ConversionHistory) {
        conversionHistoryDao.insertConversion(conversionHistory)
    }

    fun getConversionHistory(): Flow<List<ConversionHistory>> {
        return conversionHistoryDao.getRecentConversions(5) // Get last 5 conversions
    }
} 
package com.example.expensecalculator.repository

import android.content.SharedPreferences
import com.example.expensecalculator.network.ExchangeRateApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ExchangeRateRepository(
    private val api: ExchangeRateApi,
    private val prefs: SharedPreferences
) {
    private val CACHE_TTL_MS = TimeUnit.HOURS.toMillis(24)

    private fun cacheKey(base: String, target: String) = "rate_${base}_$target"
    private fun timestampKey(base: String, target: String) = "ts_${base}_$target"

    /**
     * Get exchange rate from base to target currency
     * Uses cached rate if available and not stale (< 24 hours old)
     * Otherwise fetches from API and caches the result
     */
    suspend fun getRate(baseCurrency: String, targetCurrency: String): Double = withContext(Dispatchers.IO) {
        // Same currency = 1.0
        if (baseCurrency == targetCurrency) return@withContext 1.0

        val key = cacheKey(baseCurrency, targetCurrency)
        val tsKey = timestampKey(baseCurrency, targetCurrency)

        // Check cache
        val cachedRate = prefs.getString(key, null)?.toDoubleOrNull()
        val cachedTimestamp = prefs.getLong(tsKey, 0L)
        val now = System.currentTimeMillis()

        if (cachedRate != null && (now - cachedTimestamp) < CACHE_TTL_MS) {
            return@withContext cachedRate
        }

        // Fetch from network
        try {
            val response = api.getLatestRates(baseCurrency)
            val rate = response.rates[targetCurrency]
                ?: throw IllegalArgumentException("No rate found for $targetCurrency")

            // Cache the result
            prefs.edit().apply {
                putString(key, rate.toString())
                putLong(tsKey, now)
                apply()
            }

            return@withContext rate
        } catch (e: Exception) {
            // If network fails and we have cached data, use it even if stale
            if (cachedRate != null) {
                return@withContext cachedRate
            }
            throw e
        }
    }

    /**
     * Refresh all rates for a given base currency
     */
    suspend fun refreshRates(baseCurrency: String): Map<String, Double> = withContext(Dispatchers.IO) {
        val response = api.getLatestRates(baseCurrency)
        val now = System.currentTimeMillis()

        // Cache all rates
        prefs.edit().apply {
            response.rates.forEach { (target, rate) ->
                putString(cacheKey(baseCurrency, target), rate.toString())
                putLong(timestampKey(baseCurrency, target), now)
            }
            apply()
        }

        return@withContext response.rates
    }

    /**
     * Clear all cached rates
     */
    fun clearCache() {
        prefs.edit().clear().apply()
    }
}


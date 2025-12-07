package com.example.expensecalculator.network

import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeRateApi {
    /**
     * Get latest exchange rates from base currency
     * Example: GET https://api.exchangerate.host/latest?base=USD
     */
    @GET("latest")
    suspend fun getLatestRates(@Query("base") baseCurrency: String): ExchangeRateResponse
}


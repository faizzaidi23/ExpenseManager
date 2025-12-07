package com.example.expensecalculator.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ExchangeRateResponse(
    @Json(name = "base")
    val baseCurrency: String,
    @Json(name = "date")
    val date: String,
    @Json(name = "rates")
    val rates: Map<String, Double>,
    @Json(name = "success")
    val success: Boolean = true
)


package com.example.expensecalculator.network

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.exchangerate.host/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val exchangeRateApi: ExchangeRateApi by lazy {
        retrofit.create(ExchangeRateApi::class.java)
    }
}


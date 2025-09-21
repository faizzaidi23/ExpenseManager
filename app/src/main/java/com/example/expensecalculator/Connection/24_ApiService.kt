package com.example.expensecalculator.Connection

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST

//This interface defines the API endpoints the app will communicate with

interface ApiService{



    // The @POST annotation tells Retrofit that this is a POST request.
    // The value ("/api/auth/register") is the specific path on the server.
    // The @Body annotation tells Retrofit to use the 'request' object as the JSON body of the POST request.
    // 'suspend' marks this as a function that can be paused and resumed, perfect for network calls with coroutines.

    @POST("/api/auth/register")
    suspend  fun register(@Body request: AuthRequest):Response<Unit>

    @POST("/api/auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>
}



// A singleton object to ensure you have only one instance of Retrofit for your entire app.
object RetrofitClient {
    // CRITICAL: This is the IP address the Android Emulator uses to connect to your computer's localhost.
    // Do NOT use "localhost" or "127.0.0.1".
    private const val BASE_URL = "http://10.0.2.2:8080"

    // Configure the JSON parser to be lenient about unknown keys from the server.
    private val json = Json { ignoreUnknownKeys = true }

    // Use 'lazy' to create the Retrofit instance only when it's first needed.
    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            // Add the converter factory that uses Kotlinx Serialization to parse JSON.
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        // Create the implementation of the ApiService interface.
        retrofit.create(ApiService::class.java)
    }
}
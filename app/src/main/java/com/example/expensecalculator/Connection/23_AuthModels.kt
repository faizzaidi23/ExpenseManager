package com.example.expensecalculator.Connection

import android.annotation.SuppressLint
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

//These data classes are essential for kotlinx serialization to work
/// The @Serializable annotation tells the compiler to generate code for converting
//These objects to and from JSON. The variable names must match the JSON keys



@OptIn(InternalSerializationApi::class)
@Serializable
data class AuthRequest(
    val email: String,
    val password: String
)

@OptIn(InternalSerializationApi::class)
@Serializable
data class AuthResponse(
    val token: String
)

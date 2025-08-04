package com.example.expensecalculator.tripData

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trips")

data class Trip(

    @PrimaryKey(autoGenerate = true)
    val id:Int,
    val destination: String,
    val participants: Int,
    val days: Int,
    val expenditure: Double
)
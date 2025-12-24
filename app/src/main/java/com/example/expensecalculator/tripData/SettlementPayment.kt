package com.example.expensecalculator.tripData

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "settlement_payments",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SettlementPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tripId: Int,
    val fromParticipant: String,
    val toParticipant: String,
    val amount: Double,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)


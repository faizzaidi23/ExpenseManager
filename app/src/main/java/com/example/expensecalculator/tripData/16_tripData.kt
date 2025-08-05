package com.example.expensecalculator.tripData

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val destination: String,
    val participants: Int,
    val days: Int,
    val expenditure: Double
)

@Entity(
    tableName = "tripExpense",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TripExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tripId: Int,
    val expenseName: String,
    val amount: Double,
    val paidBy: String,
    val date: String? = null
)

@Entity(
    tableName = "trip_participants",
    foreignKeys = [
        ForeignKey(
            entity = Trip::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TripParticipant(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tripId: Int,
    val participantName: String,
    val contactNumber: String? = null,
    val email: String? = null
)

// Data class for getting trip with all participants
data class TripWithParticipants(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val participants: List<TripParticipant>
)

// Data class for getting trip with all expenses
data class TripWithExpenses(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val expenses: List<TripExpense>
)

// Data class for complete trip details
data class CompleteTripDetails(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val participants: List<TripParticipant>,
    @Relation(
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val expenses: List<TripExpense>
)
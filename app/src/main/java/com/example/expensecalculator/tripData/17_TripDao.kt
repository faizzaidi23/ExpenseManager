package com.example.expensecalculator.tripData

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {


    @Insert
    suspend fun addTrip(trip: Trip): Long
    @Update
    suspend fun updateTrip(trip: Trip)
    @Delete
    suspend fun deleteTrip(trip: Trip)
    @Query("SELECT * FROM trips ORDER BY id DESC")
    fun showAllTrips(): Flow<List<Trip>>

    @Insert
    suspend fun addParticipants(participants: List<TripParticipant>)
    @Query("DELETE FROM trip_participants WHERE tripId = :tripId")
    suspend fun deleteAllParticipantsForTrip(tripId: Int)


    @Insert
    suspend fun addExpense(expense: TripExpense): Long

    @Delete
    suspend fun deleteExpense(expense: TripExpense)

    @Insert
    suspend fun addSplits(splits: List<ExpenseSplit>)

    @Query("SELECT * FROM tripExpense WHERE tripId = :tripId ORDER BY id DESC")
    fun getExpensesByTripIdFlow(tripId: Int): Flow<List<TripExpense>>


    @Transaction
    @Query("SELECT * FROM tripExpense WHERE id = :expenseId")
    fun getExpenseWithSplitsByIdFlow(expenseId: Int): Flow<ExpenseWithSplits?>


    @Transaction
    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getCompleteTripDetails(tripId: Int): CompleteTripDetails?

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun getCompleteTripDetailsFlow(tripId: Int): Flow<CompleteTripDetails?>


    @Transaction
    suspend fun addTripWithParticipants(trip: Trip, participantNames: List<String>) {
        val tripId = addTrip(trip)
        val participants = participantNames.map { name ->
            TripParticipant(tripId = tripId.toInt(), participantName = name)
        }
        addParticipants(participants)
    }

    @Transaction
    suspend fun updateTripWithParticipants(trip: Trip, participantNames: List<String>) {
        updateTrip(trip)
        deleteAllParticipantsForTrip(trip.id)
        val newParticipants = participantNames.map { name ->
            TripParticipant(tripId = trip.id, participantName = name)
        }
        addParticipants(newParticipants)
    }

    @Transaction
    suspend fun addExpenseWithSplits(expense: TripExpense, splits: List<ExpenseSplit>) {
        val expenseId = addExpense(expense)
        val splitsWithExpenseId = splits.map { it.copy(expenseId = expenseId.toInt()) }
        addSplits(splitsWithExpenseId)
    }

    @Transaction
    suspend fun deleteTripCompletely(trip: Trip) {
        deleteTrip(trip)
    }

    @Insert
    suspend fun addPhoto(photo: TripPhoto): Long

    @Delete
    suspend fun deletePhoto(photo: TripPhoto)

    @Query("SELECT * FROM trip_photos WHERE tripId = :tripId ORDER BY timestamp DESC")
    fun getPhotosByTripIdFlow(tripId: Int): Flow<List<TripPhoto>>

    // Settlement Payment operations
    @Insert
    suspend fun addSettlementPayment(payment: SettlementPayment): Long

    @Query("SELECT * FROM settlement_payments WHERE tripId = :tripId ORDER BY timestamp DESC")
    fun getSettlementPaymentsByTripIdFlow(tripId: Int): Flow<List<SettlementPayment>>

    @Query("DELETE FROM settlement_payments WHERE tripId = :tripId")
    suspend fun deleteAllSettlementPaymentsForTrip(tripId: Int)

    @Delete
    suspend fun deleteSettlementPayment(payment: SettlementPayment)

    // Currency operations
    @Query("UPDATE trips SET currency = :currency WHERE id = :tripId")
    suspend fun updateTripCurrency(tripId: Int, currency: String)
}
package com.example.expensecalculator.tripData

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    // ==================== TRIP & PARTICIPANT OPERATIONS ====================
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

    // ==================== EXPENSE & SPLIT OPERATIONS ====================
    @Insert
    suspend fun addExpense(expense: TripExpense): Long

    @Delete
    suspend fun deleteExpense(expense: TripExpense)

    @Insert
    suspend fun addSplits(splits: List<ExpenseSplit>)

    @Query("SELECT * FROM tripExpense WHERE tripId = :tripId ORDER BY id DESC")
    fun getExpensesByTripIdFlow(tripId: Int): Flow<List<TripExpense>>

    // --- NEW: Get a single expense with its splits by expense ID ---
    @Transaction
    @Query("SELECT * FROM tripExpense WHERE id = :expenseId")
    fun getExpenseWithSplitsByIdFlow(expenseId: Int): Flow<ExpenseWithSplits?>


    // ==================== COMBINED QUERIES ====================
    // --- ADDED: The missing suspend function ---
    @Transaction
    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getCompleteTripDetails(tripId: Int): CompleteTripDetails?

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun getCompleteTripDetailsFlow(tripId: Int): Flow<CompleteTripDetails?>


    // ==================== TRANSACTION METHODS ====================
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

    // ==================== PHOTO OPERATIONS ====================
    @Insert
    suspend fun addPhoto(photo: TripPhoto): Long

    @Delete
    suspend fun deletePhoto(photo: TripPhoto)

    @Query("SELECT * FROM trip_photos WHERE tripId = :tripId ORDER BY timestamp DESC")
    fun getPhotosByTripIdFlow(tripId: Int): Flow<List<TripPhoto>>
}
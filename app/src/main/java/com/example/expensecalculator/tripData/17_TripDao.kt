package com.example.expensecalculator.tripData

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    // ==================== TRIP OPERATIONS ====================
    @Insert
    suspend fun addTrip(trip: Trip): Long // Returns the ID of inserted trip

    @Delete
    suspend fun deleteTrip(trip: Trip)

    @Update
    suspend fun updateTrip(trip: Trip)

    @Query("SELECT * FROM trips ORDER BY id DESC")
    fun showAllTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Int): Trip?

    // ==================== PARTICIPANT OPERATIONS ====================
    @Insert
    suspend fun addParticipant(participant: TripParticipant)

    @Insert
    suspend fun addParticipants(participants: List<TripParticipant>)

    @Delete
    suspend fun deleteParticipant(participant: TripParticipant)

    @Update
    suspend fun updateParticipant(participant: TripParticipant)

    @Query("SELECT * FROM trip_participants WHERE tripId = :tripId")
    suspend fun getParticipantsByTripId(tripId: Int): List<TripParticipant>

    @Query("SELECT * FROM trip_participants WHERE tripId = :tripId")
    fun getParticipantsByTripIdFlow(tripId: Int): Flow<List<TripParticipant>>

    @Query("DELETE FROM trip_participants WHERE tripId = :tripId")
    suspend fun deleteAllParticipantsForTrip(tripId: Int)

    // ==================== EXPENSE OPERATIONS ====================
    @Insert
    suspend fun addExpense(expense: TripExpense)

    @Insert
    suspend fun addExpenses(expenses: List<TripExpense>)

    @Delete
    suspend fun deleteExpense(expense: TripExpense)

    @Update
    suspend fun updateExpense(expense: TripExpense)

    @Query("SELECT * FROM tripExpense WHERE tripId = :tripId ORDER BY id DESC")
    suspend fun getExpensesByTripId(tripId: Int): List<TripExpense>

    @Query("SELECT * FROM tripExpense WHERE tripId = :tripId ORDER BY id DESC")
    fun getExpensesByTripIdFlow(tripId: Int): Flow<List<TripExpense>>

    @Query("DELETE FROM tripExpense WHERE tripId = :tripId")
    suspend fun deleteAllExpensesForTrip(tripId: Int)

    // ==================== COMBINED QUERIES ====================
    @Transaction
    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripWithParticipants(tripId: Int): TripWithParticipants?

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripWithExpenses(tripId: Int): TripWithExpenses?

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getCompleteTripDetails(tripId: Int): CompleteTripDetails?

    // ⭐ ADDED: Flow version for real-time updates
    @Transaction
    @Query("SELECT * FROM trips WHERE id = :tripId")
    fun getCompleteTripDetailsFlow(tripId: Int): Flow<CompleteTripDetails?>

    @Transaction
    @Query("SELECT * FROM trips ORDER BY id DESC")
    fun getAllTripsWithParticipants(): Flow<List<TripWithParticipants>>

    // ==================== UTILITY QUERIES ====================
    @Query("SELECT COUNT(*) FROM trip_participants WHERE tripId = :tripId")
    suspend fun getParticipantCount(tripId: Int): Int

    @Query("SELECT SUM(amount) FROM tripExpense WHERE tripId = :tripId")
    suspend fun getTotalExpenseForTrip(tripId: Int): Double?

    @Query("SELECT participantName FROM trip_participants WHERE tripId = :tripId")
    suspend fun getParticipantNames(tripId: Int): List<String>

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
    suspend fun deleteTripCompletely(trip: Trip) {
        deleteAllParticipantsForTrip(trip.id)
        deleteAllExpensesForTrip(trip.id)
        deleteTrip(trip)
    }
}
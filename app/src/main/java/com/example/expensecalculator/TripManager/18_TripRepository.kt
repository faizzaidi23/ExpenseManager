package com.example.expensecalculator.TripManager

import com.example.expensecalculator.tripData.*
import kotlinx.coroutines.flow.Flow

class TripRepository(private val tripDao: TripDao) {

    fun getAllTrips(): Flow<List<Trip>> = tripDao.showAllTrips()

    suspend fun deleteTripCompletely(trip: Trip) {
        tripDao.deleteTripCompletely(trip)
    }

    suspend fun addTripWithParticipants(title: String, participantNames: List<String>) {
        val newTrip = Trip(title = title)
        tripDao.addTripWithParticipants(newTrip, participantNames)
    }

    suspend fun updateTripWithParticipants(tripId: Int, title: String, participantNames: List<String>) {
        // First, get the existing trip to preserve other details like days/budget
        val existingTrip = getCompleteTripDetails(tripId)?.trip ?: Trip(id = tripId, title = title)
        val tripToUpdate = existingTrip.copy(title = title)
        tripDao.updateTripWithParticipants(tripToUpdate, participantNames)
    }

    suspend fun getCompleteTripDetails(tripId: Int): CompleteTripDetails? {
        return tripDao.getCompleteTripDetails(tripId)
    }

    fun getCompleteTripDetailsFlow(tripId: Int): Flow<CompleteTripDetails?> {
        return tripDao.getCompleteTripDetailsFlow(tripId)
    }

    // --- UPDATED: This now calls the new transaction in the DAO ---
    suspend fun addExpenseWithSplits(expense: TripExpense, splits: List<ExpenseSplit>) {
        tripDao.addExpenseWithSplits(expense, splits)
    }

    suspend fun deleteExpense(expense: TripExpense) {
        tripDao.deleteExpense(expense)
    }

    // --- NEW: Get a single expense with its splits ---
    fun getExpenseWithSplitsById(expenseId: Int): Flow<ExpenseWithSplits?> {
        return tripDao.getExpenseWithSplitsByIdFlow(expenseId)
    }
}
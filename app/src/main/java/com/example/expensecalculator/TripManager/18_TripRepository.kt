package com.example.expensecalculator.TripManager

import com.example.expensecalculator.tripData.CompleteTripDetails
import com.example.expensecalculator.tripData.Trip
import com.example.expensecalculator.tripData.TripDao
import com.example.expensecalculator.tripData.TripExpense
import com.example.expensecalculator.tripData.TripParticipant
import com.example.expensecalculator.tripData.TripWithExpenses
import com.example.expensecalculator.tripData.TripWithParticipants
import kotlinx.coroutines.flow.Flow

class TripRepository(private val tripDao: TripDao) {

    // ==================== EXISTING FUNCTIONS (NO CHANGES) ====================
    fun getAllTrips(): Flow<List<Trip>> = tripDao.showAllTrips()

    suspend fun addTrip(trip: Trip) {
        tripDao.addTrip(trip)
    }

    suspend fun updateTrip(trip: Trip) {
        tripDao.updateTrip(trip)
    }

    suspend fun deleteTrip(trip: Trip) {
        tripDao.deleteTrip(trip)
    }

    // ==================== NEW TRIP FUNCTIONS ====================
    // Get a specific trip by ID
    suspend fun getTripById(tripId: Int): Trip? {
        return tripDao.getTripById(tripId)
    }

    // ==================== PARTICIPANT FUNCTIONS ====================
    // Add a single participant to a trip
    suspend fun addParticipant(participant: TripParticipant) {
        tripDao.addParticipant(participant)
    }

    // Add multiple participants to a trip at once
    suspend fun addParticipants(participants: List<TripParticipant>) {
        tripDao.addParticipants(participants)
    }

    // Update participant details
    suspend fun updateParticipant(participant: TripParticipant) {
        tripDao.updateParticipant(participant)
    }

    // Delete a specific participant
    suspend fun deleteParticipant(participant: TripParticipant) {
        tripDao.deleteParticipant(participant)
    }

    // Get all participants for a specific trip
    suspend fun getParticipantsByTripId(tripId: Int): List<TripParticipant> {
        return tripDao.getParticipantsByTripId(tripId)
    }

    // Get participants with Flow for real-time updates
    fun getParticipantsByTripIdFlow(tripId: Int): Flow<List<TripParticipant>> {
        return tripDao.getParticipantsByTripIdFlow(tripId)
    }

    // Delete all participants for a specific trip
    suspend fun deleteAllParticipantsForTrip(tripId: Int) {
        tripDao.deleteAllParticipantsForTrip(tripId)
    }

    // ==================== EXPENSE FUNCTIONS ====================
    // Add a single expense to a trip
    suspend fun addExpense(expense: TripExpense) {
        tripDao.addExpense(expense)
    }

    // Add multiple expenses to a trip at once
    suspend fun addExpenses(expenses: List<TripExpense>) {
        tripDao.addExpenses(expenses)
    }

    // Update expense details
    suspend fun updateExpense(expense: TripExpense) {
        tripDao.updateExpense(expense)
    }

    // Delete a specific expense
    suspend fun deleteExpense(expense: TripExpense) {
        tripDao.deleteExpense(expense)
    }

    // Get all expenses for a specific trip
    suspend fun getExpensesByTripId(tripId: Int): List<TripExpense> {
        return tripDao.getExpensesByTripId(tripId)
    }

    // Get expenses with Flow for real-time updates
    fun getExpensesByTripIdFlow(tripId: Int): Flow<List<TripExpense>> {
        return tripDao.getExpensesByTripIdFlow(tripId)
    }

    // Delete all expenses for a specific trip
    suspend fun deleteAllExpensesForTrip(tripId: Int) {
        tripDao.deleteAllExpensesForTrip(tripId)
    }

    // ==================== COMBINED DATA FUNCTIONS ====================
    // Get trip with all its participants
    suspend fun getTripWithParticipants(tripId: Int): TripWithParticipants? {
        return tripDao.getTripWithParticipants(tripId)
    }

    // Get trip with all its expenses
    suspend fun getTripWithExpenses(tripId: Int): TripWithExpenses? {
        return tripDao.getTripWithExpenses(tripId)
    }

    // Get complete trip details (trip + participants + expenses)
    suspend fun getCompleteTripDetails(tripId: Int): CompleteTripDetails? {
        return tripDao.getCompleteTripDetails(tripId)
    }

    // Get complete trip details with Flow for real-time updates
    fun getCompleteTripDetailsFlow(tripId: Int): Flow<CompleteTripDetails?> {
        return tripDao.getCompleteTripDetailsFlow(tripId)
    }

    // Get all trips with their participants (Flow for real-time updates)
    fun getAllTripsWithParticipants(): Flow<List<TripWithParticipants>> {
        return tripDao.getAllTripsWithParticipants()
    }

    // ==================== UTILITY FUNCTIONS ====================
    // Get count of participants for a trip
    suspend fun getParticipantCount(tripId: Int): Int {
        return tripDao.getParticipantCount(tripId)
    }

    // Get total expense amount for a trip
    suspend fun getTotalExpenseForTrip(tripId: Int): Double {
        return tripDao.getTotalExpenseForTrip(tripId) ?: 0.0
    }

    // Get only participant names for a trip
    suspend fun getParticipantNames(tripId: Int): List<String> {
        return tripDao.getParticipantNames(tripId)
    }

    // ==================== TRANSACTION FUNCTIONS ====================
    // Add trip with participants in one transaction
    suspend fun addTripWithParticipants(trip: Trip, participantNames: List<String>) {
        tripDao.addTripWithParticipants(trip, participantNames)
    }

    // Delete trip with all related data (participants + expenses)
    suspend fun deleteTripCompletely(trip: Trip) {
        tripDao.deleteTripCompletely(trip)
    }

    // ==================== CONVENIENCE FUNCTIONS ====================
    // Add participants by names only (creates TripParticipant objects internally)
    suspend fun addParticipantsByNames(tripId: Int, participantNames: List<String>) {
        val participants = participantNames.map { name ->
            TripParticipant(tripId = tripId, participantName = name)
        }
        tripDao.addParticipants(participants)
    }

    // Update participant name only
    suspend fun updateParticipantName(participantId: Int, tripId: Int, newName: String) {
        val updatedParticipant = TripParticipant(
            id = participantId,
            tripId = tripId,
            participantName = newName
        )
        tripDao.updateParticipant(updatedParticipant)
    }
}
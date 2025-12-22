package com.example.expensecalculator.TripManager

import com.example.expensecalculator.tripData.*
import kotlinx.coroutines.flow.Flow

class TripRepository(private val tripDao: TripDao) {

    fun getAllTrips(): Flow<List<Trip>> = tripDao.showAllTrips()

    suspend fun deleteTripCompletely(trip: Trip) {
        tripDao.deleteTripCompletely(trip)
    }

    suspend fun addTripWithParticipants(title: String, participantNames: List<String>, tripIconUri: String? = null, currency: String = "INR") {
        val newTrip = Trip(title = title, tripIconUri = tripIconUri, currency = currency)
        tripDao.addTripWithParticipants(newTrip, participantNames)
    }

    suspend fun updateTripWithParticipants(tripId: Int, title: String, participantNames: List<String>, tripIconUri: String? = null, currency: String = "INR") {
        // First, get the existing trip to preserve other details like days/budget
        val existingTrip = getCompleteTripDetails(tripId)?.trip ?: Trip(id = tripId, title = title, currency = currency)
        val tripToUpdate = existingTrip.copy(title = title, tripIconUri = tripIconUri ?: existingTrip.tripIconUri, currency = currency)
        tripDao.updateTripWithParticipants(tripToUpdate, participantNames)
    }

    suspend fun getCompleteTripDetails(tripId: Int): CompleteTripDetails? {
        return tripDao.getCompleteTripDetails(tripId)
    }

    fun getCompleteTripDetailsFlow(tripId: Int): Flow<CompleteTripDetails?> {
        return tripDao.getCompleteTripDetailsFlow(tripId)
    }

    //  Update trip icon
    suspend fun updateTripIcon(tripId: Int, iconUri: String) {
        val existingTrip = getCompleteTripDetails(tripId)?.trip ?: return
        val updatedTrip = existingTrip.copy(tripIconUri = iconUri)
        tripDao.updateTrip(updatedTrip)
    }

    // This now calls the new transaction in the DAO
    suspend fun addExpenseWithSplits(expense: TripExpense, splits: List<ExpenseSplit>) {
        tripDao.addExpenseWithSplits(expense, splits)
    }

    suspend fun deleteExpense(expense: TripExpense) {
        tripDao.deleteExpense(expense)
    }

    // Get a single expense with its splits
    fun getExpenseWithSplitsById(expenseId: Int): Flow<ExpenseWithSplits?> {
        return tripDao.getExpenseWithSplitsByIdFlow(expenseId)
    }

    //PHOTO OPERATIONS
    suspend fun addPhoto(photo: TripPhoto): Long {
        return tripDao.addPhoto(photo)
    }

    suspend fun deletePhoto(photo: TripPhoto) {
        tripDao.deletePhoto(photo)
    }

    fun getPhotosByTripId(tripId: Int): Flow<List<TripPhoto>> {
        return tripDao.getPhotosByTripIdFlow(tripId)
    }

    // SETTLEMENT PAYMENT OPERATIONS
    suspend fun addSettlementPayment(payment: SettlementPayment): Long {
        return tripDao.addSettlementPayment(payment)
    }

    suspend fun deleteSettlementPayment(payment: SettlementPayment) {
        tripDao.deleteSettlementPayment(payment)
    }

    fun getSettlementPaymentsByTripId(tripId: Int): Flow<List<SettlementPayment>> {
        return tripDao.getSettlementPaymentsByTripIdFlow(tripId)
    }

    // CURRENCY OPERATIONS
    suspend fun updateTripCurrency(tripId: Int, currency: String) {
        tripDao.updateTripCurrency(tripId, currency)
    }

    // CATEGORY OPERATIONS
    suspend fun addCategory(category: ExpenseCategory): Long {
        return tripDao.addCategory(category)
    }

    suspend fun deleteCategory(category: ExpenseCategory) {
        tripDao.deleteCategory(category)
    }

    fun getCategoriesByTripId(tripId: Int): Flow<List<ExpenseCategory>> {
        return tripDao.getCategoriesByTripIdFlow(tripId)
    }

    fun getCategoriesWithExpenses(tripId: Int): Flow<List<CategoryWithExpenses>> {
        return tripDao.getCategoriesWithExpensesFlow(tripId)
    }

    suspend fun getCategoryById(categoryId: Int): ExpenseCategory? {
        return tripDao.getCategoryById(categoryId)
    }

    suspend fun createDefaultCategories(tripId: Int) {
        tripDao.createDefaultCategories(tripId)
    }
}
package com.example.expensecalculator.TripManager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensecalculator.tripData.CompleteTripDetails
import com.example.expensecalculator.tripData.Trip
import com.example.expensecalculator.tripData.TripExpense
import com.example.expensecalculator.tripData.TripParticipant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TripViewModel(private val repository: TripRepository) : ViewModel() {

    // ==================== EXISTING PROPERTIES (NO CHANGES) ====================
    val allTrips = repository.getAllTrips()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _editingTrip = MutableStateFlow<Trip?>(null)
    val editingTrip: StateFlow<Trip?> = _editingTrip.asStateFlow()

    // ==================== NEW PROPERTIES FOR PARTICIPANTS ====================
    // Current trip being viewed for participants
    private val _currentTripId = MutableStateFlow<Int?>(null)
    val currentTripId: StateFlow<Int?> = _currentTripId.asStateFlow()

    // Participants for current trip
    private val _currentTripParticipants = MutableStateFlow<List<TripParticipant>>(emptyList())
    val currentTripParticipants: StateFlow<List<TripParticipant>> = _currentTripParticipants.asStateFlow()

    // Show participant dialog
    private val _showParticipantDialog = MutableStateFlow(false)
    val showParticipantDialog: StateFlow<Boolean> = _showParticipantDialog.asStateFlow()

    // Editing participant
    private val _editingParticipant = MutableStateFlow<TripParticipant?>(null)
    val editingParticipant: StateFlow<TripParticipant?> = _editingParticipant.asStateFlow()

    // ==================== NEW PROPERTIES FOR EXPENSES ====================
    // Expenses for current trip
    private val _currentTripExpenses = MutableStateFlow<List<TripExpense>>(emptyList())
    val currentTripExpenses: StateFlow<List<TripExpense>> = _currentTripExpenses.asStateFlow()

    // Show expense dialog
    private val _showExpenseDialog = MutableStateFlow(false)
    val showExpenseDialog: StateFlow<Boolean> = _showExpenseDialog.asStateFlow()

    // Editing expense
    private val _editingExpense = MutableStateFlow<TripExpense?>(null)
    val editingExpense: StateFlow<TripExpense?> = _editingExpense.asStateFlow()

    // Complete trip details
    private val _completeTripDetails = MutableStateFlow<CompleteTripDetails?>(null)
    val completeTripDetails: StateFlow<CompleteTripDetails?> = _completeTripDetails.asStateFlow()

    // ==================== EXISTING DIALOG FUNCTIONS (NO CHANGES) ====================
    fun showAddDialog() {
        _isEditing.value = false
        _editingTrip.value = null
        _showDialog.value = true
    }

    fun showEditDialog(trip: Trip) {
        _isEditing.value = true
        _editingTrip.value = trip
        _showDialog.value = true
    }

    fun hideDialog() {
        _showDialog.value = false
        _isEditing.value = false
        _editingTrip.value = null
    }

    // ==================== EXISTING TRIP FUNCTIONS (NO CHANGES) ====================
    fun addTrip(destination: String, participants: Int, days: Int, expenditure: Double) {
        viewModelScope.launch {
            val trip = Trip(
                id = 0, // Auto-generated
                destination = destination,
                participants = participants,
                days = days,
                expenditure = expenditure
            )
            repository.addTrip(trip)
        }
    }

    fun updateTrip(trip: Trip, destination: String, participants: Int, days: Int, expenditure: Double) {
        viewModelScope.launch {
            val updatedTrip = trip.copy(
                destination = destination,
                participants = participants,
                days = days,
                expenditure = expenditure
            )
            repository.updateTrip(updatedTrip)
        }
    }

    fun deleteTrip(trip: Trip) {
        viewModelScope.launch {
            repository.deleteTrip(trip)
        }
    }

    // ==================== NEW TRIP FUNCTIONS ====================
    // Set current trip and load its data
    fun setCurrentTrip(tripId: Int) {
        _currentTripId.value = tripId
        loadTripDetails(tripId)
        loadParticipants(tripId)
        loadExpenses(tripId)
    }

    // Get trip by ID
    fun getTripById(tripId: Int, callback: (Trip?) -> Unit) {
        viewModelScope.launch {
            val trip = repository.getTripById(tripId)
            callback(trip)
        }
    }

    // Load complete trip details
    private fun loadTripDetails(tripId: Int) {
        viewModelScope.launch {
            val details = repository.getCompleteTripDetails(tripId)
            _completeTripDetails.value = details
        }
    }

    // ==================== PARTICIPANT FUNCTIONS ====================
    // Load participants for current trip
    private fun loadParticipants(tripId: Int) {
        viewModelScope.launch {
            val participants = repository.getParticipantsByTripId(tripId)
            _currentTripParticipants.value = participants
        }
    }

    // Show add participant dialog
    fun showAddParticipantDialog() {
        _editingParticipant.value = null
        _showParticipantDialog.value = true
    }

    // Show edit participant dialog
    fun showEditParticipantDialog(participant: TripParticipant) {
        _editingParticipant.value = participant
        _showParticipantDialog.value = true
    }

    // Hide participant dialog
    fun hideParticipantDialog() {
        _showParticipantDialog.value = false
        _editingParticipant.value = null
    }

    // Add participant
    fun addParticipant(name: String, contactNumber: String? = null, email: String? = null) {
        val tripId = _currentTripId.value ?: return
        viewModelScope.launch {
            val participant = TripParticipant(
                tripId = tripId,
                participantName = name,
                contactNumber = contactNumber,
                email = email
            )
            repository.addParticipant(participant)
            loadParticipants(tripId) // Refresh the list
        }
    }

    // Add multiple participants by names
    fun addParticipantsByNames(names: List<String>) {
        val tripId = _currentTripId.value ?: return
        viewModelScope.launch {
            repository.addParticipantsByNames(tripId, names)
            loadParticipants(tripId) // Refresh the list
        }
    }

    // Update participant
    fun updateParticipant(participant: TripParticipant, name: String, contactNumber: String? = null, email: String? = null) {
        viewModelScope.launch {
            val updatedParticipant = participant.copy(
                participantName = name,
                contactNumber = contactNumber,
                email = email
            )
            repository.updateParticipant(updatedParticipant)
            loadParticipants(participant.tripId) // Refresh the list
        }
    }

    // Delete participant
    fun deleteParticipant(participant: TripParticipant) {
        viewModelScope.launch {
            repository.deleteParticipant(participant)
            loadParticipants(participant.tripId) // Refresh the list
        }
    }

    // ==================== EXPENSE FUNCTIONS ====================
    // Load expenses for current trip
    private fun loadExpenses(tripId: Int) {
        viewModelScope.launch {
            val expenses = repository.getExpensesByTripId(tripId)
            _currentTripExpenses.value = expenses
        }
    }

    // Show add expense dialog
    fun showAddExpenseDialog() {
        _editingExpense.value = null
        _showExpenseDialog.value = true
    }

    // Show edit expense dialog
    fun showEditExpenseDialog(expense: TripExpense) {
        _editingExpense.value = expense
        _showExpenseDialog.value = true
    }

    // Hide expense dialog
    fun hideExpenseDialog() {
        _showExpenseDialog.value = false
        _editingExpense.value = null
    }

    // Add expense - Updated to accept TripExpense object directly
    fun addExpense(expense: TripExpense) {
        val tripId = _currentTripId.value ?: return
        viewModelScope.launch {
            val expenseWithTripId = expense.copy(tripId = tripId)
            repository.addExpense(expenseWithTripId)
            loadExpenses(tripId) // Refresh the list
        }
    }

    // Add expense - Original method signature for backward compatibility
    fun addExpense(expenseName: String, amount: Double, paidBy: String, date: String? = null) {
        val tripId = _currentTripId.value ?: return
        viewModelScope.launch {
            val expense = TripExpense(
                tripId = tripId,
                expenseName = expenseName,
                amount = amount,
                paidBy = paidBy,
                date = date
            )
            repository.addExpense(expense)
            loadExpenses(tripId) // Refresh the list
        }
    }

    // Update expense
    fun updateExpense(expense: TripExpense, expenseName: String, amount: Double, paidBy: String, date: String? = null) {
        viewModelScope.launch {
            val updatedExpense = expense.copy(
                expenseName = expenseName,
                amount = amount,
                paidBy = paidBy,
                date = date
            )
            repository.updateExpense(updatedExpense)
            loadExpenses(expense.tripId) // Refresh the list
        }
    }

    // Delete expense
    fun deleteExpense(expense: TripExpense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            loadExpenses(expense.tripId) // Refresh the list
        }
    }

    // ==================== UTILITY FUNCTIONS ====================
    // Get participant count for a trip
    fun getParticipantCount(tripId: Int, callback: (Int) -> Unit) {
        viewModelScope.launch {
            val count = repository.getParticipantCount(tripId)
            callback(count)
        }
    }

    // Get total expenses for a trip
    fun getTotalExpenses(tripId: Int, callback: (Double) -> Unit) {
        viewModelScope.launch {
            val total = repository.getTotalExpenseForTrip(tripId)
            callback(total)
        }
    }

    // ==================== TRANSACTION FUNCTIONS ====================
    // Add trip with participants in one go
    fun addTripWithParticipants(destination: String, participantCount: Int, days: Int, expenditure: Double, participantNames: List<String>) {
        viewModelScope.launch {
            val trip = Trip(
                id = 0,
                destination = destination,
                participants = participantCount,
                days = days,
                expenditure = expenditure
            )
            repository.addTripWithParticipants(trip, participantNames)
        }
    }

    // Delete trip completely (with all participants and expenses)
    fun deleteTripCompletely(trip: Trip) {
        viewModelScope.launch {
            repository.deleteTripCompletely(trip)
        }
    }

    // Clear current trip data (call this when navigating away)
    fun clearCurrentTrip() {
        _currentTripId.value = null
        _currentTripParticipants.value = emptyList()
        _currentTripExpenses.value = emptyList()
        _completeTripDetails.value = null
    }
}

// ==================== FACTORY CLASS (NO CHANGES) ====================
class TripViewModelFactory(private val repository: TripRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
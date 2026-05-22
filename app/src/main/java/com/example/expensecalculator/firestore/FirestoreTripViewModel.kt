package com.example.expensecalculator.firestore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FirestoreTripViewModel : ViewModel() {

    private val repository = FirestoreTripRepository()
    private val uid get() = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // ─── TRIPS ───────────────────────────────────────────────────────────────

    private val _trips = MutableStateFlow<List<FirestoreTrip>>(emptyList())
    val trips: StateFlow<List<FirestoreTrip>> = _trips.asStateFlow()

    private val _currentTrip = MutableStateFlow<FirestoreTrip?>(null)
    val currentTrip: StateFlow<FirestoreTrip?> = _currentTrip.asStateFlow()

    // ─── EXPENSES ────────────────────────────────────────────────────────────

    private val _expenses = MutableStateFlow<List<FirestoreExpense>>(emptyList())
    val expenses: StateFlow<List<FirestoreExpense>> = _expenses.asStateFlow()

    // ─── INVITES ─────────────────────────────────────────────────────────────

    private val _pendingInvites = MutableStateFlow<List<FirestoreInvite>>(emptyList())
    val pendingInvites: StateFlow<List<FirestoreInvite>> = _pendingInvites.asStateFlow()

    // ─── USER SEARCH ─────────────────────────────────────────────────────────

    private val _searchResults = MutableStateFlow<List<FirestoreUser>>(emptyList())
    val searchResults: StateFlow<List<FirestoreUser>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // ─── LOADING / ERROR ─────────────────────────────────────────────────────

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ─── BALANCES (computed from expenses) ───────────────────────────────────

    val balances: StateFlow<Map<String, Double>>
        get() {
            val result = mutableMapOf<String, Double>()
            _currentTrip.value?.participants?.forEach {
                result[it.name] = 0.0
            }
            _expenses.value.forEach { expense ->
                result[expense.paidByName] =
                    (result[expense.paidByName] ?: 0.0) + expense.amount
                expense.splits.forEach { split ->
                    result[split.name] =
                        (result[split.name] ?: 0.0) - split.shareAmount
                }
            }
            return MutableStateFlow(result)
        }

    // ─── INIT ─────────────────────────────────────────────────────────────────

    init {
        loadMyTrips()
        loadMyPendingInvites()
    }

    // ─── TRIP OPERATIONS ─────────────────────────────────────────────────────

    fun loadMyTrips() {
        viewModelScope.launch {
            repository.getMyTrips().collect { _trips.value = it }
        }
    }

    fun setCurrentTrip(tripId: String) {
        viewModelScope.launch {
            repository.getTripById(tripId).collect { _currentTrip.value = it }
        }
        loadExpensesForTrip(tripId)
    }

    fun clearCurrentTrip() {
        _currentTrip.value = null
        _expenses.value = emptyList()
    }

    fun createTrip(
        title: String,
        currency: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (title.isBlank()) { onError("Title cannot be empty"); return }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val tripId = repository.createTrip(title, currency)
                onSuccess(tripId)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to create trip")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTrip(tripId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteTrip(tripId)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // ─── EXPENSE OPERATIONS ──────────────────────────────────────────────────

    private fun loadExpensesForTrip(tripId: String) {
        viewModelScope.launch {
            repository.getExpensesForTrip(tripId).collect { _expenses.value = it }
        }
    }

    fun addExpense(
        tripId: String,
        expenseName: String,
        amount: Double,
        paidByUid: String,
        paidByName: String,
        participantsInSplit: List<FirestoreParticipant>,
        categoryName: String? = null,
        onError: (String) -> Unit = {}
    ) {
        if (expenseName.isBlank() || amount <= 0) {
            onError("Invalid expense details")
            return
        }
        val shareAmount = amount / participantsInSplit.size
        val splits = participantsInSplit.map {
            FirestoreExpenseSplit(uid = it.uid, name = it.name, shareAmount = shareAmount)
        }
        viewModelScope.launch {
            try {
                repository.addExpense(
                    tripId, expenseName, amount,
                    paidByUid, paidByName, splits, categoryName
                )
            } catch (e: Exception) {
                onError(e.message ?: "Failed to add expense")
            }
        }
    }

    fun deleteExpense(tripId: String, expenseId: String) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(tripId, expenseId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // ─── USER SEARCH ─────────────────────────────────────────────────────────

    fun searchUsers(query: String) {
        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            try {
                _searchResults.value = repository.searchUsers(query)
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
    }

    // ─── INVITE OPERATIONS ───────────────────────────────────────────────────

    fun sendInvite(
        trip: FirestoreTrip,
        toUser: FirestoreUser,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.sendInvite(trip, toUser)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to send invite")
            }
        }
    }

    private fun loadMyPendingInvites() {
        viewModelScope.launch {
            repository.getMyPendingInvites().collect { _pendingInvites.value = it }
        }
    }

    fun acceptInvite(
        invite: FirestoreInvite,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.acceptInvite(invite)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to accept invite")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun declineInvite(
        invite: FirestoreInvite,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.declineInvite(invite)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to decline invite")
            }
        }
    }

    fun clearError() { _error.value = null }
}
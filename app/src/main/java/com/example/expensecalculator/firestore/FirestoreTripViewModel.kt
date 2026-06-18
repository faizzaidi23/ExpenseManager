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

    private val _tripsLoading = MutableStateFlow(true)
    val tripsLoading: StateFlow<Boolean> = _tripsLoading.asStateFlow()

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

    // ─── CATEGORIES ──────────────────────────────────────────────────────────

    private val _categories = MutableStateFlow<List<FirestoreCategory>>(emptyList())
    val categories: StateFlow<List<FirestoreCategory>> = _categories.asStateFlow()

    // ─── NOTIFICATIONS ───────────────────────────────────────────────────────

    private val _notifications = MutableStateFlow<List<FirestoreNotification>>(emptyList())
    val notifications: StateFlow<List<FirestoreNotification>> = _notifications.asStateFlow()

    // ─── PAID SETTLEMENTS STATE ──────────────────────────────────────────────

    private val _paidSettlements = MutableStateFlow<List<FirestoreSettlement>>(emptyList())
    val paidSettlements: StateFlow<List<FirestoreSettlement>> = _paidSettlements.asStateFlow()

    private var paidSettlementsJob: kotlinx.coroutines.Job? = null

    // ─── GLOBAL STATISTICS STATE ─────────────────────────────────────────────

    private val _tripBalancesSummary = MutableStateFlow<List<FirestoreTripRepository.TripBalanceSummary>>(emptyList())
    val tripBalancesSummary: StateFlow<List<FirestoreTripRepository.TripBalanceSummary>> = _tripBalancesSummary.asStateFlow()

    private val _globalPayables = MutableStateFlow(0.0)
    val globalPayables: StateFlow<Double> = _globalPayables.asStateFlow()

    private val _globalReceivables = MutableStateFlow(0.0)
    val globalReceivables: StateFlow<Double> = _globalReceivables.asStateFlow()

    private val _globalNetBalance = MutableStateFlow(0.0)
    val globalNetBalance: StateFlow<Double> = _globalNetBalance.asStateFlow()

    private val _isStatsLoading = MutableStateFlow(false)
    val isStatsLoading: StateFlow<Boolean> = _isStatsLoading.asStateFlow()

    fun loadGlobalStatistics() {
        viewModelScope.launch {
            _isStatsLoading.value = true
            val currentTrips = _trips.value
            if (currentTrips.isNotEmpty()) {
                val summaries = repository.calculateTripBalances(currentTrips)
                _tripBalancesSummary.value = summaries

                // Automatically aggregate global totals from the individual summaries
                _globalPayables.value = summaries.sumOf { it.payables }
                _globalReceivables.value = summaries.sumOf { it.receivables }
                _globalNetBalance.value = summaries.sumOf { it.netBalance }
            } else {
                _tripBalancesSummary.value = emptyList()
                _globalPayables.value = 0.0
                _globalReceivables.value = 0.0
                _globalNetBalance.value = 0.0
            }
            _isStatsLoading.value = false
        }
    }

    fun loadPaidSettlements(tripId: String) {
        paidSettlementsJob?.cancel()
        paidSettlementsJob = viewModelScope.launch {
            repository.getPaidSettlements(tripId).collect {
                _paidSettlements.value = it
            }
        }
    }

    // ─── INIT ─────────────────────────────────────────────────────────────────

    init {
        val auth = FirebaseAuth.getInstance()

        // Listen for auth state changes instead of calling once in init
        auth.addAuthStateListener { firebaseAuth ->
            val currentUid = firebaseAuth.currentUser?.uid
            if (currentUid != null) {
                loadMyTrips()
                loadMyPendingInvites()
                loadMyNotifications()
            } else {
                // User logged out - clear data immediately
                _trips.value = emptyList()
                _pendingInvites.value = emptyList()
                _currentTrip.value = null
                _expenses.value = emptyList()
                _notifications.value = emptyList()
            }
        }
    }

    // ─── TRIP OPERATIONS ─────────────────────────────────────────────────────

    fun loadMyTrips() {
        viewModelScope.launch {
            _tripsLoading.value = true
            repository.getMyTrips().collect { trips ->
                _trips.value = trips
                _tripsLoading.value = false
            }
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

    fun deleteExpense(tripId: String, expenseId: String, expenseName: String = "") {
        viewModelScope.launch {
            try {
                repository.deleteExpense(tripId, expenseId, expenseName)
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

    // ─── CATEGORY OPERATIONS ─────────────────────────────────────────────────

    fun loadCategories(tripId: String) {
        viewModelScope.launch {
            repository.getCategoriesForTrip(tripId).collect { _categories.value = it }
        }
    }

    fun addCategory(
        tripId: String,
        name: String,
        iconName: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.addCategory(tripId, name, iconName)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to add category")
            }
        }
    }

    fun deleteCategory(
        tripId: String,
        categoryId: String,
        categoryName: String = "",
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.deleteCategory(tripId, categoryId, categoryName)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    // ─── NOTIFICATION OPERATIONS ──────────────────────────────────────────────

    private fun loadMyNotifications() {
        viewModelScope.launch {
            repository.getMyNotifications().collect { _notifications.value = it }
        }
    }

    fun deleteNotification(notifId: String) {
        viewModelScope.launch {
            try {
                repository.deleteNotification(notifId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            try {
                repository.markAllNotificationsRead()
            } catch (e: Exception) {
                // Silent fail - don't show error for this
            }
        }
    }

    fun clearError() { _error.value = null }


    // ─── NEW TWO-STEP SETTLEMENT FUNCTIONS ────────────────────────────────────

    fun initiateSettlement(
        tripId: String,
        fromUid: String,
        toUid: String,
        fromName: String,
        toName: String,
        amount: Double,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.initiateSettlement(tripId, fromUid, toUid, fromName, toName, amount)
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun confirmSettlement(tripId: String, settlementId: String, toName: String) {
        viewModelScope.launch {
            try {
                repository.confirmSettlement(tripId, settlementId, toName)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun rejectSettlement(tripId: String, settlementId: String) {
        viewModelScope.launch {
            try {
                repository.rejectSettlement(tripId, settlementId)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }
}
package com.example.expensecalculator.TripManager

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensecalculator.Storage.ExportFormat
import com.example.expensecalculator.tripData.CompleteTripDetails
import com.example.expensecalculator.tripData.ExpenseSplit
import com.example.expensecalculator.tripData.ExpenseWithSplits
import com.example.expensecalculator.tripData.Trip
import com.example.expensecalculator.tripData.TripExpense
import com.example.expensecalculator.tripData.TripParticipant
import com.example.expensecalculator.tripData.TripPhoto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TripViewModel(
    private val repository: TripRepository,
    context: Context
) : ViewModel() {

    private val exportManager = TripExportManager(context.applicationContext)

    val allTrips = repository.getAllTrips()

    private val _completeTripDetails = MutableStateFlow<CompleteTripDetails?>(null)
    val completeTripDetails: StateFlow<CompleteTripDetails?> = _completeTripDetails.asStateFlow()

    val currentTripExpenses: StateFlow<List<TripExpense>> = _completeTripDetails.map {
        it?.expensesWithSplits?.map { expenseWithSplits -> expenseWithSplits.expense } ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentTripParticipants: StateFlow<List<TripParticipant>> = _completeTripDetails.map {
        it?.participants ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())



    val tripBalances: StateFlow<Map<String, Double>> = _completeTripDetails.map { details ->
        if (details == null) {
            emptyMap()
        } else {
            val balances = mutableMapOf<String, Double>()
            // 1. Initialize balances for all participants to 0
            details.participants.forEach { participant ->
                balances[participant.participantName] = 0.0
            }

            // 2. Process each expense
            details.expensesWithSplits.forEach { expenseWithSplits ->
                val expense = expenseWithSplits.expense
                val paidBy = expense.paidBy
                val totalAmount = expense.amount

                // Add the full amount to the person who paid
                balances[paidBy] = (balances[paidBy] ?: 0.0) + totalAmount

                // Subtract each person's share from their balance
                expenseWithSplits.splits.forEach { split ->
                    val participantName = split.participantName
                    val shareAmount = split.shareAmount
                    balances[participantName] = (balances[participantName] ?: 0.0) - shareAmount
                }
            }
            balances
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    // Optimized settlements calculated from balances
    val optimizedSettlements: StateFlow<List<Settlement>> = tripBalances.map { balances ->
        SettlementOptimizer.calculateOptimizedSettlements(balances)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    fun saveTrip(tripId: Int?, title: String, participantNames: List<String>, tripIconUri: String? = null) {
        viewModelScope.launch {
            if (tripId == null || tripId == -1) {
                repository.addTripWithParticipants(title, participantNames, tripIconUri)
            } else {
                repository.updateTripWithParticipants(tripId, title, participantNames, tripIconUri)
            }
        }
    }

    //  Update trip icon
    fun updateTripIcon(tripId: Int, iconUri: String) {
        viewModelScope.launch {
            repository.updateTripIcon(tripId, iconUri)
        }
    }

    suspend fun getTripById(tripId: Int): CompleteTripDetails? {
        return repository.getCompleteTripDetails(tripId)
    }

    fun deleteTripCompletely(trip: Trip) {
        viewModelScope.launch {
            repository.deleteTripCompletely(trip)
        }
    }

    fun setCurrentTrip(tripId: Int) {
        viewModelScope.launch {
            repository.getCompleteTripDetailsFlow(tripId).collect { details ->
                _completeTripDetails.value = details
            }
        }
    }

    fun clearCurrentTrip() {
        _completeTripDetails.value = null
    }

    fun addExpense(
        expenseName: String,
        amount: Double,
        paidBy: String,
        participantsInSplit: List<String>
    ) {
        val tripId = _completeTripDetails.value?.trip?.id ?: return
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            val expense = TripExpense(
                tripId = tripId,
                expenseName = expenseName,
                amount = amount,
                paidBy = paidBy,
                date = currentDate
            )

            val shareAmount = amount / participantsInSplit.size
            val splits = participantsInSplit.map { participantName ->
                ExpenseSplit(
                    expenseId = 0, // This will be set by the DAO
                    participantName = participantName,
                    shareAmount = shareAmount
                )
            }
            repository.addExpenseWithSplits(expense, splits)
        }
    }

    fun deleteExpense(expense: TripExpense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    fun getExpenseWithSplitsById(expenseId: Int): StateFlow<ExpenseWithSplits?> {
        return repository.getExpenseWithSplitsById(expenseId)
            .stateIn(viewModelScope, SharingStarted.Lazily, null)
    }


    val currentTripPhotos: StateFlow<List<TripPhoto>> = _completeTripDetails.map {
        it?.photos ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addPhoto(photoUri: String, caption: String? = null) {
        val tripId = _completeTripDetails.value?.trip?.id ?: return
        viewModelScope.launch {
            val photo = TripPhoto(
                tripId = tripId,
                photoUri = photoUri,
                caption = caption
            )
            repository.addPhoto(photo)
        }
    }

    fun deletePhoto(photo: TripPhoto) {
        viewModelScope.launch {
            repository.deletePhoto(photo)
        }
    }

    //EXPORT OPERATIONS
    fun exportTrip(format: ExportFormat, onComplete: (Uri?) -> Unit) {
        // Check if Android version supports the export functionality
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            onComplete(null)
            return
        }

        val tripDetails = _completeTripDetails.value ?: run {
            onComplete(null)
            return
        }

        viewModelScope.launch {
            val uri = when (format) {
                ExportFormat.CSV -> exportManager.exportTripToCSV(tripDetails)
                ExportFormat.PDF -> exportManager.exportTripToPDF(tripDetails)
                ExportFormat.EXCEL -> exportManager.exportTripToExcel(tripDetails)
            }
            onComplete(uri)
        }
    }
}

class TripViewModelFactory(
    private val repository: TripRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
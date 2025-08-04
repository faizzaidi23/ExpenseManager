package com.example.expensecalculator.TripManager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensecalculator.tripData.Trip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TripViewModel(private val repository: TripRepository) : ViewModel() {

    val allTrips = repository.getAllTrips()

    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    private val _editingTrip = MutableStateFlow<Trip?>(null)
    val editingTrip: StateFlow<Trip?> = _editingTrip.asStateFlow()

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
}

class TripViewModelFactory(private val repository: TripRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TripViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TripViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
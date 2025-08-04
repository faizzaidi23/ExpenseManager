package com.example.expensecalculator.TripManager

import com.example.expensecalculator.tripData.Trip
import com.example.expensecalculator.tripData.TripDao
import kotlinx.coroutines.flow.Flow

class TripRepository(private val tripDao: TripDao) {

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
}
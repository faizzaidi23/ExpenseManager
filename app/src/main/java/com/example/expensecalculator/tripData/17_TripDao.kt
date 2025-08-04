package com.example.expensecalculator.tripData

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao

interface TripDao{

    @Insert
    suspend fun addTrip(trip: Trip)

    @Delete
    suspend fun deleteTrip(trip: Trip)


    @Update
    suspend fun updateTrip(trip: Trip)

    @Query("Select * from trips order by id desc")
    fun showAllTrips():Flow<List<Trip>>
}
package com.example.expensecalculator.Data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.expensecalculator.tripData.Trip
import com.example.expensecalculator.tripData.TripDao
import com.example.expensecalculator.tripData.TripExpense
import com.example.expensecalculator.tripData.TripParticipant

@Database(
    entities = [
        Detail::class,
        Expense::class,
        Trip::class,
        TripParticipant::class,  // Added missing entity
        TripExpense::class       // Added missing entity
    ],
    version = 7  // Increment version since you're adding new entities
)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun detailDao(): DetailDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var INSTANCE: ExpenseDatabase? = null

        fun getDatabase(context: Context): ExpenseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDatabase::class.java,
                    "expense_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
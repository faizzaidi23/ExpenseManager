package com.example.expensecalculator.Data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.expensecalculator.tripData.ExpenseSplit // --- 1. ADD: Import for the new entity
import com.example.expensecalculator.tripData.Trip
import com.example.expensecalculator.tripData.TripDao
import com.example.expensecalculator.tripData.TripExpense
import com.example.expensecalculator.tripData.TripParticipant
import com.example.expensecalculator.tripData.TripPhoto
import java.util.Date

@TypeConverters(Converters::class)
@Database(
    entities = [
        Account::class,
        Expense::class,
        Trip::class,
        TripParticipant::class,
        TripExpense::class,
        ExpenseSplit::class,
        TripPhoto::class
    ],
    version = 11,
    exportSchema = false
)
abstract class ExpenseDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
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

class Converters {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @androidx.room.TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
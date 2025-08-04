package com.example.expensecalculator.Data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.expensecalculator.tripData.Trip
import com.example.expensecalculator.tripData.TripDao

@Database(entities=[Detail::class , Expense::class,Trip::class],version=4)

abstract class ExpenseDatabase:RoomDatabase(){

    abstract fun detailDao(): DetailDao  // It is compulsory to make this function an abstract one since I want the room library to generate the code for me
    abstract fun expenseDao(): ExpenseDao
    abstract fun tripDao(): TripDao

    companion object{
        @Volatile private var INSTANCE: ExpenseDatabase?=null

        fun getDatabase(context:Context): ExpenseDatabase{
            return INSTANCE ?: synchronized (this){
                val instance= Room.databaseBuilder(
                                context.applicationContext,
                                ExpenseDatabase::class.java,
                                "expense_db"
                            ).fallbackToDestructiveMigration().build()
                INSTANCE=instance
                instance
            }
        }
    }

}
package com.example.expensecalculator.Data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao

interface ExpenseDao{

    @Insert
    suspend fun  addExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Query("Select * from expenses order by id desc")

    fun getAllExpenses():Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expenses WHERE accountId = :accountId")
    fun getTotalForAccount(accountId: Int): Flow<Double?>


    @Query("Select * from expenses where accountId=:accountId order by id desc")
    fun getExpensesForAccount(accountId:Int):Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    fun getExpenseById(expenseId: Int): Flow<Expense?>
}
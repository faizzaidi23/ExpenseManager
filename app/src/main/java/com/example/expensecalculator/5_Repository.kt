package com.example.expensecalculator

import com.example.expensecalculator.Data.Account // Renamed from Detail
import com.example.expensecalculator.Data.AccountDao // Renamed from DetailDao
import com.example.expensecalculator.Data.Expense
import com.example.expensecalculator.Data.ExpenseDao
import kotlinx.coroutines.flow.Flow

// Renamed from 'repository' to 'ExpenseRepository' for clarity
class ExpenseRepository(private val accountDao: AccountDao, private val expenseDao: ExpenseDao ){

    // Renamed from 'details' to 'allAccounts'
    val allAccounts: Flow<List<Account>> = accountDao.getAllAccounts()

    suspend fun addAccount(account: Account){
        accountDao.addAccount(account = account)
    }

    suspend fun deleteAccount(account: Account){
        accountDao.deleteAccount(account = account)
    }

    suspend fun updateAccount(account: Account){
        accountDao.updateAccount(account = account)
    }

    fun getTotalForAccount(accountId:Int):Flow<Double?>{
        return expenseDao.getTotalForAccount(accountId=accountId)
    }

    val allExpenses:Flow<List<Expense>> = expenseDao.getAllExpenses()

    suspend fun addExpense(expense: Expense){
        expenseDao.addExpense(expense=expense)
    }

    suspend fun deleteExpense(expense:Expense){
        expenseDao.deleteExpense(expense=expense)
    }

    suspend fun updateExpense(expense:Expense){
        expenseDao.updateExpense(expense=expense)
    }

    fun getExpensesForAccount(accountId:Int):Flow<List<Expense>>{
        return expenseDao.getExpensesForAccount(accountId=accountId)
    }
}
package com.example.expensecalculator

import com.example.expensecalculator.Data.Detail
import com.example.expensecalculator.Data.DetailDao
import com.example.expensecalculator.Data.Expense
import com.example.expensecalculator.Data.ExpenseDao
import kotlinx.coroutines.flow.Flow


/*
This repo acts as a clean api for the apps data it acts as a mediator between the data
sources like room database a network api etc. and the rest of the app like the viewModel
*/

class repository(private val detailsDao: DetailDao,private val expenseDao: ExpenseDao ){


    val details:Flow<List<Detail>>
        get() {
            return detailsDao.getAllDetails()
        }

    suspend fun addDetail(detail: Detail){
        detailsDao.insertDetail(detail=detail)
    }

    suspend fun deleteDetail(detail:Detail){
        detailsDao.deleteDetail(detail=detail)
    }

    suspend fun updateDetail(detail: Detail){
        detailsDao.updateDetail(detail=detail)
    }

    fun getTotalForAccount(accountId:Int):Flow<Double?>{
        return expenseDao.getTotalForAccount(accountId=accountId)
    }

    val expenses:Flow<List<Expense>>
            get(){
                return expenseDao.getAllExpenses()
            }


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

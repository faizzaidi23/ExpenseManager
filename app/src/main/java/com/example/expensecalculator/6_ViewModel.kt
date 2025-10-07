package com.example.expensecalculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.expensecalculator.Data.Account // Renamed from Detail
import com.example.expensecalculator.Data.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Renamed from 'viewModel' to 'ExpenseViewModel'
class ExpenseViewModel(private val repository: ExpenseRepository):ViewModel(){

    // Renamed from 'details' to 'allAccounts'
    val allAccounts = repository.allAccounts.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(), emptyList()
    )

    fun addAccount(account: Account){
        viewModelScope.launch{
            repository.addAccount(account = account)
        }
    }

    fun deleteAccount(account: Account){
        viewModelScope.launch{
            repository.deleteAccount(account = account)
        }
    }

    fun updateAccount(account: Account){
        viewModelScope.launch{
            repository.updateAccount(account = account)
        }
    }

    fun addExpense(expense: Expense){
        viewModelScope.launch{
            repository.addExpense(expense = expense)
        }
    }

    fun deleteExpense(expense: Expense){
        viewModelScope.launch{
            repository.deleteExpense(expense = expense)
        }
    }

    fun updateExpense(expense: Expense){
        viewModelScope.launch {
            repository.updateExpense(expense = expense)
        }
    }

    fun getTotalForAccount(accountId: Int): Flow<Double?> {
        return repository.getTotalForAccount(accountId = accountId)
    }

    fun getExpensesForAccount(accountId: Int): Flow<List<Expense>> {
        return repository.getExpensesForAccount(accountId = accountId)
    }
}

// Renamed from 'AppViewModelFactory' to 'ExpenseViewModelFactory'
class ExpenseViewModelFactory(private val repository: ExpenseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
package com.example.expensecalculator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.expensecalculator.Data.Detail
import com.example.expensecalculator.Data.Expense
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class viewModel(val repository: repository):ViewModel(){


    val details=repository.details.map{it.reversed()}.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(),emptyList()
    )

    val expenses=repository.expenses.map{it.reversed()}.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(),emptyList()
    )

    fun addDetail(detail:Detail){
        viewModelScope.launch{
            repository.addDetail(detail=detail)
        }
    }

    fun deleteDetail(detail: Detail){
        viewModelScope.launch{
            repository.deleteDetail(detail=detail)
        }
    }

    fun updateDetail(detail:Detail){
        viewModelScope.launch{
            repository.updateDetail(detail=detail)
        }
    }

    fun addExpense(expense:Expense){
        viewModelScope.launch{
            repository.addExpense(expense=expense)
        }
    }

    fun deleteExpense(expense:Expense){
        viewModelScope.launch{
            repository.deleteExpense(expense=expense)
        }
    }

    fun updateExpense(expense:Expense){
        viewModelScope.launch {
            repository.updateExpense(expense=expense)
        }
    }

    // To get the total amount for a specific account
    fun getTotalForAccount(accountId: Int):Flow<Double?>{
        return repository.getTotalForAccount(accountId = accountId)
    }

    // To get all the expenses for a particular account
    fun getExpensesForAccount(accountId:Int):Flow<List<Expense>>{
        return repository.getExpensesForAccount(accountId=accountId)
    }
}



class AppViewModelFactory(private val repository: repository) : ViewModelProvider.Factory {

    /**
     * This 'create' method is called by the system to create a ViewModel instance.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        // Check if the requested ViewModel class is your 'viewModel' class
        if (modelClass.isAssignableFrom(viewModel::class.java)) {

            // If it is, create an instance of your viewModel, passing the repository to it.
            // The Suppress annotation is needed to handle the generic type casting.
            @Suppress("UNCHECKED_CAST")
            return viewModel(repository) as T
        }

        // If an unknown ViewModel is requested, throw an error.
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}



package com.example.expensecalculator


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.expensecalculator.Data.ExpenseDatabase
import com.example.expensecalculator.TripManager.TripRepository
import com.example.expensecalculator.TripManager.TripViewModel
import com.example.expensecalculator.TripManager.TripViewModelFactory
import com.example.expensecalculator.ui.theme.ExpenseCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val db = ExpenseDatabase.getDatabase(this)

        val accountDao = db.accountDao()
        val expenseDao = db.expenseDao()
        val tripDao = db.tripDao()

        val expenseRepository = ExpenseRepository(accountDao = accountDao, expenseDao = expenseDao)
        val tripRepository = TripRepository(tripDao = tripDao)


        val expenseViewModelFactory = ExpenseViewModelFactory(repository = expenseRepository)
        val tripViewModelFactory = TripViewModelFactory(repository = tripRepository)

        enableEdgeToEdge()
        setContent {
            ExpenseCalculatorTheme {
                val expenseViewModel: ExpenseViewModel = viewModel(factory = expenseViewModelFactory)
                val tripViewModel: TripViewModel = viewModel(factory = tripViewModelFactory)
                val navController = rememberNavController()

                NavGraph(
                    navController = navController,
                    expenseViewModel = expenseViewModel,
                    tripViewModel = tripViewModel
                )
            }
        }
    }
}
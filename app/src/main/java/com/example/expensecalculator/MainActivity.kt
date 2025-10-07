package com.example.expensecalculator


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.expensecalculator.Authentication.AuthNavigator
import com.example.expensecalculator.Data.ExpenseDatabase
import com.example.expensecalculator.TripManager.TripRepository
import com.example.expensecalculator.TripManager.TripViewModel
import com.example.expensecalculator.TripManager.TripViewModelFactory
import com.example.expensecalculator.ui.theme.ExpenseCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- UPDATED: Using all the new, renamed components ---

        // 1. Get the database instance
        val db = ExpenseDatabase.getDatabase(this)

        // 2. Get all the DAOs from the database
        val accountDao = db.accountDao() // Changed from detailDao()
        val expenseDao = db.expenseDao()
        val tripDao = db.tripDao()

        // 3. Create a separate repository for each feature
        val expenseRepository = ExpenseRepository(accountDao = accountDao, expenseDao = expenseDao) // Renamed
        val tripRepository = TripRepository(tripDao = tripDao)

        // 4. Create a factory for each ViewModel
        val expenseViewModelFactory = ExpenseViewModelFactory(repository = expenseRepository) // Renamed
        val tripViewModelFactory = TripViewModelFactory(repository = tripRepository)

        enableEdgeToEdge()
        setContent {
            ExpenseCalculatorTheme {
                // State to track if the user is logged in
                var isLoggedIn by remember { mutableStateOf(false) }

                if (isLoggedIn) {
                    // --- If logged in, show your main app ---
                    val expenseViewModel: ExpenseViewModel = viewModel(factory = expenseViewModelFactory) // Renamed
                    val tripViewModel: TripViewModel = viewModel(factory = tripViewModelFactory)
                    val navController = rememberNavController()

                    NavGraph(
                        navController = navController,
                        expenseViewModel = expenseViewModel, // Pass the correctly named ViewModel
                        tripViewModel = tripViewModel
                    )
                } else {
                    // --- If not logged in, show the Authentication Flow ---
                    AuthNavigator(onLoginSuccess = {
                        isLoggedIn = true
                    })
                }
            }
        }
    }
}
package com.example.expensecalculator


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.expensecalculator.Data.ExpenseDatabase
import com.example.expensecalculator.TripManager.TripRepository
import com.example.expensecalculator.TripManager.TripViewModel
import com.example.expensecalculator.TripManager.TripViewModelFactory
import com.example.expensecalculator.ui.theme.ExpenseCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the AndroidX splash screen handler
        installSplashScreen()

        // Switch to the normal app theme
        setTheme(R.style.Theme_ExpenseCalculator)

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
            var showSplash by remember { mutableStateOf(true) }

            if (showSplash) {
                // Show the animated splash screen
                SplashScreen(onComplete = { showSplash = false })
            } else {
                // Show the main app after splash completes
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
}
package com.example.expensecalculator

import NavGraph
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
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

        val db= ExpenseDatabase.getDatabase(this)
        val detailDao=db.detailDao()
        val expenseDao=db.expenseDao()
        val tripDao=db.tripDao()

        // Create a separate repo for each dao
        val repository= repository(detailsDao = detailDao, expenseDao = expenseDao)

        val repository2= TripRepository(tripDao = tripDao)

        val factory= AppViewModelFactory(repository = repository)
        val factory2= TripViewModelFactory(repository = repository2)

        enableEdgeToEdge()
        setContent {
            val variableViewModel:viewModel= viewModel(factory = factory)
            val tripViewModel: TripViewModel= viewModel(factory = factory2)
            val navController= rememberNavController()
            ExpenseCalculatorTheme {
                    NavGraph(navController = navController, viewModel = variableViewModel, tripViewModel = tripViewModel)
            }
        }
    }
}

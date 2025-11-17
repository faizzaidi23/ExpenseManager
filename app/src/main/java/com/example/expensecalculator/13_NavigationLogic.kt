package com.example.expensecalculator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.expensecalculator.TripManager.AddTripScreen
import com.example.expensecalculator.TripManager.FirstScreen
import com.example.expensecalculator.TripManager.TripDetailScreen
import com.example.expensecalculator.TripManager.TripExpenseDetailScreen
import com.example.expensecalculator.TripManager.TripMainScreen
import com.example.expensecalculator.TripManager.TripViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    expenseViewModel: ExpenseViewModel, // Renamed for clarity
    tripViewModel: TripViewModel
){
    NavHost(navController = navController, startDestination = "trip_main"){

        // First screen - kept but not in navigation flow
        composable("first_screen"){
            FirstScreen(navController = navController)
        }

        // Main screen - kept but not in navigation flow
        composable("main_screen"){ // Renamed from detail_screen
            MainScreen(navController = navController, viewModel = expenseViewModel)
        }

        composable("trip_main"){ // Renamed from trip_details
            TripMainScreen(navController = navController, viewModel = tripViewModel)
        }

        // --- UPDATED: Combined Add/Edit Trip Route ---
        // Uses an optional argument. If tripId is not passed, it defaults to -1 (Add Mode)
        composable(
            route = "add_trip?tripId={tripId}",
            arguments = listOf(navArgument("tripId") {
                type = NavType.IntType
                defaultValue = -1 // Default value for adding a new trip
            })
        ) { backStackEntry ->
            val tripId = backStackEntry.arguments?.getInt("tripId")
            AddTripScreen(
                navController = navController,
                viewModel = tripViewModel,
                tripId = if (tripId == -1) null else tripId // Pass null for Add Mode
            )
        }

        // --- REMOVED: The old edit_trip route is no longer needed ---

        composable(
            "trip_detail/{tripId}",
            arguments = listOf(navArgument("tripId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("tripId")
            if (id != null) {
                TripDetailScreen(navController = navController, viewModel = tripViewModel, tripId = id)
            }
        }

        // --- NEW: Trip Expense Detail Route ---
        composable(
            route = "trip_expense_detail/{expenseId}",
            arguments = listOf(navArgument("expenseId") { type = NavType.IntType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getInt("expenseId")
            if (expenseId != null) {
                val expenseWithSplits by tripViewModel.getExpenseWithSplitsById(expenseId).collectAsState(initial = null)
                expenseWithSplits?.let { expWithSplits ->
                    TripExpenseDetailScreen(
                        navController = navController,
                        viewModel = tripViewModel,
                        expenseWithSplits = expWithSplits
                    )
                }
            }
        }

        composable(
            route = "expense_screen/{accountId}", // Renamed from detailId
            arguments = listOf(navArgument("accountId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("accountId")
            if (id != null) {
                ExpenseScreen(
                    viewModel = expenseViewModel,
                    accountId = id,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { expense ->
                        navController.navigate("expense_detail/${expense.id}")
                    }
                )
            }
        }

        composable(
            route = "expense_detail/{expenseId}",
            arguments = listOf(navArgument("expenseId") { type = NavType.IntType })
        ) { backStackEntry ->
            val expenseId = backStackEntry.arguments?.getInt("expenseId")
            if (expenseId != null) {
                val expense by expenseViewModel.getExpenseById(expenseId).collectAsState(initial = null)
                expense?.let { exp ->
                    ExpenseDetailScreen(
                        expense = exp,
                        onNavigateBack = { navController.popBackStack() },
                        onEdit = {
                            navController.popBackStack()
                            // The edit dialog will be shown in the expense screen
                        },
                        onDelete = {
                            expenseViewModel.deleteExpense(exp)
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
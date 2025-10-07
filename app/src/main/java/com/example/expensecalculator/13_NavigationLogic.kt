package com.example.expensecalculator

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.expensecalculator.TripManager.AddTripScreen
import com.example.expensecalculator.TripManager.FirstScreen
import com.example.expensecalculator.TripManager.TripDetailScreen
import com.example.expensecalculator.TripManager.TripMainScreen
import com.example.expensecalculator.TripManager.TripViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    expenseViewModel: ExpenseViewModel, // Renamed for clarity
    tripViewModel: TripViewModel
){
    NavHost(navController = navController, startDestination = "first_screen"){

        composable("first_screen"){
            FirstScreen(navController = navController)
        }

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

        composable(
            route = "expense_screen/{accountId}", // Renamed from detailId
            arguments = listOf(navArgument("accountId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("accountId")
            if (id != null) {
                ExpenseScreen(
                    viewModel = expenseViewModel,
                    accountId = id,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
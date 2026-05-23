package com.example.expensecalculator

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.expensecalculator.Authentication.AuthViewModel
import com.example.expensecalculator.TripManager.AddTripScreen
import com.example.expensecalculator.TripManager.FirstScreen
import com.example.expensecalculator.TripManager.InviteParticipantsScreen
import com.example.expensecalculator.TripManager.NewAddTripScreen
import com.example.expensecalculator.TripManager.NewTripDetailScreen
import com.example.expensecalculator.TripManager.NewTripMainScreen
import com.example.expensecalculator.TripManager.NewNotificationsScreen
import com.example.expensecalculator.TripManager.ProfileScreen
import com.example.expensecalculator.TripManager.TripViewModel
import com.example.expensecalculator.firestore.FirestoreTripViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun NavGraph(
    navController: NavHostController,
    expenseViewModel: ExpenseViewModel,
    tripViewModel: TripViewModel,
    firestoreTripViewModel: FirestoreTripViewModel,
    themePreferences: ThemePreferences
) {
    val authViewModel: AuthViewModel = viewModel()

    MainScaffold(
        navController = navController,
        firestoreTripViewModel = firestoreTripViewModel,
        themePreferences = themePreferences
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "trip_main",
            modifier = Modifier.padding(paddingValues)
        ) {

            // ── MAIN TRIPS LIST (Firestore) ───────────────────────────────
            composable("trip_main") {
                NewTripMainScreen(
                    navController = navController,
                    viewModel = firestoreTripViewModel,
                    themePreferences = themePreferences
                )
            }

            // ── ADD TRIP (Firestore) ──────────────────────────────────────
            composable("new_add_trip") {
                NewAddTripScreen(
                    navController = navController,
                    viewModel = firestoreTripViewModel
                )
            }

            // ── TRIP DETAIL (Firestore) ───────────────────────────────────
            composable(
                route = "new_trip_detail/{tripId}",
                arguments = listOf(navArgument("tripId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                NewTripDetailScreen(
                    navController = navController,
                    viewModel = firestoreTripViewModel,
                    tripId = tripId
                )
            }

            // ── INVITE PARTICIPANTS ───────────────────────────────────────
            composable(
                route = "invite_participants/{tripId}",
                arguments = listOf(navArgument("tripId") { type = NavType.StringType })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                InviteParticipantsScreen(
                    navController = navController,
                    viewModel = firestoreTripViewModel,
                    tripId = tripId
                )
            }

            // ── CATEGORY EXPENSES SCREEN ──────────────────────────────────
            composable(
                route = "category_expenses/{tripId}/{categoryId}",
                arguments = listOf(
                    navArgument("tripId") { type = NavType.StringType },
                    navArgument("categoryId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getString("tripId") ?: return@composable
                val categoryId = backStackEntry.arguments?.getString("categoryId") ?: return@composable
                val categories by firestoreTripViewModel.categories.collectAsState()
                val expenses by firestoreTripViewModel.expenses.collectAsState()
                val category = categories.find { it.id == categoryId } ?: return@composable

                com.example.expensecalculator.TripManager.CategoryExpensesScreen(
                    navController = navController,
                    category = category,
                    expenses = expenses,
                    currencySymbol = "₹",
                    viewModel = firestoreTripViewModel,
                    tripId = tripId
                )
            }

            // ── OLD ROOM SCREENS (keep for account manager) ───────────────
            composable("first_screen") {
                FirstScreen(navController = navController)
            }

            composable("main_screen") {
                MainScreen(
                    navController = navController,
                    viewModel = expenseViewModel
                )
            }

            composable(
                route = "add_trip?tripId={tripId}",
                arguments = listOf(navArgument("tripId") {
                    type = NavType.IntType
                    defaultValue = -1
                })
            ) { backStackEntry ->
                val tripId = backStackEntry.arguments?.getInt("tripId")
                AddTripScreen(
                    navController = navController,
                    viewModel = tripViewModel,
                    tripId = if (tripId == -1) null else tripId
                )
            }

            composable(
                route = "expense_screen/{accountId}",
                arguments = listOf(navArgument("accountId") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("accountId") ?: return@composable
                ExpenseScreen(
                    viewModel = expenseViewModel,
                    accountId = id,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToDetail = { expense ->
                        navController.navigate("expense_detail/${expense.id}")
                    }
                )
            }

            composable(
                route = "expense_detail/{expenseId}",
                arguments = listOf(navArgument("expenseId") { type = NavType.IntType })
            ) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getInt("expenseId") ?: return@composable
                val expense by expenseViewModel.getExpenseById(expenseId).collectAsState(initial = null)
                expense?.let { exp ->
                    ExpenseDetailScreen(
                        expense = exp,
                        onNavigateBack = { navController.popBackStack() },
                        onEdit = { navController.popBackStack() },
                        onDelete = {
                            expenseViewModel.deleteExpense(exp)
                            navController.popBackStack()
                        }
                    )
                }
            }

            // ── NOTIFICATIONS (Firestore) ─────────────────────────────────
            composable("notifications") {
                NewNotificationsScreen(
                    viewModel = firestoreTripViewModel
                )
            }

            // ── PROFILE ───────────────────────────────────────────────────
            composable("profile") {
                ProfileScreen(
                    themePreferences = themePreferences,
                    authViewModel = authViewModel,
                    onLogout = {
                        com.example.expensecalculator.Data.ExpenseDatabase.clearInstance()
                        FirebaseAuth.getInstance().signOut()
                    }
                )
            }
        }
    }
}
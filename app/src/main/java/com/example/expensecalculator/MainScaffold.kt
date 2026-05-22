package com.example.expensecalculator

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.expensecalculator.firestore.FirestoreTripViewModel

data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("Trips", Icons.Filled.Flight, Icons.Outlined.Flight),
    BottomNavItem("Notifications", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    BottomNavItem("Profile", Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun MainScaffold(
    navController: NavController,
    firestoreTripViewModel: FirestoreTripViewModel,
    themePreferences: ThemePreferences,
    content: @Composable (PaddingValues) -> Unit
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Pending invites badge count
    val pendingInvites by firestoreTripViewModel.pendingInvites.collectAsState()
    val inviteCount = pendingInvites.size

    val selectedTab = when {
        currentRoute?.startsWith("trip_main") == true ||
                currentRoute?.startsWith("new_trip_detail") == true ||
                currentRoute?.startsWith("new_add_trip") == true ||
                currentRoute?.startsWith("invite_participants") == true ||
                currentRoute?.startsWith("main_screen") == true ||
                currentRoute?.startsWith("expense_screen") == true ||
                currentRoute?.startsWith("expense_detail") == true -> 0
        currentRoute == "notifications" -> 1
        currentRoute == "profile" -> 2
        else -> 0
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color.Black
            ) {
                bottomNavItems.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            when (index) {
                                0 -> navController.navigate("trip_main") {
                                    popUpTo("trip_main") { inclusive = false }
                                    launchSingleTop = true
                                }
                                1 -> navController.navigate("notifications") {
                                    popUpTo("notifications") { inclusive = false }
                                    launchSingleTop = true
                                }
                                2 -> navController.navigate("profile") {
                                    popUpTo("profile") { inclusive = false }
                                    launchSingleTop = true
                                }
                            }
                        },
                        icon = {
                            // Show badge on notifications if there are pending invites
                            if (index == 1 && inviteCount > 0) {
                                BadgedBox(badge = {
                                    Badge { Text("$inviteCount") }
                                }) {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.label,
                                        tint = if (isSelected) Color.Black else Color.Gray
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) Color.Black else Color.Gray
                                )
                            }
                        },
                        label = {
                            Text(
                                text = item.label,
                                color = if (isSelected) Color.Black else Color.Gray
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            selectedTextColor = Color.Black,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        content(paddingValues)
    }
}
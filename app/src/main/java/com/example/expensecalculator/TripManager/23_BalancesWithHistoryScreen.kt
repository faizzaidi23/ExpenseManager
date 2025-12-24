package com.example.expensecalculator.TripManager

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensecalculator.tripData.SettlementPayment

@Composable
fun BalancesWithHistoryScreen(
    balances: Map<String, Double>,
    settlements: List<Settlement>,
    settlementPayments: List<SettlementPayment>,
    currencySymbol: String = "₹",
    onRecordPayment: (String, String, Double) -> Unit = { _, _, _ -> },
    onDeletePayment: (SettlementPayment) -> Unit = {}
) {
    var selectedSubTab by remember { mutableStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-tab row for Settlement Plan and History
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.background,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    height = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = {
                    Text(
                        "Settlement Plan",
                        fontSize = 14.sp,
                        fontWeight = if (selectedSubTab == 0) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = {
                    Text(
                        "Payment History",
                        fontSize = 14.sp,
                        fontWeight = if (selectedSubTab == 1) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // Sub-tab content
        when (selectedSubTab) {
            0 -> {
                // Settlement Plan
                SmartSettlementContent(
                    balances = balances,
                    settlements = settlements,
                    currencySymbol = currencySymbol,
                    onRecordPayment = onRecordPayment
                )
            }
            1 -> {
                // Payment History
                PaymentHistoryScreen(
                    settlementPayments = settlementPayments,
                    currencySymbol = currencySymbol,
                    onDeletePayment = onDeletePayment
                )
            }
        }
    }
}


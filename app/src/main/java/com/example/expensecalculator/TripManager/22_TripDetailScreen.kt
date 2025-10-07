package com.example.expensecalculator.TripManager

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.expensecalculator.tripData.TripExpense
import com.example.expensecalculator.tripData.TripParticipant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

 val ScreenBackground = Color.White
 val PrimaryBlue = Color(0xFF007AFF)
 val PrimaryText = Color(0xFF222222)
 val SecondaryText = Color(0xFF8A8A8E)
 val TabIndicatorColor = PrimaryBlue
 val SelectedTabColor = PrimaryBlue
 val UnselectedTabColor = SecondaryText
 val IconBackground = Color(0xFFEBF5FF)
 val ErrorColor = Color(0xFFD32F2F)
 val PositiveBalanceColor = Color(0xFF34C759)
 val NegativeBalanceColor = Color(0xFFFF3B30)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    navController: NavController,
    viewModel: TripViewModel,
    tripId: Int
) {
    val completeTripDetails by viewModel.completeTripDetails.collectAsState()
    val currentTripParticipants by viewModel.currentTripParticipants.collectAsState()
    val tripBalances by viewModel.tripBalances.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(tripId) {
        viewModel.setCurrentTrip(tripId)
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clearCurrentTrip() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* No title */ },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back", tint = PrimaryText)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle Search */ }) {
                        Icon(Icons.Default.Search, "Search", tint = PrimaryText)
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.MoreVert, "More Options", tint = PrimaryText)
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    menuExpanded = false
                                    navController.navigate("add_trip/$tripId")
                                },
                                leadingIcon = { Icon(Icons.Default.Edit, "Edit Icon") }
                            )
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Delete", color = ErrorColor) },
                                onClick = {
                                    menuExpanded = false
                                    completeTripDetails?.trip?.let {
                                        viewModel.deleteTripCompletely(it)
                                        navController.popBackStack()
                                    }
                                },
                                leadingIcon = { Icon(Icons.Default.Delete, "Delete Icon", tint = ErrorColor) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreenBackground)
            )
        },
        containerColor = ScreenBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trip Header
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(IconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = rememberVectorPainter(Icons.Default.Umbrella),
                        contentDescription = "Trip Icon",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = completeTripDetails?.trip?.title ?: "Loading...",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = ScreenBackground,
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            height = 2.dp,
                            color = TabIndicatorColor
                        )
                    }
                ) {
                    Tab(selected = selectedTabIndex == 0, onClick = { selectedTabIndex = 0 }, text = { Text("Expenses") }, selectedContentColor = SelectedTabColor, unselectedContentColor = UnselectedTabColor)
                    Tab(selected = selectedTabIndex == 1, onClick = { selectedTabIndex = 1 }, text = { Text("Balances") }, selectedContentColor = SelectedTabColor, unselectedContentColor = UnselectedTabColor)
                    Tab(selected = selectedTabIndex == 2, onClick = { selectedTabIndex = 2 }, text = { Text("Photos") }, selectedContentColor = SelectedTabColor, unselectedContentColor = UnselectedTabColor)
                }

                // Tab Content
                when (selectedTabIndex) {
                    0 -> ExpensesContent(
                        expensesWithSplits = completeTripDetails?.expensesWithSplits ?: emptyList(),
                        onDeleteExpense = { viewModel.deleteExpense(it) }
                    )
                    1 -> BalancesContent(balances = tripBalances)
                    2 -> TripDetailEmptyState(
                        icon = Icons.Filled.PhotoLibrary,
                        title = "Photos Coming Soon",
                        subtitle = "This feature is currently under development."
                    )
                }
            }

            // Custom FAB
            Column(
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FloatingActionButton(
                    onClick = { showAddExpenseDialog = true },
                    containerColor = PrimaryBlue,
                    shape = CircleShape
                ) { Icon(Icons.Default.Add, "Add Expense", tint = Color.White) }
                Spacer(modifier = Modifier.height(4.dp))
                Text("Add Expense", color = SecondaryText, fontSize = 12.sp)
            }
        }
    }

    if (showAddExpenseDialog) {
        AddExpenseDialogWithSplits(
            participants = currentTripParticipants,
            onDismiss = { showAddExpenseDialog = false },
            // --- FIXED: Added the missing '->' arrow ---
            onAddExpense = { expenseName, amount, paidBy, participantsInSplit ->
                viewModel.addExpense(expenseName, amount, paidBy, participantsInSplit)
                showAddExpenseDialog = false
            }
        )
    }
}

@Composable
fun ExpensesContent(expensesWithSplits: List<com.example.expensecalculator.tripData.ExpenseWithSplits>, onDeleteExpense: (TripExpense) -> Unit) {
    if (expensesWithSplits.isEmpty()) {
        TripDetailEmptyState(
            icon = Icons.Default.Search,
            title = "No Expenses Yet",
            subtitle = "Add an expense by tapping on the \"+\" to start\ntracking and splitting your expenses"
        )
    } else {
        val totalExpenses = expensesWithSplits.sumOf { it.expense.amount }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Expenses", style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
                        Text("₹${"%,.2f".format(totalExpenses)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = PrimaryText)
                    }
                }
            }

            val groupedExpenses = expensesWithSplits.groupBy { it.expense.date }
            groupedExpenses.forEach { (date, expenses) ->
                item {
                    Text(text = date ?: "No Date", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
                }
                items(expenses) { expenseWithSplits ->
                    ExpenseCard(expense = expenseWithSplits.expense, onDelete = { onDeleteExpense(expenseWithSplits.expense) })
                }
            }
        }
    }
}

// --- FIXED: Included the missing BalancesContent composable ---
@Composable
fun BalancesContent(balances: Map<String, Double>) {
    val isAllGood = balances.values.all { abs(it) < 0.01 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = IconBackground)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ThumbUp, contentDescription = "Status", tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(if (isAllGood) "All good!" else "Settle up", fontWeight = FontWeight.Bold, color = PrimaryText)
                        Text(if (isAllGood) "You don't need to balance" else "Some participants need to settle up", color = SecondaryText, fontSize = 14.sp)
                    }
                }
            }
        }

        item {
            Text("Balances", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
        }

        if (balances.isEmpty()) {
            item {
                Text("No participants to calculate balances.", color = SecondaryText, modifier = Modifier.padding(8.dp))
            }
        } else {
            items(balances.entries.toList()) { (name, balance) ->
                BalanceItem(name = name, balance = balance)
            }
        }
    }
}

@Composable
fun BalanceItem(name: String, balance: Double) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(IconBackground), contentAlignment = Alignment.Center) {
            Text(name.firstOrNull()?.toString() ?: "", fontWeight = FontWeight.Bold, color = PrimaryBlue)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, color = PrimaryText)

        val color = if (balance > 0) PositiveBalanceColor else if (balance < 0) NegativeBalanceColor else SecondaryText
        val sign = if (balance > 0) "+" else ""
        Text(text = "$sign₹${"%,.2f".format(abs(balance))}", fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun TripDetailEmptyState(icon: ImageVector, title: String, subtitle: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = title, modifier = Modifier.size(56.dp), tint = SecondaryText.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = subtitle, fontSize = 16.sp, color = SecondaryText, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun ExpenseCard(expense: TripExpense, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ScreenBackground),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.ReceiptLong, "Expense Icon", modifier = Modifier.size(40.dp).clip(CircleShape).background(IconBackground).padding(8.dp), tint = PrimaryBlue)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.expenseName, fontWeight = FontWeight.SemiBold, color = PrimaryText)
                Text("Paid by: ${expense.paidBy}", color = SecondaryText, fontSize = 12.sp)
            }
            Text(text = "₹${"%,.0f".format(expense.amount)}", fontWeight = FontWeight.Bold, color = PrimaryText)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, "Delete", tint = ErrorColor)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialogWithSplits(
    participants: List<TripParticipant>,
    onDismiss: () -> Unit,
    onAddExpense: (String, Double, String, List<String>) -> Unit
) {
    var expenseName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf(participants.firstOrNull()) }
    var splitParticipants by remember { mutableStateOf(participants.map { it.participantName }.toSet()) }

    val amountValue = amount.toDoubleOrNull() ?: 0.0
    val splitValue = if (splitParticipants.isNotEmpty()) amountValue / splitParticipants.size else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Expense", fontWeight = FontWeight.Bold) },
        containerColor = ScreenBackground,
        shape = RoundedCornerShape(20.dp),
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(value = expenseName, onValueChange = { expenseName = it }, label = { Text("Title *") })
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount *") })

                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(value = paidBy?.participantName ?: "Select Payer", onValueChange = {}, readOnly = true, label = { Text("Paid By *") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }, modifier = Modifier.menuAnchor())
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        participants.forEach { participant ->
                            DropdownMenuItem(text = { Text(participant.participantName) }, onClick = { paidBy = participant; expanded = false })
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text("Split", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

                participants.forEach { participant ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable {
                            val currentSelection = splitParticipants.toMutableSet()
                            if (splitParticipants.contains(participant.participantName)) {
                                currentSelection.remove(participant.participantName)
                            } else {
                                currentSelection.add(participant.participantName)
                            }
                            splitParticipants = currentSelection
                        },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = splitParticipants.contains(participant.participantName),
                            onCheckedChange = { isChecked ->
                                val currentSelection = splitParticipants.toMutableSet()
                                if (isChecked) {
                                    currentSelection.add(participant.participantName)
                                } else {
                                    currentSelection.remove(participant.participantName)
                                }
                                splitParticipants = currentSelection
                            }
                        )
                        Text(participant.participantName, modifier = Modifier.weight(1f))
                        Text("₹${"%.2f".format(splitValue)}")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (expenseName.isNotBlank() && amountValue > 0 && paidBy != null && splitParticipants.isNotEmpty()) {
                        onAddExpense(expenseName, amountValue, paidBy!!.participantName, splitParticipants.toList())
                    }
                },
                enabled = expenseName.isNotBlank() && amountValue > 0 && paidBy != null && splitParticipants.isNotEmpty()
            ) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
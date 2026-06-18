package com.example.expensecalculator.TripManager

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.expensecalculator.firestore.FirestoreCategory
import com.example.expensecalculator.firestore.FirestoreExpense
import com.example.expensecalculator.firestore.FirestoreParticipant
import com.example.expensecalculator.firestore.FirestoreSettlement
import com.example.expensecalculator.firestore.FirestoreTripViewModel
import com.example.expensecalculator.ui.theme.IconBackground
import com.example.expensecalculator.ui.theme.NegativeBalanceColor
import com.example.expensecalculator.ui.theme.PositiveBalanceColor
import com.example.expensecalculator.util.CurrencyCode
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun NewTripDetailScreen(
    navController: NavController,
    viewModel: FirestoreTripViewModel,
    tripId: String
) {
    val context = LocalContext.current
    val currentTrip by viewModel.currentTrip.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val isCreator = currentTrip?.createdBy == uid

    var selectedTabIndex by remember { mutableStateOf(0) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showDeleteTripDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showEditExpenseDialog by remember { mutableStateOf(false) }
    var selectedExpenseForEdit by remember { mutableStateOf<FirestoreExpense?>(null) }

    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            scope.launch {
                isRefreshing = true
                viewModel.setCurrentTrip(tripId)
                viewModel.loadCategories(tripId)
                viewModel.loadPaidSettlements(tripId)
                delay(1000)
                isRefreshing = false
            }
        }
    )

    LaunchedEffect(tripId) {
        viewModel.setCurrentTrip(tripId)
        viewModel.loadCategories(tripId)
        viewModel.loadPaidSettlements(tripId)
    }

    val balances = remember(expenses, currentTrip) {
        val result = mutableMapOf<String, Double>()
        currentTrip?.participants?.forEach { result[it.uid] = 0.0 }
        expenses.forEach { expense ->
            result[expense.paidByUid] = (result[expense.paidByUid] ?: 0.0) + expense.amount
            expense.splits.forEach { split ->
                result[split.uid] = (result[split.uid] ?: 0.0) - split.shareAmount
            }
        }
        result.toMap()
    }

    val currencySymbol = currentTrip?.currency?.let { CurrencyCode.fromCode(it).symbol } ?: "₹"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // Ensures whole screen matches
        topBar = {
            TopAppBar(
                title = { },
                // FIX 1: Set the TopAppBar background to transparent so it blends perfectly
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Go back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("invite_participants/$tripId") }) {
                        Icon(Icons.Default.PersonAdd, "Invite")
                    }
                    // FIX 2: Only show the three dots if the user is actually the creator
                    if (isCreator) {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, "More Options")
                            }
                            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                                DropdownMenuItem(
                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                    onClick = { menuExpanded = false; showDeleteTripDialog = true },
                                    leadingIcon = { Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error) }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                // FIX 3: Customizing the Floating Action Button back to Blue
                FloatingActionButton(
                    onClick = { showAddExpenseDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Add")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).pullRefresh(pullRefreshState)) {
            Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(IconBackground), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Flight, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = currentTrip?.title ?: "Loading...", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(text = "${currentTrip?.participants?.size ?: 0} participants", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(20.dp))

                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.background,
                    edgePadding = 16.dp,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            height = 2.dp, color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    listOf("Expenses", "Balances", "People", "Categories").forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(text = title, fontSize = 14.sp, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium) },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                when (selectedTabIndex) {
                    0 -> NewExpensesTab(expenses = expenses, currencySymbol = currencySymbol, tripId = tripId, viewModel = viewModel, onDeleteExpense = { viewModel.deleteExpense(tripId, it.id, it.expenseName) }, onEditExpense = { selectedExpenseForEdit = it; showEditExpenseDialog = true })
                    1 -> NewBalancesTab(balances = balances, participants = currentTrip?.participants ?: emptyList(), currencySymbol = currencySymbol, tripId = tripId, viewModel = viewModel, currentUid = uid)
                    2 -> PeopleTab(participants = currentTrip?.participants ?: emptyList(), currentUid = uid)
                    3 -> CategoriesTab(tripId = tripId, expenses = expenses, currencySymbol = currencySymbol, viewModel = viewModel, navController = navController)
                }
            }
            PullRefreshIndicator(refreshing = isRefreshing, state = pullRefreshState, modifier = Modifier.align(Alignment.TopCenter))
        }
    }

    if (showAddExpenseDialog) {
        NewAddExpenseDialog(
            participants = currentTrip?.participants ?: emptyList(),
            categories = viewModel.categories.collectAsState().value,
            currencySymbol = currencySymbol,
            onDismiss = { showAddExpenseDialog = false },
            onAdd = { expenseName, amount, paidBy, splitAmong, categoryName ->
                viewModel.addExpense(tripId, expenseName, amount, paidBy.uid, paidBy.name, splitAmong, categoryName)
                showAddExpenseDialog = false
            }
        )
    }

    if (showDeleteTripDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteTripDialog = false },
            title = { Text("Delete Trip") },
            text = { Text("Are you sure? This cannot be undone.") },
            confirmButton = { Button(onClick = { viewModel.deleteTrip(tripId) { navController.popBackStack() }; showDeleteTripDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteTripDialog = false }) { Text("Cancel") } }
        )
    }

    if (showAddCategoryDialog) {
        CategoryPickerDialog(onDismiss = { showAddCategoryDialog = false }, onCategorySelected = { categoryName -> viewModel.addCategory(tripId, categoryName, "category"); showAddCategoryDialog = false })
    }

    if (showEditExpenseDialog && selectedExpenseForEdit != null) {
        EditFirestoreExpenseDialog(
            expense = selectedExpenseForEdit!!, participants = currentTrip?.participants ?: emptyList(), categories = viewModel.categories.collectAsState().value, currencySymbol = currencySymbol, onDismiss = { showEditExpenseDialog = false },
            onSave = { name, amt, paidBy, split, cat -> viewModel.deleteExpense(tripId, selectedExpenseForEdit!!.id); viewModel.addExpense(tripId, name, amt, paidBy.uid, paidBy.name, split, cat); showEditExpenseDialog = false }
        )
    }
}

// ─── EXPENSES TAB ─────────────────────────────────────────────────────────────

@Composable
private fun NewExpensesTab(
    expenses: List<FirestoreExpense>, currencySymbol: String, tripId: String,
    viewModel: FirestoreTripViewModel, onDeleteExpense: (FirestoreExpense) -> Unit, onEditExpense: (FirestoreExpense) -> Unit
) {
    if (expenses.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(52.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))
                Text("No Expenses Yet", style = MaterialTheme.typography.titleMedium)
            }
        }
    } else {
        val total = expenses.sumOf { it.amount }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text("Total: $currencySymbol${"%.2f".format(total)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }
            items(expenses) { expense ->
                Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(expense.expenseName, fontWeight = FontWeight.SemiBold)
                            Text("Paid by: ${expense.paidByName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            if (!expense.categoryName.isNullOrEmpty()) Text("Category: ${expense.categoryName}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        Text("$currencySymbol${"%.2f".format(expense.amount)}", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { onEditExpense(expense) }) { Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary) }
                        IconButton(onClick = { onDeleteExpense(expense) }) { Icon(Icons.Default.DeleteOutline, "Delete", tint = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }
}

// ─── BALANCES TAB ─────────────────────────────────────────

@Composable
private fun NewBalancesTab(
    balances: Map<String, Double>,
    participants: List<FirestoreParticipant>,
    currencySymbol: String,
    tripId: String,
    viewModel: FirestoreTripViewModel,
    currentUid: String
) {
    val context = LocalContext.current
    val paidSettlements by viewModel.paidSettlements.collectAsState()

    val adjustedBalances = remember(balances, paidSettlements) {
        val adjusted = balances.toMutableMap()
        paidSettlements.filter { it.status == "confirmed" }.forEach { paid ->
            adjusted[paid.fromUid] = (adjusted[paid.fromUid] ?: 0.0) + paid.amount
            adjusted[paid.toUid] = (adjusted[paid.toUid] ?: 0.0) - paid.amount
        }
        adjusted.toMap()
    }

    val activeSettlements = SettlementOptimizer.calculateOptimizedSettlements(adjustedBalances)

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Individual Balances", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }

        items(adjustedBalances.entries.toList()) { (participantUid, balance) ->
            val displayName = participants.find { it.uid == participantUid }?.name ?: "Unknown"

            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(IconBackground), contentAlignment = Alignment.Center) {
                    Text(displayName.firstOrNull()?.toString()?.uppercase() ?: "?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(displayName, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                val (text, color) = when {
                    balance > 0.01 -> "gets back $currencySymbol${"%.2f".format(abs(balance))}" to PositiveBalanceColor
                    balance < -0.01 -> "owes $currencySymbol${"%.2f".format(abs(balance))}" to NegativeBalanceColor
                    else -> "settled" to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                }
                Text(text, color = color, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (activeSettlements.isNotEmpty()) {
            item { Spacer(modifier = Modifier.height(8.dp)); Text("Settlement Plan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }

            items(activeSettlements) { settlement ->
                val fromName = participants.find { it.uid == settlement.from }?.name ?: "Unknown"
                val toName = participants.find { it.uid == settlement.to }?.name ?: "Unknown"
                val pendingRequest = paidSettlements.find {
                    it.fromUid == settlement.from && it.toUid == settlement.to && it.status == "pending"
                }

                Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(IconBackground), contentAlignment = Alignment.Center) {
                                Text(fromName.firstOrNull()?.toString()?.uppercase() ?: "?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(fromName, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 8.dp).size(18.dp))
                            Text(toName, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            Text("$currencySymbol${"%.2f".format(settlement.amount)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        if (pendingRequest != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(10.dp))

                            if (currentUid == settlement.to) {
                                Text("$fromName says they paid this.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), modifier = Modifier.padding(bottom = 8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { viewModel.rejectSettlement(tripId, pendingRequest.id) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error), border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)) { Text("Reject") }
                                    Button(onClick = { viewModel.confirmSettlement(tripId, pendingRequest.id, toName) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF34C759))) { Text("Confirm Receipt") }
                                }
                            } else {
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Waiting for $toName to confirm...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                                }
                            }
                        } else if (currentUid == settlement.from) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    viewModel.initiateSettlement(
                                        tripId = tripId, fromUid = settlement.from, toUid = settlement.to,
                                        fromName = fromName, toName = toName, amount = settlement.amount,
                                        onSuccess = { Toast.makeText(context, "Request sent to $toName!", Toast.LENGTH_SHORT).show() }
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mark as Paid", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        } else {
            item { Spacer(modifier = Modifier.height(8.dp)); Text("All settled!", style = MaterialTheme.typography.bodyMedium, color = PositiveBalanceColor, fontWeight = FontWeight.SemiBold) }
        }
    }
}

// ─── PEOPLE TAB ───────────────────────────────────────────────────────────────

@Composable
private fun PeopleTab(participants: List<FirestoreParticipant>, currentUid: String) {
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("${participants.size} Participant${if (participants.size != 1) "s" else ""}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold) }
        items(participants) { participant ->
            Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(IconBackground), contentAlignment = Alignment.Center) {
                        Text(participant.name.firstOrNull()?.toString()?.uppercase() ?: "?", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(participant.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                            if (participant.uid == currentUid) { Spacer(modifier = Modifier.width(6.dp)); Text("(you)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
                        }
                        Text(participant.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

// ─── ADD EXPENSE DIALOG ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAddExpenseDialog(
    participants: List<FirestoreParticipant>, categories: List<FirestoreCategory>, currencySymbol: String,
    onDismiss: () -> Unit, onAdd: (String, Double, FirestoreParticipant, List<FirestoreParticipant>, String?) -> Unit
) {
    var expenseName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf(participants.firstOrNull()) }
    var splitAmong by remember { mutableStateOf(participants.toSet()) }
    var paidByExpanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<FirestoreCategory?>(null) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val amountValue = amount.toDoubleOrNull() ?: 0.0
    val splitValue = if (splitAmong.isNotEmpty()) amountValue / splitAmong.size else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(value = expenseName, onValueChange = { expenseName = it }, label = { Text("Title *") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount *") }, prefix = { Text(currencySymbol) }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                ExposedDropdownMenuBox(expanded = paidByExpanded, onExpandedChange = { paidByExpanded = !paidByExpanded }) {
                    OutlinedTextField(value = paidBy?.name ?: "Select", onValueChange = {}, readOnly = true, label = { Text("Paid by *") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paidByExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                    ExposedDropdownMenu(expanded = paidByExpanded, onDismissRequest = { paidByExpanded = false }) {
                        participants.forEach { p -> DropdownMenuItem(text = { Text(p.name) }, onClick = { paidBy = p; paidByExpanded = false }) }
                    }
                }

                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = !categoryExpanded }) {
                    OutlinedTextField(value = selectedCategory?.name ?: "No Category", onValueChange = {}, readOnly = true, label = { Text("Category (Optional)") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        DropdownMenuItem(text = { Text("No Category") }, onClick = { selectedCategory = null; categoryExpanded = false })
                        categories.forEach { category -> DropdownMenuItem(text = { Text(category.name) }, onClick = { selectedCategory = category; categoryExpanded = false }) }
                    }
                }

                HorizontalDivider()
                Text("Split among", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)

                participants.forEach { participant ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = splitAmong.contains(participant), onCheckedChange = { checked -> splitAmong = if (checked) splitAmong + participant else splitAmong - participant })
                        Text(participant.name, modifier = Modifier.weight(1f))
                        if (splitAmong.contains(participant)) Text("$currencySymbol${"%.2f".format(splitValue)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { val p = paidBy ?: return@Button; if (expenseName.isNotBlank() && amountValue > 0 && splitAmong.isNotEmpty()) onAdd(expenseName, amountValue, p, splitAmong.toList(), selectedCategory?.name) }, enabled = expenseName.isNotBlank() && amountValue > 0 && paidBy != null && splitAmong.isNotEmpty()) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
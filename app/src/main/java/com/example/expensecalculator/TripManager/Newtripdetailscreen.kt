package com.example.expensecalculator.TripManager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.expensecalculator.firestore.FirestoreCategory
import com.example.expensecalculator.firestore.FirestoreExpense
import com.example.expensecalculator.firestore.FirestoreParticipant
import com.example.expensecalculator.firestore.FirestoreTripViewModel
import com.example.expensecalculator.ui.theme.IconBackground
import com.example.expensecalculator.ui.theme.NegativeBalanceColor
import com.example.expensecalculator.ui.theme.PositiveBalanceColor
import com.example.expensecalculator.util.CurrencyCode
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTripDetailScreen(
    navController: NavController,
    viewModel: FirestoreTripViewModel,
    tripId: String
) {
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

    LaunchedEffect(tripId) {
        viewModel.setCurrentTrip(tripId)
        viewModel.loadCategories(tripId)
    }

    // Compute balances from expenses
    val balances = remember(expenses, currentTrip) {
        val result = mutableMapOf<String, Double>()
        currentTrip?.participants?.forEach { result[it.name] = 0.0 }
        expenses.forEach { expense ->
            result[expense.paidByName] = (result[expense.paidByName] ?: 0.0) + expense.amount
            expense.splits.forEach { split ->
                result[split.name] = (result[split.name] ?: 0.0) - split.shareAmount
            }
        }
        result.toMap()
    }

    val settlements = remember(balances) {
        SettlementOptimizer.calculateOptimizedSettlements(balances)
    }

    val currencySymbol = currentTrip?.currency?.let {
        CurrencyCode.fromCode(it).symbol
    } ?: "₹"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Go back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    // Invite participants button
                    IconButton(onClick = {
                        navController.navigate("invite_participants/$tripId")
                    }) {
                        Icon(
                            Icons.Default.PersonAdd,
                            "Invite",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                "More Options",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            if (isCreator) {
                                DropdownMenuItem(
                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                    onClick = {
                                        menuExpanded = false
                                        showDeleteTripDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Delete, "Delete",
                                            tint = MaterialTheme.colorScheme.error)
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 0 || selectedTabIndex == 3) {
                FloatingActionButton(
                    onClick = {
                        when (selectedTabIndex) {
                            0 -> showAddExpenseDialog = true
                            3 -> showAddCategoryDialog = true
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, "Add", tint = Color.White)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Trip icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(IconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Flight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = currentTrip?.title ?: "Loading...",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${currentTrip?.participants?.size ?: 0} participants",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        height = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                listOf("Expenses", "Balances", "People", "Categories").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, fontSize = 14.sp) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> NewExpensesTab(
                    expenses = expenses,
                    currencySymbol = currencySymbol,
                    currentUid = uid,
                    tripId = tripId,
                    viewModel = viewModel,
                    participants = currentTrip?.participants ?: emptyList(),
                    categories = viewModel.categories.collectAsState().value,
                    onDeleteExpense = { expense ->
                        viewModel.deleteExpense(tripId, expense.id, expense.expenseName)
                    },
                    onEditExpense = { expense ->
                        selectedExpenseForEdit = expense
                        showEditExpenseDialog = true
                    }
                )
                1 -> NewBalancesTab(
                    balances = balances,
                    settlements = settlements,
                    currencySymbol = currencySymbol
                )
                2 -> PeopleTab(
                    participants = currentTrip?.participants ?: emptyList(),
                    currentUid = uid
                )
                3 -> CategoriesTab(
                    tripId = tripId,
                    expenses = expenses,
                    currencySymbol = currencySymbol,
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        val participants = currentTrip?.participants ?: emptyList()
        val categories = viewModel.categories.collectAsState().value
        NewAddExpenseDialog(
            participants = participants,
            categories = categories,
            currencySymbol = currencySymbol,
            onDismiss = { showAddExpenseDialog = false },
            onAdd = { expenseName, amount, paidBy, splitAmong, categoryName ->
                viewModel.addExpense(
                    tripId = tripId,
                    expenseName = expenseName,
                    amount = amount,
                    paidByUid = paidBy.uid,
                    paidByName = paidBy.name,
                    participantsInSplit = splitAmong,
                    categoryName = categoryName
                )
                showAddExpenseDialog = false
            }
        )
    }

    // Delete trip dialog
    if (showDeleteTripDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteTripDialog = false },
            title = { Text("Delete Trip") },
            text = { Text("Are you sure? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTrip(tripId) {
                            navController.popBackStack()
                        }
                        showDeleteTripDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteTripDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        CategoryPickerDialog(
            onDismiss = { showAddCategoryDialog = false },
            onCategorySelected = { categoryName ->
                val iconName = PredefinedCategories.categories
                    .find { it.name.equals(categoryName, ignoreCase = true) }?.name ?: "Other"
                viewModel.addCategory(tripId, categoryName, iconName)
                showAddCategoryDialog = false
            }
        )
    }

    // Edit Expense Dialog
    if (showEditExpenseDialog && selectedExpenseForEdit != null) {
        EditFirestoreExpenseDialog(
            expense = selectedExpenseForEdit!!,
            participants = currentTrip?.participants ?: emptyList(),
            categories = viewModel.categories.collectAsState().value,
            currencySymbol = currencySymbol,
            onDismiss = { showEditExpenseDialog = false },
            onSave = { updatedName, updatedAmount, updatedPaidBy, updatedSplit, updatedCategory ->
                viewModel.deleteExpense(tripId, selectedExpenseForEdit!!.id)
                viewModel.addExpense(
                    tripId = tripId,
                    expenseName = updatedName,
                    amount = updatedAmount,
                    paidByUid = updatedPaidBy.uid,
                    paidByName = updatedPaidBy.name,
                    participantsInSplit = updatedSplit,
                    categoryName = updatedCategory
                )
                showEditExpenseDialog = false
            }
        )
    }
}

// ─── EXPENSES TAB ─────────────────────────────────────────────────────────────

@Composable
private fun NewExpensesTab(
    expenses: List<FirestoreExpense>,
    currencySymbol: String,
    currentUid: String,
    tripId: String,
    viewModel: FirestoreTripViewModel,
    participants: List<FirestoreParticipant>,
    categories: List<FirestoreCategory>,
    onDeleteExpense: (FirestoreExpense) -> Unit,
    onEditExpense: (FirestoreExpense) -> Unit
) {
    if (expenses.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text("No Expenses Yet", style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Tap + to add an expense", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center)
            }
        }
    } else {
        val total = expenses.sumOf { it.amount }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "Total: $currencySymbol${"%.2f".format(total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            items(expenses) { expense ->
                NewExpenseCard(
                    expense = expense,
                    currencySymbol = currencySymbol,
                    onDelete = { onDeleteExpense(expense) },
                    onEdit = { onEditExpense(expense) }
                )
            }
        }
    }
}

@Composable
private fun NewExpenseCard(
    expense: FirestoreExpense,
    currencySymbol: String,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.expenseName, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground)
                Text("Paid by: ${expense.paidByName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                if (!expense.categoryName.isNullOrEmpty()) {
                    Text("Category: ${expense.categoryName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary)
                }
                if (expense.date.isNotEmpty()) {
                    Text(expense.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }
            Text(
                "$currencySymbol${"%.2f".format(expense.amount)}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit",
                    tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, "Delete",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

// ─── BALANCES TAB ────────────────────────────────────────────���────────────────

@Composable
private fun NewBalancesTab(
    balances: Map<String, Double>,
    settlements: List<Settlement>,
    currencySymbol: String
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Individual Balances", style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold)
        }
        items(balances.entries.toList()) { (name, balance) ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(38.dp).clip(CircleShape).background(IconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        name.firstOrNull()?.toString()?.uppercase() ?: "?",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary, fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(name, modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge)
                val (text, color) = when {
                    balance > 0.01 -> "gets back $currencySymbol${"%.2f".format(abs(balance))}" to PositiveBalanceColor
                    balance < -0.01 -> "owes $currencySymbol${"%.2f".format(abs(balance))}" to NegativeBalanceColor
                    else -> "settled" to MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                }
                Text(text, color = color, fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium)
            }
        }

        if (settlements.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Settlement Plan", style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold)
            }
            items(settlements) { settlement ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(settlement.from, fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp))
                        Text(settlement.to, fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f).padding(start = 8.dp))
                        Text("$currencySymbol${"%.2f".format(settlement.amount)}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        } else {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("All settled!", style = MaterialTheme.typography.bodyMedium,
                    color = PositiveBalanceColor, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ─── PEOPLE TAB ──────────────────────���────────────────────────────────────────

@Composable
private fun PeopleTab(
    participants: List<FirestoreParticipant>,
    currentUid: String
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("${participants.size} Participant${if (participants.size != 1) "s" else ""}",
                style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        items(participants) { participant ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(IconBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            participant.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(participant.name, fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyLarge)
                            if (participant.uid == currentUid) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("(you)", style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Text(participant.email, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
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
    participants: List<FirestoreParticipant>,
    categories: List<FirestoreCategory>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onAdd: (String, Double, FirestoreParticipant, List<FirestoreParticipant>, String?) -> Unit
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
                OutlinedTextField(
                    value = expenseName,
                    onValueChange = { expenseName = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount *") },
                    prefix = { Text(currencySymbol) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Paid by dropdown
                ExposedDropdownMenuBox(
                    expanded = paidByExpanded,
                    onExpandedChange = { paidByExpanded = !paidByExpanded }
                ) {
                    OutlinedTextField(
                        value = paidBy?.name ?: "Select",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Paid by *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = paidByExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = paidByExpanded,
                        onDismissRequest = { paidByExpanded = false }
                    ) {
                        participants.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.name) },
                                onClick = { paidBy = p; paidByExpanded = false }
                            )
                        }
                    }
                }

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "No Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category (Optional)") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("No Category") },
                            onClick = { selectedCategory = null; categoryExpanded = false }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = { selectedCategory = category; categoryExpanded = false }
                            )
                        }
                    }
                }

                HorizontalDivider()
                Text("Split among", fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge)

                participants.forEach { participant ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = splitAmong.contains(participant),
                            onCheckedChange = { checked ->
                                splitAmong = if (checked) splitAmong + participant
                                else splitAmong - participant
                            }
                        )
                        Text(participant.name, modifier = Modifier.weight(1f))
                        if (splitAmong.contains(participant)) {
                            Text("$currencySymbol${"%.2f".format(splitValue)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = paidBy ?: return@Button
                    if (expenseName.isNotBlank() && amountValue > 0 && splitAmong.isNotEmpty()) {
                        onAdd(expenseName, amountValue, p, splitAmong.toList(), selectedCategory?.name)
                    }
                },
                enabled = expenseName.isNotBlank() && amountValue > 0 &&
                        paidBy != null && splitAmong.isNotEmpty()
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

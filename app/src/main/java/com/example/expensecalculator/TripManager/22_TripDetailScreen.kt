package com.example.expensecalculator.TripManager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expensecalculator.tripData.Trip
import com.example.expensecalculator.tripData.TripExpense
import com.example.expensecalculator.tripData.TripParticipant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripDetailScreen(
    navController: NavController,
    viewModel: TripViewModel,
    tripId: Int
) {
    // State for current trip details
    val currentTripParticipants by viewModel.currentTripParticipants.collectAsState()
    val completeTripDetails by viewModel.completeTripDetails.collectAsState()
    val currentTripExpenses by viewModel.currentTripExpenses.collectAsState()
    val showParticipantDialog by viewModel.showParticipantDialog.collectAsState()
    val editingParticipant by viewModel.editingParticipant.collectAsState()

    // Local state for UI
    var selectedTab by remember { mutableStateOf(0) }
    var showAddParticipantsSection by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var participantNames by remember { mutableStateOf(listOf("")) }

    // Load trip data when screen opens
    LaunchedEffect(tripId) {
        viewModel.setCurrentTrip(tripId)
    }

    // Clear trip data when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearCurrentTrip()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = completeTripDetails?.trip?.destination ?: "Trip Details",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        0 -> { // Participants tab
                            if (currentTripParticipants.size < (completeTripDetails?.trip?.participants ?: 0)) {
                                showAddParticipantsSection = !showAddParticipantsSection
                            }
                        }
                        1 -> { // Expenses tab
                            if (currentTripParticipants.isNotEmpty()) {
                                showAddExpenseDialog = true
                            }
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = when (selectedTab) {
                        0 -> if (showAddParticipantsSection) Icons.Default.Close else Icons.Default.PersonAdd
                        1 -> Icons.Default.Add
                        else -> Icons.Default.Add
                    },
                    contentDescription = when (selectedTab) {
                        0 -> if (showAddParticipantsSection) "Close" else "Add Participants"
                        1 -> "Add Expense"
                        else -> "Add"
                    },
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { internalPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(internalPadding)
        ) {
            // Trip Information Card
            TripInfoCard(
                trip = completeTripDetails?.trip,
                totalExpenses = currentTripExpenses.sumOf { it.amount },
                participantCount = currentTripParticipants.size
            )

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Participants") },
                    icon = { Icon(Icons.Default.Groups, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Expenses") },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = null) }
                )
            }

            // Tab Content
            when (selectedTab) {
                0 -> ParticipantsTab(
                    currentTripParticipants = currentTripParticipants,
                    completeTripDetails = completeTripDetails,
                    showAddParticipantsSection = showAddParticipantsSection,
                    participantNames = participantNames,
                    onParticipantNamesChange = { participantNames = it },
                    onAddParticipants = { names ->
                        viewModel.addParticipantsByNames(names.filter { it.isNotBlank() })
                        participantNames = listOf("")
                        showAddParticipantsSection = false
                    },
                    onEditParticipant = { viewModel.showEditParticipantDialog(it) },
                    onDeleteParticipant = { viewModel.deleteParticipant(it) }
                )
                1 -> ExpensesTab(
                    currentTripExpenses = currentTripExpenses,
                    currentTripParticipants = currentTripParticipants,
                    onDeleteExpense = { viewModel.deleteExpense(it) }
                )
            }
        }
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            participants = currentTripParticipants,
            onDismiss = { showAddExpenseDialog = false },
            onAddExpense = { expense ->
                viewModel.addExpense(expense)
                showAddExpenseDialog = false
            }
        )
    }

    // Participant Dialog
    if (showParticipantDialog) {
        ParticipantDialog(
            participant = editingParticipant,
            onDismiss = { viewModel.hideParticipantDialog() },
            onSave = { name, contact, email ->
                if (editingParticipant != null) {
                    viewModel.updateParticipant(editingParticipant!!, name, contact, email)
                } else {
                    viewModel.addParticipant(name, contact, email)
                }
            }
        )
    }
}

@Composable
fun TripInfoCard(trip: Trip?, totalExpenses: Double, participantCount: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Trip Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (trip != null) {
                InfoRow(icon = Icons.Default.Place, label = "Destination", value = trip.destination)
                InfoRow(icon = Icons.Default.CalendarToday, label = "Days", value = "${trip.days} days")
                InfoRow(icon = Icons.Default.Groups, label = "Participants", value = "$participantCount/${trip.participants}")
                InfoRow(icon = Icons.Default.AttachMoney, label = "Budget", value = "$${trip.expenditure}")
                InfoRow(icon = Icons.Default.Receipt, label = "Total Spent", value = "$${String.format("%.2f", totalExpenses)}")

                if (participantCount > 0 && totalExpenses > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Per Person Cost:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$${String.format("%.2f", totalExpenses / participantCount)}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantsTab(
    currentTripParticipants: List<TripParticipant>,
    completeTripDetails: Any?, // Replace with your actual type
    showAddParticipantsSection: Boolean,
    participantNames: List<String>,
    onParticipantNamesChange: (List<String>) -> Unit,
    onAddParticipants: (List<String>) -> Unit,
    onEditParticipant: (TripParticipant) -> Unit,
    onDeleteParticipant: (TripParticipant) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Participants Progress
        item {
            ParticipantsProgressCard(
                currentCount = currentTripParticipants.size,
                totalCount = (completeTripDetails as? com.example.expensecalculator.tripData.CompleteTripDetails)?.trip?.participants ?: 0
            )
        }

        // Add Participants Section
        if (showAddParticipantsSection) {
            item {
                AddParticipantsSection(
                    participantNames = participantNames,
                    onParticipantNamesChange = onParticipantNamesChange,
                    remainingSlots = ((completeTripDetails as? com.example.expensecalculator.tripData.CompleteTripDetails)?.trip?.participants ?: 0) - currentTripParticipants.size,
                    onAddParticipants = onAddParticipants
                )
            }
        }

        // Current Participants List
        item {
            Text(
                text = "Current Participants",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (currentTripParticipants.isEmpty()) {
            item {
                EmptyParticipantsCard()
            }
        } else {
            items(currentTripParticipants) { participant ->
                ParticipantCard(
                    participant = participant,
                    onEdit = { onEditParticipant(participant) },
                    onDelete = { onDeleteParticipant(participant) }
                )
            }
        }
    }
}

@Composable
fun ExpensesTab(
    currentTripExpenses: List<TripExpense>,
    currentTripParticipants: List<TripParticipant>,
    onDeleteExpense: (TripExpense) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (currentTripParticipants.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = "No participants",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Add participants first",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Text(
                            "You need to add participants before adding expenses",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Expense Summary Card
            item {
                ExpenseSummaryCard(
                    totalExpenses = currentTripExpenses.sumOf { it.amount },
                    expenseCount = currentTripExpenses.size,
                    participantCount = currentTripParticipants.size
                )
            }

            // Expenses List
            if (currentTripExpenses.isEmpty()) {
                item {
                    EmptyExpensesCard()
                }
            } else {
                items(currentTripExpenses) { expense ->
                    ExpenseCard(
                        expense = expense,
                        onDelete = { onDeleteExpense(expense) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseSummaryCard(totalExpenses: Double, expenseCount: Int, participantCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$${String.format("%.2f", totalExpenses)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Total Spent",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$expenseCount",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Expenses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (participantCount > 0) "$${String.format("%.2f", totalExpenses / participantCount)}" else "$0.00",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Per Person",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun EmptyExpensesCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = "No expenses",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No expenses added yet",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tap the + button to add your first expense",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ExpenseCard(expense: TripExpense, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = "Expense",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.expenseName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Paid by: ${expense.paidBy}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                expense.date?.let { date ->
                    Text(
                        text = "Date: $date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "$${String.format("%.2f", expense.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(
    participants: List<TripParticipant>,
    onDismiss: () -> Unit,
    onAddExpense: (TripExpense) -> Unit
) {
    var expenseName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedExpenseType by remember { mutableStateOf("Group") } // "Group" or "Personal"
    var selectedParticipant by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Expense") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Expense Type Selection
                Text(
                    text = "Expense Type",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Row {
                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = selectedExpenseType == "Group",
                                onClick = { selectedExpenseType = "Group" }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedExpenseType == "Group",
                            onClick = { selectedExpenseType = "Group" }
                        )
                        Text("Group Expense")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Row(
                        modifier = Modifier
                            .selectable(
                                selected = selectedExpenseType == "Personal",
                                onClick = { selectedExpenseType = "Personal" }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedExpenseType == "Personal",
                            onClick = { selectedExpenseType = "Personal" }
                        )
                        Text("Personal")
                    }
                }

                OutlinedTextField(
                    value = expenseName,
                    onValueChange = { expenseName = it; showError = false },
                    label = { Text("Expense Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && expenseName.isBlank(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it; showError = false },
                    label = { Text("Amount *") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = showError && (amount.isBlank() || amount.toDoubleOrNull() == null),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Text("$") }
                )

                // Paid By Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedParticipant,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Paid By *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        isError = showError && selectedParticipant.isBlank(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        participants.forEach { participant ->
                            DropdownMenuItem(
                                text = { Text(participant.participantName) },
                                onClick = {
                                    selectedParticipant = participant.participantName
                                    expanded = false
                                    showError = false
                                }
                            )
                        }
                    }
                }

                if (selectedExpenseType == "Group") {
                    Text(
                        text = "This expense will be split equally among all ${participants.size} participants",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(
                        text = "This is a personal expense for the selected participant",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                if (showError) {
                    Text(
                        text = "Please fill in all required fields with valid values",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (expenseName.isNotBlank() && amountValue != null && amountValue > 0 && selectedParticipant.isNotBlank()) {
                        // Create expense with expense type indicator in the name
                        val finalExpenseName = if (selectedExpenseType == "Group") {
                            "$expenseName (Group - Split ${participants.size} ways)"
                        } else {
                            "$expenseName (Personal - ${selectedParticipant})"
                        }

                        val expense = TripExpense(
                            tripId = 0, // This will be set by the ViewModel
                            expenseName = finalExpenseName,
                            amount = amountValue,
                            paidBy = selectedParticipant,
                            date = currentDate
                        )
                        onAddExpense(expense)
                    } else {
                        showError = true
                    }
                }
            ) {
                Text("Add Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Keep all the existing composables from your original code
@Composable
fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ParticipantsProgressCard(currentCount: Int, totalCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (currentCount == totalCount)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Participants Added",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$currentCount / $totalCount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = if (totalCount > 0) currentCount.toFloat() / totalCount.toFloat() else 0f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            if (currentCount < totalCount) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${totalCount - currentCount} participants remaining",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AddParticipantsSection(
    participantNames: List<String>,
    onParticipantNamesChange: (List<String>) -> Unit,
    remainingSlots: Int,
    onAddParticipants: (List<String>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Add Participants",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            participantNames.forEachIndexed { index, name ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { newName ->
                            val newList = participantNames.toMutableList()
                            newList[index] = newName
                            onParticipantNamesChange(newList)
                        },
                        label = { Text("Participant ${index + 1}") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Person",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    )

                    if (participantNames.size > 1) {
                        IconButton(
                            onClick = {
                                val newList = participantNames.toMutableList()
                                newList.removeAt(index)
                                onParticipantNamesChange(newList)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (participantNames.size < remainingSlots) {
                    OutlinedButton(
                        onClick = {
                            onParticipantNamesChange(participantNames + "")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add More",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add More")
                    }
                }

                Button(
                    onClick = { onAddParticipants(participantNames) },
                    modifier = Modifier.weight(1f),
                    enabled = participantNames.any { it.isNotBlank() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save Participants")
                }
            }
        }
    }
}

@Composable
fun ParticipantCard(
    participant: TripParticipant,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Participant",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = participant.participantName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                if (!participant.contactNumber.isNullOrBlank() || !participant.email.isNullOrBlank()) {
                    Text(
                        text = listOfNotNull(participant.contactNumber, participant.email)
                            .filter { it.isNotBlank() }
                            .joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptyParticipantsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = "No participants",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No participants added yet",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Tap the + button to add participants",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticipantDialog(
    participant: TripParticipant?,
    onDismiss: () -> Unit,
    onSave: (String, String?, String?) -> Unit
) {
    var name by remember { mutableStateOf(participant?.participantName ?: "") }
    var contact by remember { mutableStateOf(participant?.contactNumber ?: "") }
    var email by remember { mutableStateOf(participant?.email ?: "") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (participant == null) "Add Participant" else "Edit Participant")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; showError = false },
                    label = { Text("Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = showError && name.isBlank(),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    shape = RoundedCornerShape(12.dp)
                )

                if (showError) {
                    Text(
                        text = "Name is required",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            name.trim(),
                            contact.takeIf { it.isNotBlank() },
                            email.takeIf { it.isNotBlank() }
                        )
                        onDismiss()
                    } else {
                        showError = true
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
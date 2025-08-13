package com.example.expensecalculator.TripManager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.expensecalculator.tripData.Trip
import com.example.expensecalculator.tripData.TripExpense
import com.example.expensecalculator.tripData.TripParticipant
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// --- Custom Color Palette based on the provided images ---
val DarkPurple = Color(0xFF3A1D7E)
val MidPurple = Color(0xFF6D35DE)
val LightPurple = Color(0xFFF3EFFF)
val AccentBlue = Color(0xFF0084F4)
val OffWhite = Color(0xFFFAFAFA)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFE0E0E0)
val CardBackground = Color(0xFFFFFFFF)
val ErrorColor = Color(0xFFD32F2F)

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
        modifier = Modifier.fillMaxSize().background(OffWhite),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = completeTripDetails?.trip?.destination ?: "Trip Details",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.background(
                    Brush.horizontalGradient(
                        colors = listOf(DarkPurple, MidPurple)
                    )
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = TextPrimary
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
                containerColor = AccentBlue,
                shape = RoundedCornerShape(16.dp)
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
                    tint = TextPrimary
                )
            }
        }
    ) { internalPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(internalPadding)
                .background(OffWhite)
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
                modifier = Modifier.fillMaxWidth(),
                containerColor = CardBackground,
                contentColor = MidPurple,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = 3.dp,
                        color = AccentBlue
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Participants", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    icon = { Icon(Icons.Default.Groups, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Expenses", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
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
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Brush.verticalGradient(listOf(MidPurple, DarkPurple)))
                .padding(20.dp)
        ) {
            if (trip != null) {
                InfoRow(icon = Icons.Default.Place, label = "Destination", value = trip.destination)
                InfoRow(icon = Icons.Default.CalendarToday, label = "Days", value = "${trip.days} days")
                InfoRow(icon = Icons.Default.Groups, label = "Participants", value = "${participantCount}/${trip.participants}")
                InfoRow(icon = Icons.Default.AttachMoney, label = "Budget", value = "₹${trip.expenditure}")
                InfoRow(icon = Icons.Default.Receipt, label = "Total Spent", value = "₹${String.format("%.2f", totalExpenses)}")

                if (participantCount > 0 && totalExpenses > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = TextSecondary.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Per Person Cost:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "₹${String.format("%.2f", totalExpenses / participantCount)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            } else {
                CircularProgressIndicator(color = Color.White)
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
            .padding(top = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
        if (currentTripParticipants.isNotEmpty()){
            item {
                Text(
                    text = "Current Participants",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DarkPurple,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }


        if (currentTripParticipants.isEmpty() && !showAddParticipantsSection) {
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
            .padding(top = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (currentTripParticipants.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = ErrorColor.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.PersonOff,
                            contentDescription = "No participants",
                            modifier = Modifier.size(48.dp),
                            tint = ErrorColor
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Add Participants First",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = ErrorColor
                        )
                        Text(
                            "You need to add trip participants before you can log any expenses.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.DarkGray,
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightPurple)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "₹${String.format("%.2f", totalExpenses)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkPurple
                )
                Text(
                    text = "Total Spent",
                    style = MaterialTheme.typography.bodySmall,
                    color = MidPurple
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$expenseCount",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkPurple
                )
                Text(
                    text = "Expenses",
                    style = MaterialTheme.typography.bodySmall,
                    color = MidPurple
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (participantCount > 0) "₹${String.format("%.2f", totalExpenses / participantCount)}" else "₹0.00",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DarkPurple
                )
                Text(
                    text = "Per Person",
                    style = MaterialTheme.typography.bodySmall,
                    color = MidPurple
                )
            }
        }
    }
}

@Composable
fun EmptyExpensesCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = "No expenses",
                modifier = Modifier.size(56.dp),
                tint = MidPurple.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Expenses Added Yet",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = DarkPurple
            )
            Text(
                text = "Tap the + button to add your first expense",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun ExpenseCard(expense: TripExpense, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.MonetizationOn,
                contentDescription = "Expense",
                tint = AccentBlue,
                modifier = Modifier
                    .size(40.dp)
                    .background(AccentBlue.copy(alpha = 0.1f), CircleShape)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.expenseName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Paid by: ${expense.paidBy}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                expense.date?.let { date ->
                    Text(
                        text = "Date: $date",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Text(
                text = "₹${String.format("%.2f", expense.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkPurple
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = ErrorColor
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
    var selectedParticipant by remember { mutableStateOf<TripParticipant?>(null) }
    var showError by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Expense", fontWeight = FontWeight.Bold) },
        containerColor = CardBackground,
        shape = RoundedCornerShape(20.dp),
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ... (rest of the dialog remains functionally the same, with styling tweaks)
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
                    leadingIcon = { Text("₹", color = MidPurple, fontSize = 18.sp) }
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedParticipant?.participantName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Paid By *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        isError = showError && selectedParticipant == null,
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
                                    selectedParticipant = participant
                                    expanded = false
                                    showError = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (expenseName.isNotBlank() && amountValue != null && amountValue > 0 && selectedParticipant != null) {
                        val expense = TripExpense(
                            tripId = 0, // This will be set by the ViewModel
                            expenseName = expenseName,
                            amount = amountValue,
                            paidBy = selectedParticipant!!.participantName,
                            date = currentDate
                        )
                        onAddExpense(expense)
                    } else {
                        showError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MidPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Add Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
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
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = TextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(120.dp),
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = LightPurple)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Add New Participants",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = DarkPurple
            )
            Spacer(modifier = Modifier.height(12.dp))

            participantNames.forEachIndexed { index, name ->
                OutlinedTextField(
                    value = name,
                    onValueChange = { newName ->
                        val newList = participantNames.toMutableList()
                        newList[index] = newName
                        onParticipantNamesChange(newList)
                    },
                    label = { Text("Participant ${index + 1} Name") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Person",
                            tint = MidPurple
                        )
                    }
                )
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
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MidPurple)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add More",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add More")
                    }
                }

                Button(
                    onClick = { onAddParticipants(participantNames) },
                    modifier = Modifier.weight(1f),
                    enabled = participantNames.any { it.isNotBlank() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MidPurple)
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Save")
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = "Participant",
                tint = MidPurple,
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = participant.participantName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!participant.contactNumber.isNullOrBlank() || !participant.email.isNullOrBlank()) {
                    Text(
                        text = listOfNotNull(participant.contactNumber, participant.email)
                            .filter { it.isNotBlank() }
                            .joinToString(" • "),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = AccentBlue
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = ErrorColor
                )
            }
        }
    }
}

@Composable
fun EmptyParticipantsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
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
                modifier = Modifier.size(56.dp),
                tint = MidPurple.copy(alpha = 0.7f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Participants Yet",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = DarkPurple
            )
            Text(
                text = "Tap the + button to add your first participant",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
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
            Text(text = if (participant == null) "Add Participant" else "Edit Participant", fontWeight = FontWeight.Bold)
        },
        containerColor = CardBackground,
        shape = RoundedCornerShape(20.dp),
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        text = "Participant name is required.",
                        color = ErrorColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
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
                },
                colors = ButtonDefaults.buttonColors(containerColor = MidPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

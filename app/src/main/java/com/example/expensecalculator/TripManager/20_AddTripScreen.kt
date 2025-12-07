package com.example.expensecalculator.TripManager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    navController: NavController,
    viewModel: TripViewModel,
    tripId: Int?
) {
    val isEditMode = tripId != null && tripId != -1

    var title by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("INR") }
    val participants = remember { mutableStateListOf("") }
    var isDataLoaded by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = tripId) {
        if (isEditMode && !isDataLoaded) {
            val tripToEdit = viewModel.getTripById(tripId!!)
            if (tripToEdit != null) {
                title = tripToEdit.trip.title
                selectedCurrency = tripToEdit.trip.currency
                participants.clear()
                participants.addAll(tripToEdit.participants.map { it.participantName })
                if (participants.isEmpty()) participants.add("")
                isDataLoaded = true
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Edit Trip" else "New Trip",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(22.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title Section
            Text(
                "Title",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Enter Destination") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                singleLine = true
            )

            // Currency Section
            Text(
                "Currency",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            CurrencyChip(
                currency = selectedCurrency,
                onClick = { showCurrencyDialog = true },
                modifier = Modifier.fillMaxWidth()
            )

            // Participants Section
            Text(
                "Participants",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )

            participants.forEachIndexed { index, participant ->
                OutlinedTextField(
                    value = participant,
                    onValueChange = { participants[index] = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Participant name") },
                    shape = MaterialTheme.shapes.small,
                    singleLine = true,
                    trailingIcon = {
                        if (participants.size > 1) {
                            IconButton(onClick = { participants.removeAt(index) }) {
                                Icon(
                                    Icons.Default.Close,
                                    "Remove Participant",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            TextButton(
                onClick = { participants.add("") }
            ) {
                Text(
                    "Add Participant",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Create/Update Button
            Button(
                onClick = {
                    val participantNames = participants.filter { it.isNotBlank() }
                    if (title.isNotBlank() && participantNames.isNotEmpty()) {
                        viewModel.saveTrip(
                            tripId = tripId,
                            title = title,
                            participantNames = participantNames,
                            tripIconUri = null,
                            currency = selectedCurrency
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    if (isEditMode) "Update Trip" else "Create Trip",
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    // Currency Selection Dialog
    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currentCurrency = selectedCurrency,
            onDismiss = { showCurrencyDialog = false },
            onCurrencySelected = { currency ->
                selectedCurrency = currency
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyDropdown(
    selected: String,
    onSelect: (String) -> Unit
) {
    val currencies = listOf("Indian Rupee", "US Dollar", "Euro", "Pound Sterling")
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Currency") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.small
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            currencies.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency) },
                    onClick = {
                        onSelect(currency)
                        expanded = false
                    }
                )
            }
        }
    }
}

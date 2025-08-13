package com.example.expensecalculator.TripManager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTripScreen(
    navController: NavController,
    viewModel: TripViewModel,
    tripId: Int
) {
    val trips by viewModel.allTrips.collectAsState(initial = emptyList())
    val trip = trips.find { it.id == tripId }

    var destination by remember { mutableStateOf("") }
    var participants by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }
    var expenditure by remember { mutableStateOf("") }

    var errors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isInitialized by remember { mutableStateOf(false) }

    // Initialize fields when trip is loaded
    LaunchedEffect(trip) {
        if (trip != null && !isInitialized) {
            destination = trip.destination
            participants = trip.participants.toString()
            days = trip.days.toString()
            expenditure = trip.expenditure.toString()
            isInitialized = true
        }
    }

    // Loading state
    if (trip == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(OffWhite),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MidPurple)
                Text(
                    "Loading trip details...",
                    modifier = Modifier.padding(top = 16.dp),
                    color = DarkPurple,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(OffWhite),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Edit Trip",
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
                    val newErrors = mutableMapOf<String, String>()
                    if (destination.isBlank()) {
                        newErrors["destination"] = "Destination cannot be empty."
                    }

                    val participantsInt = participants.toIntOrNull()
                    if (participantsInt == null || participantsInt <= 0) {
                        newErrors["participants"] = "Please enter a valid number."
                    }

                    val daysInt = days.toIntOrNull()
                    if (daysInt == null || daysInt <= 0) {
                        newErrors["days"] = "Please enter a valid number of days."
                    }

                    val expenditureDouble = expenditure.toDoubleOrNull()
                    if (expenditureDouble == null || expenditureDouble <= 0) {
                        newErrors["expenditure"] = "Please enter a valid budget amount."
                    }

                    errors = newErrors

                    if (newErrors.isEmpty()) {
                        viewModel.updateTrip(trip, destination, participantsInt!!, daysInt!!, expenditureDouble!!)
                        navController.popBackStack()
                    }
                },
                containerColor = AccentBlue,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Update Trip", tint = TextPrimary)
            }
        }
    ) { internalPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(internalPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            TitledTextField(
                title = "Destination",
                icon = Icons.Default.Place,
                value = destination,
                onValueChange = { destination = it; errors = errors - "destination" },
                errorMessage = errors["destination"]
            )
            Spacer(modifier = Modifier.height(24.dp))

            TitledTextField(
                title = "Number of Participants",
                icon = Icons.Default.Groups,
                value = participants,
                onValueChange = { participants = it; errors = errors - "participants" },
                keyboardType = KeyboardType.Number,
                errorMessage = errors["participants"]
            )
            Spacer(modifier = Modifier.height(24.dp))

            TitledTextField(
                title = "Number of Days",
                icon = Icons.Default.CalendarToday,
                value = days,
                onValueChange = { days = it; errors = errors - "days" },
                keyboardType = KeyboardType.Number,
                errorMessage = errors["days"]
            )
            Spacer(modifier = Modifier.height(24.dp))

            TitledTextField(
                title = "Total Budget",
                icon = Icons.Default.AttachMoney,
                value = expenditure,
                onValueChange = { expenditure = it; errors = errors - "expenditure" },
                keyboardType = KeyboardType.Decimal,
                errorMessage = errors["expenditure"]
            )
        }
    }
}

@Composable
private fun TitledTextField(
    title: String,
    icon: ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    errorMessage: String?
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MidPurple,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = DarkPurple
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            isError = errorMessage != null,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MidPurple,
                unfocusedBorderColor = Color.LightGray,
                errorBorderColor = ErrorColor
            )
        )
        AnimatedVisibility(visible = errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = ErrorColor,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

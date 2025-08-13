package com.example.expensecalculator.TripManager

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
import androidx.compose.ui.draw.clip
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
fun AddTripScreen(
    navController: NavController,
    viewModel: TripViewModel // Corrected ViewModel import
) {
    var destination by remember { mutableStateOf("") }
    var participants by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }
    var expenditure by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(OffWhite),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add New Trip",
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
                    val participantsInt = participants.toIntOrNull()
                    val daysInt = days.toIntOrNull()
                    val expenditureDouble = expenditure.toDoubleOrNull()

                    if (destination.isNotBlank() &&
                        participantsInt != null && participantsInt > 0 &&
                        daysInt != null && daysInt > 0 &&
                        expenditureDouble != null && expenditureDouble > 0) {
                        viewModel.addTrip(destination, participantsInt, daysInt, expenditureDouble)
                        navController.popBackStack()
                    } else {
                        showError = true
                    }
                },
                containerColor = AccentBlue,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Trip", tint = TextPrimary)
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
                onValueChange = { destination = it; showError = false },
                isError = showError && destination.isBlank()
            )
            Spacer(modifier = Modifier.height(24.dp))

            TitledTextField(
                title = "Number of Participants",
                icon = Icons.Default.Groups,
                value = participants,
                onValueChange = { participants = it; showError = false },
                keyboardType = KeyboardType.Number,
                isError = showError && (participants.toIntOrNull() ?: 0) <= 0
            )
            Spacer(modifier = Modifier.height(24.dp))

            TitledTextField(
                title = "Number of Days",
                icon = Icons.Default.CalendarToday,
                value = days,
                onValueChange = { days = it; showError = false },
                keyboardType = KeyboardType.Number,
                isError = showError && (days.toIntOrNull() ?: 0) <= 0
            )
            Spacer(modifier = Modifier.height(24.dp))

            TitledTextField(
                title = "Total Budget",
                icon = Icons.Default.AttachMoney,
                value = expenditure,
                onValueChange = { expenditure = it; showError = false },
                keyboardType = KeyboardType.Decimal,
                isError = showError && (expenditure.toDoubleOrNull() ?: 0.0) <= 0
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (showError) {
                ErrorDisplay(message = "Please fill all fields with valid values.")
            }
        }
    }
}

@Composable
fun TitledTextField(
    title: String,
    icon: ImageVector,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean
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
            isError = isError,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MidPurple,
                unfocusedBorderColor = Color.LightGray,
                errorBorderColor = ErrorColor
            )
        )
    }
}

@Composable
fun ErrorDisplay(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(ErrorColor.copy(alpha = 0.1f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "Error",
            tint = ErrorColor
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            color = ErrorColor,
            fontWeight = FontWeight.Medium
        )
    }
}

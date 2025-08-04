package com.example.expensecalculator.TripManager

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.expensecalculator.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    navController: NavController,
    viewModel: TripViewModel
) {
    var destination by remember { mutableStateOf("") }
    var participants by remember { mutableStateOf("") }
    var days by remember { mutableStateOf("") }
    var expenditure by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Add New Trip",
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
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save Trip", tint = MaterialTheme.colorScheme.onPrimary)
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
            TitledTextField(
                title = "Destination",
                icon = Icons.Default.Place,
                value = destination,
                onValueChange = { destination = it; showError = false },
                isError = showError && destination.isBlank()
            )
            Spacer(modifier = Modifier.height(16.dp))

            TitledTextField(
                title = "Number of Participants",
                icon = Icons.Default.Groups,
                value = participants,
                onValueChange = { participants = it; showError = false },
                keyboardType = KeyboardType.Number,
                isError = showError && (participants.toIntOrNull() ?: 0) <= 0
            )
            Spacer(modifier = Modifier.height(16.dp))

            TitledTextField(
                title = "Number of Days",
                icon = Icons.Default.CalendarToday,
                value = days,
                onValueChange = { days = it; showError = false },
                keyboardType = KeyboardType.Number,
                isError = showError && (days.toIntOrNull() ?: 0) <= 0
            )
            Spacer(modifier = Modifier.height(16.dp))

            TitledTextField(
                title = "Total Expenditure",
                icon = Icons.Default.AttachMoney,
                value = expenditure,
                onValueChange = { expenditure = it; showError = false },
                keyboardType = KeyboardType.Decimal,
                isError = showError && (expenditure.toDoubleOrNull() ?: 0.0) <= 0
            )
            Spacer(modifier = Modifier.height(24.dp))

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
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
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
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
fun ErrorDisplay(message: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Medium
        )
    }
}

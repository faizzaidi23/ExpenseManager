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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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



    var showError by remember { mutableStateOf(false) }

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



    if (trip == null) {

        Box(

            modifier = Modifier.fillMaxSize(),

            contentAlignment = Alignment.Center

        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                CircularProgressIndicator()

                Text(

                    "Loading trip details...",

                    modifier = Modifier.padding(top = 16.dp)

                )

            }

        }

        return

    }



    Scaffold(

        modifier = Modifier.fillMaxSize(),

        topBar = {

            TopAppBar(

                title = {

                    Text(

                        text = "Edit Trip",

                        fontSize = 28.sp,

                        fontWeight = FontWeight.ExtraBold,

                        textAlign = TextAlign.Center

                    )

                },

                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondary),

                navigationIcon = {

                    IconButton(

                        onClick = { navController.popBackStack() }

                    ) {

                        Icon(

                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,

                            contentDescription = "Go back"

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



                        viewModel.updateTrip(trip, destination, participantsInt, daysInt, expenditureDouble)

                        navController.popBackStack()

                    } else {

                        showError = true

                    }

                }

            ) {

                Icon(Icons.Default.Save, contentDescription = "Update Trip")

            }

        }

    ) { internalPadding ->

        Column(

            modifier = Modifier

                .fillMaxSize()

                .padding(internalPadding)

                .padding(16.dp),

            horizontalAlignment = Alignment.CenterHorizontally,

            verticalArrangement = Arrangement.spacedBy(16.dp)

        ) {



            Text(

                "Edit Trip Details",

                fontSize = 32.sp,

                fontWeight = FontWeight.ExtraBold,

                modifier = Modifier.padding(vertical = 16.dp)

            )



            OutlinedTextField(

                value = destination,

                onValueChange = {

                    destination = it

                    showError = false

                },

                label = { Text("Destination") },

                modifier = Modifier.fillMaxWidth(),

                singleLine = true,

                isError = showError && destination.isBlank()

            )



            OutlinedTextField(

                value = participants,

                onValueChange = {

                    participants = it

                    showError = false

                },

                label = { Text("Number of Participants") },

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),

                modifier = Modifier.fillMaxWidth(),

                singleLine = true,

                isError = showError && (participants.toIntOrNull() ?: 0) <= 0

            )



            OutlinedTextField(

                value = days,

                onValueChange = {

                    days = it

                    showError = false

                },

                label = { Text("Number of Days") },

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),

                modifier = Modifier.fillMaxWidth(),

                singleLine = true,

                isError = showError && (days.toIntOrNull() ?: 0) <= 0

            )



            OutlinedTextField(

                value = expenditure,

                onValueChange = {

                    expenditure = it

                    showError = false

                },

                label = { Text("Total Expenditure ($)") },

                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),

                modifier = Modifier.fillMaxWidth(),

                singleLine = true,

                isError = showError && (expenditure.toDoubleOrNull() ?: 0.0) <= 0

            )



            if (showError) {

                Card(

                    modifier = Modifier.fillMaxWidth(),

                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)

                ) {

                    Text(

                        text = "Please fill all fields with valid values",

                        color = MaterialTheme.colorScheme.onErrorContainer,

                        modifier = Modifier.padding(16.dp),

                        textAlign = TextAlign.Center

                    )

                }

            }



// Preview card if all fields are valid

            val participantsInt = participants.toIntOrNull()

            val daysInt = days.toIntOrNull()

            val expenditureDouble = expenditure.toDoubleOrNull()



            if (destination.isNotBlank() &&

                participantsInt != null && participantsInt > 0 &&

                daysInt != null && daysInt > 0 &&

                expenditureDouble != null && expenditureDouble > 0) {



                Card(

                    modifier = Modifier.fillMaxWidth(),

                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)

                ) {

                    Column(

                        modifier = Modifier.padding(16.dp),

                        horizontalAlignment = Alignment.CenterHorizontally

                    ) {

                        Text(

                            "Updated Preview",

                            fontSize = 18.sp,

                            fontWeight = FontWeight.Bold,

                            color = MaterialTheme.colorScheme.onPrimaryContainer

                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Destination: $destination", color = MaterialTheme.colorScheme.onPrimaryContainer)

                        Text("Participants: $participantsInt", color = MaterialTheme.colorScheme.onPrimaryContainer)

                        Text("Days: $daysInt", color = MaterialTheme.colorScheme.onPrimaryContainer)

                        Text("Total: $${String.format("%.2f", expenditureDouble)}", color = MaterialTheme.colorScheme.onPrimaryContainer)

                        Text(

                            "Per Person: ${String.format("%.2f", expenditureDouble / participantsInt)}",

                            fontWeight = FontWeight.Bold,

                            color = MaterialTheme.colorScheme.onPrimaryContainer

                        )

                    }

                }

            }

        }

    }

}


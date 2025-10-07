package com.example.expensecalculator.TripManager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// --- THEME COLORS (Copied from other screens for consistency) ---
 val DarkBlueBackground = Color(0xFF1A387E)
// Added missing color
 val WhiteCard = Color(0xFFFFFFFF)
 val DarkGreyText = Color(0xFF555555)
 val HintGray = Color(0xFF8A8A8A)
 val LightGray = Color(0xFFD3D3D3)
// --- END THEME COLORS ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripScreen(
    navController: NavController,
    viewModel: TripViewModel,
    tripId: Int?
) {
    val isEditMode = tripId != null && tripId != -1

    var title by remember { mutableStateOf("") }
    val participants = remember { mutableStateListOf("") }
    var isDataLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = tripId) {
        if (isEditMode && !isDataLoaded) {
            val tripToEdit = viewModel.getTripById(tripId!!)
            if (tripToEdit != null) {
                title = tripToEdit.trip.title
                participants.clear()
                participants.addAll(tripToEdit.participants.map { it.participantName })
                if (participants.isEmpty()) participants.add("")
                isDataLoaded = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Edit Trip" else "Add New Trip",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Go back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBlueBackground
                )
            )
        },
        containerColor = DarkBlueBackground
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                color = WhiteCard,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SectionTitle(text = "Title")
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("E.g. City Trip", color = HintGray) },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = themedTextFieldColors()
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    SectionTitle(text = "Participants")
                    participants.forEachIndexed { index, participant ->
                        OutlinedTextField(
                            value = participant,
                            onValueChange = { participants[index] = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Participant name", color = HintGray) },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = themedTextFieldColors(),
                            trailingIcon = {
                                if (participants.size > 1) {
                                    IconButton(onClick = { participants.removeAt(index) }) {
                                        Icon(Icons.Default.Close, "Remove Participant", tint = HintGray)
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    TextButton(
                        onClick = { participants.add("") },
                        modifier = Modifier.align(Alignment.Start)
                    ) {
                        Text("+ Add Participant", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val participantNames = participants.filter { it.isNotBlank() }
                            if (title.isNotBlank() && participantNames.isNotEmpty()) {
                                // --- CORRECTED: Call the single saveTrip function ---
                                // It handles both add and edit mode automatically
                                viewModel.saveTrip(
                                    tripId = tripId,
                                    title = title,
                                    participantNames = participantNames
                                )
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text(
                            if (isEditMode) "Update Trip" else "Create Trip",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = DarkGreyText,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun themedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryBlue,
    unfocusedBorderColor = LightGray,
    focusedLabelColor = PrimaryBlue,
    unfocusedLabelColor = HintGray,
    cursorColor = PrimaryBlue,
    unfocusedContainerColor = Color(0xFFF0F0F0),
    focusedContainerColor = Color(0xFFF0F0F0)
)
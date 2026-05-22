package com.example.expensecalculator.TripManager

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.expensecalculator.firestore.FirestoreTripViewModel
import com.example.expensecalculator.firestore.FirestoreUser
import com.example.expensecalculator.ui.theme.IconBackground

// ─── CREATE TRIP SCREEN ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewAddTripScreen(
    navController: NavController,
    viewModel: FirestoreTripViewModel
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("INR") }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("New Trip", style = MaterialTheme.typography.titleMedium)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
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
            Text("Title", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Enter trip name") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                singleLine = true
            )

            Text("Currency", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            CurrencyChip(
                currency = selectedCurrency,
                onClick = { showCurrencyDialog = true },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    if (title.isBlank()) {
                        Toast.makeText(context, "Please enter a trip title", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    viewModel.createTrip(
                        title = title,
                        currency = selectedCurrency,
                        onSuccess = { tripId ->
                            // After creating, go to invite screen for this trip
                            navController.navigate("invite_participants/$tripId")
                        },
                        onError = { msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                enabled = !isLoading && title.isNotBlank(),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Create Trip", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            currentCurrency = selectedCurrency,
            onDismiss = { showCurrencyDialog = false },
            onCurrencySelected = { selectedCurrency = it }
        )
    }
}

// ─── INVITE PARTICIPANTS SCREEN ──────────────────────────────────────────────
// Shown after creating a trip — search and invite users

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InviteParticipantsScreen(
    navController: NavController,
    viewModel: FirestoreTripViewModel,
    tripId: String
) {
    val context = LocalContext.current
    val currentTrip by viewModel.currentTrip.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var invitedUsers by remember { mutableStateOf<Set<String>>(emptySet()) } // track invited uids

    LaunchedEffect(tripId) {
        viewModel.setCurrentTrip(tripId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invite Participants", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        navController.navigate("new_trip_detail/$tripId")
                    }) {
                        Text("Done", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
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
                .padding(16.dp)
        ) {
            // Current participants
            currentTrip?.let { trip ->
                if (trip.participants.isNotEmpty()) {
                    Text(
                        "Current Participants (${trip.participants.size})",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    trip.participants.forEach { participant ->
                        ParticipantChip(name = participant.name, email = participant.email)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchUsers(it)
                },
                placeholder = { Text("Search by name or email") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            viewModel.clearSearch()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (searchQuery.length in 1..1) {
                Text(
                    "Type at least 2 characters to search",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Search results
            if (isSearching) {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(searchResults) { user ->
                        val alreadyParticipant = currentTrip?.participants?.any { it.uid == user.uid } == true
                        val alreadyInvited = invitedUsers.contains(user.uid)

                        SearchResultCard(
                            user = user,
                            alreadyParticipant = alreadyParticipant,
                            alreadyInvited = alreadyInvited,
                            onInvite = {
                                val trip = currentTrip ?: return@SearchResultCard
                                viewModel.sendInvite(
                                    trip = trip,
                                    toUser = user,
                                    onSuccess = {
                                        invitedUsers = invitedUsers + user.uid
                                        Toast.makeText(context, "Invite sent to ${user.name}", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantChip(name: String, email: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(IconBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.firstOrNull()?.toString()?.uppercase() ?: "?",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(email, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun SearchResultCard(
    user: FirestoreUser,
    alreadyParticipant: Boolean,
    alreadyInvited: Boolean,
    onInvite: () -> Unit
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
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(IconBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                Text(user.email, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            when {
                alreadyParticipant -> {
                    Text(
                        "In trip",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
                alreadyInvited -> {
                    Text(
                        "Invited",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        fontWeight = FontWeight.Medium
                    )
                }
                else -> {
                    IconButton(onClick = onInvite) {
                        Icon(
                            Icons.Default.PersonAdd,
                            contentDescription = "Invite",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

// ─── INVITE FROM TRIP DETAIL (already existing trip) ────────────────────────
// Reuse InviteParticipantsScreen — same screen, just navigated from detail
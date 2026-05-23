package com.example.expensecalculator.TripManager

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.expensecalculator.firestore.FirestoreCategory
import com.example.expensecalculator.firestore.FirestoreExpense
import com.example.expensecalculator.firestore.FirestoreParticipant
import com.example.expensecalculator.firestore.FirestoreTripViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryExpensesScreen(
    navController: NavController,
    category: FirestoreCategory,
    expenses: List<FirestoreExpense>,
    currencySymbol: String,
    viewModel: FirestoreTripViewModel,
    tripId: String
) {
    val categoryExpenses = expenses.filter { it.categoryName == category.name }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedExpense by remember { mutableStateOf<FirestoreExpense?>(null) }
    val participants by viewModel.currentTrip.collectAsState()
    val categories by viewModel.categories.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(category.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                            tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (categoryExpenses.isEmpty()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
                contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Receipt, null, modifier = Modifier.size(52.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No expenses in this category",
                        style = MaterialTheme.typography.titleMedium)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "Total: $currencySymbol${"%.2f".format(categoryExpenses.sumOf { it.amount })}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                items(categoryExpenses) { expense ->
                    CategoryExpenseCard(
                        expense = expense,
                        currencySymbol = currencySymbol,
                        onEdit = {
                            selectedExpense = expense
                            showEditDialog = true
                        },
                        onDelete = {
                            viewModel.deleteExpense(tripId, expense.id)
                        }
                    )
                }
            }
        }
    }

    if (showEditDialog && selectedExpense != null) {
        EditFirestoreExpenseDialog(
            expense = selectedExpense!!,
            participants = participants?.participants ?: emptyList(),
            categories = categories,
            currencySymbol = currencySymbol,
            onDismiss = { showEditDialog = false },
            onSave = { updatedName, updatedAmount, updatedPaidBy, updatedSplit, updatedCategory ->
                viewModel.deleteExpense(tripId, selectedExpense!!.id)
                viewModel.addExpense(
                    tripId = tripId,
                    expenseName = updatedName,
                    amount = updatedAmount,
                    paidByUid = updatedPaidBy.uid,
                    paidByName = updatedPaidBy.name,
                    participantsInSplit = updatedSplit,
                    categoryName = updatedCategory
                )
                showEditDialog = false
            }
        )
    }
}

@Composable
fun CategoryExpenseCard(
    expense: FirestoreExpense,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

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
                if (expense.date.isNotEmpty()) {
                    Text(expense.date, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            }
            Text("$currencySymbol${"%.2f".format(expense.amount)}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground)
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.DeleteOutline, "Delete",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Delete \"${expense.expenseName}\"?") },
            confirmButton = {
                Button(onClick = { onDelete(); showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}


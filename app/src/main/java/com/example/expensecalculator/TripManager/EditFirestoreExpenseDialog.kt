package com.example.expensecalculator.TripManager

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensecalculator.firestore.FirestoreCategory
import com.example.expensecalculator.firestore.FirestoreParticipant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFirestoreExpenseDialog(
    expense: com.example.expensecalculator.firestore.FirestoreExpense,
    participants: List<FirestoreParticipant>,
    categories: List<FirestoreCategory>,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSave: (String, Double, FirestoreParticipant, List<FirestoreParticipant>, String?) -> Unit
) {
    var expenseName by remember { mutableStateOf(expense.expenseName) }
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var paidBy by remember {
        mutableStateOf(participants.find { it.uid == expense.paidByUid } ?: participants.firstOrNull())
    }
    var splitAmong by remember {
        mutableStateOf(
            if (expense.splits.isNotEmpty()) {
                // If splits exist, filter participants that are in the splits
                val matchedParticipants = participants.filter { p ->
                    expense.splits.any { it.uid == p.uid }
                }.toSet()

                // If we found matching participants, use them; otherwise use paidBy
                if (matchedParticipants.isNotEmpty()) {
                    matchedParticipants
                } else {
                    participants.find { it.uid == expense.paidByUid }?.let { setOf(it) } ?: participants.toSet()
                }
            } else {
                // If no splits exist, default to the paidBy participant only
                participants.find { it.uid == expense.paidByUid }?.let { setOf(it) } ?: participants.toSet()
            }
        )
    }
    var selectedCategory by remember {
        mutableStateOf(categories.find { it.name == expense.categoryName })
    }
    var paidByExpanded by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val amountValue = amount.toDoubleOrNull() ?: 0.0
    val splitValue = if (splitAmong.isNotEmpty()) amountValue / splitAmong.size else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = expenseName,
                    onValueChange = { expenseName = it },
                    label = { Text("Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount *") },
                    prefix = { Text(currencySymbol) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Paid by dropdown
                ExposedDropdownMenuBox(
                    expanded = paidByExpanded,
                    onExpandedChange = { paidByExpanded = !paidByExpanded }
                ) {
                    OutlinedTextField(
                        value = paidBy?.name ?: "Select",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Paid by *") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = paidByExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = paidByExpanded,
                        onDismissRequest = { paidByExpanded = false }
                    ) {
                        participants.forEach { p ->
                            DropdownMenuItem(
                                text = { Text(p.name) },
                                onClick = { paidBy = p; paidByExpanded = false }
                            )
                        }
                    }
                }

                // Category dropdown
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.name ?: "No Category",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category (Optional)") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("No Category") },
                            onClick = { selectedCategory = null; categoryExpanded = false }
                        )
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = { selectedCategory = category; categoryExpanded = false }
                            )
                        }
                    }
                }

                HorizontalDivider()
                Text("Split among", fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge)

                participants.forEach { participant ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = splitAmong.contains(participant),
                            onCheckedChange = { checked ->
                                splitAmong = if (checked) splitAmong + participant
                                else splitAmong - participant
                            }
                        )
                        Text(participant.name, modifier = Modifier.weight(1f))
                        if (splitAmong.contains(participant)) {
                            Text("$currencySymbol${"%.2f".format(splitValue)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = paidBy ?: return@Button
                    if (expenseName.isNotBlank() && amountValue > 0 && splitAmong.isNotEmpty()) {
                        onSave(expenseName, amountValue, p, splitAmong.toList(), selectedCategory?.name)
                    }
                },
                enabled = expenseName.isNotBlank() && amountValue > 0 &&
                        paidBy != null && splitAmong.isNotEmpty()
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

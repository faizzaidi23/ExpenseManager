package com.example.expensecalculator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensecalculator.Data.Expense
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun addExpense(
    accountId: Int,
    onDismissRequest: () -> Unit,
    onSave: (Expense) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        icon = {
            Icon(
                Icons.Default.Receipt,
                contentDescription = "Add Expense Icon",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                "Add New Expense",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = amount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                            amount = newValue
                        }
                        showError = false
                    },
                    label = { Text("Amount*") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Amount",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = showError && (amount.isBlank() || amount.toDoubleOrNull() == null),
                    shape = MaterialTheme.shapes.small
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Description",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    shape = MaterialTheme.shapes.small
                )

                if (showError && (amount.isBlank() || amount.toDoubleOrNull() == null)) {
                    Text(
                        text = "Please enter a valid amount.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalAmount = amount.toDoubleOrNull()
                    if (finalAmount != null && finalAmount > 0) {
                        val newExpense = Expense(
                            amount = finalAmount,
                            description = description,
                            accountId = accountId,
                            date = Date()
                        )
                        onSave(newExpense)
                    } else {
                        showError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = MaterialTheme.shapes.small
            ) {
                Icon(
                    Icons.Default.Save,
                    contentDescription = "Save",
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

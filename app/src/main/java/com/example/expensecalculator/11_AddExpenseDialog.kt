package com.example.expensecalculator

import android.R.attr.label
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensecalculator.Data.Expense

@Composable
fun addExpense(
    accountId: Int,
    onDismissRequest: () -> Unit,
    onSave: (Expense) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") } // Simplified to String

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = {
                // --- SOLUTION ---
                // Validate the amount here before saving
                val finalAmount = amount.toDoubleOrNull()
                if (finalAmount != null) {
                    val newExpense = Expense(
                        amount = finalAmount,
                        description = description.takeIf { it.isNotBlank() }, // Save null if description is blank
                        accountId = accountId
                    )
                    onSave(newExpense)
                }
            }) {
                Text("Add Expense")
            }
        },
        title = { Text("Add Expense") },
        text = {
            Column {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = amount,
                    onValueChange = { newValue ->
                        // Allow any numeric-style input
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                            amount = newValue
                        }
                    },
                    label = { Text("Enter the amount") },
                    // Important for numeric input
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = description, // Use the string directly
                    onValueChange = {
                        description = it
                    },
                    label = { Text("Enter the description (optional)") }
                )
            }
        }
    )
}
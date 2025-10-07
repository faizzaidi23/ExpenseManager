package com.example.expensecalculator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensecalculator.Authentication.HintGray
import com.example.expensecalculator.Data.Expense
// Importing the correct, public theme colors
import com.example.expensecalculator.TripManager.ErrorColor

import com.example.expensecalculator.TripManager.PrimaryBlue
import com.example.expensecalculator.TripManager.PrimaryText
import com.example.expensecalculator.TripManager.ScreenBackground
import com.example.expensecalculator.TripManager.SecondaryText
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
        containerColor = ScreenBackground, // Use theme color
        shape = RoundedCornerShape(20.dp),
        icon = {
            Icon(Icons.Default.Receipt, contentDescription = "Add Expense Icon", tint = PrimaryBlue) // Use theme color
        },
        title = {
            Text("Add New Expense", fontWeight = FontWeight.Bold, color = PrimaryText) // Use theme color
        },
        text = {
            Column {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = amount,
                    onValueChange = { newValue ->
                        // Allow only valid decimal/integer input
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
                            tint = SecondaryText
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = showError && (amount.isBlank() || amount.toDoubleOrNull() == null),
                    shape = RoundedCornerShape(12.dp),
                    colors = themedTextFieldColors() // Use themed colors
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
                            tint = SecondaryText
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = themedTextFieldColors() // Use themed colors
                )

                if (showError && (amount.isBlank() || amount.toDoubleOrNull() == null)) {
                    Text(
                        text = "Please enter a valid amount.",
                        color = ErrorColor,
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
                            date = Date() // Set the current date
                        )
                        onSave(newExpense)
                    } else {
                        showError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue), // Use theme color
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.padding(end = 4.dp))
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel", color = SecondaryText) // Use theme color
            }
        }
    )
}

// Helper function for consistent TextField styling across your app
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
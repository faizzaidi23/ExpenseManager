package com.example.expensecalculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensecalculator.Authentication.HintGray
import com.example.expensecalculator.Data.Expense
import com.example.expensecalculator.TripManager.PrimaryBlue
import com.example.expensecalculator.TripManager.PrimaryText
import com.example.expensecalculator.TripManager.ScreenBackground
import com.example.expensecalculator.TripManager.SecondaryText
import com.example.expensecalculator.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun editExpense(
    expense: Expense,
    onDismissRequest: () -> Unit,
    onSave: (Expense) -> Unit
) {
    var amount by remember { mutableStateOf(expense.amount.toString()) }
    var description by remember { mutableStateOf(expense.description ?: "") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = ScreenBackground,
        shape = RoundedCornerShape(20.dp),
        icon = { Icon(Icons.Default.Edit, "Edit Expense Icon", tint = PrimaryBlue) },
        title = { Text("Edit Expense", fontWeight = FontWeight.Bold, color = PrimaryText) },
        text = {
            Column {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d{0,2}\$"))) {
                            amount = newValue
                        }
                        showError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount*") },
                    isError = showError && (amount.isBlank() || amount.toDoubleOrNull() == null),
                    colors = themedTextFieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description (optional)") },
                    colors = themedTextFieldColors(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalAmount = amount.toDoubleOrNull()
                    if (finalAmount != null && finalAmount > 0) {
                        onSave(expense.copy(amount = finalAmount, description = description))
                    } else {
                        showError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) { Text("Update") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancel", color = SecondaryText) }
        }
    )
}

// Helper function for consistent TextField styling
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
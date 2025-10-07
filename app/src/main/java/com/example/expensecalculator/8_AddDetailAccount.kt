package com.example.expensecalculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensecalculator.Authentication.HintGray
import com.example.expensecalculator.Data.Account

import com.example.expensecalculator.TripManager.PrimaryBlue
import com.example.expensecalculator.TripManager.PrimaryText
import com.example.expensecalculator.TripManager.ScreenBackground
import com.example.expensecalculator.TripManager.SecondaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccount( // Renamed from AddDetail
    onDismissRequest: () -> Unit,
    onSave: (Account) -> Unit
) {
    var newName by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = ScreenBackground,
        shape = RoundedCornerShape(20.dp),
        icon = { Icon(Icons.Default.AddCard, "Add Account Icon", tint = PrimaryBlue) },
        title = { Text("Add New Account", fontWeight = FontWeight.Bold, color = PrimaryText) },
        text = {
            Column {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it; showError = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Account Name*") },
                    isError = showError && newName.isBlank(),
                    colors = themedTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = newDescription,
                    onValueChange = { newDescription = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description (Optional)") },
                    colors = themedTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newName.isNotBlank()) {
                        onSave(Account(name = newName, description = newDescription))
                    } else {
                        showError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) { Text("Save") }
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
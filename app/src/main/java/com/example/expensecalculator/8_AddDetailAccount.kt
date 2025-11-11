package com.example.expensecalculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensecalculator.Data.Account

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccount(
    onDismissRequest: () -> Unit,
    onSave: (Account) -> Unit
) {
    var newName by remember { mutableStateOf("") }
    var newDescription by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large,
        icon = {
            Icon(
                Icons.Default.AddCard,
                "Add Account Icon",
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                "Add New Account",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it; showError = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Account Name*") },
                    isError = showError && newName.isBlank(),
                    shape = MaterialTheme.shapes.small,
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = newDescription,
                    onValueChange = { newDescription = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description (Optional)") },
                    shape = MaterialTheme.shapes.small,
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("Cancel")
            }
        }
    )
}

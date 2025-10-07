package com.example.expensecalculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensecalculator.Data.Account
// Import your central theme colors
import com.example.expensecalculator.TripManager.ErrorColor
import com.example.expensecalculator.TripManager.HintGray
import com.example.expensecalculator.TripManager.LightGray
import com.example.expensecalculator.TripManager.PrimaryBlue
import com.example.expensecalculator.TripManager.PrimaryText
import com.example.expensecalculator.TripManager.ScreenBackground
import com.example.expensecalculator.TripManager.SecondaryText

@Composable
fun EditAccount(
    account: Account,
    onDismissRequest: () -> Unit,
    onSave: (Account) -> Unit
) {
    var updatedName by remember { mutableStateOf(account.name) }
    var updatedDescription by remember { mutableStateOf(account.description ?: "") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = ScreenBackground,
        shape = RoundedCornerShape(20.dp),
        icon = { Icon(Icons.Default.EditNote, "Edit Account Icon", tint = PrimaryBlue) },
        title = { Text("Edit Account", fontWeight = FontWeight.Bold, color = PrimaryText) },
        text = {
            Column {
                OutlinedTextField(
                    value = updatedName,
                    onValueChange = { updatedName = it; showError = false },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Account Name*") },
                    isError = showError && updatedName.isBlank(),
                    colors = themedTextFieldColors(), // This will now work
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = updatedDescription,
                    onValueChange = { updatedDescription = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description (Optional)") },
                    colors = themedTextFieldColors(), // This will now work
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (updatedName.isNotBlank()) {
                        onSave(account.copy(name = updatedName, description = updatedDescription))
                    } else {
                        showError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) { Text("Save Changes") }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancel", color = SecondaryText) }
        }
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
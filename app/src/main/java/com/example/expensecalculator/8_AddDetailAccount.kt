package com.example.expensecalculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DriveFileRenameOutline
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.expensecalculator.Data.Detail
import com.example.expensecalculator.TripManager.CardBackground
import com.example.expensecalculator.TripManager.DarkPurple
import com.example.expensecalculator.TripManager.ErrorColor
import com.example.expensecalculator.TripManager.MidPurple


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDetail(
    onDismissRequest: () -> Unit,
    onSave: (Detail) -> Unit
) {

    var newname by remember { mutableStateOf("") }
    var newdescription by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = CardBackground,
        shape = RoundedCornerShape(20.dp),
        icon = {
            Icon(Icons.Default.AddCard, contentDescription = "Add Account Icon", tint = MidPurple)
        },
        title = {
            Text("Add New Account", fontWeight = FontWeight.Bold, color = DarkPurple)
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newname,
                    onValueChange = {
                        newname = it
                        showError = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Account Name*") },
                    placeholder = { Text("e.g., Groceries, Fuel") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DriveFileRenameOutline,
                            contentDescription = "Name Icon",
                            tint = MidPurple
                        )
                    },
                    singleLine = true,
                    isError = showError && newname.isBlank(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MidPurple,
                        unfocusedBorderColor = Color.LightGray,
                        errorBorderColor = ErrorColor
                    )
                )

                OutlinedTextField(
                    value = newdescription,
                    onValueChange = { newdescription = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Description (Optional)") },
                    placeholder = { Text("e.g., Monthly household food expenses") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Description Icon",
                            tint = MidPurple
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MidPurple,
                        unfocusedBorderColor = Color.LightGray
                    )
                )
                if (showError && newname.isBlank()) {
                    Text(
                        text = "Account name is required.",
                        color = ErrorColor,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newname.isNotBlank()) {
                        val newDetail = Detail(
                            name = newname,
                            description = newdescription,
                            amount = 0.0
                        )
                        onSave(newDetail)
                    } else {
                        showError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MidPurple),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.padding(end = 4.dp))
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissRequest
            ) {
                Text("Cancel", color = Color.Gray)
            }
        }
    )
}

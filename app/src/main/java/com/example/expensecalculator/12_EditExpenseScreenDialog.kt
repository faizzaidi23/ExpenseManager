package com.example.expensecalculator

import android.R.attr.text
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import com.example.expensecalculator.Data.Expense

@Composable
fun editExpense(
    expense: Expense,
    onDismissRequest:()-> Unit,
    onSave:(Expense)-> Unit
){

    var updatedAmount by remember{mutableStateOf(expense.amount)}

    var stringUpdatedAmount by remember{mutableStateOf(updatedAmount.toString())}

    var updatedDescription by remember{mutableStateOf(expense.description.toString())}

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedExpense=expense.copy(
                        amount = updatedAmount,
                        description = updatedDescription,
                        accountId = expense.accountId
                    )

                    onSave(updatedExpense)
                }
            ){
                Text("Update")
            }
        },
        title = {Text("Update Expense")},
        text={
            OutlinedTextField(
                modifier=Modifier.fillMaxWidth(),
                value=stringUpdatedAmount,
                onValueChange = {
                    stringUpdatedAmount=it
                    updatedAmount=stringUpdatedAmount.toDouble()
                },
                label={Text("Enter Updated Amount")}
            )

            Spacer(modifier=Modifier.height(8.dp))

            OutlinedTextField(
                modifier=Modifier.fillMaxWidth(),
                value=updatedDescription,
                onValueChange = {
                    updatedDescription=it
                },
                label={Text("Enter the updated Description ")}
            )
        }
    )

}
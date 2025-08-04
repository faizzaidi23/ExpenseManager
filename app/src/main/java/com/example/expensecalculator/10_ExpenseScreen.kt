package com.example.expensecalculator

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.room.util.copy
import com.example.expensecalculator.Data.Expense

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    viewModel: viewModel,
    accountId: Int,
    onNavigateBack: () -> Unit
) {
    // FIX: Get expenses for the SPECIFIC account, not all expenses.
    val expenseList by viewModel.getExpensesForAccount(accountId).collectAsState(initial = emptyList())


    var showAddExpenseDialog by remember { mutableStateOf(false) }


    var showEditDialog by remember{mutableStateOf(false)}

    var currentExpense by remember{mutableStateOf<Expense?>(null)}

    var copyCurrentExpense=currentExpense

    if (showAddExpenseDialog) {
        addExpense(
            accountId = accountId, // Pass the current account's ID to the dialog
            onDismissRequest = { showAddExpenseDialog = false },
            onSave = { expense ->
                viewModel.addExpense(expense)
                showAddExpenseDialog = false
            }
        )
    }

    // condition to show the edit Dialog or not

    if(showEditDialog && currentExpense!=null){

        editExpense(
            expense= copyCurrentExpense!!,
            onDismissRequest = {showEditDialog=false},
            onSave = {updatedExpense->
                viewModel.updateExpense(updatedExpense)
            }
        )

    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Expense Screen") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { // FIX: Call the navigation function
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors= TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.secondary)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {

                    showAddExpenseDialog = true
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add an Expense")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(expenseList) { expense ->

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "₹${expense.amount}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (!expense.description.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = expense.description,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }

                        Row {
                            IconButton(onClick = {
                                currentExpense=expense
                                showEditDialog=true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Expense")
                            }
                            IconButton(onClick = {viewModel.deleteExpense(expense = expense)}) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete an expense")
                            }
                        }
                    }
                }
            }
        }
    }
}
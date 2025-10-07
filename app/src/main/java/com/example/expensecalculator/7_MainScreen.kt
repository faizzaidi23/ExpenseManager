package com.example.expensecalculator

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.expensecalculator.Data.Account
// --- CORRECTED IMPORTS ---
import com.example.expensecalculator.TripManager.ErrorColor
import com.example.expensecalculator.TripManager.IconBackground
import com.example.expensecalculator.TripManager.PrimaryBlue
import com.example.expensecalculator.TripManager.PrimaryText
import com.example.expensecalculator.TripManager.ScreenBackground
import com.example.expensecalculator.TripManager.SecondaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController, viewModel: ExpenseViewModel) {

    // Added initial value for safety
    val accountsList by viewModel.allAccounts.collectAsState(initial = emptyList())
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var currentAccount by remember { mutableStateOf<Account?>(null) }
    val accountCopy = currentAccount

    if (showAddAccountDialog) {
        // Renamed for consistency
        AddAccount(
            onDismissRequest = { showAddAccountDialog = false },
            onSave = { account ->
                viewModel.addAccount(account)
                showAddAccountDialog = false
            }
        )
    }

    if (showEditDialog && accountCopy != null) {
        EditAccount(
            account = accountCopy,
            onDismissRequest = { showEditDialog = false },
            onSave = { updatedAccount ->
                viewModel.updateAccount(updatedAccount)
                showEditDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Accounts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back", tint = PrimaryText)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ScreenBackground,
                    titleContentColor = PrimaryText
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAccountDialog = true },
                containerColor = PrimaryBlue,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add new Expense Account")
            }
        },
        containerColor = ScreenBackground
    ) { innerpadding ->
        if (accountsList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerpadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.AccountBalanceWallet, "No Accounts",
                        tint = SecondaryText.copy(alpha = 0.7f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No Accounts Yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = PrimaryText)
                    Text("Tap the '+' button to add your first account.", style = MaterialTheme.typography.bodyLarge, color = SecondaryText)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerpadding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(accountsList) { account ->
                    AccountCard(
                        account = account,
                        viewModel = viewModel,
                        onClick = {
                            if (account.id != null) {
                                navController.navigate("expense_screen/${account.id}")
                            }
                        },
                        onEdit = {
                            currentAccount = account
                            showEditDialog = true
                        },
                        onDelete = {
                            viewModel.deleteAccount(account)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun AccountCard(
    account: Account,
    viewModel: ExpenseViewModel,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // Handled nullable ID safely
    val totalAmount by if (account.id != null) {
        viewModel.getTotalForAccount(account.id).collectAsState(initial = 0.0)
    } else {
        remember { mutableStateOf(0.0) }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1f, label = "cardScale")

    Card(
        modifier = Modifier.fillMaxWidth().scale(scale).clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(IconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AccountBalanceWallet, "Account Icon",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = account.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryText)
                if (!account.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = account.description, fontSize = 14.sp, color = SecondaryText)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Paid, "Amount Icon", Modifier.size(18.dp), tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Total Spent: ₹${"%.2f".format(totalAmount ?: 0.0)}", fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit Account info", tint = SecondaryText)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete Account info", tint = ErrorColor)
                }
            }
        }
    }
}
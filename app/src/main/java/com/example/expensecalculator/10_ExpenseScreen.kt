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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.expensecalculator.Data.Expense
import com.example.expensecalculator.TripManager.AccentBlue
import com.example.expensecalculator.TripManager.DarkPurple
import com.example.expensecalculator.TripManager.ErrorColor
import com.example.expensecalculator.TripManager.MidPurple
import com.example.expensecalculator.TripManager.OffWhite
import com.example.expensecalculator.TripManager.TextPrimary
import com.example.expensecalculator.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    viewModel: viewModel,
    accountId: Int,
    onNavigateBack: () -> Unit
) {
    val expenseList by viewModel.getExpensesForAccount(accountId).collectAsState(initial = emptyList())

    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var currentExpense by remember { mutableStateOf<Expense?>(null) }
    val copyCurrentExpense = currentExpense

    if (showAddExpenseDialog) {
        addExpense(
            accountId = accountId,
            onDismissRequest = { showAddExpenseDialog = false },
            onSave = { expense ->
                viewModel.addExpense(expense)
                showAddExpenseDialog = false
            }
        )
    }

    if (showEditDialog && copyCurrentExpense != null) {
        editExpense(
            expense = copyCurrentExpense,
            onDismissRequest = { showEditDialog = false },
            onSave = { updatedExpense ->
                viewModel.updateExpense(updatedExpense)
                showEditDialog = false
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().background(OffWhite),
        topBar = {
            TopAppBar(
                title = { Text("Account Expenses", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = TextPrimary
                ),
                modifier = Modifier.background(
                    Brush.horizontalGradient(
                        colors = listOf(DarkPurple, MidPurple)
                    )
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddExpenseDialog = true },
                containerColor = AccentBlue,
                contentColor = TextPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add an Expense")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { innerPadding ->
        if (expenseList.isEmpty()) {
            EmptyState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(expenseList) { expense ->
                    ExpenseItemCard(
                        expense = expense,
                        onEdit = {
                            currentExpense = expense
                            showEditDialog = true
                        },
                        onDelete = {
                            viewModel.deleteExpense(expense = expense)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ExpenseItemCard(
    expense: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ), label = ""
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onEdit
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = "Expense Icon",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AccentBlue.copy(alpha = 0.1f))
                    .padding(10.dp),
                tint = AccentBlue
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "₹${"%.2f".format(expense.amount)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkPurple
                )
                if (!expense.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = expense.description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray
                    )
                }
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Expense",
                        tint = MidPurple
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete an expense",
                        tint = ErrorColor
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.HourglassEmpty,
            contentDescription = "Empty List",
            modifier = Modifier.size(64.dp),
            tint = MidPurple.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No expenses recorded yet.",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DarkPurple
        )
        Text(
            text = "Tap the '+' button to add your first one.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}

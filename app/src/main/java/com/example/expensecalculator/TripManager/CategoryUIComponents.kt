package com.example.expensecalculator.TripManager

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.expensecalculator.firestore.FirestoreCategory
import com.example.expensecalculator.firestore.FirestoreExpense
import com.example.expensecalculator.firestore.FirestoreTripViewModel

// Predefined categories with their icons
object PredefinedCategories {
    data class CategoryOption(
        val name: String,
        val icon: ImageVector,
        val color: Color
    )

    val categories = listOf(
        CategoryOption("Food & Dining", Icons.Default.Restaurant, Color(0xFFE74C3C)),
        CategoryOption("Transportation", Icons.Default.DirectionsCar, Color(0xFF3498DB)),
        CategoryOption("Shopping", Icons.Default.ShoppingCart, Color(0xFF9B59B6)),
        CategoryOption("Entertainment", Icons.Default.Movie, Color(0xFFF39C12)),
        CategoryOption("Accommodation", Icons.Default.Hotel, Color(0xFF1ABC9C)),
        CategoryOption("Activities", Icons.Default.Hiking, Color(0xFF27AE60)),
        CategoryOption("Utilities", Icons.Default.Lightbulb, Color(0xFFE67E22)),
        CategoryOption("Healthcare", Icons.Default.LocalHospital, Color(0xFFE91E63)),
        CategoryOption("Education", Icons.Default.School, Color(0xFF2196F3)),
        CategoryOption("Gifts & Souvenirs", Icons.Default.CardGiftcard, Color(0xFFFF6B6B)),
        CategoryOption("Personal Care", Icons.Default.Spa, Color(0xFFBA68C8)),
        CategoryOption("Other", Icons.Default.MoreHoriz, Color(0xFF95A5A6))
    )

    fun getCategoryIcon(categoryName: String): Pair<ImageVector, Color> {
        val category = categories.find { it.name.equals(categoryName, ignoreCase = true) }
        return if (category != null) {
            Pair(category.icon, category.color)
        } else {
            Pair(Icons.Default.Category, Color(0xFF7F8C8D))
        }
    }
}

// Enhanced Categories tab content with full UI
@Composable
fun CategoriesTabContent(
    categories: List<FirestoreCategory>,
    currencySymbol: String,
    expenses: List<FirestoreExpense>,
    onAddCategory: (String) -> Unit,
    onDeleteCategory: (String) -> Unit,
    onCategoryClick: (String) -> Unit = {}
) {
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (categories.isEmpty()) {
            EmptyCategoriesState()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { category ->
                    EnhancedCategoryCard(
                        category = category,
                        expenses = expenses,
                        currencySymbol = currencySymbol,
                        onDeleteCategory = { onDeleteCategory(category.id) },
                        onCategoryClick = { onCategoryClick(category.id) }
                    )
                }
            }
        }

        // FAB is handled by the parent screen
    }

    if (showAddCategoryDialog) {
        CategoryPickerDialog(
            onDismiss = { showAddCategoryDialog = false },
            onCategorySelected = { categoryName ->
                onAddCategory(categoryName)
                showAddCategoryDialog = false
            }
        )
    }
}

// Enhanced category card with icon and color
@Composable
fun EnhancedCategoryCard(
    category: FirestoreCategory,
    expenses: List<FirestoreExpense>,
    currencySymbol: String,
    onDeleteCategory: () -> Unit = {},
    onCategoryClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // Filter expenses for this category
    val categoryExpenses = expenses.filter { it.categoryName == category.name }
    val totalAmount = categoryExpenses.sumOf { it.amount }

    // Get icon and color for this category
    val (icon, color) = PredefinedCategories.getCategoryIcon(category.name)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Category Header with Icon - clickable to navigate
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategoryClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp) // Adjusted spacing
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = category.name,
                        modifier = Modifier.size(24.dp),
                        tint = color
                    )
                }

                // Category Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        category.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "${categoryExpenses.size} expense${if (categoryExpenses.size != 1) "s" else ""}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Total Amount
                Text(
                    "$currencySymbol${"%.2f".format(totalAmount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = color
                )

                // Delete Button inline with amount
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Category",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Expand Icon
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { expanded = !expanded },
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Expanded content (expenses in this category)
            if (expanded && categoryExpenses.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categoryExpenses.forEach { expense ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    expense.expenseName,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    "Paid by: ${expense.paidByName}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            Text(
                                "$currencySymbol${"%.2f".format(expense.amount)}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Category") },
            text = { Text("Delete \"${category.name}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteCategory()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Category Picker Dialog with predefined categories
@Composable
fun CategoryPickerDialog(
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var customCategoryName by remember { mutableStateOf("") }
    var useCustom by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Text(
                    "Add New Category",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                // Tab toggle between predefined and custom
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TabButton(
                        text = "Predefined",
                        isSelected = !useCustom,
                        modifier = Modifier.weight(1f),
                        onClick = { useCustom = false }
                    )
                    TabButton(
                        text = "Custom",
                        isSelected = useCustom,
                        modifier = Modifier.weight(1f),
                        onClick = { useCustom = true }
                    )
                }

                // Content based on tab
                if (useCustom) {
                    // Custom category input
                    OutlinedTextField(
                        value = customCategoryName,
                        onValueChange = { customCategoryName = it },
                        label = { Text("Category Name") },
                        placeholder = { Text("e.g., Emergency, Misc") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    // Predefined categories grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(PredefinedCategories.categories) { categoryOption ->
                            CategoryChip(
                                categoryOption = categoryOption,
                                isSelected = selectedCategory == categoryOption.name,
                                onClick = { selectedCategory = categoryOption.name }
                            )
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val categoryName = if (useCustom) {
                                customCategoryName.trim()
                            } else {
                                selectedCategory ?: ""
                            }
                            if (categoryName.isNotBlank()) {
                                onCategorySelected(categoryName)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = if (useCustom) {
                            customCategoryName.isNotBlank()
                        } else {
                            selectedCategory != null
                        }
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

// Tab button for category picker
@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Transparent
            },
            contentColor = if (isSelected) {
                Color.White
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// Category chip for predefined categories
@Composable
fun CategoryChip(
    categoryOption: PredefinedCategories.CategoryOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                categoryOption.color.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.background
            },
            contentColor = MaterialTheme.colorScheme.onBackground
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) categoryOption.color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(categoryOption.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryOption.icon,
                    contentDescription = categoryOption.name,
                    modifier = Modifier.size(20.dp),
                    tint = categoryOption.color
                )
            }
            Text(
                categoryOption.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Main Categories Tab - wrapper function that connects to ViewModel
@Composable
fun CategoriesTab(
    tripId: String,
    expenses: List<FirestoreExpense>,
    currencySymbol: String,
    viewModel: FirestoreTripViewModel,
    navController: androidx.navigation.NavController
) {
    val categories by viewModel.categories.collectAsState()
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (categories.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
                Text(
                    "No Categories Yet",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    "Create categories to organize your expenses\nTap the + button to get started",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { category ->
                    EnhancedCategoryCard(
                        category = category,
                        expenses = expenses,
                        currencySymbol = currencySymbol,
                        onDeleteCategory = {
                            viewModel.deleteCategory(tripId, category.id)
                        },
                        onCategoryClick = {
                            navController.navigate("category_expenses/$tripId/${category.id}")
                        }
                    )
                }
            }
        }

        // FAB for adding new category
        FloatingActionButton(
            onClick = { showAddCategoryDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Category", tint = Color.White)
        }
    }

    if (showAddCategoryDialog) {
        CategoryPickerDialog(
            onDismiss = { showAddCategoryDialog = false },
            onCategorySelected = { categoryName ->
                val iconName = PredefinedCategories.categories
                    .find { it.name.equals(categoryName, ignoreCase = true) }?.name ?: "Other"
                viewModel.addCategory(tripId, categoryName, iconName)
                showAddCategoryDialog = false
            }
        )
    }
}

// Empty state for categories
@Composable
fun EmptyCategoriesState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Category,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Text(
                "No Categories Yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Create categories to organize your expenses\nTap the + button to get started",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )
        }
    }
}
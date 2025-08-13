package com.example.expensecalculator.TripManager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirstScreen(navController: NavController) {
    // CHANGED: Background to pure white/light gray
    val backgroundColor = Color(0xFFF8F9FA)

    // Header gradient remains purple-to-blue
    val topBarGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF6a11cb), Color(0xFF2575fc))
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Trek & Track",
                        fontSize = 32.sp, // CHANGED: Increased font size
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(topBarGradient)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor) // CHANGED
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(40.dp, Alignment.CenterVertically)
            ) {
                WelcomeSection()

                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FeatureCard(
                        title = "Trip Manager",
                        description = "Plan and track your travel expenses",
                        icon = Icons.Default.Flight,
                        onClick = { navController.navigate("trip_details") },
                        color = Color(0xFF1E88E5) // CHANGED: Deep teal blue
                    )
                    FeatureCard(
                        title = "Account Manager",
                        description = "Manage your daily expenses and accounts",
                        icon = Icons.Default.AccountBalance,
                        onClick = { navController.navigate("detail_screen") },
                        color = Color(0xFF673AB7) // CHANGED: Rich purple
                    )
                }
            }
        }
    }
}

@Composable
fun WelcomeSection() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Welcome",
            fontSize = 36.sp, // CHANGED: Increased font size
            fontWeight = FontWeight.Bold,
            color = Color(0xFF212529), // CHANGED: Darker for clarity
            textAlign = TextAlign.Center
        )
        Text(
            text = "Manage your finances efficiently",
            fontSize = 18.sp, // CHANGED: Increased font size
            color = Color(0xFF343A40), // CHANGED: Darker for clarity
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureCard(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp), // CHANGED: Increased height for readability
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 12.dp,
            pressedElevation = 16.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // CHANGED: Larger icon and clearer background contrast
            Box(
                modifier = Modifier
                    .size(70.dp) // CHANGED: Increased size
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(color.copy(alpha = 0.9f), Color.White.copy(alpha = 0.2f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(36.dp), // CHANGED: Increased size
                    tint = Color.White
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp) // CHANGED: More spacing
            ) {
                Text(
                    text = title,
                    fontSize = 22.sp, // CHANGED: Increased font size
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 16.sp, // CHANGED: Increased font size
                    color = Color.White.copy(alpha = 0.95f),
                    lineHeight = 20.sp
                )
            }
        }
    }
}
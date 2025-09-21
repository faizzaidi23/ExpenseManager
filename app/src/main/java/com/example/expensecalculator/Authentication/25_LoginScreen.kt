package com.example.expensecalculator.Authentication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// Define colors inspired by the theme.
// In a real app, these would be in your Theme.kt file.
val AppBackgroundColor = Color(0xFFF7F8FC)
val PrimaryAccentColor = Color(0xFF8A76F8)
val TextColor = Color(0xFF333333)
val MutedTextColor = Color(0xFF666666)
val TextFieldBackgroundColor = Color(0xFFFFFFFF)

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppBackgroundColor // 1. Set the background color for the whole screen
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp), // 2. Increased padding for more white space
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Welcome Back!", // 3. More engaging title
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TextColor
            )
            Text(
                "Log in to manage your home",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedTextColor
            )
            Spacer(modifier = Modifier.height(48.dp)) // 4. Increased spacing

            // Email Text Field
            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp), // 5. Rounded corners
                colors = TextFieldDefaults.colors( // 6. Custom colors
                    focusedContainerColor = TextFieldBackgroundColor,
                    unfocusedContainerColor = TextFieldBackgroundColor,
                    disabledContainerColor = TextFieldBackgroundColor,
                    focusedIndicatorColor = PrimaryAccentColor,
                    unfocusedIndicatorColor = Color.LightGray,
                    cursorColor = PrimaryAccentColor
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Password Text Field
            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.onPasswordChange(it) },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = TextFieldBackgroundColor,
                    unfocusedContainerColor = TextFieldBackgroundColor,
                    disabledContainerColor = TextFieldBackgroundColor,
                    focusedIndicatorColor = PrimaryAccentColor,
                    unfocusedIndicatorColor = Color.LightGray,
                    cursorColor = PrimaryAccentColor
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                // 7. Added password visibility toggle
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                }
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Login Button
            Button(
                onClick = {
                    viewModel.login(context) {
                        onLoginSuccess()
                    }
                },
                enabled = !viewModel.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp), // 8. Rounded corners for button
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryAccentColor,
                    contentColor = Color.White
                ) // 9. Themed button colors
            ) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White // Spinner color contrasts with button
                    )
                } else {
                    Text("Login", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }

            // Register Text Button
            TextButton(onClick = { navController.navigate("register") }) {
                Text(
                    "Don't have an account? Register",
                    color = MutedTextColor // 10. Softer color for the text button
                )
            }
        }
    }
}
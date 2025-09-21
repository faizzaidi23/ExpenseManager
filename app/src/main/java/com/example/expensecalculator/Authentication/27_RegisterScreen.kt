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



@Composable
fun RegisterScreen(navController: NavController, viewModel: AuthViewModel) {
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    // NOTE: You will need to add a 'confirmPassword' state to your AuthViewModel
    // For now, we can use a local state for demonstration.
    var confirmPassword by remember { mutableStateOf("") }


    Surface(
        modifier = Modifier.fillMaxSize(),
        color = AppBackgroundColor // 1. Set the background color
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp), // 2. Increased padding
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Create Your Account", // 3. More engaging title
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TextColor
            )
            Text(
                "Get started with your new smart home",
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
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, "Toggle password visibility")
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 7. Added "Confirm Password" Field for better UX
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
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
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(imageVector = image, "Toggle password visibility")
                    }
                }
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Register Button
            Button(
                onClick = {
                    // You would add validation here to check if password == confirmPassword
                    viewModel.register(context) {
                        navController.popBackStack()
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
                        color = Color.White
                    )
                } else {
                    Text("Create Account", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                }
            }

            // Login Text Button
            TextButton(onClick = { navController.popBackStack() }) {
                Text(
                    "Already have an account? Login",
                    color = MutedTextColor // 10. Softer color for the text button
                )
            }
        }
    }
}
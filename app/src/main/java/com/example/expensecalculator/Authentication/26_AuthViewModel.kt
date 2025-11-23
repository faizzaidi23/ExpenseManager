package com.example.expensecalculator.Authentication

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensecalculator.Connection.AuthRequest
import com.example.expensecalculator.Connection.RetrofitClient
import kotlinx.coroutines.launch

// --- ViewModel to handle UI State and Logic ---
class AuthViewModel : ViewModel() {
    var email by mutableStateOf<String>("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)

    fun onEmailChange(newEmail: String) {
        email = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        password = newPassword
    }

    fun register(context: Context, onRegistrationSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val request = AuthRequest(email, password)
                RetrofitClient.instance.register(request)
                Toast.makeText(context, "Registration Successful!", Toast.LENGTH_SHORT).show()
                onRegistrationSuccess()
            } catch (e: Exception) {
                errorMessage = "Registration failed: ${e.message}"
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    fun login(context: Context, onLoginSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val request = AuthRequest(email, password)
                val response = RetrofitClient.instance.login(request)
                // TODO: Securely save the response.token
                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            } catch (e: Exception) {
                errorMessage = "Login failed: Invalid credentials."
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }
}
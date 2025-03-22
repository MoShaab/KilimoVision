package com.example.kilimovision.ui.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kilimovision.R
import com.example.kilimovision.repository.FirebaseRepository
import com.example.kilimovision.viewmodel.AuthState
import com.example.kilimovision.viewmodel.AuthViewModel
import com.example.kilimovision.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    role: String,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    authViewModel: AuthViewModel = viewModel(),
    profileViewModel: ProfileViewModel = viewModel()
) {
    // Log for debugging
    Log.d("LoginScreen", "LoginScreen composable started with role: $role")
    val repository = FirebaseRepository()

    // State
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Observe auth state
    val authState by authViewModel.authState.collectAsState()
    val userType by authViewModel.userType.collectAsState()

    // Effect to handle authentication success and update user type
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {

//            // Set the user type based on the role
//            Log.d("LoginScreen", "Authentication successful, updating user type to: $role")
//            authViewModel.updateUserType(role)

            // Create initial profile for the user if needed
            val userId = (authState as AuthState.Authenticated).userId
            val userTypeResult = repository.getUserType(userId)
            if (userTypeResult.isSuccess) {
                   //val userType = userTypeResult.getOrNull() ?: "farmer"
                userType?.let { onLoginSuccess(it) }
            } else {
                // Handle error
                errorMessage = "Failed to retrieve account type"
            }

            if (role == "farmer") {
                profileViewModel.createInitialFarmerProfile(userId, "Nairobi")
            } else if (role == "seller") {
                profileViewModel.createInitialSellerProfile(userId, "Your Business", "Nairobi")
            }

            // Add delay to give time for user type update
            delay(500)

            // Navigate with user type
            onLoginSuccess(role)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Image(
            painter = painterResource(id = R.drawable.kilimovision_logo),
            contentDescription = "KilimoVision Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 24.dp)
        )

        Text(
            text = if (role == "farmer") "Farmer Login" else "Seller Login",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Error message
        if (authState is AuthState.Error) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = (authState as AuthState.Error).message,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Login button
        Button(
            onClick = {
                Log.d("LoginScreen", "Login button clicked with email: $email")
                authViewModel.signIn(email, password)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = email.isNotBlank() && password.isNotBlank() && authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Register link
        TextButton(
            onClick = {
                Log.d("LoginScreen", "Register button clicked")
                authViewModel.resetError()
                onNavigateToRegister()
            }
        ) {
            Text("Don't have an account? Register")
        }
    }
}
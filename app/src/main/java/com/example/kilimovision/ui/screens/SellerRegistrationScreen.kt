package com.example.kilimovision.ui.screens

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kilimovision.repository.FirebaseRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerRegistrationScreen(
    userId: String,
    onRegistrationComplete: () -> Unit,
    onCancelRegistration: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { FirebaseRepository() }

    // Form state
    var businessName by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("Nairobi") }
    val regions = listOf("Nairobi", "Nakuru", "Mombasa", "Kisumu", "Eldoret", "Other")

    // Status
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Region selection dropdown state
    var expanded by remember { mutableStateOf(false) }

    Log.d("SellerRegistrationScreen", "Rendering with userId: $userId")

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Register Your Business",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancelRegistration) {
                        Text("←", fontSize = 24.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Business Information",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Error message
            errorMessage?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Business name field
            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Business Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true
            )

            // Address field
            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true
            )

            // Region selection
            Text(
                text = "Select Your Region",
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = selectedRegion,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Region") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    regions.forEach { region ->
                        DropdownMenuItem(
                            text = { Text(region) },
                            onClick = {
                                selectedRegion = region
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Phone field
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true
            )

            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Business Email") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )

            // Description field
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Business Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Register button
            Button(
                onClick = {
                    if (businessName.isBlank() || address.isBlank() || phone.isBlank()) {
                        errorMessage = "Please fill in all required fields"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    coroutineScope.launch {
                        try {
                            Log.d("SellerRegistration", "Creating seller profile for userId: $userId")

                            // Create SellerProfile object using the updated model structure
                            val sellerProfile = com.example.kilimovision.model.SellerProfile(
                                userId = userId,
                                businessName = businessName,
                                businessAddress = address,
                                region = selectedRegion,
                                businessDescription = description,
                                businessHours = "8:00 AM - 5:00 PM",  // Default value
                                establishedYear = 2023,  // Default value
                                website = "",
                                socialMediaLinks = emptyMap(),
                                certifications = emptyList(),
                                specialties = emptyList(),
                                rating = 0.0,
                                reviewCount = 0,
                                verified = false,
                                subscription = com.example.kilimovision.model.SubscriptionDetails(
                                    plan = "free",
                                    startDate = com.google.firebase.Timestamp.now(),
                                    endDate = com.google.firebase.Timestamp.now(),
                                    autoRenew = false,
                                    features = listOf("Basic listing", "Standard visibility")
                                )
                            )

                            // Call the updated createOrUpdateSellerProfile method
                            val result = repository.createOrUpdateSellerProfile(sellerProfile)

                            if (result.isSuccess) {
                                Log.d("SellerRegistration", "Profile created successfully")
                                Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()

                                // Update user document to ensure userType is set to "seller"
                                repository.getCurrentUser().collect { user ->
                                    if (user != null) {
                                        val updatedUser = user.copy(userType = "seller")
                                        repository.updateUser(updatedUser)
                                    }
                                    onRegistrationComplete()
                                }
                            } else {
                                Log.e("SellerRegistration", "Failed to create profile: ${result.exceptionOrNull()?.message}")
                                errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
                            }
                        } catch (e: Exception) {
                            Log.e("SellerRegistration", "Error creating profile: ${e.message}", e)
                            errorMessage = "Registration error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Complete Registration")
                }
            }
        }
    }
}
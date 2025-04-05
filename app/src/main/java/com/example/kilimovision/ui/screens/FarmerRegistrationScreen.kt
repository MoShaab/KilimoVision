package com.example.kilimovision.ui.screens

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
import com.example.kilimovision.model.FarmerProfile
import com.example.kilimovision.repository.FirebaseRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerRegistrationScreen(
    userId: String,
    onRegistrationComplete: () -> Unit,
    onCancelRegistration: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { FirebaseRepository() }

    var farmName by remember { mutableStateOf("") }
    var farmSize by remember { mutableStateOf("") }
    var farmType by remember { mutableStateOf("Crop") }
    var primaryCrops by remember { mutableStateOf("") }
    var selectedRegion by remember { mutableStateOf("Nairobi") }
    val regions = listOf("Nairobi", "Nakuru", "Mombasa", "Kisumu", "Eldoret", "Other")
    val farmTypes = listOf("Crop", "Livestock", "Mixed")

    var expandedRegion by remember { mutableStateOf(false) }
    var expandedType by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Register as Farmer", fontWeight = FontWeight.Bold) },
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
            Text("Farm Information", fontSize = 20.sp, fontWeight = FontWeight.Bold)

            errorMessage?.let {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Text(it, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(16.dp))
                }
            }

            OutlinedTextField(
                value = farmName,
                onValueChange = { farmName = it },
                label = { Text("Farm Name") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = farmSize,
                onValueChange = { farmSize = it },
                label = { Text("Farm Size (acres)") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            ExposedDropdownMenuBox(
                expanded = expandedType,
                onExpandedChange = { expandedType = it }
            ) {
                OutlinedTextField(
                    value = farmType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Farm Type") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )

                ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                    farmTypes.forEach { type ->
                        DropdownMenuItem(text = { Text(type) }, onClick = {
                            farmType = type
                            expandedType = false
                        })
                    }
                }
            }

            OutlinedTextField(
                value = primaryCrops,
                onValueChange = { primaryCrops = it },
                label = { Text("Primary Crops (comma-separated)") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                maxLines = 3
            )

            ExposedDropdownMenuBox(
                expanded = expandedRegion,
                onExpandedChange = { expandedRegion = it }
            ) {
                OutlinedTextField(
                    value = selectedRegion,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Region") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRegion) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )

                ExposedDropdownMenu(expanded = expandedRegion, onDismissRequest = { expandedRegion = false }) {
                    regions.forEach { region ->
                        DropdownMenuItem(text = { Text(region) }, onClick = {
                            selectedRegion = region
                            expandedRegion = false
                        })
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (farmName.isBlank() || farmSize.isBlank()) {
                        errorMessage = "Please fill in all required fields"
                        return@Button
                    }

                    val size = farmSize.toDoubleOrNull()
                    if (size == null || size <= 0.0) {
                        errorMessage = "Farm size must be a valid number"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null

                    coroutineScope.launch {
                        try {
                            val farmerProfile = FarmerProfile(
                                userId = userId,
                                farmName = farmName,
                                farmSize = size,
                                farmType = farmType,
                                primaryCrops = primaryCrops.split(",").map { it.trim() },
                                region = selectedRegion
                            )

                            val result = repository.createOrUpdateFarmerProfile(farmerProfile)

                            if (result.isSuccess) {
                                Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
                                repository.getCurrentUser().collect { user ->
                                    user?.let {
                                        val updatedUser = user.copy(userType = "farmer")
                                        repository.updateUser(updatedUser)
                                    }
                                    onRegistrationComplete()
                                }
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
                            }
                        } catch (e: Exception) {
                            Log.e("FarmerRegistration", "Error: ${e.message}", e)
                            errorMessage = "Registration error: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Complete Registration")
                }
            }
        }
    }
}

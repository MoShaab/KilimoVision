package com.example.kilimovision.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kilimovision.repository.FirebaseRepository
import com.example.kilimovision.viewmodel.AdvertisementViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    onNavigateBack: () -> Unit,
    sellerId: String,
    onBackPressed: () -> Unit,
    onProductCreated: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { FirebaseRepository() }

    // Form state
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Fungicide") }
    var price by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unitOfMeasure by remember { mutableStateOf("Litres") }
    var inStock by remember { mutableStateOf(true) }
    var selectedDiseases = remember { mutableStateListOf<String>() }
    var showDiseaseSelector by remember { mutableStateOf(false) }

    // Status
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Get seller ID
    var sellerId by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            try {
                val db = FirebaseFirestore.getInstance()
                val sellerDocs = db.collection("sellerProfiles")
                    .whereEqualTo("userId", currentUser.uid)
                    .limit(1)
                    .get()
                    .await()

                if (!sellerDocs.isEmpty) {
                    sellerId = sellerDocs.documents.first().id
                }
            } catch (e: Exception) {
                errorMessage = "Error loading seller profile: ${e.message}"
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Add New Product",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
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
            // Error/Success message
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

            successMessage?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Form fields
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .height(120.dp),
                maxLines = 5
            )

            // Category dropdown
            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = { /* Handle dropdown */ }
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .menuAnchor()
                )

                // Category dropdown menu would be implemented here
            }

            // Price field
            OutlinedTextField(
                value = price,
                onValueChange = {
                    // Only allow digits and decimal point
                    if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*\$"))) {
                        price = it
                    }
                },
                label = { Text("Price (KES)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                singleLine = true
            )

            // Quantity and unit
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            quantity = it
                        }
                    },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = { /* Handle dropdown */ }
                ) {
                    OutlinedTextField(
                        value = unitOfMeasure,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = false) },
                        modifier = Modifier
                            .width(120.dp)
                            .menuAnchor(),
                        singleLine = true
                    )

                    // Unit dropdown menu would be implemented here
                }
            }

            // In stock switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "In Stock",
                    modifier = Modifier.weight(1f)
                )

                Switch(
                    checked = inStock,
                    onCheckedChange = { inStock = it }
                )
            }

            // Disease selector
            OutlinedTextField(
                value = if (selectedDiseases.isEmpty()) "Select applicable diseases"
                else "${selectedDiseases.size} diseases selected",
                onValueChange = { },
                readOnly = true,
                label = { Text("Applicable Diseases") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { showDiseaseSelector = true },
                trailingIcon = {
                    IconButton(onClick = { showDiseaseSelector = true }) {
                        Text("...")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Submit button
            Button(
                onClick = {
                    if (name.isBlank() || description.isBlank() || price.isBlank() ||
                        quantity.isBlank() || selectedDiseases.isEmpty()) {
                        errorMessage = "Please fill in all required fields"
                        return@Button
                    }

                    isLoading = true
                    errorMessage = null
                    successMessage = null

                    coroutineScope.launch {
                        try {
                            val result = repository.createProduct(
                                sellerId = sellerId,
                                name = name,
                                description = description,
                                price = price.toDoubleOrNull() ?: 0.0,
                                discount = 0.0,
                                category = category,
                                applicableDiseases = selectedDiseases.toList(),
                                inStock = inStock,
                                quantity = quantity.toIntOrNull() ?: 0,
                                unitOfMeasure = unitOfMeasure
                            )

                            if (result.isSuccess) {
                                successMessage = "Product created successfully!"
                                // Clear form
                                name = ""
                                description = ""
                                price = ""
                                quantity = ""
                                selectedDiseases.clear()
                            } else {
                                errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error creating product: ${e.message}"
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && sellerId.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Add Product")
                }
            }
        }
    }

    // Disease selector dialog
    if (showDiseaseSelector) {
        DiseaseSelectionDialog(
            currentSelections = selectedDiseases,
            onSelectionChanged = { selectedDiseases.clear(); selectedDiseases.addAll(it) },
            onDismiss = { showDiseaseSelector = false }
        )
    }
}
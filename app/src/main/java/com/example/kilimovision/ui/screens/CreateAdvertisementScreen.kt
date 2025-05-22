package com.example.kilimovision.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kilimovision.viewmodel.AdCreationStatus
import com.example.kilimovision.viewmodel.AdvertisementViewModel
import com.example.kilimovision.viewmodel.PaymentStatus
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAdvertisementScreen(
    sellerId: String,
    onBackPressed: () -> Unit,
    onAdCreated: () -> Unit,
    advertisementViewModel: AdvertisementViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State for form fields
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") } // In a real app, implement image upload
    val selectedDiseases = remember { mutableStateListOf<String>() }
    var showDiseaseSelector by remember { mutableStateOf(false) }
    var selectedPlan by remember { mutableStateOf("standard") }
    var durationDays by remember { mutableStateOf("30") }

    // State for payment
    var showPaymentDialog by remember { mutableStateOf(false) }
    var adCost by remember { mutableStateOf(0.0) }
    var adId by remember { mutableStateOf("") }

    // Observe view model states
    val adCreationStatus by advertisementViewModel.adCreationStatus.collectAsState()
    val paymentStatus by advertisementViewModel.paymentStatus.collectAsState()

    // Handle status changes
    LaunchedEffect(adCreationStatus) {
        when (adCreationStatus) {
            is AdCreationStatus.Success -> {
                val status = adCreationStatus as AdCreationStatus.Success
                adId = status.adId
                adCost = status.cost
                showPaymentDialog = true
            }
            is AdCreationStatus.Error -> {
                // Show error message
                // In a real app, implement proper error handling
                advertisementViewModel.resetAdCreationStatus()
            }
            else -> {}
        }
    }

    LaunchedEffect(paymentStatus) {
        when (paymentStatus) {
            is PaymentStatus.Success -> {
                // Show success message
                // In a real app, implement proper success handling
                advertisementViewModel.resetPaymentStatus()
                onAdCreated()
            }
            is PaymentStatus.Error -> {
                // Show error message
                // In a real app, implement proper error handling
                advertisementViewModel.resetPaymentStatus()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackPressed) {
                Text("←", fontSize = 24.sp)
            }
            Text(
                text = "Create Advertisement",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Form fields
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Advertisement Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        // Temporary image URL field (replace with image picker in a real app)
        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("Image URL") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Disease selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(4.dp)
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(4.dp)
                )
                .clickable { showDiseaseSelector = true }
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "Target Diseases",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (selectedDiseases.isEmpty()) "Select diseases"
                    else selectedDiseases.joinToString(", "),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Plan selection
        Text(
            text = "Advertisement Plan",
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlanOption(
                name = "Basic",
                description = "Lower visibility",
                price = "KES 5/day",
                isSelected = selectedPlan == "basic",
                onClick = { selectedPlan = "basic" },
                modifier = Modifier.weight(1f)
            )

            PlanOption(
                name = "Standard",
                description = "Regular visibility",
                price = "KES 10/day",
                isSelected = selectedPlan == "standard",
                onClick = { selectedPlan = "standard" },
                modifier = Modifier.weight(1f)
            )

            PlanOption(
                name = "Premium",
                description = "High visibility",
                price = "KES 20/day",
                isSelected = selectedPlan == "premium",
                onClick = { selectedPlan = "premium" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Duration
        OutlinedTextField(
            value = durationDays,
            onValueChange = {
                if (it.isBlank() || it.toIntOrNull() != null) {
                    durationDays = it
                }
            },
            label = { Text("Duration (Days)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Calculate cost button
        Button(
            onClick = {
                val duration = durationDays.toIntOrNull() ?: 30
                adCost = advertisementViewModel.calculateAdCost(selectedPlan, duration)
                // Show a toast with the calculated cost
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text("Calculate Cost")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Submit button
        Button(
            onClick = {
                if (title.isBlank() || description.isBlank() || selectedDiseases.isEmpty()) {
                    // Show validation error
                    return@Button
                }

                val duration = durationDays.toIntOrNull() ?: 30

                advertisementViewModel.createAdvertisement(
                    sellerId = sellerId,
                    title = title,
                    price = price.toDoubleOrNull() ?: 0.0,
                    description = description,
                    imageUrl = imageUrl.ifBlank { "https://via.placeholder.com/300" },
                    targetDiseases = selectedDiseases,
                    durationDays = duration,
                    plan = selectedPlan
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = title.isNotBlank() && description.isNotBlank() && selectedDiseases.isNotEmpty()
        ) {
            Text("Create Advertisement")
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

    // Payment dialog
    if (showPaymentDialog) {
        PaymentDialog(
            amount = adCost,
            onPaymentConfirmed = { paymentMethod ->
                coroutineScope.launch {
                    advertisementViewModel.processPayment(
                        userId = "current_user_id", // In a real app, get from auth
                        adId = adId,
                        amount = adCost,
                        paymentMethod = paymentMethod
                    )
                }
                showPaymentDialog = false
            },
            onDismiss = {
                showPaymentDialog = false
            }
        )
    }
}

@Composable
fun PlanOption(
    name: String,
    description: String,
    price: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = price,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DiseaseSelectionDialog(
    currentSelections: List<String>,
    onSelectionChanged: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    val tomato_diseases = listOf(
        "Tomato___Bacterial_spot",
        "Tomato___Early_blight",
        "Tomato___Late_blight",
        "Tomato___Leaf_Mold",
        "Tomato___Septoria_leaf_spot",
        "Tomato___Spider_mites Two-spotted_spider_mite",
        "Tomato___Target_Spot",
        "Tomato___Tomato_Yellow_Leaf_Curl_Virus",
        "Tomato___Tomato_mosaic_virus",
        "Tomato___healthy"
    )

    val selectedDiseases = remember { currentSelections.toMutableStateList() }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Select Target Diseases",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    tomato_diseases.forEach { disease ->
                        val formattedDisease = disease.replace("Tomato___", "").replace("_", " ")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedDiseases.contains(disease)) {
                                        selectedDiseases.remove(disease)
                                    } else {
                                        selectedDiseases.add(disease)
                                    }
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedDiseases.contains(disease),
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedDiseases.add(disease)
                                    } else {
                                        selectedDiseases.remove(disease)
                                    }
                                }
                            )

                            Text(
                                text = formattedDisease,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onSelectionChanged(selectedDiseases.toList())
                            onDismiss()
                        }
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentDialog(
    amount: Double,
    onPaymentConfirmed: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPaymentMethod by remember { mutableStateOf("mpesa") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Payment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Amount: KES ${String.format("%.2f", amount)}",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Payment Method",
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPaymentMethod = "mpesa" }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPaymentMethod == "mpesa",
                        onClick = { selectedPaymentMethod = "mpesa" }
                    )

                    Text(
                        text = "M-Pesa",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedPaymentMethod = "card" }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = selectedPaymentMethod == "card",
                        onClick = { selectedPaymentMethod = "card" }
                    )

                    Text(
                        text = "Credit/Debit Card",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onPaymentConfirmed(selectedPaymentMethod)
                        }
                    ) {
                        Text("Pay Now")
                    }
                }
            }
        }
    }
}
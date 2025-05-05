package com.example.kilimovision.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kilimovision.model.Consultation
import com.example.kilimovision.model.FarmerProfile
import com.example.kilimovision.model.User
import com.example.kilimovision.viewmodel.ProfileViewModel
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog
import com.example.kilimovision.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerProfileScreen(
    onNavigateBack: () -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val user by profileViewModel.userData.collectAsState()
    val farmerProfile by profileViewModel.farmerProfile.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val error by profileViewModel.error.collectAsState()

    // UI state
    var isEditing by remember { mutableStateOf(false) }
    var showConsultationDialog by remember { mutableStateOf(false) }

    // Form state
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var farmName by remember { mutableStateOf("") }
    var farmSize by remember { mutableStateOf("") }
    var farmType by remember { mutableStateOf("") }
    var primaryCrops by remember { mutableStateOf("") }

    // Initialize form values from user data
    LaunchedEffect(user, farmerProfile) {
        user?.let {
            name = it.name
            phone = it.phone
            region = it.region
            address = it.address
        }

        farmerProfile?.let {
            farmName = it.farmName
            farmSize = it.farmSize.toString()
            farmType = it.farmType
            primaryCrops = it.primaryCrops.joinToString(", ")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Farmer Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {

                        Icon(
                            painter = painterResource(id = R.drawable.back_icon),
                            contentDescription = "Arrow back",
                            tint = Color.Black
                        )

                    }
                },
                actions = {
                    if (!isEditing) {

                            IconButton(onClick = { isEditing = true }) {

                                Icon(
                                    painter = painterResource(id = R.drawable.edit_icon),
                                    contentDescription = "Edit icon",
                                    tint = Color.Black
                                )

                            }

                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Error message
                if (error != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = error ?: "An error occurred",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                if (isEditing) {
                    // Edit mode
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    var expanded by remember { mutableStateOf(false) }
                    val regions = listOf("Nairobi", "Nakuru", "Mombasa", "Kisumu", "Eldoret", "Other")

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = region,
                            onValueChange = { },
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
                            regions.forEach { regionOption ->
                                DropdownMenuItem(
                                    text = { Text(regionOption) },
                                    onClick = {
                                        region = regionOption
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    HorizontalDivider()

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Farm Information",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = farmName,
                        onValueChange = { farmName = it },
                        label = { Text("Farm Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = farmSize,
                        onValueChange = {
                            if (it.isEmpty() || it.toDoubleOrNull() != null) {
                                farmSize = it
                            }
                        },
                        label = { Text("Farm Size (acres)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    var farmTypeExpanded by remember { mutableStateOf(false) }
                    val farmTypes = listOf("Crop", "Livestock", "Mixed", "Other")

                    ExposedDropdownMenuBox(
                        expanded = farmTypeExpanded,
                        onExpandedChange = { farmTypeExpanded = !farmTypeExpanded }
                    ) {
                        OutlinedTextField(
                            value = farmType,
                            onValueChange = { },
                            readOnly = true,
                            label = { Text("Farm Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = farmTypeExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = farmTypeExpanded,
                            onDismissRequest = { farmTypeExpanded = false }
                        ) {
                            farmTypes.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        farmType = option
                                        farmTypeExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = primaryCrops,
                        onValueChange = { primaryCrops = it },
                        label = { Text("Primary Crops (comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { isEditing = false }
                        ) {
                            Text("Cancel")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = {
                                // Update user data
                                user?.let {
                                    val updatedUser = it.copy(
                                        name = name,
                                        phone = phone,
                                        region = region,
                                        address = address
                                    )
                                    profileViewModel.updateUserData(updatedUser)
                                }

                                // Update farmer profile
                                farmerProfile?.let {
                                    val updatedProfile = it.copy(
                                        farmName = farmName,
                                        farmSize = farmSize.toDoubleOrNull() ?: 0.0,
                                        farmType = farmType,
                                        primaryCrops = primaryCrops.split(",").map { crop -> crop.trim() },
                                        region = region
                                    )
                                    profileViewModel.updateFarmerProfile(updatedProfile)
                                }

                                isEditing = false
                            }
                        ) {
                            Text("Save")
                        }
                    }
                } else {
                    // View mode
                    ProfileSection("Personal Information") {
                        ProfileField("Name", user?.name ?: "")
                        ProfileField("Email", user?.email ?: "")
                        ProfileField("Phone", user?.phone ?: "")
                        ProfileField("Region", user?.region ?: "")
                        ProfileField("Address", user?.address ?: "")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    ProfileSection("Farm Information") {
                        ProfileField("Farm Name", farmerProfile?.farmName ?: "")
                        ProfileField("Farm Size", "${farmerProfile?.farmSize ?: 0.0} acres")
                        ProfileField("Farm Type", farmerProfile?.farmType ?: "")
                        ProfileField("Primary Crops", farmerProfile?.primaryCrops?.joinToString(", ") ?: "")
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Consultation history
                    Text(
                        text = "Consultation History",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showConsultationDialog = true }
                        ) {
                            Text("Add Consultation")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val consultations = farmerProfile?.consultationHistory ?: emptyList()
                    if (consultations.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No consultation history yet",
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Display consultations
                        consultations.forEach { consultation ->
                            ConsultationCard(consultation)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

//    // Consultation dialog
    if (showConsultationDialog) {
        AddConsultationDialog(
            onDismiss = { showConsultationDialog = false },
            onAddConsultation = { disease, crop, treatment, seller, success, notes ->
                val consultation = Consultation(
                    disease = disease,
                    cropAffected = crop,
                    treatmentDetails = treatment,
                    sellerUsed = seller,
                    treatmentSuccess = success,
                    notes = notes
                )
                profileViewModel.addConsultation(consultation)
                showConsultationDialog = false
            }
        )
    }
}

@Composable
fun ProfileSection(title: String, content: @Composable () -> Unit) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun ProfileField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value.ifEmpty { "Not provided" },
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun ConsultationCard(consultation: Consultation) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = consultation.disease.replace("Tomato___", "").replace("_", " "),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Date: ${consultation.dateDiagnosed?.toDate()?.let { formatDate(it) } ?: "Unknown"}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Crop: ${consultation.cropAffected}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Treatment: ${consultation.treatmentDetails}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Outcome: ${if (consultation.treatmentSuccess) "Successful" else "Unsuccessful"}",
                color = if (consultation.treatmentSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            if (consultation.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Notes: ${consultation.notes}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddConsultationDialog(
    onDismiss: () -> Unit,
    onAddConsultation: (String, String, String, String, Boolean, String) -> Unit
) {
    var disease by remember { mutableStateOf("") }
    var crop by remember { mutableStateOf("") }
    var treatment by remember { mutableStateOf("") }
    var seller by remember { mutableStateOf("") }
    var isSuccessful by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Add Consultation",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = disease,
                    onValueChange = { disease = it },
                    label = { Text("Disease") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = crop,
                    onValueChange = { crop = it },
                    label = { Text("Crop Affected") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = treatment,
                    onValueChange = { treatment = it },
                    label = { Text("Treatment Used") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = seller,
                    onValueChange = { seller = it },
                    label = { Text("Seller Name (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Treatment Successful?")

                    Spacer(modifier = Modifier.weight(1f))

                    Switch(
                        checked = isSuccessful,
                        onCheckedChange = { isSuccessful = it }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Additional Notes") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (disease.isNotBlank() && crop.isNotBlank() && treatment.isNotBlank()) {
                                onAddConsultation(
                                    disease, crop, treatment, seller, isSuccessful, notes
                                )
                            }
                        },
                        enabled = disease.isNotBlank() && crop.isNotBlank() && treatment.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

// Helper function to format dates
private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(date)
}
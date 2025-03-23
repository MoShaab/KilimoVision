package com.example.kilimovision.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.kilimovision.model.Review
import com.example.kilimovision.model.SellerProfile
import com.example.kilimovision.model.User
import com.example.kilimovision.viewmodel.ProfileViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProfileScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAddProduct: (Any?) -> Unit,
    onNavigateToCreateAd: (String) -> Unit,
    profileViewModel: ProfileViewModel = viewModel()
) {
    val user by profileViewModel.userData.collectAsState()
    val sellerProfile by profileViewModel.sellerProfile.collectAsState()

    val sellerReviews by profileViewModel.sellerReviews.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val error by profileViewModel.error.collectAsState()

    // UI state
    var isEditing by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Profile", "Reviews", "Subscription")

    // Form state
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var businessAddress by remember { mutableStateOf("") }
    var businessDescription by remember { mutableStateOf("") }
    var businessHours by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var establishedYear by remember { mutableStateOf("") }


    // Initialize form values from user data
    LaunchedEffect(user, sellerProfile) {
        user?.let {
            name = it.name
            phone = it.phone
            region = it.region
        }

        sellerProfile?.let {
            businessName = it.businessName
            businessAddress = it.businessAddress
            businessDescription = it.businessDescription
            businessHours = it.businessHours
            website = it.website
            establishedYear = it.establishedYear.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seller Profile") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", fontSize = 24.sp)
                    }
                },
                actions = {
                    if (!isEditing && selectedTabIndex == 0) {
                        IconButton(onClick = { isEditing = true }) {
                            Text("✎") // Edit icon
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
            ) {
                // Error message
                if (error != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = error ?: "An error occurred",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // Business name header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = sellerProfile?.businessName ?: user?.name ?: "Your Business",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${sellerProfile?.rating ?: 0.0}",
                                fontWeight = FontWeight.Bold
                            )

                            Text(" (${sellerProfile?.reviewCount ?: 0} reviews)")

                            if (sellerProfile?.verified == true) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("✓ Verified", color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = sellerProfile?.region ?: user?.region ?: "Location not set",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Tabs
                TabRow(selectedTabIndex = selectedTabIndex) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title) }
                        )
                    }
                }

                // Tab content
                when (selectedTabIndex) {
                    0 -> {
                        if (isEditing) {
                            EditProfileTab(
                                name = name,
                                onNameChange = { name = it },
                                phone = phone,
                                onPhoneChange = { phone = it },
                                region = region,
                                onRegionChange = { region = it },
                                businessName = businessName,
                                onBusinessNameChange = { businessName = it },
                                businessAddress = businessAddress,
                                onBusinessAddressChange = { businessAddress = it },
                                businessDescription = businessDescription,
                                onBusinessDescriptionChange = { businessDescription = it },
                                businessHours = businessHours,
                                onBusinessHoursChange = { businessHours = it },
                                website = website,
                                onWebsiteChange = { website = it },
                                establishedYear = establishedYear,
                                onEstablishedYearChange = { establishedYear = it },
                                onCancel = { isEditing = false },
                                onSave = {
                                    // Update user data
                                    user?.let {
                                        val updatedUser = it.copy(
                                            name = name,
                                            phone = phone,
                                            region = region
                                        )
                                        profileViewModel.updateUserData(updatedUser)
                                    }

                                    // Update seller profile
                                    sellerProfile?.let {
                                        val updatedProfile = it.copy(
                                            businessName = businessName,
                                            businessAddress = businessAddress,
                                            businessDescription = businessDescription,
                                            businessHours = businessHours,
                                            website = website,
                                            establishedYear = establishedYear.toIntOrNull() ?: 2020,
                                            region = region
                                        )
                                        profileViewModel.updateSellerProfile(updatedProfile)
                                    }

                                    isEditing = false
                                }
                            )
                        } else {
                            ViewProfileTab(sellerProfile, user)
                        }
                    }
                    1 -> ReviewsTab(sellerReviews)
                    2 -> SubscriptionTab(
                        sellerProfile = sellerProfile,
                        onNavigateToCreateAd = {
                            sellerProfile?.userId?.let { onNavigateToCreateAd(it) }
                        },
                        onNavigateToAddProduct = {
                            sellerProfile?.userId?.let { onNavigateToAddProduct(it) }


                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileTab(
    name: String,
    onNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    region: String,
    onRegionChange: (String) -> Unit,
    businessName: String,
    onBusinessNameChange: (String) -> Unit,
    businessAddress: String,
    onBusinessAddressChange: (String) -> Unit,
    businessDescription: String,
    onBusinessDescriptionChange: (String) -> Unit,
    businessHours: String,
    onBusinessHoursChange: (String) -> Unit,
    website: String,
    onWebsiteChange: (String) -> Unit,
    establishedYear: String,
    onEstablishedYearChange: (String) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Personal information
        Text(
            text = "Personal Information",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        var regionExpanded by remember { mutableStateOf(false) }
        val regions = listOf("Nairobi", "Nakuru", "Mombasa", "Kisumu", "Eldoret", "Other")

        ExposedDropdownMenuBox(
            expanded = regionExpanded,
            onExpandedChange = { regionExpanded = !regionExpanded }
        ) {
            OutlinedTextField(
                value = region,
                onValueChange = { },
                readOnly = true,
                label = { Text("Region") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = regionExpanded,
                onDismissRequest = { regionExpanded = false }
            ) {
                regions.forEach { regionOption ->
                    DropdownMenuItem(
                        text = { Text(regionOption) },
                        onClick = {
                            onRegionChange(regionOption)
                            regionExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Business information
        Text(
            text = "Business Information",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = businessName,
            onValueChange = onBusinessNameChange,
            label = { Text("Business Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = businessAddress,
            onValueChange = onBusinessAddressChange,
            label = { Text("Business Address") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = businessDescription,
            onValueChange = onBusinessDescriptionChange,
            label = { Text("Business Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = businessHours,
            onValueChange = onBusinessHoursChange,
            label = { Text("Business Hours") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = website,
            onValueChange = onWebsiteChange,
            label = { Text("Website (Optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = establishedYear,
            onValueChange = {
                if (it.isEmpty() || it.toIntOrNull() != null) {
                    onEstablishedYearChange(it)
                }
            },
            label = { Text("Established Year") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onCancel
            ) {
                Text("Cancel")
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSave
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
fun ViewProfileTab(sellerProfile: SellerProfile?, user: User?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileSection("Contact Information") {
            ProfileField("Name", user?.name ?: "")
            ProfileField("Email", user?.email ?: "")
            ProfileField("Phone", user?.phone ?: "")
            ProfileField("Region", user?.region ?: "")
        }

        Spacer(modifier = Modifier.height(24.dp))

        ProfileSection("Business Information") {
            ProfileField("Business Name", sellerProfile?.businessName ?: "")
            ProfileField("Business Address", sellerProfile?.businessAddress ?: "")
            ProfileField("Business Description", sellerProfile?.businessDescription ?: "")
            ProfileField("Business Hours", sellerProfile?.businessHours ?: "")
            ProfileField("Website", sellerProfile?.website ?: "")
            ProfileField("Established Year", sellerProfile?.establishedYear?.toString() ?: "")
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!sellerProfile?.specialties.isNullOrEmpty()) {
            ProfileSection("Specialties") {
                Text(sellerProfile?.specialties?.joinToString(", ") ?: "")
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        if (!sellerProfile?.certifications.isNullOrEmpty()) {
            ProfileSection("Certifications") {
                Text(sellerProfile?.certifications?.joinToString(", ") ?: "")
            }
        }
    }
}

@Composable
fun ReviewsTab(reviews: List<Review>) {
    if (reviews.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No reviews yet")
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(reviews) { review ->
                ReviewCard(review)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ReviewCard(review: Review) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "★".repeat(review.rating),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = review.date?.toDate()?.let { formatDate(it) } ?: "",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(review.comment)

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Product: ${review.productPurchased}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (review.verified) {
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "✓ Verified Purchase",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
fun SubscriptionTab(
    sellerProfile: SellerProfile?,
    onNavigateToCreateAd: () -> Unit,
    onNavigateToAddProduct: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Current Plan: ${sellerProfile?.subscription?.plan?.capitalize() ?: "Free"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (sellerProfile?.subscription?.endDate != null) {
                    Text(
                        text = "Valid until: ${sellerProfile.subscription.endDate.toDate()?.let { formatDate(it) }}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                Text("Features:")

                sellerProfile?.subscription?.features?.forEach { feature ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text("• ")
                        Text(feature)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Quick actions
        Text(
            text = "Quick Actions",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Button(
            onClick = onNavigateToAddProduct,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Add New Product")
        }

        Button(
            onClick = onNavigateToCreateAd,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Create Advertisement")
        }

        // Upgrade section
        if (sellerProfile?.subscription?.plan?.lowercase() != "premium") {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Upgrade Your Plan",
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
                    Text(
                        text = "Premium Plan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Benefits:")

                    listOf(
                        "Priority listing in search results",
                        "Featured product placements",
                        "Detailed analytics",
                        "Verified seller badge",
                        "Unlimited products"
                    ).forEach { benefit ->
                        Row(
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Text("• ")
                            Text(benefit)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { /* Handle upgrade */ },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Upgrade Now")
                    }
                }
            }
        }
    }
}

// Helper function to capitalize first letter
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}

// Helper function to format dates
private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(date)
}
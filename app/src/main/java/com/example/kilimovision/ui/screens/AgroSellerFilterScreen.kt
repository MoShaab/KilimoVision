//package com.example.kilimovision.ui.screens
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.example.kilimovision.model.Seller
//import com.example.kilimovision.ui.components.SellerCard
//import com.example.kilimovision.ui.components.SellerDetailsDialog
//import com.example.kilimovision.viewmodel.SellerViewModel
//
//import kotlinx.coroutines.delay
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun AgroSellerFilterScreen(
//    detectedDisease: String,
//    onBackPressed: () -> Unit,
//    sellerViewModel: SellerViewModel = viewModel()
//) {
//    var selectedRegion by remember { mutableStateOf("All Regions") }
//    var searchQuery by remember { mutableStateOf("") }
//    var selectedSeller by remember { mutableStateOf<Seller?>(null) }
//
//    // Fetch data from Firebase
//    val sellers by sellerViewModel.sellers.collectAsState()
//    val isLoading by sellerViewModel.isLoading.collectAsState()
//    val error by sellerViewModel.error.collectAsState()
//
//    // Load data when disease or region changes
//    LaunchedEffect(detectedDisease, selectedRegion) {
//        sellerViewModel.fetchSellersByDiseaseAndRegion(detectedDisease, selectedRegion)
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Find Treatment Suppliers") },
//                navigationIcon = {
//                    IconButton(onClick = onBackPressed) {
//                        Text("←", fontSize = 24.sp)
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(padding)
//                .padding(16.dp)
//        ) {
//            // Disease info
//            Card(
//                modifier = Modifier.fillMaxWidth(),
//            ) {
//                Column(
//                    modifier = Modifier.padding(16.dp)
//                ) {
//                    Text(
//                        text = "Detected Disease:",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Medium
//                    )
//                    Text(
//                        text = detectedDisease.replace("Tomato___", "").replace("_", " "),
//                        fontSize = 18.sp,
//                        fontWeight = FontWeight.Bold
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Search bar
//            OutlinedTextField(
//                value = searchQuery,
//                onValueChange = { searchQuery = it },
//                label = { Text("Search sellers or products") },
//                modifier = Modifier.fillMaxWidth()
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Region selector
//            Text(
//                text = "Select Region",
//                fontWeight = FontWeight.Medium
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            // Region buttons
//            val regions = listOf("All Regions", "Nairobi", "Nakuru", "Mombasa", "Kisumu", "Eldoret")
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 8.dp),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                regions.take(3).forEach { region ->
//                    FilterChip(
//                        selected = selectedRegion == region,
//                        onClick = { selectedRegion = region },
//                        label = { Text(region) }
//                    )
//                }
//            }
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(vertical = 8.dp),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                regions.drop(3).forEach { region ->
//                    FilterChip(
//                        selected = selectedRegion == region,
//                        onClick = { selectedRegion = region },
//                        label = { Text(region) }
//                    )
//                }
//            }
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // Sellers list
//            Text(
//                text = "Available Sellers in ${selectedRegion}",
//                fontSize = 18.sp,
//                fontWeight = FontWeight.Bold
//            )
//
//            Spacer(modifier = Modifier.height(8.dp))
//
//            if (isLoading) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .weight(1f),
//                    contentAlignment = Alignment.Center
//                ) {
//                    CircularProgressIndicator()
//                }
//            } else if (error != null) {
//                Card(
//                    modifier = Modifier.fillMaxWidth(),
//                    colors = CardDefaults.cardColors(
//                        containerColor = MaterialTheme.colorScheme.errorContainer
//                    )
//                ) {
//                    Text(
//                        text = error ?: "An error occurred",
//                        modifier = Modifier.padding(16.dp),
//                        color = MaterialTheme.colorScheme.onErrorContainer
//                    )
//                }
//            } else if (sellers.isEmpty()) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .weight(1f),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text("No sellers found. Try changing your search criteria.")
//                }
//            } else {
//                // Filter sellers by search query if needed
//                val filteredSellers = if (searchQuery.isBlank()) {
//                    sellers
//                } else {
//                    sellers.filter { seller ->
//                        seller.name.contains(searchQuery, ignoreCase = true) ||
//                                seller.address.contains(searchQuery, ignoreCase = true) ||
//                                seller.products.any { product ->
//                                    product.name.contains(searchQuery, ignoreCase = true)
//                                }
//                    }
//                }
//
//                LazyColumn(
//                    modifier = Modifier.weight(1f),
//                    verticalArrangement = Arrangement.spacedBy(8.dp)
//                ) {
//                    items(filteredSellers) { seller ->
//                        SellerCard(
//                            seller = seller,
//                            onClick = { selectedSeller = seller }
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//    // Seller details dialog
//    if (selectedSeller != null) {
//        SellerDetailsDialog(
//            seller = selectedSeller!!,
//            onDismiss = { selectedSeller = null }
//        )
//    }
//}
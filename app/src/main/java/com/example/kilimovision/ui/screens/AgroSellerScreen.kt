package com.example.kilimovision.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kilimovision.model.Advertisement
import com.example.kilimovision.model.Seller
import com.example.kilimovision.ui.components.AdvertisementCard
import com.example.kilimovision.ui.components.SellerCard
import com.example.kilimovision.ui.components.SellerDetailsDialog
import com.example.kilimovision.ui.components.DiseaseTreatmentInfo
import com.example.kilimovision.viewmodel.SellerViewModel

@Composable
fun AgroSellerScreen(
    detectedDisease: String,
    onBackPressed: () -> Unit,
    sellerViewModel: SellerViewModel = viewModel()
) {
    val context = LocalContext.current
    var selectedSeller by remember { mutableStateOf<Seller?>(null) }

    // Fetch data from Firebase
    val sellers by sellerViewModel.sellers.collectAsState()
    val advertisements by sellerViewModel.advertisements.collectAsState()
    val diseaseTreatment by sellerViewModel.diseaseTreatment.collectAsState()
    val isLoading by sellerViewModel.isLoading.collectAsState()
    val error by sellerViewModel.error.collectAsState()

    // Load data
    LaunchedEffect(detectedDisease) {
        sellerViewModel.fetchSellersByDiseaseAndRegion(detectedDisease)
        sellerViewModel.fetchAdvertisementsForDisease(detectedDisease)
        sellerViewModel.fetchDiseaseTreatmentInfo(detectedDisease)
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
                // Back arrow icon
                Text("←", fontSize = 24.sp)
            }
            Text(
                text = "Treatment Solutions",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Disease info
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Detected Disease:",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = detectedDisease.replace("Tomato___", "").replace("_", " "),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Show disease treatment information if available
        diseaseTreatment?.let { treatment ->
            DiseaseTreatmentInfo(treatment = treatment)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Advertisements section (if any)
        if (advertisements.isNotEmpty()) {
            Text(
                text = "Recommended Products",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            advertisements.forEach { ad ->
                AdvertisementCard(
                    advertisement = ad,
                    onClick = { /* Handle ad click */ }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Sellers section
        Text(
            text = "Nearby Sellers",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = error ?: "An error occurred",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        } else if (sellers.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "No sellers found in your area for this disease treatment.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                sellers.forEach { seller ->
                    SellerCard(
                        seller = seller,
                        onClick = { selectedSeller = seller }
                    )
                }
            }
        }
    }

    // Seller details dialog
    if (selectedSeller != null) {
        SellerDetailsDialog(
            seller = selectedSeller!!,
            onDismiss = { selectedSeller = null }
        )
    }
}
package com.example.kilimovision.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kilimovision.R
import com.example.kilimovision.model.Advertisement
import com.example.kilimovision.model.Seller
import com.example.kilimovision.ui.components.AdvertisementCard
import com.example.kilimovision.ui.components.SellerCard
import com.example.kilimovision.ui.components.SellerDetailsDialog
import com.example.kilimovision.ui.components.DiseaseTreatmentInfo
import com.example.kilimovision.viewmodel.SellerViewModel
import com.example.kilimovision.viewmodel.ProfileViewModel
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.toArgb
import coil.compose.AsyncImage

@Composable
fun AgroSellerScreen(
    detectedDisease: String,
    onBackPressed: () -> Unit,
    sellerViewModel: SellerViewModel = viewModel()
) {
    val context = LocalContext.current
    var profileViewModel = ProfileViewModel()

    // Define custom colors for a more vibrant UI
    val primaryGreen = Color(0xFF4CAF50)
    val accentOrange = Color(0xFFFFA000)
    val lightGreen = Color(0xFFAED581)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF5F9F5),
            Color(0xFFEBF5E6)
        )
    )

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header with colorful background
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = primaryGreen
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackPressed,
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.back_icon),
                            contentDescription = "Arrow back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Treatment Solutions",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Disease info with styled card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = accentOrange,
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Detected Disease:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )

                    Text(
                        text = detectedDisease.replace("Tomato___", "").replace("_", " "),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Show disease treatment information if available
            diseaseTreatment?.let { treatment ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = lightGreen.copy(alpha = 0.4f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Treatment Information",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryGreen
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        DiseaseTreatmentInfo(treatment = treatment)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Advertisements section with enhanced layout
            if (advertisements.isNotEmpty()) {
                Text(
                    text = "Recommended Products",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryGreen,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Larger advertisement cards in a horizontal scroll
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(advertisements) { ad ->
                        EnhancedAdvertisementCard(
                            advertisement = ad,
                            accentColor = accentOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Sellers section with styled header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp, 24.dp)
                        .background(accentOrange, RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Nearby Sellers",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
            }

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = accentOrange)
                }
            } else if (error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error ?: "An error occurred",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center
                    )
                }
            } else if (sellers.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.seller_icon), // Make sure you have this icon
                            contentDescription = "No sellers",
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "No sellers found in your area for this disease treatment.",
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    sellers.forEach { seller ->
                        EnhancedSellerCard(
                            seller = seller,
                            primaryColor = primaryGreen,
                            onClick = { selectedSeller = seller }
                        )
                    }
                }
            }

            // Extra space at bottom for better scrolling experience
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Seller details dialog
    if (selectedSeller != null) {
        SellerDetailsDialog(
            seller = selectedSeller!!,
            profileViewModel = profileViewModel,
            onDismiss = { selectedSeller = null }
        )
    }
}

@Composable
fun EnhancedAdvertisementCard(
    advertisement: Advertisement,
    accentColor: Color
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .clickable { /* Handle click */ },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column {
            // Image section (larger)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                // Display product image with error handling
                AsyncImage(
                    model = advertisement.imageUrl,
                    contentDescription = advertisement.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = painterResource(id = R.drawable.plant_placeholder), // Ensure you have this placeholder
                    placeholder = painterResource(id = R.drawable.plant_placeholder)
                )

                // Price tag overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(accentColor, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "KES ${advertisement.price}",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // Product details
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = advertisement.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = advertisement.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { /* Handle order */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("View Details")
                }
            }
        }
    }
}

@Composable
fun EnhancedSellerCard(
    seller: Seller,
    primaryColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Seller Avatar/Image
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(primaryColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                seller.profilePicture?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = "Seller image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        error = painterResource(id = R.drawable.profile_icon),
                        placeholder = painterResource(id = R.drawable.profile_icon)
                    )
                } ?: Icon(
                    painter = painterResource(id = R.drawable.profile_icon),
                    contentDescription = "Seller",
                    tint = primaryColor,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Seller information
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = seller.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

//                Row(
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.location_icon),
//                        contentDescription = "Location",
//                        tint = Color.Gray,
//                        modifier = Modifier.size(16.dp)
//                    )
//
//                    Spacer(modifier = Modifier.width(4.dp))
//
//                    Text(
//                        text = "Same County",
//                        fontSize = 14.sp,
//                        color = Color.Gray
//                    )
  //              }

                Spacer(modifier = Modifier.height(4.dp))

                // Rating display
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        Icon(
                            painter = painterResource(id = R.drawable.star_icon),
                            contentDescription = "Rating star",
                            tint = if (index < (seller.rating.toInt() ?: 0)) Color(0xFFFFC107) else Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "(${seller.reviewCount ?: 0})",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            // Contact button
            IconButton(
                onClick = { /* Contact seller */ },
                modifier = Modifier
                    .size(40.dp)
                    .background(primaryColor, RoundedCornerShape(12.dp))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.phone_icon),
                    contentDescription = "Contact seller",
                    tint = Color.White
                )
            }
        }
    }
}
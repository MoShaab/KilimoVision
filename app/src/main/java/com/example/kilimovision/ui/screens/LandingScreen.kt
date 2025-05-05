package com.example.kilimovision.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.kilimovision.R

@Composable
fun LandingScreen(
    onContinueAsFarmer: () -> Unit,
    onContinueAsSeller: () -> Unit
) {
    // Get context for Toast
    val context = LocalContext.current

    // For debugging
    Log.d("LandingScreen", "LandingScreen composable started")

    // Agricultural theme colors
    val greenDark = Color(0xFF1B5E20)
    val greenMedium = Color(0xFF2E7D32)
    val greenLight = Color(0xFF4CAF50)
    val earthBrown = Color(0xFF795548)
    val sunYellow = Color(0xFFFFC107)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE8F5E9), // Very light green at top
                        Color(0xFFC8E6C9)  // Slightly darker green at bottom
                    )
                )
            )
    ) {
        // Background farm imagery
        Image(
            painter = painterResource(id = R.drawable.farm_background),
            contentDescription = "Farm background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.2f // Make it subtle background
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo with plant icon


                Image(
                    painter = painterResource(id = R.drawable.kilimovision_logo),
                    contentDescription = "KilimoVision Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(3))
                )


            Text(
                text = "KilimoVision",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = greenDark,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Plant Disease Detection and Treatment",
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Tagline with farming reference
            Text(
                text = "Cultivating healthier harvests through technology",
                fontSize = 14.sp,
                color = earthBrown,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Card containing user type selection
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 6.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to the Field",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = greenDark,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = "I am a:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    // Farmer button with icon and custom styling
                    Button(
                        onClick = {
                            Log.d("LandingScreen", "Farmer button clicked")
                            Toast.makeText(context, "Continuing as Farmer", Toast.LENGTH_SHORT).show()
                            onContinueAsFarmer()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = greenMedium
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.farmer_icon),
                                contentDescription = "Farmer Icon",
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Farmer",
                                fontSize = 18.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Seller button with icon and custom styling
                    Button(
                        onClick = {
                            Log.d("LandingScreen", "Seller button clicked")
                            Toast.makeText(context, "Continuing as Seller", Toast.LENGTH_SHORT).show()
                            onContinueAsSeller()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = earthBrown
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.seller_icon),
                                contentDescription = "Seller Icon",
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Agrochemical Seller",
                                fontSize = 18.sp,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer with agricultural theme
            Text(
                text = "Growing better yields, one scan at a time",
                fontSize = 14.sp,
                color = greenDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
package com.example.kilimovision.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo placeholder (use a Box instead of trying to load an image)
        Box(
            modifier = Modifier
                .size(150.dp)
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("LOGO", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Text(
            text = "KilimoVision",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Plant Disease Detection and Treatment",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = "I am a:",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Farmer button with feedback
        Button(
            onClick = {
                Log.d("LandingScreen", "Farmer button clicked")
                Toast.makeText(context, "Continuing as Farmer", Toast.LENGTH_SHORT).show()
                onContinueAsFarmer()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Farmer",
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Seller button with feedback
        Button(
            onClick = {
                Log.d("LandingScreen", "Seller button clicked")
                Toast.makeText(context, "Continuing as Seller", Toast.LENGTH_SHORT).show()
                onContinueAsSeller()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = "Agrochemical Seller",
                fontSize = 18.sp
            )
        }
    }
}
package com.example.kilimovision

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.example.kilimovision.navigation.AppNavigation
import com.example.kilimovision.ui.theme.KilimoVisionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // For debugging
        Log.d("MainActivity", "onCreate called")

        setContent {
            Log.d("MainActivity", "setContent called")

            KilimoVisionTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    // Use the navigation system
                    AppNavigation()
                }
            }
        }
    }
}
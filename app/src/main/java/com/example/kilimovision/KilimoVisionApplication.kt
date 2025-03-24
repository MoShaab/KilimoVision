package com.example.kilimovision

import android.app.Application
import android.util.Log
import com.example.kilimovision.repository.FirebaseRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class KilimoVisionApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Configure Firestore
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)  // Enable offline persistence
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings

        // Initialize application data
        initializeApplicationData()
    }

    private fun initializeApplicationData() {
        // Initialize application data in a background coroutine
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repository = FirebaseRepository()

                // Initialize disease treatments data
//                repository.getDiseaseTreatmentInfo()

                Log.d("KilimoVisionApp", "Application data initialization complete")
            } catch (e: Exception) {
                Log.e("KilimoVisionApp", "Error initializing application data", e)
            }
        }
    }
}
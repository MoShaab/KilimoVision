package com.example.kilimovision.model

import com.google.firebase.Timestamp

// Base user class
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val userType: String = "", // "farmer" or "seller"
    val profilePicture: String = "",
    val createdAt: Timestamp? = null,
    val region: String = "Nairobi", // Default region
    val address: String = ""
)

// Farmer-specific profile
data class FarmerProfile(
    val userId: String = "",
    val farmName: String = "",
    val farmSize: Double = 0.0, // In acres
    val farmType: String = "", // e.g., "Crop", "Livestock", "Mixed"
    val primaryCrops: List<String> = emptyList(),
    val region: String = "",
    val consultationHistory: List<Consultation> = emptyList(),
    val preferredSellers: List<String> = emptyList() // IDs of preferred sellers
)

// Farmer consultation history
data class Consultation(
    val id: String = "",
    val disease: String = "",
    val dateDiagnosed: Timestamp? = null,
    val cropAffected: String = "",
    val treatmentDetails: String = "",
    val sellerUsed: String = "", // ID of seller used
    val treatmentSuccess: Boolean = false,
    val notes: String = ""
)

// Seller profile (extended with business details)
data class SellerProfile(
    val userId: String = "",
    val businessName: String = "",
    val businessAddress: String = "",
    val region: String = "",
    val businessDescription: String = "",
    val businessHours: String = "8:00 AM - 5:00 PM",
    val establishedYear: Int = 2020,
    val website: String = "",
    val socialMediaLinks: Map<String, String> = emptyMap(), // e.g., "facebook" to URL
    val certifications: List<String> = emptyList(),
    val specialties: List<String> = emptyList(),
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val verified: Boolean = false,
    val subscription: SubscriptionDetails? = null
)

// Seller subscription details
data class SubscriptionDetails(
    val plan: String = "free", // "free", "basic", "premium"
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val autoRenew: Boolean = false,
    val features: List<String> = emptyList()
)

// Reviews for sellers
data class Review(
    val id: String = "",
    val sellerId: String = "",
    val farmerId: String = "",
    val rating: Int = 0, // 1-5 stars
    val comment: String = "",
    val date: Timestamp? = null,
    val productPurchased: String = "",
    val verified: Boolean = false
)
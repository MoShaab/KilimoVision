package com.example.kilimovision.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Seller(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val businessName: String = "",
    val address: String = "",
    val location: GeoPoint? = null,
    val phone: String = "",
    val email: String = "",
    val description: String = "",
    val profilePicture: String = "",
    val rating: Double = 0.0,
    val verified: Boolean = false,
    val createdAt: Timestamp? = null,
    val subscription: SellerSubscription? = null,

    // Fields for UI display
    val distance: Double = 0.0,
    val products: List<Product> = emptyList()
)

data class SellerSubscription(
    val plan: String = "free", // free, basic, premium
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null
)
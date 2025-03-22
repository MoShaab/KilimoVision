package com.example.kilimovision.model

import com.google.firebase.Timestamp

data class Product(
    val id: String = "",
    val sellerId: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val discount: Double = 0.0,
    val images: List<String> = emptyList(),
    val category: String = "",
    val applicableDiseases: List<String> = emptyList(),
    val inStock: Boolean = false,
    val quantity: Int = 0,
    val unitOfMeasure: String = "",
    val featured: Boolean = false,
    val createdAt: Timestamp? = null,

    // Added for compatibility with existing code
    val disease: String = "",
    val availability: Boolean = false
)
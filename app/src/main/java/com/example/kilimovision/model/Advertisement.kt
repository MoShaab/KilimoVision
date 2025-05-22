package com.example.kilimovision.model

import com.google.firebase.Timestamp

data class Advertisement(
    val id: String = "",
    val sellerId: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val targetDiseases: List<String> = emptyList(),
    val startDate: Timestamp? = null,
    val endDate: Timestamp? = null,
    val status: String = "", // pending, active, expired
    val impressions: Int = 0,
    val price: Double = 0.0,
    val clicks: Int = 0,
    val plan: String = "", // premium, standard, basic
    val cost: Double = 0.0,
    val paymentStatus: String = "", // paid, pending
    val createdAt: Timestamp? = null
)
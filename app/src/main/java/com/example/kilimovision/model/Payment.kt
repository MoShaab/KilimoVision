package com.example.kilimovision.model

import com.google.firebase.Timestamp

data class Payment(
    val id: String = "",
    val userId: String = "",
    val amount: Double = 0.0,
    val currency: String = "",
    val type: String = "", // subscription, advertisement
    val referenceId: String = "", // adId or sellerId
    val status: String = "", // pending, completed, failed
    val paymentMethod: String = "", // mpesa, card, etc.
    val transactionId: String = "",
    val timestamp: Timestamp? = null
)
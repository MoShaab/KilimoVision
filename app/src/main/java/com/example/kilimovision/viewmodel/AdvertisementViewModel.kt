package com.example.kilimovision.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kilimovision.model.Advertisement
import com.example.kilimovision.repository.FirebaseRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Calendar

class AdvertisementViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    // State for ad creation status
    private val _adCreationStatus = MutableStateFlow<AdCreationStatus>(AdCreationStatus.Idle)
    val adCreationStatus: StateFlow<AdCreationStatus> = _adCreationStatus

    // State for payment status
    private val _paymentStatus = MutableStateFlow<PaymentStatus>(PaymentStatus.Idle)
    val paymentStatus: StateFlow<PaymentStatus> = _paymentStatus

    // Calculate ad cost based on plan and duration
    fun calculateAdCost(plan: String, durationDays: Int): Double {
        val baseCostPerDay = when (plan) {
            "premium" -> 20.0
            "standard" -> 10.0
            "basic" -> 5.0
            else -> 0.0
        }

        return baseCostPerDay * durationDays
    }

    // Create an advertisement
    fun createAdvertisement(
        sellerId: String,
        title: String,
        description: String,
        imageUrl: String,
        targetDiseases: List<String>,
        durationDays: Int,
        plan: String
    ) {
        viewModelScope.launch {
            _adCreationStatus.value = AdCreationStatus.Loading

            try {
                // Calculate dates
                val startDate = Timestamp.now()
                val endCalendar = Calendar.getInstance()
                endCalendar.time = startDate.toDate()
                endCalendar.add(Calendar.DAY_OF_YEAR, durationDays)
                val endDate = Timestamp(endCalendar.time)

                // Calculate cost
                val cost = calculateAdCost(plan, durationDays)

                // Create the ad
                val result = repository.createAdvertisement(
                    sellerId = sellerId,
                    title = title,
                    description = description,
                    imageUrl = imageUrl,
                    targetDiseases = targetDiseases,
                    startDate = startDate,
                    endDate = endDate,
                    plan = plan,
                    cost = cost
                )

                if (result.isSuccess) {
                    _adCreationStatus.value = AdCreationStatus.Success(result.getOrNull()!!, cost)
                } else {
                    _adCreationStatus.value = AdCreationStatus.Error(result.exceptionOrNull()?.message ?: "Unknown error")
                }
            } catch (e: Exception) {
                _adCreationStatus.value = AdCreationStatus.Error("Error creating advertisement: ${e.message}")
            }
        }
    }

    // Process payment for an advertisement
    fun processPayment(
        userId: String,
        adId: String,
        amount: Double,
        paymentMethod: String
    ) {
        viewModelScope.launch {
            _paymentStatus.value = PaymentStatus.Processing

            try {
                // Create payment record
                val paymentResult = repository.createPayment(
                    userId = userId,
                    amount = amount,
                    currency = "KES", // Kenyan Shilling
                    type = "advertisement",
                    referenceId = adId,
                    paymentMethod = paymentMethod
                )

                if (paymentResult.isSuccess) {
                    val paymentId = paymentResult.getOrNull()!!

                    // In a real app, integrate with M-Pesa or other payment gateway here
                    // For this example, we'll simulate a successful payment
                    val transactionId = "MPESA-${System.currentTimeMillis()}"

                    // Update payment status
                    val updateResult = repository.updatePaymentStatus(
                        paymentId = paymentId,
                        status = "completed",
                        transactionId = transactionId
                    )

                    if (updateResult.isSuccess) {
                        _paymentStatus.value = PaymentStatus.Success(transactionId)
                    } else {
                        _paymentStatus.value = PaymentStatus.Error(
                            updateResult.exceptionOrNull()?.message ?: "Failed to update payment status"
                        )
                    }
                } else {
                    _paymentStatus.value = PaymentStatus.Error(
                        paymentResult.exceptionOrNull()?.message ?: "Failed to create payment"
                    )
                }
            } catch (e: Exception) {
                _paymentStatus.value = PaymentStatus.Error("Payment processing error: ${e.message}")
            }
        }
    }

    // Reset states
    fun resetAdCreationStatus() {
        _adCreationStatus.value = AdCreationStatus.Idle
    }

    fun resetPaymentStatus() {
        _paymentStatus.value = PaymentStatus.Idle
    }
}

// Sealed classes for UI state
sealed class AdCreationStatus {
    object Idle : AdCreationStatus()
    object Loading : AdCreationStatus()
    data class Success(val adId: String, val cost: Double) : AdCreationStatus()
    data class Error(val message: String) : AdCreationStatus()
}

sealed class PaymentStatus {
    object Idle : PaymentStatus()
    object Processing : PaymentStatus()
    data class Success(val transactionId: String) : PaymentStatus()
    data class Error(val message: String) : PaymentStatus()
}
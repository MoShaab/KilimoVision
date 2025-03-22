package com.example.kilimovision.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kilimovision.model.*
import com.example.kilimovision.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class ProfileViewModel : ViewModel() {
    private val repository = UserRepository()

    // User data state
    private val _userData = MutableStateFlow<User?>(null)
    val userData: StateFlow<User?> = _userData

    // Farmer profile state
    private val _farmerProfile = MutableStateFlow<FarmerProfile?>(null)
    val farmerProfile: StateFlow<FarmerProfile?> = _farmerProfile

    // Seller profile state
    private val _sellerProfile = MutableStateFlow<SellerProfile?>(null)
    val sellerProfile: StateFlow<SellerProfile?> = _sellerProfile

    // Seller reviews
    private val _sellerReviews = MutableStateFlow<List<Review>>(emptyList())
    val sellerReviews: StateFlow<List<Review>> = _sellerReviews

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Initialize by loading current user data
    init {
        loadCurrentUserData()
    }

    // Load current user data
    fun loadCurrentUserData() {
        val userId = repository.getCurrentUserId()
        if (userId != null) {
            loadUserData(userId)
        }
    }

    // Load user data by ID
    fun loadUserData(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.getUserData(userId).collect { user ->
                _userData.value = user

                if (user != null) {
                    when (user.userType) {
                        "farmer" -> loadFarmerProfile(userId)
                        "seller" -> loadSellerProfile(userId)
                    }
                }

                _isLoading.value = false
            }
        }
    }

    // Load farmer profile
    private fun loadFarmerProfile(userId: String) {
        viewModelScope.launch {
            repository.getFarmerProfile(userId).collect { profile ->
                _farmerProfile.value = profile
            }
        }
    }

    // Load seller profile
    private fun loadSellerProfile(userId: String) {
        viewModelScope.launch {
            repository.getSellerProfile(userId).collect { profile ->
                _sellerProfile.value = profile

                // Load reviews for the seller
                if (profile != null) {
                    loadSellerReviews(userId)
                }
            }
        }
    }

    // Load seller reviews
    private fun loadSellerReviews(sellerId: String) {
        viewModelScope.launch {
            repository.getSellerReviews(sellerId).collect { reviews ->
                _sellerReviews.value = reviews
            }
        }
    }

    // Update user data
    fun updateUserData(user: User) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.updateUserData(user)
                if (result.isSuccess) {
                    _userData.value = user
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Error updating user data"
                }
            } catch (e: Exception) {
                _error.value = "Error updating user data: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Update farmer profile
    fun updateFarmerProfile(profile: FarmerProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.updateFarmerProfile(profile)
                if (result.isSuccess) {
                    _farmerProfile.value = profile
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Error updating farmer profile"
                }
            } catch (e: Exception) {
                _error.value = "Error updating farmer profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Update seller profile
    fun updateSellerProfile(profile: SellerProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val result = repository.updateSellerProfile(profile)
                if (result.isSuccess) {
                    _sellerProfile.value = profile
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Error updating seller profile"
                }
            } catch (e: Exception) {
                _error.value = "Error updating seller profile: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Add consultation record
    fun addConsultation(consultation: Consultation) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val userId = repository.getCurrentUserId() ?: return@launch

                val result = repository.addConsultation(userId, consultation)
                if (result.isSuccess) {
                    // Reload farmer profile to include the new consultation
                    loadFarmerProfile(userId)
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Error adding consultation"
                }
            } catch (e: Exception) {
                _error.value = "Error adding consultation: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Add review for seller
    fun addReview(sellerId: String, rating: Int, comment: String, productPurchased: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val farmerId = repository.getCurrentUserId() ?: return@launch

                val review = Review(
                    sellerId = sellerId,
                    farmerId = farmerId,
                    rating = rating,
                    comment = comment,
                    productPurchased = productPurchased,
                    verified = true // Assuming all reviews from the app are verified
                )

                val result = repository.addReview(review)
                if (result.isSuccess) {
                    // Reload seller reviews
                    loadSellerReviews(sellerId)
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Error adding review"
                }
            } catch (e: Exception) {
                _error.value = "Error adding review: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Create initial farmer profile
    fun createInitialFarmerProfile(userId: String, region: String) {
        val initialProfile = FarmerProfile(
            userId = userId,
            region = region
        )

        updateFarmerProfile(initialProfile)
    }

    // Create initial seller profile
    fun createInitialSellerProfile(userId: String, businessName: String, region: String) {
        val initialProfile = SellerProfile(
            userId = userId,
            businessName = businessName,
            region = region,
            subscription = SubscriptionDetails(
                plan = "free",
                features = listOf("Basic product listings", "Standard visibility")
            )
        )

        updateSellerProfile(initialProfile)
    }
}
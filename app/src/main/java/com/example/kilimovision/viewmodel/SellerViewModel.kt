package com.example.kilimovision.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kilimovision.model.Advertisement
import com.example.kilimovision.model.DiseaseTreatment
import com.example.kilimovision.model.Seller
import com.example.kilimovision.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SellerViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    // State for nearby sellers
    private val _sellers = MutableStateFlow<List<Seller>>(emptyList())
    val sellers: StateFlow<List<Seller>> = _sellers

    // State for advertisements
    private val _advertisements = MutableStateFlow<List<Advertisement>>(emptyList())
    val advertisements: StateFlow<List<Advertisement>> = _advertisements

    // State for disease treatment information
    private val _diseaseTreatment = MutableStateFlow<DiseaseTreatment?>(null)
    val diseaseTreatment: StateFlow<DiseaseTreatment?> = _diseaseTreatment

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Get sellers by disease and region
    fun fetchSellersByDiseaseAndRegion(disease: String, region: String = "All Regions") {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                repository.getSellersByDiseaseAndRegion(
                    disease,
                    region
                ).collect { sellersList ->
                    _sellers.value = sellersList
                }
            } catch (e: Exception) {
                _error.value = "Error finding sellers: ${e.message}"
                _sellers.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Get advertisements for a disease
    fun fetchAdvertisementsForDisease(disease: String) {
        viewModelScope.launch {
            try {
                repository.getAdvertisementsForDisease(disease).collect { ads ->
                    _advertisements.value = ads
                }
            } catch (e: Exception) {
                _error.value = "Error loading advertisements: ${e.message}"
            }
        }
    }

    // Get disease treatment info
    fun fetchDiseaseTreatmentInfo(diseaseName: String) {
        viewModelScope.launch {
            try {
                repository.getDiseaseTreatmentInfo(diseaseName).collect { treatment ->
                    _diseaseTreatment.value = treatment
                }
            } catch (e: Exception) {
                _error.value = "Error loading disease information: ${e.message}"
            }
        }
    }
}
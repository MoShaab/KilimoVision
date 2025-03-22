package com.example.kilimovision.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kilimovision.model.Seller
import com.example.kilimovision.repository.FirebaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AgroSellerViewModel : ViewModel() {

    private val repository = FirebaseRepository()

    // Sellers state
    private val _sellers = MutableStateFlow<List<Seller>>(emptyList())
    val sellers: StateFlow<List<Seller>> = _sellers

    // Region state
    private val _selectedRegion = MutableStateFlow("All Regions")
    val selectedRegion: StateFlow<String> = _selectedRegion

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Set the selected region
    fun setRegion(region: String) {
        _selectedRegion.value = region
        // Refresh sellers when region changes
        _sellers.value.firstOrNull()?.let {
            fetchSellersByDiseaseAndRegion(it.products.firstOrNull()?.disease ?: "", region)
        }
    }

    // Get sellers by disease and region
    fun fetchSellersByDiseaseAndRegion(disease: String, region: String) {
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
}
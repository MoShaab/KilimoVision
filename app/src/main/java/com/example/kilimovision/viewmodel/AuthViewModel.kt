package com.example.kilimovision.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kilimovision.repository.FirebaseRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val repository = FirebaseRepository()
    private val auth = FirebaseAuth.getInstance()

    // Auth state
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    // User type (farmer or seller)
    private val _userType = MutableStateFlow<String?>(null)
    val userType: StateFlow<String?> = _userType

    init {
        // Check if user is already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(currentUser.uid)
            checkUserType()
        } else {
            _authState.value = AuthState.Unauthenticated
        }
    }

    // Get current user ID
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Reset error state
    fun resetError() {
        if (_authState.value is AuthState.Error) {
            _authState.value = AuthState.Unauthenticated
        }
    }

    // Update user type in Firestore only (without updating local state)
    fun updateUserTypeInFirestore(userId: String, newUserType: String) {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()

                // Update the user document with new user type
                db.collection("users").document(userId)
                    .update("userType", newUserType)
                    .await()

                Log.d("AuthViewModel", "User type updated in Firestore: $newUserType for user $userId")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error updating user type in Firestore: ${e.message}")
            }
        }
    }

    // Sign in and get user type
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val result = repository.signIn(email, password)
                if (result.isSuccess) {
                    _authState.value = AuthState.Authenticated(result.getOrNull() ?: "")
                } else {
                    _authState.value = AuthState.Error(
                        result.exceptionOrNull()?.message ?: "Authentication failed"
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Authentication error: ${e.message}")
            }
        }
    }

    fun signUp(name: String, email: String, password: String, userType: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            try {
                val result = repository.signUp(email, password, name, userType)

                if (result.isSuccess) {
                    val userId = result.getOrNull()!!
                    _authState.value = AuthState.Authenticated(userId)
                    _userType.value = userType
                } else {
                    _authState.value = AuthState.Error(
                        result.exceptionOrNull()?.message ?: "Registration failed"
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Registration error: ${e.message}")
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
        _userType.value = null
    }

    private fun checkUserType() {
        viewModelScope.launch {
            try {
                // Get user document
                val userId = auth.currentUser?.uid ?: return@launch
                val db = FirebaseFirestore.getInstance()
                val userDoc = db.collection("users").document(userId).get().await()

                if (userDoc.exists()) {
                    _userType.value = userDoc.getString("userType")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error checking user type: ${e.message}")
            }
        }
    }

    // Update user type
    fun updateUserType(newUserType: String) {
        viewModelScope.launch {
            try {
                // Get user document
                val userId = auth.currentUser?.uid ?: return@launch
                val db = FirebaseFirestore.getInstance()

                // Update the user document with new user type
                db.collection("users").document(userId)
                    .update("userType", newUserType)
                    .await()

                // Update local state
                _userType.value = newUserType
            } catch (e: Exception) {
                // Just update local state if Firestore update fails
                _userType.value = newUserType

                // Log error but don't block the flow
                Log.e("AuthViewModel", "Error updating user type: ${e.message}")
            }
        }
    }
}

// Sealed class to represent authentication state
sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}
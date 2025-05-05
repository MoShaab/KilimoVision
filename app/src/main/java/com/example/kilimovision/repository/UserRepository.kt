package com.example.kilimovision.repository

import android.util.Log
import com.example.kilimovision.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Get current user ID
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // Get user data
    suspend fun getUserData(userId: String): Flow<User?> = flow {
        try {
            val userDoc = db.collection("users").document(userId).get().await()

            if (userDoc.exists()) {
                val user = User(
                    id = userDoc.id,
                    name = userDoc.getString("name") ?: "",
                    email = userDoc.getString("email") ?: "",
                    phone = userDoc.getString("phone") ?: "",
                    userType = userDoc.getString("userType") ?: "",
                    profilePicture = userDoc.getString("profilePicture") ?: "",
                    createdAt = userDoc.getTimestamp("createdAt"),
                    region = userDoc.getString("region") ?: "Nairobi",
                    address = userDoc.getString("address") ?: ""
                )
                emit(user)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }

    // Update user data
    suspend fun updateUserData(user: User): Result<Boolean> {
        return try {
            val userData = hashMapOf(
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "userType" to user.userType,
                "profilePicture" to user.profilePicture,
                "region" to user.region,
                "address" to user.address
            )

            db.collection("users").document(user.id).update(userData as Map<String, Any>).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get farmer profile
    suspend fun getFarmerProfile(userId: String): Flow<FarmerProfile?> = flow {
        try {
            val profileDoc = db.collection("farmerProfiles").document(userId).get().await()

            if (profileDoc.exists()) {
                val consultationsDocs = db.collection("farmerProfiles")
                    .document(userId)
                    .collection("consultations")
                    .get()
                    .await()

                val consultations = consultationsDocs.documents.map { doc ->
                    Consultation(
                        id = doc.id,
                        disease = doc.getString("disease") ?: "",
                        dateDiagnosed = doc.getTimestamp("dateDiagnosed"),
                        cropAffected = doc.getString("cropAffected") ?: "",
                        treatmentDetails = doc.getString("treatmentDetails") ?: "",
                        sellerUsed = doc.getString("sellerUsed") ?: "",
                        treatmentSuccess = doc.getBoolean("treatmentSuccess") ?: false,
                        notes = doc.getString("notes") ?: ""
                    )
                }

                @Suppress("UNCHECKED_CAST")
                val profile = FarmerProfile(
                    userId = profileDoc.id,
                    farmName = profileDoc.getString("farmName") ?: "",
                    farmSize = profileDoc.getDouble("farmSize") ?: 0.0,
                    farmType = profileDoc.getString("farmType") ?: "",
                    primaryCrops = (profileDoc.get("primaryCrops") as? List<String>) ?: emptyList(),
                    region = profileDoc.getString("region") ?: "",
                    consultationHistory = consultations,
                    preferredSellers = (profileDoc.get("preferredSellers") as? List<String>) ?: emptyList()
                )

                emit(profile)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }

    // Create or update farmer profile
    suspend fun updateFarmerProfile(profile: FarmerProfile): Result<Boolean> {
        return try {
            val profileData = hashMapOf(
                "userId" to profile.userId,
                "farmName" to profile.farmName,
                "farmSize" to profile.farmSize,
                "farmType" to profile.farmType,
                "primaryCrops" to profile.primaryCrops,
                "region" to profile.region,
                "preferredSellers" to profile.preferredSellers
            )

            db.collection("farmerProfiles").document(profile.userId).set(profileData).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Add consultation record
    suspend fun addConsultation(userId: String, consultation: Consultation): Result<String> {
        return try {
            val consultationData = hashMapOf(
                "disease" to consultation.disease,
                "dateDiagnosed" to (consultation.dateDiagnosed ?: Timestamp.now()),
                "cropAffected" to consultation.cropAffected,
                "treatmentDetails" to consultation.treatmentDetails,
                "sellerUsed" to consultation.sellerUsed,
                "treatmentSuccess" to consultation.treatmentSuccess,
                "notes" to consultation.notes
            )

            val consultationRef = db.collection("farmerProfiles")
                .document(userId)
                .collection("consultations")
                .document()

            consultationRef.set(consultationData).await()
            Result.success(consultationRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get seller profile
    suspend fun getSellerProfile(userId: String): Flow<SellerProfile?> = flow {
        try {
            val profileDoc = db.collection("sellerProfiles").document(userId).get().await()

            if (profileDoc.exists()) {
                val subscription = profileDoc.get("subscription") as? Map<String, Any>

                @Suppress("UNCHECKED_CAST")
                val profile = SellerProfile(
                    userId = profileDoc.id,
                    businessName = profileDoc.getString("businessName") ?: "",
                    businessAddress = profileDoc.getString("businessAddress") ?: "",
                    region = profileDoc.getString("region") ?: "",
                    businessDescription = profileDoc.getString("businessDescription") ?: "",
                    businessHours = profileDoc.getString("businessHours") ?: "8:00 AM - 5:00 PM",
                    establishedYear = profileDoc.getLong("establishedYear")?.toInt() ?: 2020,
                    website = profileDoc.getString("website") ?: "",
                    socialMediaLinks = (profileDoc.get("socialMediaLinks") as? Map<String, String>) ?: emptyMap(),
                    certifications = (profileDoc.get("certifications") as? List<String>) ?: emptyList(),
                    specialties = (profileDoc.get("specialties") as? List<String>) ?: emptyList(),
                    rating = profileDoc.getDouble("rating") ?: 0.0,
                    reviewCount = profileDoc.getLong("reviewCount")?.toInt() ?: 0,
                    verified = profileDoc.getBoolean("verified") ?: false,
                    subscription = if (subscription != null) {
                        SubscriptionDetails(
                            plan = subscription["plan"] as? String ?: "free",
                            startDate = subscription["startDate"] as? Timestamp,
                            endDate = subscription["endDate"] as? Timestamp,
                            autoRenew = subscription["autoRenew"] as? Boolean ?: false,
                            features = (subscription["features"] as? List<String>) ?: emptyList()
                        )
                    } else null
                )

                emit(profile)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }

    // Create or update seller profile
    suspend fun updateSellerProfile(profile: SellerProfile): Result<Boolean> {
        return try {
            val profileData = hashMapOf(
                "userId" to profile.userId,
                "businessName" to profile.businessName,
                "businessAddress" to profile.businessAddress,
                "region" to profile.region,
                "businessDescription" to profile.businessDescription,
                "businessHours" to profile.businessHours,
                "establishedYear" to profile.establishedYear,
                "website" to profile.website,
                "socialMediaLinks" to profile.socialMediaLinks,
                "certifications" to profile.certifications,
                "specialties" to profile.specialties,
                "rating" to profile.rating,
                "reviewCount" to profile.reviewCount,
                "verified" to profile.verified
            )

            // Add subscription data if it exists
            profile.subscription?.let {
                val subscriptionData = hashMapOf(
                    "plan" to it.plan,
                    "startDate" to it.startDate,
                    "endDate" to it.endDate,
                    "autoRenew" to it.autoRenew,
                    "features" to it.features
                )
                profileData["subscription"] = subscriptionData
            }

            db.collection("sellerProfiles").document(profile.userId).set(profileData).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Add review for seller
    suspend fun addReview(review: Review): Result<String> {
        return try {
            val reviewData = hashMapOf(
                "sellerId" to review.sellerId,
                "farmerId" to review.farmerId,
                "rating" to review.rating,
                "comment" to review.comment,
                "date" to (review.date ?: Timestamp.now()),
                "productPurchased" to review.productPurchased,
                "verified" to review.verified
            )

            val reviewRef = db.collection("reviews").document()
            reviewRef.set(reviewData).await()

            // Update seller's rating
            val sellerReviews = db.collection("reviews")
                .whereEqualTo("sellerId", review.sellerId)
                .get()
                .await()

            val ratings = sellerReviews.documents.mapNotNull { it.getLong("rating")?.toInt() }
            val avgRating = if (ratings.isNotEmpty()) ratings.average() else 0.0

            db.collection("sellerProfiles").document(review.sellerId)
                .update(
                    "rating", avgRating,
                    "reviewCount", ratings.size
                )
                .await()

            Result.success(reviewRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get reviews for seller
    suspend fun getSellerReviews(sellerId: String): Flow<List<Review>> = flow {
        try {
            val reviewsSnapshot = db.collection("reviews")
                .whereEqualTo("sellerId", sellerId)
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val reviews = reviewsSnapshot.documents.map { doc ->
                Review(
                    id = doc.id,
                    sellerId = doc.getString("sellerId") ?: "",
                    farmerId = doc.getString("farmerId") ?: "",
                    rating = doc.getLong("rating")?.toInt() ?: 0,
                    comment = doc.getString("comment") ?: "",
                    date = doc.getTimestamp("date"),
                    productPurchased = doc.getString("productPurchased") ?: "",
                    verified = doc.getBoolean("verified") ?: false
                )
            }


            emit(reviews)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}
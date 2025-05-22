package com.example.kilimovision.repository

import android.util.Log
import com.example.kilimovision.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await


class FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // User authentication
    suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(authResult.user?.uid ?: "")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, name: String, userType: String): Result<String> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: ""

            // Create user document
            val userData = hashMapOf(
                "name" to name,
                "email" to email,
                "userType" to userType,
                "phone" to "",
                "region" to "Nairobi",
                "address" to "",
                "profilePicture" to "",
                "createdAt" to Timestamp.now()
            )

            db.collection("users").document(userId).set(userData).await()

//            // Initialize profile based on user type
//            when (userType) {
//                "farmer" -> {
//                    val farmerProfile = FarmerProfile(
//                        userId = userId,
//                        region = "Nairobi"
//                    )
//                    createOrUpdateFarmerProfile(farmerProfile)
//                }
//                "seller" -> {
//                    val sellerProfile = SellerProfile(
//                        userId = userId,
//                        region = "Nairobi",
//                        businessName = name + "'s Shop",
//                        subscription = SubscriptionDetails(
//                            plan = "free",
//                            startDate = Timestamp.now(),
//                            endDate = Timestamp.now(),
//                            features = listOf("Basic listing", "Standard visibility")
//                        )
//                    )
//                    createOrUpdateSellerProfile(sellerProfile)
//                }
//            }

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // User profile operations
    suspend fun getCurrentUser(): Flow<User?> = flow {
        try {
            val userId = auth.currentUser?.uid ?: ""
            if (userId.isNotEmpty()) {
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
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting current user: ${e.message}")
            emit(null)
        }
    }

    suspend fun updateUser(user: User): Result<Boolean> {
        return try {
            val userData = hashMapOf(
                "name" to user.name,
                "email" to user.email,
                "phone" to user.phone,
                "region" to user.region,
                "address" to user.address,
                "profilePicture" to user.profilePicture,
                "userType" to user.userType
            )

            db.collection("users").document(user.id).update(userData as Map<String, Any>).await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Farmer profile operations
    suspend fun getFarmerProfile(userId: String): Flow<FarmerProfile?> = flow {
        try {
            val profileDoc = db.collection("farmerProfiles").document(userId).get().await()

            if (profileDoc.exists()) {
                // Get consultation history
                val consultationDocs = db.collection("farmerProfiles")
                    .document(userId)
                    .collection("consultations")
                    .get()
                    .await()

                val consultations = consultationDocs.documents.map { doc ->
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
                    userId = userId,
                    farmName = profileDoc.getString("farmName") ?: "",
                    farmSize = profileDoc.getDouble("farmSize") ?: 0.0,
                    farmType = profileDoc.getString("farmType") ?: "",
                    primaryCrops = (profileDoc.get("primaryCrops") as? List<String>) ?: emptyList(),
                    region = profileDoc.getString("region") ?: "Nairobi",
                    consultationHistory = consultations,
                    preferredSellers = (profileDoc.get("preferredSellers") as? List<String>) ?: emptyList()
                )

                emit(profile)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting farmer profile: ${e.message}")
            emit(null)
        }
    }

    suspend fun createOrUpdateFarmerProfile(profile: FarmerProfile): Result<Boolean> {
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

    // Seller profile operations
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
                    region = profileDoc.getString("region") ?: "Nairobi",
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
            Log.e("FirebaseRepository", "Error getting seller profile: ${e.message}")
            emit(null)
        }
    }

    suspend fun createOrUpdateSellerProfile(profile: SellerProfile): Result<Boolean> {
        return try {
            val profileData = hashMapOf(
                "userId" to profile.userId,
                "businessName" to profile.businessName,
                "businessAddress" to profile.businessAddress,
                "region" to profile.region,
                "phone" to profile.phone,
                "email" to profile.email,
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

            // Add subscription data if available
            profile.subscription?.let {
                val subscriptionData = hashMapOf(
                    "plan" to it.plan,
                    "startDate" to (it.startDate ?: Timestamp.now()),
                    "endDate" to (it.endDate ?: Timestamp.now()),
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

    // Product operations
    suspend fun createProduct(
        sellerId: String,
        name: String,
        description: String,
        price: Double,
        discount: Double,
        category: String,
        applicableDiseases: List<String>,
        inStock: Boolean,
        quantity: Int,
        unitOfMeasure: String,
        region: String
    ): Result<String> {
        return try {
            val productData = hashMapOf(
                "sellerId" to sellerId,
                "name" to name,
                "description" to description,
                "price" to price,
                "discount" to discount,
                "images" to listOf<String>(),
                "category" to category,
                "applicableDiseases" to applicableDiseases,
                "inStock" to inStock,
                "quantity" to quantity,
                "unitOfMeasure" to unitOfMeasure,
                "region" to region,
                "featured" to false,
                "createdAt" to Timestamp.now()
            )

            val productRef = db.collection("products").document()
            productRef.set(productData).await()
            Result.success(productRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProducts(sellerId: String): Flow<List<Product>> = flow {
        try {
            val productsSnapshot = db.collection("products")
                .whereEqualTo("sellerId", sellerId)
                .get()
                .await()

            val products = productsSnapshot.documents.map { doc ->
                @Suppress("UNCHECKED_CAST")
                Product(
                    id = doc.id,
                    sellerId = doc.getString("sellerId") ?: "",
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    price = doc.getDouble("price") ?: 0.0,
                    discount = doc.getDouble("discount") ?: 0.0,
                    images = (doc.get("images") as? List<String>) ?: emptyList(),
                    category = doc.getString("category") ?: "",
                    applicableDiseases = (doc.get("applicableDiseases") as? List<String>) ?: emptyList(),
                    inStock = doc.getBoolean("inStock") ?: false,
                    quantity = doc.getLong("quantity")?.toInt() ?: 0,
                    unitOfMeasure = doc.getString("unitOfMeasure") ?: "",
                    featured = doc.getBoolean("featured") ?: false,
                    createdAt = doc.getTimestamp("createdAt")
                )
            }

            emit(products)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting products: ${e.message}")
            emit(emptyList())
        }
    }

    // Get products for disease and region
    suspend fun getProductsForDiseaseAndRegion(disease: String, region: String): Flow<List<Product>> = flow {
        try {
            val query = if (region != "All Regions") {
                db.collection("products")
                    .whereArrayContains("applicableDiseases", disease)
                    .whereEqualTo("region", region)
            } else {
                db.collection("products")
                    .whereArrayContains("applicableDiseases", disease)
            }

            val productsSnapshot = query.get().await()

            val products = productsSnapshot.documents.map { doc ->
                @Suppress("UNCHECKED_CAST")
                Product(
                    id = doc.id,
                    sellerId = doc.getString("sellerId") ?: "",
                    name = doc.getString("name") ?: "",
                    description = doc.getString("description") ?: "",
                    price = doc.getDouble("price") ?: 0.0,
                    discount = doc.getDouble("discount") ?: 0.0,
                    images = (doc.get("images") as? List<String>) ?: emptyList(),
                    category = doc.getString("category") ?: "",
                    applicableDiseases = (doc.get("applicableDiseases") as? List<String>) ?: emptyList(),
                    inStock = doc.getBoolean("inStock") ?: false,
                    quantity = doc.getLong("quantity")?.toInt() ?: 0,
                    unitOfMeasure = doc.getString("unitOfMeasure") ?: "",
                    featured = doc.getBoolean("featured") ?: false,
                    createdAt = doc.getTimestamp("createdAt")
                )
            }

            emit(products)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting products for disease: ${e.message}")
            emit(emptyList())
        }
    }

    // Get sellers by disease and region
    suspend fun getSellersByDiseaseAndRegion(disease: String, region: String): Flow<List<Seller>> = flow {
        try {
            // Get all products that treat this disease in the specified region
            val products = mutableListOf<Product>()
            getProductsForDiseaseAndRegion(disease, region).collect {
                products.addAll(it)
            }

            // Get seller IDs that have products for this disease
            val sellerIds = products.map { it.sellerId }.toSet()

            // If empty, return empty list
            if (sellerIds.isEmpty()) {
                emit(emptyList())
                return@flow
            }

            // Fetch sellers by IDs
            val sellersSnapshot = db.collection("sellerProfiles")
                .whereIn(com.google.firebase.firestore.FieldPath.documentId(), sellerIds.toList())
                .get()
                .await()

            // Group products by seller
            val productsBySeller = products.groupBy { it.sellerId }

            // Map seller documents to Seller objects
            val sellers = sellersSnapshot.documents.mapNotNull { doc ->
                val sellerId = doc.id
                val sellerProducts = productsBySeller[sellerId] ?: emptyList()

                if (sellerProducts.isEmpty()) return@mapNotNull null

                Seller(
                    id = sellerId,
                    name = doc.getString("businessName") ?: "",
                    address = doc.getString("businessAddress") ?: "",
                    phone = doc.getString("phone") ?: "",
                    distance = 0.0, // Not using distance
                    products = sellerProducts.map { product ->
                        Product(
                            id = product.id,
                            name = product.name,
                            disease = disease,
                            price = product.price,
                            availability = product.inStock
                        )
                    }
                )
            }

            emit(sellers)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error finding sellers: ${e.message}")
            emit(emptyList())
        }
    }

    // Advertisement operations
    suspend fun createAdvertisement(
        sellerId: String,
        title: String,
        price: Double,
        description: String,
        imageUrl: String,
        targetDiseases: List<String>,
        startDate: Timestamp,
        endDate: Timestamp,
        plan: String,
        cost: Double,
        region: String
    ): Result<String> {
        return try {
            val adData = hashMapOf(
                "sellerId" to sellerId,
                "title" to title,
                "description" to description,
                "image" to imageUrl,
                "targetDiseases" to targetDiseases,
                "startDate" to startDate,
                "endDate" to endDate,
                "status" to "pending",
                "impressions" to 0,
                "clicks" to 0,
                "plan" to plan,
                "cost" to cost,
                "paymentStatus" to "pending",
                "region" to region,
                "createdAt" to Timestamp.now()
            )

            val adRef = db.collection("advertisements").document()
            adRef.set(adData).await()
            Result.success(adRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get advertisements for a disease in a region
    suspend fun getAdvertisementsForDiseaseAndRegion(disease: String, region: String): Flow<List<Advertisement>> = flow {
        try {
            val query = if (region != "All Regions") {
                db.collection("advertisements")
                    .whereArrayContains("targetDiseases", disease)
                    .whereEqualTo("region", region)
                    .whereEqualTo("status", "active")
                    .whereGreaterThan("endDate", Timestamp.now())
            } else {
                db.collection("advertisements")
                    .whereArrayContains("targetDiseases", disease)
                    .whereEqualTo("status", "active")
                    .whereGreaterThan("endDate", Timestamp.now())
            }

            val ads = query.get().await().documents.map { doc ->
                Advertisement(
                    id = doc.id,
                    sellerId = doc.getString("sellerId") ?: "",
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    imageUrl = doc.getString("image") ?: "",
                    targetDiseases = (doc.get("targetDiseases") as? List<String>) ?: emptyList(),
                    startDate = doc.getTimestamp("startDate"),
                    endDate = doc.getTimestamp("endDate"),
                    status = doc.getString("status") ?: "",
                    impressions = doc.getLong("impressions")?.toInt() ?: 0,
                    clicks = doc.getLong("clicks")?.toInt() ?: 0,
                    plan = doc.getString("plan") ?: "",
                    cost = doc.getDouble("cost") ?: 0.0,
                    paymentStatus = doc.getString("paymentStatus") ?: "",
                    createdAt = doc.getTimestamp("createdAt")
                )
            }

            emit(ads)
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting advertisements: ${e.message}")
            emit(emptyList())
        }
    }

    // Payment operations
    suspend fun createPayment(
        userId: String,
        amount: Double,
        currency: String,
        type: String,
        referenceId: String,
        paymentMethod: String
    ): Result<String> {
        return try {
            val paymentData = hashMapOf(
                "userId" to userId,
                "amount" to amount,
                "currency" to currency,
                "type" to type,
                "referenceId" to referenceId,
                "status" to "pending",
                "paymentMethod" to paymentMethod,
                "transactionId" to "",
                "timestamp" to Timestamp.now()
            )

            val paymentRef = db.collection("payments").document()
            paymentRef.set(paymentData).await()
            Result.success(paymentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update payment status
    suspend fun updatePaymentStatus(
        paymentId: String,
        status: String,
        transactionId: String
    ): Result<Boolean> {
        return try {
            db.collection("payments").document(paymentId)
                .update(
                    mapOf(
                        "status" to status,
                        "transactionId" to transactionId
                    )
                ).await()

            // If payment is for an ad and status is completed, update ad status
            val payment = db.collection("payments").document(paymentId).get().await()
            if (payment.getString("type") == "advertisement" && status == "completed") {
                val adId = payment.getString("referenceId") ?: ""
                db.collection("advertisements").document(adId)
                    .update("status", "active", "paymentStatus", "paid")
                    .await()
            }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Review operations
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
            updateSellerRating(review.sellerId)

            Result.success(reviewRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getReviewsForSeller(sellerId: String): Flow<List<Review>> = flow {
        try {
            val reviewsSnapshot = db.collection("reviews")
                .whereEqualTo("sellerId", sellerId)
                .orderBy("date", Query.Direction.DESCENDING)
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
            Log.e("FirebaseRepository", "Error getting reviews: ${e.message}")
            emit(emptyList())
        }
    }

    // Helper to update seller rating when a new review is added
    private suspend fun updateSellerRating(sellerId: String) {
        try {
            val reviewsSnapshot = db.collection("reviews")
                .whereEqualTo("sellerId", sellerId)
                .get()
                .await()

            val ratings = reviewsSnapshot.documents.mapNotNull { it.getLong("rating")?.toInt() }
            val avgRating = if (ratings.isNotEmpty()) ratings.average() else 0.0

            db.collection("sellerProfiles").document(sellerId)
                .update(
                    "rating", avgRating,
                    "reviewCount", ratings.size
                )
                .await()
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error updating seller rating: ${e.message}")
        }
    }

    // Disease treatment information
    suspend fun getDiseaseTreatmentInfo(diseaseName: String): Flow<DiseaseTreatment?> = flow {
        try {
            val treatmentDocs = db.collection("diseaseTreatments")
                .whereEqualTo("diseaseName", diseaseName)
                .limit(1)
                .get()
                .await()
                .documents

            if (treatmentDocs.isNotEmpty()) {
                val doc = treatmentDocs[0]
                @Suppress("UNCHECKED_CAST")
                val treatment = DiseaseTreatment(
                    id = doc.id,
                    name = doc.getString("diseaseName") ?: "",
                    description = doc.getString("description") ?: "",
                    symptoms = (doc.get("symptoms") as? List<String>) ?: listOf(),
                    recommendedProducts = (doc.get("recommendedProducts") as? List<String>) ?: listOf(),
                    preventionTips = (doc.get("preventionTips") as? List<String>) ?: listOf(),
                    imageUrl = doc.getString("imageUrl") ?: ""
                )
                emit(treatment)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            Log.e("FirebaseRepository", "Error getting disease treatment: ${e.message}")
            emit(null)
        }
    }



    // Add these functions to your main class or wherever appropriate
    fun updateProductStock(productId: String, inStock: Boolean, onComplete: (Boolean) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("products")
            .document(productId)
            .update("inStock", inStock)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun deleteProduct(productId: String, onComplete: (Boolean) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("products")
            .document(productId)
            .delete()
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun updateAdvertisementStatus(adId: String, status: String, onComplete: (Boolean) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("advertisements")
            .document(adId)
            .update("status", status)
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }

    fun deleteAdvertisement(adId: String, onComplete: (Boolean) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("advertisements")
            .document(adId)
            .delete()
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener {
                onComplete(false)
            }
    }
}
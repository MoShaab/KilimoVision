package com.example.kilimovision.repository

import com.example.kilimovision.model.Advertisement
import com.example.kilimovision.model.DiseaseTreatment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.example.kilimovision.model.Seller
import com.example.kilimovision.model.Product
import com.google.firebase.Timestamp
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

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
                "createdAt" to Timestamp.now()
            )

            db.collection("users").document(userId).set(userData).await()
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get user type
    suspend fun getUserType(userId: String): Result<String> {
        return try {
            val userDoc = db.collection("users").document(userId).get().await()
            if (userDoc.exists()) {
                Result.success(userDoc.getString("userType") ?: "farmer")
            } else {
                Result.failure(Exception("User document not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update user type
    suspend fun updateUserType(userId: String, newUserType: String): Result<Boolean> {
        return try {
            db.collection("users").document(userId)
                .update("userType", newUserType)
                .await()
            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get sellers by disease and region
    fun getSellersByDiseaseAndRegion(
        disease: String,
        region: String
    ): Flow<List<Seller>> = flow {
        try {
            // Get all products that treat this disease
            val productsSnapshot = db.collection("products")
                .whereArrayContains("applicableDiseases", disease)
                .get()
                .await()

            // Get seller IDs that have products for this disease
            val sellerIds = productsSnapshot.documents.map { it.getString("sellerId") }.toSet()

            // If a specific region is selected (not "All Regions")
            val sellersQuery = if (region != "All Regions") {
                db.collection("sellers")
                    .whereEqualTo("region", region)
            } else {
                db.collection("sellers")
            }

            // Get sellers
            val sellersSnapshot = sellersQuery.get().await()

            // Filter and map sellers
            val sellers = sellersSnapshot.documents
                .filter { it.id in sellerIds }
                .map { doc ->
                    // Get seller's products for this disease
                    val products = productsSnapshot.documents
                        .filter { it.getString("sellerId") == doc.id }
                        .map { productDoc ->
                            Product(
                                id = productDoc.id,
                                name = productDoc.getString("name") ?: "",
                                disease = disease,
                                price = productDoc.getDouble("price") ?: 0.0,
                                availability = productDoc.getBoolean("inStock") ?: false
                            )
                        }

                    Seller(
                        id = doc.id,
                        name = doc.getString("businessName") ?: "",
                        address = doc.getString("address") ?: "",
                        phone = doc.getString("phone") ?: "",
                        distance = 0.0, // Not using distance calculation
                        products = products
                    )
                }

            emit(sellers)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Create seller profile with region
    suspend fun createSellerProfile(
        userId: String,
        businessName: String,
        address: String,
        region: String,
        phone: String,
        email: String,
        description: String
    ): Result<String> {
        return try {
            val sellerData = hashMapOf(
                "userId" to userId,
                "businessName" to businessName,
                "address" to address,
                "region" to region,
                "phone" to phone,
                "email" to email,
                "description" to description,
                "profilePicture" to "",
                "rating" to 0,
                "verified" to false,
                "createdAt" to Timestamp.now(),
                "subscription" to hashMapOf(
                    "plan" to "free",
                    "startDate" to Timestamp.now(),
                    "endDate" to Timestamp.now()
                )
            )

            val sellerRef = db.collection("sellers").document()
            sellerRef.set(sellerData).await()

            // Update user type to ensure consistency
            db.collection("users").document(userId)
                .update("userType", "seller")
                .await()

            Result.success(sellerRef.id)
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
        unitOfMeasure: String
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

    // Get products by seller
    suspend fun getProductsBySeller(sellerId: String): Flow<List<Product>> = flow {
        try {
            val products = db.collection("products")
                .whereEqualTo("sellerId", sellerId)
                .get()
                .await()
                .documents
                .map { doc ->
                    Product(
                        id = doc.id,
                        sellerId = doc.getString("sellerId") ?: "",
                        name = doc.getString("name") ?: "",
                        description = doc.getString("description") ?: "",
                        price = doc.getDouble("price") ?: 0.0,
                        discount = doc.getDouble("discount") ?: 0.0,
                        inStock = doc.getBoolean("inStock") ?: false,
                        quantity = doc.getLong("quantity")?.toInt() ?: 0
                    )
                }

            emit(products)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    // Advertisement operations
    suspend fun createAdvertisement(
        sellerId: String,
        title: String,
        description: String,
        imageUrl: String,
        targetDiseases: List<String>,
        startDate: Timestamp,
        endDate: Timestamp,
        plan: String,
        cost: Double
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
                "createdAt" to Timestamp.now()
            )

            val adRef = db.collection("advertisements").document()
            adRef.set(adData).await()
            Result.success(adRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Get advertisements for a disease
    suspend fun getAdvertisementsForDisease(disease: String): Flow<List<Advertisement>> = flow {
        try {
            val ads = db.collection("advertisements")
                .whereArrayContains("targetDiseases", disease)
                .whereEqualTo("status", "active")
                .whereGreaterThan("endDate", Timestamp.now())
                .get()
                .await()
                .documents
                .map { doc ->
                    Advertisement(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        imageUrl = doc.getString("image") ?: "",
                        sellerId = doc.getString("sellerId") ?: ""
                    )
                }

            emit(ads)
        } catch (e: Exception) {
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
                val treatment = DiseaseTreatment(
                    id = doc.id,
                    name = doc.getString("diseaseName") ?: "",
                    description = doc.getString("description") ?: "",
                    symptoms = (doc.get("symptoms") as? List<String>) ?: listOf(),
                    preventionTips = (doc.get("preventionTips") as? List<String>) ?: listOf(),
                    imageUrl = doc.getString("imageUrl") ?: ""
                )
                emit(treatment)
            } else {
                emit(null)
            }
        } catch (e: Exception) {
            emit(null)
        }
    }

    // Check if user is a seller
    suspend fun checkIsSeller(userId: String): Result<Boolean> {
        return try {
            val sellerDocs = db.collection("sellers")
                .whereEqualTo("userId", userId)
                .limit(1)
                .get()
                .await()

            Result.success(!sellerDocs.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Initialize disease treatments
    suspend fun initializeDiseaseTreatments() {
        val treatments = listOf(
            hashMapOf(
                "diseaseName" to "Tomato___Early_blight",
                "description" to "Early blight is a fungal disease that affects tomato plants. It's characterized by brown spots with concentric rings that appear on lower leaves first and can spread upward.",
                "symptoms" to listOf(
                    "Dark brown spots with concentric rings",
                    "Yellowing of leaves around spots",
                    "Lower leaves affected first",
                    "Leaf drop in severe cases"
                ),
                "preventionTips" to listOf(
                    "Rotate crops every 2-3 years",
                    "Remove and destroy infected plant debris",
                    "Water at the base of plants to keep foliage dry",
                    "Ensure good air circulation between plants",
                    "Apply organic fungicides preventatively"
                ),
                "imageUrl" to ""
            ),
            hashMapOf(
                "diseaseName" to "Tomato___Late_blight",
                "description" to "Late blight is a destructive disease caused by the fungus-like organism Phytophthora infestans. It can destroy plants within days and spreads rapidly in cool, wet conditions.",
                "symptoms" to listOf(
                    "Dark, water-soaked spots on leaves",
                    "White fuzzy growth on leaf undersides in humid conditions",
                    "Brown lesions on stems",
                    "Firm, dark, greasy spots on fruit"
                ),
                "preventionTips" to listOf(
                    "Use resistant varieties when available",
                    "Improve air circulation",
                    "Remove volunteer tomato and potato plants",
                    "Apply fungicides preventatively during cool, wet weather",
                    "Destroy infected plants immediately"
                ),
                "imageUrl" to ""
            ),
            hashMapOf(
                "diseaseName" to "Tomato___Leaf_Mold",
                "description" to "Leaf mold is a fungal disease that primarily affects tomato plants in high humidity environments, especially in greenhouses.",
                "symptoms" to listOf(
                    "Pale green or yellowish spots on upper leaf surfaces",
                    "Olive-green to grayish-purple velvety mold on leaf undersides",
                    "Leaves curling, withering and dropping",
                    "Reduced fruit yield"
                ),
                "preventionTips" to listOf(
                    "Space plants for good air circulation",
                    "Use drip irrigation instead of overhead watering",
                    "Prune lower leaves and suckers",
                    "Remove and destroy infected plants",
                    "Control greenhouse humidity"
                ),
                "imageUrl" to ""
            ),
            hashMapOf(
                "diseaseName" to "Tomato___Bacterial_spot",
                "description" to "Bacterial spot is caused by several Xanthomonas species and affects tomatoes and peppers. It can cause significant yield losses in warm, humid conditions.",
                "symptoms" to listOf(
                    "Small, dark, water-soaked spots on leaves, stems and fruit",
                    "Spots enlarge and turn brown with a yellow halo",
                    "Leaf spots may merge, causing leaf blight",
                    "Fruit spots are raised and scabby"
                ),
                "preventionTips" to listOf(
                    "Use disease-free seeds and transplants",
                    "Rotate crops with non-host plants",
                    "Avoid overhead irrigation",
                    "Remove and destroy infected debris",
                    "Apply copper-based bactericides preventatively"
                ),
                "imageUrl" to ""
            )
        )

        for (treatment in treatments) {
            val existingDocs = db.collection("diseaseTreatments")
                .whereEqualTo("diseaseName", treatment["diseaseName"])
                .get()
                .await()

            if (existingDocs.isEmpty) {
                db.collection("diseaseTreatments").add(treatment).await()
            }
        }
    }
}
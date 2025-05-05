package com.example.kilimovision.ui.screens

import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.kilimovision.R
import com.example.kilimovision.model.TomatoDiseaseClassifier
import com.example.kilimovision.util.FileHelper
import com.example.kilimovision.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu

// Define agricultural theme colors
val greenDark = Color(0xFF1B5E20)
val greenMedium = Color(0xFF2E7D32)
val greenLight = Color(0xFF4CAF50)
val earthBrown = Color(0xFF795548)
val sunYellow = Color(0xFFFFC107)
val leafGreen = Color(0xFF81C784)
val skyBlue = Color(0xFF90CAF9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerMainScreen(
    onNavigateToSellers: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToRegister: () -> Unit = {},
    onRedirectToLanding: () -> Unit = {}
) {
    val context = LocalContext.current

    // Check user type to prevent unauthorized access
    val authViewModel: AuthViewModel = viewModel()
    val userType by authViewModel.userType.collectAsState()

    // Effect to check user type and redirect if not a farmer
    LaunchedEffect(userType) {
        if (userType != "farmer" && userType != null) {
            Log.d("FarmerMainScreen", "Unauthorized access attempt. User type: $userType")
            Toast.makeText(
                context,
                "Access denied: Your account is not registered as a farmer",
                Toast.LENGTH_LONG
            ).show()

            onRedirectToLanding()
        }
    }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var classificationResult by remember { mutableStateOf<Pair<String, Float>?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showCropOption by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Initialize the classifier
    var classifier by remember { mutableStateOf<TomatoDiseaseClassifier?>(null) }

    // Initialize classifier when screen is first composed
    LaunchedEffect(Unit) {
        try {
            classifier = TomatoDiseaseClassifier(context)
            Log.d("FarmerMainScreen", "Classifier initialized successfully")
        } catch (e: Exception) {
            Log.e("FarmerMainScreen", "Error initializing classifier: ${e.message}", e)
            errorMessage = "Could not initialize disease detector: ${e.message}"
        }
    }

    // Clean up classifier when screen is disposed
    DisposableEffect(Unit) {
        onDispose {
            classifier?.close()
            Log.d("FarmerMainScreen", "Classifier resources released")
        }
    }

    // Crop image launcher
    val cropImageLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { croppedUri ->
                imageUri = croppedUri
                // Reset classification result when new image is cropped
                classificationResult = null
                errorMessage = null
                showCropOption = false
            }
        } else {
            result.error?.let { error ->
                Log.e("FarmerMainScreen", "Crop error: ${error.message}")
                errorMessage = "Failed to crop image: ${error.message}"
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri?.let { uri ->
                imageUri = uri
                // Show crop option after taking a photo
                showCropOption = true
                // Reset classification result when new image is captured
                classificationResult = null
                errorMessage = null
            }
        } else {
            errorMessage = "Failed to capture image"
            Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                val photoFile = FileHelper.createImageFile(context)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                tempImageUri = uri
                cameraLauncher.launch(uri)
            } catch (ex: Exception) {
                Log.e("FarmerMainScreen", "Error creating image file: ${ex.message}", ex)
                errorMessage = "Error creating image file: ${ex.message}"
                Toast.makeText(context, "Error creating image file", Toast.LENGTH_SHORT).show()
            }
        } else {
            errorMessage = "Camera permission is needed to take photos"
            Toast.makeText(context, "Camera permission is needed to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            // Show crop option after selecting from gallery
            showCropOption = true
            // Reset classification result when new image is selected
            classificationResult = null
            errorMessage = null
        } else {
            errorMessage = "No image selected"
        }
    }

    // Function to launch image cropper
    fun launchCropper(uri: Uri) {
        val cropOptions = CropImageContractOptions(
            uri,
            CropImageOptions().apply {
                guidelines = CropImageView.Guidelines.ON
                outputCompressFormat = android.graphics.Bitmap.CompressFormat.JPEG
                outputCompressQuality = 90
                fixAspectRatio = false // Allow free-form cropping
                cropShape = CropImageView.CropShape.RECTANGLE
                activityTitle = "Crop Plant Image"
                activityMenuIconColor = android.graphics.Color.BLACK
            }
        )
        cropImageLauncher.launch(cropOptions)
    }

    Scaffold(
        topBar = {
            Surface(
                color = greenMedium,
                tonalElevation = 8.dp
            ) {
                TopAppBar(
                    title = {
                        Text(
                            "KilimoVision for Farmers",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,  // Reduced from default (which is typically 18.sp)
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { /* Open your drawer/menu */ }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        // Profile button
                        IconButton(onClick = onNavigateToProfile) {
                            Icon(
                                painter = painterResource(id = R.drawable.profile_icon),
                                contentDescription = "Profile",
                                tint = Color.White
                            )
                        }
                        // Settings button
                        IconButton(onClick = onLogout) {
                            Icon(
                                painter = painterResource(id = R.drawable.logout_icon),
                                contentDescription = "Log out",
                                tint = Color.White
                            )
                        }
                        // Register Icon
                        IconButton(onClick = onNavigateToRegister) {
                            Icon(
                                painter = painterResource(id = R.drawable.register_icon),
                                contentDescription = "Register",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE8F5E9), // Very light green at top
                            Color(0xFFC8E6C9)  // Slightly darker green at bottom
                        )
                    )
                )
        ) {
            // Background farm imagery with low opacity
            Image(
                painter = painterResource(id = R.drawable.farm_background),
                contentDescription = "Farm pattern",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.05f // Very subtle background
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Title with leaf decoration
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.leaf_icon),
                        contentDescription = "Leaf Icon",
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Plant Disease Detection",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = greenDark
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = R.drawable.leaf_icon),
                        contentDescription = "Leaf Icon",
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Description
                Text(
                    text = "Detect tomato plant diseases using your camera",
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Error message
                errorMessage?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }

                // Image Preview Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 4.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(imageUri),
                                contentDescription = "Selected plant image",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.plant_placeholder),
                                    contentDescription = "Plant Placeholder",
                                    modifier = Modifier.size(80.dp),
                                    alpha = 0.7f
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No image selected",
                                    color = Color.Gray
                                )
                                Text(
                                    "Take a photo or select from gallery",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Camera and Gallery buttons in a card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 2.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Buttons row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .padding(end = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = greenMedium
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.camera_icon),
                                        contentDescription = "Camera",
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Take Photo")
                                }
                            }

                            Button(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .padding(start = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = earthBrown
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.gallery_icon),
                                        contentDescription = "Gallery",
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Gallery")
                                }
                            }
                        }

                        // Crop button (visible only when an image is selected)
                        if (showCropOption && imageUri != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { imageUri?.let { launchCropper(it) } },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = sunYellow
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.crop_icon),
                                        contentDescription = "Crop Image",
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Crop Image")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Analysis button
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    try {
                                        isAnalyzing = true
                                        errorMessage = null

                                        if (classifier == null) {
                                            throw Exception("Disease detector not initialized")
                                        }

                                        if (imageUri == null) {
                                            throw Exception("No image selected")
                                        }

                                        // Use the actual classifier to detect disease
                                        val result = withContext(Dispatchers.IO) {
                                            classifier!!.classifyImage(context, imageUri!!)
                                        }

                                        // Process result
                                        if (result != null) {
                                            if (result.first == TomatoDiseaseClassifier.NOT_A_PLANT) {
                                                errorMessage = "This doesn't appear to be a tomato plant"
                                            } else if (result.first == TomatoDiseaseClassifier.VALIDATION_FAILED) {
                                                errorMessage = "Unable to analyze this image"
                                            } else {
                                                classificationResult = result
                                                Log.d("FarmerMainScreen", "Classification result: ${result.first} with confidence ${result.second}")
                                            }
                                        } else {
                                            throw Exception("Classification failed")
                                        }
                                    } catch (e: Exception) {
                                        Log.e("FarmerMainScreen", "Analysis error: ${e.message}", e)
                                        errorMessage = "Analysis failed: ${e.message}"
                                    } finally {
                                        isAnalyzing = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = imageUri != null && !isAnalyzing && classifier != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = greenDark,
                                disabledContainerColor = greenDark.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isAnalyzing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White
                                )
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.analyze_icon),
                                        contentDescription = "Analyze",
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Analyze Image")
                                }
                            }
                        }
                    }
                }

                // Classification Result
                if (isAnalyzing) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(16.dp)
                        ) {
                            CircularProgressIndicator(color = greenMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Analyzing your plant...", color = greenDark)
                            Text("Please wait while our AI identifies any diseases", fontSize = 14.sp, color = Color.Gray)
                        }
                    }
                } else if (classificationResult != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Result header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.diagnosis_icon),
                                    contentDescription = "Diagnosis Icon",
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Disease Detected",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = greenDark
                                )
                            }

                            HorizontalDivider(
                                color = greenLight.copy(alpha = 0.3f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )

                            // Disease name
                            Text(
                                text = classificationResult?.first?.replace("Tomato___", "")?.replace("_", " ") ?: "",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (classificationResult?.first?.contains("Healthy") == true) greenDark else MaterialTheme.colorScheme.error
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Confidence percentage
                            val confidence = ((classificationResult?.second ?: 0f) * 100).toInt()
                            LinearProgressIndicator(
                                progress = { classificationResult?.second ?: 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = when {
                                    confidence > 80 -> greenDark
                                    confidence > 50 -> sunYellow
                                    else -> MaterialTheme.colorScheme.error
                                },
                                trackColor = Color.LightGray,
                            )

                            Text(
                                text = "Confidence: $confidence%",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }

                    // Find suppliers button
                    Button(
                        onClick = {
                            classificationResult?.first?.let { disease ->
                                onNavigateToSellers(disease)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (classificationResult?.first?.contains("Healthy") == true)
                                greenMedium else MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.treatment_icon),
                                contentDescription = "Treatment",
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (classificationResult?.first?.contains("Healthy") == true)
                                    "Browse Preventive Products"
                                else
                                    "Find Treatment Suppliers by Region",
                                fontSize = 16.sp
                            )
                        }
                    }
                }

                // Add educational tip at the bottom
                if (!isAnalyzing && classificationResult == null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = leafGreen.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.tip_icon),
                                contentDescription = "Tip",
                                tint = greenDark,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Farmer's Tip:",
                                    fontWeight = FontWeight.Bold,
                                    color = greenDark
                                )
                                Text(
                                    "For best results, take photos in good lighting and focus on affected plant areas.",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
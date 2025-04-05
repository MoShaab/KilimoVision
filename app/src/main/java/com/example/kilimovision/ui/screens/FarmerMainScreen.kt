package com.example.kilimovision.ui.screens

import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.kilimovision.model.TomatoDiseaseClassifier
import com.example.kilimovision.util.FileHelper
import com.example.kilimovision.viewmodel.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerMainScreen(
    onNavigateToSellers: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToRegister: () -> Unit  ={},
    onRedirectToLanding: () -> Unit = {} // Made optional with default implementation for backward compatibility
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

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = tempImageUri
            // Reset classification result when new image is captured
            classificationResult = null
            errorMessage = null
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
            // Reset classification result when new image is selected
            classificationResult = null
            errorMessage = null
        } else {
            errorMessage = "No image selected"
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("KilimoVision", fontWeight = FontWeight.Bold)
                },
                actions = {
                    // Profile button
                    IconButton(onClick = onNavigateToProfile) {
                        Text("👤")
                    }
                    // Logout button
                    IconButton(onClick = onLogout) {
                        Text("⚙️")
                    }
                    //Register Icon
                    IconButton(onClick =   onNavigateToRegister) {
                        // Logout icon
                        Text("®️")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Title
            Text(
                text = "Plant Disease Detection",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            // Description
            Text(
                text = "Detect tomato plant diseases using your camera",
                fontSize = 16.sp,
                modifier = Modifier.padding(bottom = 8.dp)
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
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "No image selected",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Take a photo or select from gallery",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Buttons
            Button(
                onClick = {
                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Take Photo")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Choose from Gallery")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Analysis button (enabled only when image is selected and classifier is ready)
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
                enabled = imageUri != null && !isAnalyzing && classifier != null
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Analyze Image")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Classification Result
            if (isAnalyzing) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Analyzing image...", color = MaterialTheme.colorScheme.onSurface)
                }
            } else if (classificationResult != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Disease Detected:",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = classificationResult?.first?.replace("Tomato___", "")?.replace("_", " ") ?: "",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Confidence: ${((classificationResult?.second ?: 0f) * 100).toInt()}%",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Find suppliers button
                Button(
                    onClick = {
                        classificationResult?.first?.let { disease ->
                            onNavigateToSellers(disease)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Find Treatment Suppliers by Region")
                }
            }
        }
    }
}
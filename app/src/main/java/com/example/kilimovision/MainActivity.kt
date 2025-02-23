package com.example.kilimovision

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }
}

// TFLite model class
class TomatoDiseaseClassifier(context: Context) {

    private var interpreter: Interpreter? = null
    private val INPUT_SIZE = 224
    private val PIXEL_SIZE = 3
    private val BATCH_SIZE = 1

    // Class labels
    private val labels = arrayOf(
        "Tomato___Bacterial_spot",
        "Tomato___Early_blight",
        "Tomato___Late_blight",
        "Tomato___Leaf_Mold",
        "Tomato___Septoria_leaf_spot",
        "Tomato___Spider_mites Two-spotted_spider_mite",
        "Tomato___Target_Spot",
        "Tomato___Tomato_Yellow_Leaf_Curl_Virus",
        "Tomato___Tomato_mosaic_virus",
        "Tomato___healthy"
    )

    init {
        try {
            // Load the model from assets folder
            val assetFileDescriptor = context.assets.openFd("KilimoVision.tflite")
            val fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
            val fileChannel = fileInputStream.channel
            val startOffset = assetFileDescriptor.startOffset
            val declaredLength = assetFileDescriptor.declaredLength
            val modelBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)

            // Create interpreter
            interpreter = Interpreter(modelBuffer)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun close() {
        interpreter?.close()
        interpreter = null
    }

    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun preprocessBitmap(bitmap: Bitmap): ByteBuffer {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true)
        val byteBuffer = ByteBuffer.allocateDirect(BATCH_SIZE * INPUT_SIZE * INPUT_SIZE * PIXEL_SIZE * 4)
        byteBuffer.order(ByteOrder.nativeOrder())

        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        scaledBitmap.getPixels(intValues, 0, scaledBitmap.width, 0, 0, scaledBitmap.width, scaledBitmap.height)

        var pixel = 0
        for (i in 0 until INPUT_SIZE) {
            for (j in 0 until INPUT_SIZE) {
                val value = intValues[pixel++]

                // Scale RGB values by 1/255 to match training preprocessing
                byteBuffer.putFloat((value shr 16 and 0xFF) / 255f)
                byteBuffer.putFloat((value shr 8 and 0xFF) / 255f)
                byteBuffer.putFloat((value and 0xFF) / 255f)
            }
        }

        return byteBuffer
    }


    fun classifyImage(context: Context, uri: Uri): Pair<String, Float>? {
        val bitmap = loadBitmapFromUri(context, uri) ?: return null

        val inputBuffer = preprocessBitmap(bitmap)
        val outputBuffer = Array(1) { FloatArray(labels.size) }

        interpreter?.run(inputBuffer, outputBuffer)

        var maxIndex = 0
        var maxVal = outputBuffer[0][0]

        for (i in 1 until labels.size) {
            if (outputBuffer[0][i] > maxVal) {
                maxIndex = i
                maxVal = outputBuffer[0][i]
            }
        }

        return Pair(labels[maxIndex], maxVal)
    }
}

private fun createImageFile(context: Context): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val storageDir = context.getExternalFilesDir(null)
    return File.createTempFile(
        imageFileName,  /* prefix */
        ".jpeg",         /* suffix */
        storageDir      /* directory */
    )
}

private fun loadTestImage(context: Context, fileName: String): Uri? {
    try {
        // List all files in test_images directory to debug
        val files = context.assets.list("test_images")
        Log.d("TestImages", "Available files: ${files?.joinToString()}")

        // Try to open the file
        val inputStream = context.assets.open("test_images/$fileName")
        val file = File(context.filesDir, fileName)

        file.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    } catch (e: Exception) {
        Log.e("TestImages", "Error loading image: $fileName", e)
        return null
    }
}

@Composable
fun MainScreen() {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var classificationResult by remember { mutableStateOf<Pair<String, Float>?>(null) }
    var isAnalyzing by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Create classifier
    var classifier by remember { mutableStateOf<TomatoDiseaseClassifier?>(null) }
    LaunchedEffect(key1 = Unit) {
        classifier = TomatoDiseaseClassifier(context)
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            classifier?.close()
        }
    }

    //Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            imageUri = tempImageUri
            // Reset classification result when new image is selected
            classificationResult = null
        } else {
            Toast.makeText(context, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            try {
                val photoFile = createImageFile(context)
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    photoFile
                )
                tempImageUri = uri
                cameraLauncher.launch(uri)
            } catch (ex: Exception) {
                Toast.makeText(context, "Error creating image file", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Camera permission is needed to take photos", Toast.LENGTH_SHORT).show()
        }
    }



    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        // Reset classification result when new image is selected
        classificationResult = null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title
        Text(
            text = "KilimoVision",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 70.dp)
        )

        // Description
        Text(
            text = "Detect tomato plant diseases using your camera",
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Image Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            if (imageUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(data = imageUri)
                            .build()
                    ),
                    contentDescription = "Selected image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No image selected")
                    }
                }
            }
        }

        // Classification Result
        if (isAnalyzing) {
            CircularProgressIndicator()
        } else if (classificationResult != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Disease: ${classificationResult?.first?.replace("Tomato___", "")?.replace("_", " ")}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Confidence: ${(classificationResult?.second ?: 0f) * 100}%",
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Buttons

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { imageUri = loadTestImage(context, "test1.jpeg") },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text("Test Image 1")
            }

            Button(
                onClick = { imageUri = loadTestImage(context, "test2.jpeg") },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
            ) {
                Text("Test Image 2")
            }
        }

        Button(
            onClick = {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Take Photo")
        }

        Button(
            onClick = { galleryLauncher.launch("image/*") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Choose from Gallery")
        }

        // Analysis button (enabled only when image is selected)
        Button(
            onClick = {
                if (imageUri != null && classifier != null) {
                    isAnalyzing = true
                    coroutineScope.launch {
                        val result = withContext(Dispatchers.Default) {
                            classifier?.classifyImage(context, imageUri!!)
                        }
                        classificationResult = result
                        isAnalyzing = false
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = imageUri != null && !isAnalyzing
        ) {
            Text("Analyze Image")
        }
    }

}
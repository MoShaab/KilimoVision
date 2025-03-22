package com.example.kilimovision.model

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class TomatoDiseaseClassifier(context: Context) {

    private var interpreter: Interpreter? = null
    private val INPUT_SIZE = 224
    private val PIXEL_SIZE = 3
    private val BATCH_SIZE = 1
    private val CONFIDENCE_THRESHOLD = 0.7f

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

    // Results when validation fails
    companion object {
        const val NOT_A_PLANT = "Not a tomato plant"
        const val VALIDATION_FAILED = "Unable to analyze image"
    }

    init {
        try {
            // Load model from assets folder
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


    private fun checkPredictionDistribution(outputProbabilities: FloatArray): Boolean {
        // Check if the prediction distribution looks reasonable
        // Sort probabilities (descending)
        val sortedProbs = outputProbabilities.sortedArrayDescending()

        // Calculate the difference between top predictions
        val topDiff = if (sortedProbs.size >= 2) {
            sortedProbs[0] - sortedProbs[1]
        } else {
            1.0f
        }

        // If one class is extremely confident compared to others, it might be suspicious
        return topDiff < 0.7f
    }

    fun classifyImage(context: Context, uri: Uri): Pair<String, Float>? {
        val bitmap = loadBitmapFromUri(context, uri) ?: return Pair(VALIDATION_FAILED, 0.0f)

        // First validate if this is likely a tomato plant image
//        val validator = ImageValidator()
//        if (!validator.validateImage(bitmap)) {
//            return Pair(NOT_A_PLANT, 0.0f)
//        }

        val inputBuffer = preprocessBitmap(bitmap)
        val outputBuffer = Array(1) { FloatArray(labels.size) }

        interpreter?.run(inputBuffer, outputBuffer)

        // Check if the prediction distribution looks suspicious
//        if (!checkPredictionDistribution(outputBuffer[0])) {
//            return Pair(NOT_A_PLANT, 0.0f)
//        }

        var maxIndex = 0
        var maxVal = outputBuffer[0][0]

        for (i in 1 until labels.size) {
            if (outputBuffer[0][i] > maxVal) {
                maxIndex = i
                maxVal = outputBuffer[0][i]
            }
        }

        // Only return a classification if the confidence is above threshold
        return if (maxVal >= CONFIDENCE_THRESHOLD) {
            Pair(labels[maxIndex], maxVal)
        } else {
            Pair(NOT_A_PLANT, maxVal)
        }
    }
}

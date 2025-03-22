//package com.example.kilimovision.model
//
//import android.graphics.Bitmap
//
//class ImageValidator {
//
//    fun validateImage(bitmap: Bitmap): Boolean {
//        // 1. Check if the image likely contains a plant
//        val isPlant = isLikelyPlantImage(bitmap)
//
//        // 2. Check for uniform background (common in portraits/product photos)
//        val hasUniformBg = hasUniformBackground(bitmap)
//
//        // 3. Check for face-like features
//        val hasFace = hasFaceFeatures(bitmap)
//
//        // Combined check:
//        // - Should have plant-like features
//        // - Shouldn't have a very uniform background
//        // - Shouldn't have face-like features
//        return isPlant && !hasUniformBg && !hasFace
//    }
//
//    private fun isLikelyPlantImage(bitmap: Bitmap): Boolean {
//        // Create a downsampled bitmap for faster analysis
//        val sampleSize = 32
//        val sampledBitmap = Bitmap.createScaledBitmap(bitmap, sampleSize, sampleSize, true)
//
//        // Calculate color statistics
//        var greenPixelCount = 0
//        var totalPixels = sampleSize * sampleSize
//
//        val pixels = IntArray(totalPixels)
//        sampledBitmap.getPixels(pixels, 0, sampleSize, 0, 0, sampleSize, sampleSize)
//
//        // Green color dominance check
//        for (pixel in pixels) {
//            val r = pixel shr 16 and 0xFF
//            val g = pixel shr 8 and 0xFF
//            val b = pixel and 0xFF
//
//            // Check if this pixel is "greenish" (more green than red and blue)
//            if (g > r && g > b && g > 60) {
//                greenPixelCount++
//            }
//        }
//
//        // If at least 15% of the image has green pixels, it might be a plant
//        val greenRatio = greenPixelCount.toFloat() / totalPixels
//        return greenRatio > 0.15f
//    }
//
//    private fun hasUniformBackground(bitmap: Bitmap): Boolean {
//        // Check if image has a very uniform background (likely a photo of a person or solid object)
//        val sampleSize = 32
//        val sampledBitmap = Bitmap.createScaledBitmap(bitmap, sampleSize, sampleSize, true)
//
//        val pixels = IntArray(sampleSize * sampleSize)
//        sampledBitmap.getPixels(pixels, 0, sampleSize, 0, 0, sampleSize, sampleSize)
//
//        // Count different colors in border regions (to check for studio photos)
//        val borderPixels = mutableSetOf<Int>()
//
//        // Top and bottom rows
//        for (x in 0 until sampleSize) {
//            borderPixels.add(pixels[x]) // Top row
//            borderPixels.add(pixels[(sampleSize - 1) * sampleSize + x]) // Bottom row
//        }
//
//        // Left and right columns
//        for (y in 0 until sampleSize) {
//            borderPixels.add(pixels[y * sampleSize]) // Left column
//            borderPixels.add(pixels[y * sampleSize + (sampleSize - 1)]) // Right column
//        }
//
//        // If border has few unique colors, likely a studio photograph
//        return borderPixels.size < 10
//    }
//
//    private fun hasFaceFeatures(bitmap: Bitmap): Boolean {
//        // This is a simplified check for face-like features
//        // A production app should use ML Kit or similar for proper face detection
//        val sampleSize = 100
//        val sampledBitmap = Bitmap.createScaledBitmap(bitmap, sampleSize, sampleSize, true)
//
//        val pixels = IntArray(sampleSize * sampleSize)
//        sampledBitmap.getPixels(pixels, 0, sampleSize, 0, 0, sampleSize, sampleSize)
//
//        // Simplified skin tone detection
//        var skinTonePixels = 0
//        val totalPixels = sampleSize * sampleSize
//
//        for (pixel in pixels) {
//            val r = pixel shr 16 and 0xFF
//            val g = pixel shr 8 and 0xFF
//            val b = pixel and 0xFF
//
//            // Very basic skin tone detection
//            if (r > 60 && g > 40 && b > 20 &&
//                r > g && g > b &&
//                (r - g) > 15 && (g - b) > 15) {
//                skinTonePixels++
//            }
//        }
//
//        // If more than 25% of the image has skin-like tones, might be a human
//        return skinTonePixels.toFloat() / totalPixels > 0.25f
//    }
//}
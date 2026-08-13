package com.novacamera.app.processing

import android.graphics.Bitmap

enum class SceneType {
    PERSON, FACE, FOOD, ANIMAL, FLOWER, LANDSCAPE, BUILDING, SKY, SUNSET, NIGHT, DOCUMENT, UNKNOWN,
}

/**
 * An intentionally dependency-free scene classifier. It uses luminance and color
 * heuristics as a safe baseline; an ML Kit/TFLite model can implement the same
 * interface later without changing CameraUI or ImageProcessor.
 */
class AIEngine {
    fun detect(bitmap: Bitmap): SceneType {
        val sample = bitmap.getPixel(bitmap.width / 2, bitmap.height / 2)
        val r = android.graphics.Color.red(sample)
        val g = android.graphics.Color.green(sample)
        val b = android.graphics.Color.blue(sample)
        val luminance = (0.2126 * r + 0.7152 * g + 0.0722 * b)
        return when {
            luminance < 35 -> SceneType.NIGHT
            b > r * 1.25 && b > g * 1.1 -> SceneType.SKY
            r > b * 1.35 && r > g * 1.08 -> SceneType.SUNSET
            else -> SceneType.UNKNOWN
        }
    }
}
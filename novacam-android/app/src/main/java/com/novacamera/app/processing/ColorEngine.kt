package com.novacamera.app.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.novacamera.app.config.CameraConfig

object ColorEngine {
    fun apply(bitmap: Bitmap, config: CameraConfig): Bitmap {
        val output = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val processing = config.processing
        val matrix = ColorMatrix().apply {
            setSaturation((processing.saturation * config.color.vibrance).coerceIn(0f, 3f))
            val contrast = processing.contrast.coerceIn(0.2f, 2.5f)
            val translate = 128f * (1f - contrast)
            postConcat(ColorMatrix(floatArrayOf(
                contrast, 0f, 0f, 0f, translate,
                0f, contrast, 0f, 0f, translate,
                0f, 0f, contrast, 0f, translate,
                0f, 0f, 0f, 1f, 0f,
            )))
        }
        Canvas(output).drawBitmap(
            bitmap,
            0f,
            0f,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { colorFilter = ColorMatrixColorFilter(matrix) },
        )
        return output
    }
}
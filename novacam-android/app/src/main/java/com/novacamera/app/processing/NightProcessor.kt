package com.novacamera.app.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.novacamera.app.config.CameraConfig

class NightProcessor(private val hdrProcessor: HDRProcessor = HDRProcessor()) {
    fun process(frames: List<Bitmap>, config: CameraConfig): Bitmap {
        val merged = hdrProcessor.merge(frames, config.night.shadowRecovery)
        val matrix = ColorMatrix(floatArrayOf(
            1.08f, 0f, 0f, 0f, 5f,
            0f, 1.08f, 0f, 0f, 5f,
            0f, 0f, 1.08f, 0f, 5f,
            0f, 0f, 0f, 1f, 0f,
        ))
        return merged.copy(Bitmap.Config.ARGB_8888, true).also {
            Canvas(it).drawBitmap(merged, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                colorFilter = ColorMatrixColorFilter(matrix)
            })
        }
    }
}
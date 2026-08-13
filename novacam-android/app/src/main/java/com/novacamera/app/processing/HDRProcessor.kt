package com.novacamera.app.processing

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.min

class HDRProcessor {
    fun merge(frames: List<Bitmap>, strength: Float = 1f): Bitmap {
        require(frames.isNotEmpty()) { "At least one frame is required" }
        if (frames.size == 1) return frames.first().copy(Bitmap.Config.ARGB_8888, true)
        val base = frames.first().copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(base)
        val alpha = (strength.coerceIn(0f, 2f) / frames.size.toFloat()).coerceIn(0.05f, 0.45f)
        frames.drop(1).forEach { frame ->
            canvas.drawBitmap(frame, 0f, 0f, Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.alpha = (alpha * 255).toInt()
            })
        }
        return base
    }

    fun recommendedFrameCount(iso: Int?, lowEnd: Boolean): Int = when {
        lowEnd -> 3
        (iso ?: 200) > 1600 -> min(8, 5)
        (iso ?: 200) > 800 -> 5
        else -> 3
    }
}
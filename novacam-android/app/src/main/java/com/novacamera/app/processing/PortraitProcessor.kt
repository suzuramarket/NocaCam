package com.novacamera.app.processing

import android.graphics.Bitmap
import com.novacamera.app.config.CameraConfig

/**
 * Portrait processing is deliberately conservative. A device depth stream can be
 * added through Camera2 DEPTH16/DEPTH_POINT_CLOUD without changing the public API.
 * On phones without depth, the original frame is returned rather than producing
 * an unsafe cutout around hair or objects.
 */
class PortraitProcessor {
    fun process(bitmap: Bitmap, config: CameraConfig, hasDepthStream: Boolean): Bitmap {
        if (!hasDepthStream) return bitmap
        return bitmap
    }
}
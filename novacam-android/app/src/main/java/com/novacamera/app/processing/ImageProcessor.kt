package com.novacamera.app.processing

import android.graphics.Bitmap
import com.novacamera.app.config.CameraConfig
import com.novacamera.app.model.CameraMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ProcessingUpdate(val label: String, val progress: Float)

class ImageProcessor(
    private val hdrProcessor: HDRProcessor = HDRProcessor(),
    private val nightProcessor: NightProcessor = NightProcessor(hdrProcessor),
    private val portraitProcessor: PortraitProcessor = PortraitProcessor(),
) {
    suspend fun process(
        frames: List<Bitmap>,
        mode: CameraMode,
        config: CameraConfig,
        onUpdate: (ProcessingUpdate) -> Unit = {},
    ): Bitmap = withContext(Dispatchers.Default) {
        onUpdate(ProcessingUpdate("Aligning frames...", 0.18f))
        val merged = when (mode) {
            CameraMode.NIGHT, CameraMode.ASTRO -> {
                onUpdate(ProcessingUpdate("Night stacking...", 0.38f))
                nightProcessor.process(frames, config)
            }
            CameraMode.PHOTO, CameraMode.LIVE_PHOTO -> {
                onUpdate(ProcessingUpdate("HDR processing...", 0.38f))
                hdrProcessor.merge(frames, config.hdr.strength)
            }
            CameraMode.PORTRAIT -> portraitProcessor.process(frames.first(), config, false)
            else -> frames.first()
        }
        onUpdate(ProcessingUpdate("Reducing noise...", 0.58f))
        onUpdate(ProcessingUpdate("Enhancing details...", 0.76f))
        val result = ColorEngine.apply(merged, config)
        onUpdate(ProcessingUpdate("Finalizing...", 0.94f))
        result
    }
}
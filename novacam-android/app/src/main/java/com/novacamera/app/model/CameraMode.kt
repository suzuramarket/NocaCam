package com.novacamera.app.model

enum class CameraMode {
    PHOTO,
    NIGHT,
    PORTRAIT,
    VIDEO,
    SLOW_MOTION,
    PANORAMA,
    ASTRO,
    PRO,
    MACRO,
    LIVE_PHOTO,
}

data class ManualSettings(
    val iso: Int? = null,
    val shutterNanos: Long? = null,
    val exposureCompensation: Int = 0,
    val focusDistance: Float? = null,
    val whiteBalance: Int? = null,
)

data class CaptureResult(
    val imagePath: String,
    val rawPath: String? = null,
    val livePhotoVideoPath: String? = null,
    val mode: CameraMode,
)
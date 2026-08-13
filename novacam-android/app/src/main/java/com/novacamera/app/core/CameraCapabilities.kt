package com.novacamera.app.core

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import android.graphics.ImageFormat
import android.util.Range
import android.util.Size

data class HardwareSnapshot(
    val cameraCount: Int,
    val cameraId: String?,
    val hardwareLevel: Int?,
    val sensorSize: Size?,
    val supportsRaw: Boolean,
    val supportsOis: Boolean,
    val supportsFlash: Boolean,
    val supportsHdr: Boolean,
    val supportsEis: Boolean,
    val isoRange: Range<Int>?,
    val exposureRange: Range<Long>?,
    val fpsRange: Range<Int>?,
    val maxDigitalZoom: Float,
    val focalLengths: List<Float>,
    val jpegSize: Size?,
)

class CameraCapabilities(private val cameraManager: CameraManager) {
    fun inspect(): HardwareSnapshot {
        val ids = runCatching { cameraManager.cameraIdList.toList() }.getOrDefault(emptyList())
        val cameraId = ids.firstOrNull { id ->
            runCatching {
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            }.getOrDefault(false)
        } ?: ids.firstOrNull()

        val characteristics = cameraId?.let { runCatching { cameraManager.getCameraCharacteristics(it) }.getOrNull() }
        val map = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val formats = map?.outputFormats?.toSet().orEmpty()
        val capabilities = characteristics
            ?.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
            ?.toSet()
            .orEmpty()

        return HardwareSnapshot(
            cameraCount = ids.size,
            cameraId = cameraId,
            hardwareLevel = characteristics?.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL),
            sensorSize = characteristics?.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE),
            supportsRaw = formats.contains(ImageFormat.RAW_SENSOR),
            supportsOis = characteristics?.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION)
                ?.contains(CameraCharacteristics.LENS_OPTICAL_STABILIZATION_MODE_ON) == true,
            supportsFlash = characteristics?.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true,
            supportsHdr = capabilities.contains(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_YUV_REPROCESSING,
            ) || map?.outputFormats?.contains(ImageFormat.YUV_420_888) == true,
            supportsEis = characteristics?.get(CameraCharacteristics.CONTROL_AVAILABLE_VIDEO_STABILIZATION_MODES)
                ?.contains(CameraCharacteristics.CONTROL_VIDEO_STABILIZATION_MODE_ON) == true,
            isoRange = characteristics?.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE),
            exposureRange = characteristics?.get(CameraCharacteristics.SENSOR_INFO_EXPOSURE_TIME_RANGE),
            fpsRange = characteristics?.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES)
                ?.maxByOrNull { it.upper },
            maxDigitalZoom = characteristics?.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 1f,
            focalLengths = characteristics?.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS)
                ?.toList()
                .orEmpty(),
            jpegSize = map?.getOutputSizes(ImageFormat.JPEG)?.maxByOrNull { it.width.toLong() * it.height },
        )
    }
}
package com.novacamera.app.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import com.novacamera.app.config.CameraConfig
import com.novacamera.app.model.CameraMode
import com.novacamera.app.model.CaptureResult
import com.novacamera.app.processing.ImageProcessor
import com.novacamera.app.processing.LivePhotoProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

class CameraEngine(
    private val context: Context,
    private val storageManager: StorageManager = StorageManager(context),
    private val imageProcessor: ImageProcessor = ImageProcessor(),
) {
    private val cameraManager = context.getSystemService(CameraManager::class.java)
    private val backgroundThread = HandlerThread("NovaCamCamera").apply { start() }
    private val backgroundHandler = Handler(backgroundThread.looper)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val livePhotoProcessor = LivePhotoProcessor(context.cacheDir)
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var previewSurface: Surface? = null
    private var imageReader: ImageReader? = null
    private var previewRequest: CaptureRequest? = null
    private var currentBuilder: CaptureRequest.Builder? = null
    private var cameraId: String? = null
    private var characteristics: CameraCharacteristics? = null
    private var zoomRatio = 1f
    private var pendingCapture = AtomicBoolean(false)
    private var livePhotoEnabled = false
    private var livePhotoSurface: Surface? = null

    /** Rebuilds the capture session so the Live Photo recorder surface is added/removed as a target. */
    fun setLivePhotoEnabled(enabled: Boolean) {
        if (livePhotoEnabled == enabled) return
        livePhotoEnabled = enabled
        if (cameraDevice != null) createPreviewSession()
    }

    fun attachPreview(textureView: TextureView) {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: android.graphics.SurfaceTexture, width: Int, height: Int) {
                previewSurface = Surface(surface)
                openCamera()
            }

            override fun onSurfaceTextureSizeChanged(surface: android.graphics.SurfaceTexture, width: Int, height: Int) = Unit
            override fun onSurfaceTextureDestroyed(surface: android.graphics.SurfaceTexture): Boolean {
                close()
                return true
            }
            override fun onSurfaceTextureUpdated(surface: android.graphics.SurfaceTexture) = Unit
        }
        if (textureView.isAvailable) {
            previewSurface = Surface(textureView.surfaceTexture)
            openCamera()
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        if (!PermissionManager.cameraGranted(context)) return
        val id = cameraId ?: cameraManager.cameraIdList.firstOrNull { candidate ->
            cameraManager.getCameraCharacteristics(candidate)
                .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        } ?: return
        cameraId = id
        characteristics = cameraManager.getCameraCharacteristics(id)
        val map = characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val size = map?.getOutputSizes(android.graphics.ImageFormat.JPEG)
            ?.maxByOrNull { it.width.toLong() * it.height }
            ?: android.util.Size(1920, 1080)
        imageReader?.close()
        imageReader = ImageReader.newInstance(size.width, size.height, android.graphics.ImageFormat.JPEG, 3).also { reader ->
            reader.setOnImageAvailableListener({ source ->
                val image = source.acquireLatestImage() ?: return@setOnImageAvailableListener
                image.use { frame ->
                    val buffer = frame.planes.firstOrNull()?.buffer ?: return@use
                    val bytes = ByteArray(buffer.remaining()).also(buffer::get)
                    handleCapturedJpeg(bytes)
                }
            }, backgroundHandler)
        }
        cameraManager.openCamera(id, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                createPreviewSession()
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
                cameraDevice = null
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
                cameraDevice = null
            }
        }, backgroundHandler)
    }

    private fun createPreviewSession() {
        val device = cameraDevice ?: return
        val preview = previewSurface ?: return
        val readerSurface = imageReader?.surface ?: return
        runCatching {
            // Recorder surface must exist before the session is built; MediaRecorder.start()
            // requires its surface to already belong to a configured Camera2 session.
            if (livePhotoSurface != null) livePhotoProcessor.cancel()
            livePhotoSurface = if (livePhotoEnabled) livePhotoProcessor.prepareSurface() else null

            currentBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(preview)
                livePhotoSurface?.let { addTarget(it) }
                set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                applyZoom(this)
            }
            val targets = listOfNotNull(preview, readerSurface, livePhotoSurface)
            device.createCaptureSession(
                targets,
                object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        captureSession = session
                        previewRequest = currentBuilder?.build()
                        previewRequest?.let { session.setRepeatingRequest(it, null, backgroundHandler) }
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) = Unit
                },
                backgroundHandler,
            )
        }
    }

    fun setZoom(ratio: Float) {
        zoomRatio = ratio.coerceAtLeast(1f)
        val builder = currentBuilder ?: return
        applyZoom(builder)
        captureSession?.setRepeatingRequest(builder.build(), null, backgroundHandler)
    }

    fun setFocus(x: Float, y: Float, viewWidth: Int, viewHeight: Int) {
        val builder = currentBuilder ?: return
        val sensor = characteristics?.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE) ?: return
        val focusX = (x / viewWidth * sensor.width()).roundToInt().coerceIn(0, sensor.width() - 1)
        val focusY = (y / viewHeight * sensor.height()).roundToInt().coerceIn(0, sensor.height() - 1)
        val region = Rect(
            (focusX - sensor.width() / 12).coerceAtLeast(0),
            (focusY - sensor.height() / 12).coerceAtLeast(0),
            (focusX + sensor.width() / 12).coerceAtMost(sensor.width()),
            (focusY + sensor.height() / 12).coerceAtMost(sensor.height()),
        )
        builder.set(CaptureRequest.CONTROL_AF_REGIONS, arrayOf(android.hardware.camera2.params.MeteringRectangle(region, 1000)))
        builder.set(CaptureRequest.CONTROL_AE_REGIONS, arrayOf(android.hardware.camera2.params.MeteringRectangle(region, 1000)))
        builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
        captureSession?.capture(builder.build(), null, backgroundHandler)
        captureSession?.setRepeatingRequest(builder.build(), null, backgroundHandler)
    }

    fun capture(
        mode: CameraMode,
        config: CameraConfig,
        livePhoto: Boolean,
        onUpdate: (String) -> Unit,
        onComplete: (CaptureResult) -> Unit,
        onError: (String) -> Unit,
    ) {
        val session = captureSession
        val device = cameraDevice
        val readerSurface = imageReader?.surface
        if (session == null || device == null || readerSurface == null) {
            onError("Kamera belum siap.")
            return
        }
        if (!pendingCapture.compareAndSet(false, true)) return
        onUpdate(if (mode == CameraMode.NIGHT) "Capturing frames..." else "Capturing...")
        if (livePhoto && livePhotoSurface != null) livePhotoProcessor.start()
        pendingCallback = PendingCapture(mode, config, livePhoto, onUpdate, onComplete, onError)
        runCatching {
            val builder = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(readerSurface)
                set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                applyZoom(this)
            }
            session.stopRepeating()
            session.capture(builder.build(), object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: android.hardware.camera2.TotalCaptureResult,
                ) {
                    session.setRepeatingRequest(previewRequest ?: return, null, backgroundHandler)
                }
            }, backgroundHandler)
        }.onFailure {
            pendingCapture.set(false)
            pendingCallback?.onError?.invoke("Capture gagal: ${it.message ?: "unknown error"}")
            pendingCallback = null
        }
    }

    private data class PendingCapture(
        val mode: CameraMode,
        val config: CameraConfig,
        val livePhoto: Boolean,
        val onUpdate: (String) -> Unit,
        val onComplete: (CaptureResult) -> Unit,
        val onError: (String) -> Unit,
    )

    private var pendingCallback: PendingCapture? = null

    private fun handleCapturedJpeg(bytes: ByteArray) {
        val pending = pendingCallback ?: return
        pending.onUpdate("Applying config: ${pending.config.name}")
        scope.launch {
            runCatching {
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    ?: error("Unable to decode camera frame")
                val resultBitmap = imageProcessor.process(
                    frames = listOf(bitmap),
                    mode = pending.mode,
                    config = pending.config,
                ) { update -> pending.onUpdate(update.label) }
                val path = storageManager.saveBitmap(resultBitmap)
                val liveBundle = if (pending.livePhoto) {
                    pending.onUpdate("Saving Live Photo...")
                    livePhotoProcessor.stop(path)
                } else {
                    null
                }
                pending.onComplete(CaptureResult(path, livePhotoVideoPath = liveBundle?.videoPath, mode = pending.mode))
            }.onFailure {
                pending.onError("Pemrosesan gagal: ${it.message ?: "unknown error"}")
            }
            pendingCapture.set(false)
            pendingCallback = null
        }
    }

    private fun applyZoom(builder: CaptureRequest.Builder) {
        val sensor = characteristics?.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE) ?: return
        val maxZoom = characteristics?.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM) ?: 1f
        val clamped = zoomRatio.coerceIn(1f, maxZoom)
        val cropWidth = (sensor.width() / clamped).roundToInt()
        val cropHeight = (sensor.height() / clamped).roundToInt()
        val left = (sensor.width() - cropWidth) / 2
        val top = (sensor.height() - cropHeight) / 2
        builder.set(CaptureRequest.SCALER_CROP_REGION, Rect(left, top, left + cropWidth, top + cropHeight))
    }

    fun close() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
        livePhotoProcessor.cancel()
        livePhotoSurface = null
    }

    fun shutdown() {
        close()
        livePhotoProcessor.cancel()
        backgroundThread.quitSafely()
        scope.cancel()
    }
}
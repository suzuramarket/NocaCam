package com.novacamera.app.processing

import android.media.MediaRecorder
import android.os.Build
import org.json.JSONObject
import java.io.File

data class LivePhotoBundle(
    val imagePath: String,
    val videoPath: String?,
    val metadataPath: String?,
)

class LivePhotoProcessor(private val cacheDirectory: File) {
    private var recorder: MediaRecorder? = null
    private var currentVideo: File? = null
    private var started = false

    /**
     * Creates and prepares the MediaRecorder, returning its input Surface so the
     * caller can add it as a Camera2 session target BEFORE calling [start].
     * Must be called (and the returned surface added to the capture session)
     * before [start] — MediaRecorder.start() requires the surface to already be
     * part of a configured capture session, otherwise it fails silently.
     */
    fun prepareSurface(): android.view.Surface? = runCatching {
        val output = File(cacheDirectory, "motion_${System.currentTimeMillis()}.mp4")
        val mediaRecorder = MediaRecorder()
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder.setVideoFrameRate(30)
        mediaRecorder.setVideoSize(1280, 720)
        mediaRecorder.setVideoEncodingBitRate(6_000_000)
        mediaRecorder.setMaxDuration(4000)
        mediaRecorder.setOutputFile(output.absolutePath)
        mediaRecorder.prepare()
        recorder = mediaRecorder
        currentVideo = output
        started = false
        mediaRecorder.surface
    }.getOrNull()

    /** Starts writing frames. Call only after the recorder's surface is part of an active capture session. */
    fun start(): Boolean {
        val mediaRecorder = recorder ?: return false
        if (started) return true
        return runCatching {
            mediaRecorder.start()
            started = true
        }.isSuccess
    }

    fun stop(imagePath: String): LivePhotoBundle? {
        val mediaRecorder = recorder ?: return null
        val video = currentVideo ?: return null
        if (!started) return null
        return runCatching {
            mediaRecorder.stop()
            mediaRecorder.reset()
            mediaRecorder.release()
            recorder = null
            started = false
            val metadata = File(cacheDirectory, "${video.nameWithoutExtension}.json").apply {
                writeText(JSONObject().apply {
                    put("type", "novacam-live-photo")
                    put("image", imagePath)
                    put("video", video.absolutePath)
                    put("durationMs", 3000)
                    put("sound", false)
                    put("appVersion", "0.1.0")
                }.toString(2))
            }
            LivePhotoBundle(imagePath, video.absolutePath, metadata.absolutePath)
        }.getOrNull()
    }

    fun cancel() {
        runCatching {
            if (started) recorder?.stop()
            recorder?.release()
        }
        recorder = null
        started = false
        currentVideo?.delete()
        currentVideo = null
    }
}
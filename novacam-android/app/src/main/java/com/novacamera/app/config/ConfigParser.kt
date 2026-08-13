package com.novacamera.app.config

import org.json.JSONArray
import org.json.JSONObject

object ConfigParser {
    fun parse(text: String): Result<CameraConfig> = runCatching {
        val root = JSONObject(text)
        val name = root.optString("name").ifBlank { "Imported Config" }
        val processing = root.optJSONObject("processing")
        val color = root.optJSONObject("color")
        val hdr = root.optJSONObject("hdr")
        val night = root.optJSONObject("night")
        val portrait = root.optJSONObject("portrait")

        CameraConfig(
            name = name,
            version = root.optString("version", "1.0"),
            appVersion = root.optString("appVersion", "0.1.0"),
            deviceCompatibility = root.optStringArray("deviceCompatibility"),
            cameraCompatibility = root.optStringArray("cameraCompatibility"),
            processingCompatibility = root.optStringArray("processingCompatibility"),
            processing = ProcessingConfig(
                sharpness = processing?.boundedFloat("sharpness", 1f, 0f, 3f) ?: 1f,
                noiseReduction = processing?.boundedFloat("noiseReduction", 1f, 0f, 3f) ?: 1f,
                contrast = processing?.boundedFloat("contrast", 1f, 0f, 3f) ?: 1f,
                saturation = processing?.boundedFloat("saturation", 1f, 0f, 3f) ?: 1f,
                highlightRecovery = processing?.boundedFloat("highlightRecovery", 1f, 0f, 3f) ?: 1f,
                shadowRecovery = processing?.boundedFloat("shadowRecovery", 1f, 0f, 3f) ?: 1f,
            ),
            color = ColorConfig(
                temperature = color?.boundedInt("temperature", 5200, 2000, 12000) ?: 5200,
                tint = color?.boundedFloat("tint", 1f, 0.5f, 1.5f) ?: 1f,
                vibrance = color?.boundedFloat("vibrance", 1f, 0f, 3f) ?: 1f,
            ),
            hdr = HdrConfig(
                enabled = hdr?.optBoolean("enabled", true) ?: true,
                frameCount = hdr?.boundedInt("frameCount", 5, 1, 15) ?: 5,
                strength = hdr?.boundedFloat("strength", 1f, 0f, 2f) ?: 1f,
                ghostReduction = hdr?.boundedFloat("ghostReduction", 1f, 0f, 2f) ?: 1f,
            ),
            night = NightConfig(
                exposureMultiplier = night?.boundedFloat("exposureMultiplier", 1.2f, 0.5f, 4f) ?: 1.2f,
                noiseReduction = night?.boundedFloat("noiseReduction", 1.2f, 0f, 3f) ?: 1.2f,
                frameCount = night?.boundedInt("frameCount", 8, 1, 15) ?: 8,
                shadowRecovery = night?.boundedFloat("shadowRecovery", 1.1f, 0f, 3f) ?: 1.1f,
            ),
            portrait = PortraitConfig(
                blurStrength = portrait?.boundedFloat("blurStrength", 0.45f, 0f, 1f) ?: 0.45f,
                edgeRefinement = portrait?.boundedFloat("edgeRefinement", 1f, 0f, 2f) ?: 1f,
                skinProcessing = portrait?.boundedFloat("skinProcessing", 1f, 0f, 2f) ?: 1f,
            ),
        )
    }

    fun serialize(config: CameraConfig): String = JSONObject().apply {
        put("name", config.name)
        put("version", config.version)
        put("appVersion", config.appVersion)
        put("deviceCompatibility", JSONArray(config.deviceCompatibility))
        put("cameraCompatibility", JSONArray(config.cameraCompatibility))
        put("processingCompatibility", JSONArray(config.processingCompatibility))
        put("processing", JSONObject().apply {
            put("sharpness", config.processing.sharpness)
            put("noiseReduction", config.processing.noiseReduction)
            put("contrast", config.processing.contrast)
            put("saturation", config.processing.saturation)
            put("highlightRecovery", config.processing.highlightRecovery)
            put("shadowRecovery", config.processing.shadowRecovery)
        })
        put("color", JSONObject().apply {
            put("temperature", config.color.temperature)
            put("tint", config.color.tint)
            put("vibrance", config.color.vibrance)
        })
        put("hdr", JSONObject().apply {
            put("enabled", config.hdr.enabled)
            put("frameCount", config.hdr.frameCount)
            put("strength", config.hdr.strength)
            put("ghostReduction", config.hdr.ghostReduction)
        })
        put("night", JSONObject().apply {
            put("exposureMultiplier", config.night.exposureMultiplier)
            put("noiseReduction", config.night.noiseReduction)
            put("frameCount", config.night.frameCount)
            put("shadowRecovery", config.night.shadowRecovery)
        })
        put("portrait", JSONObject().apply {
            put("blurStrength", config.portrait.blurStrength)
            put("edgeRefinement", config.portrait.edgeRefinement)
            put("skinProcessing", config.portrait.skinProcessing)
        })
    }.toString(2)

    private fun JSONObject.optStringArray(key: String): List<String> {
        val array = optJSONArray(key) ?: return emptyList()
        return buildList {
            for (index in 0 until array.length()) add(array.optString(index))
        }
    }

    private fun JSONObject.boundedFloat(key: String, fallback: Float, min: Float, max: Float): Float {
        val value = optDouble(key, fallback.toDouble()).toFloat()
        return value.coerceIn(min, max)
    }

    private fun JSONObject.boundedInt(key: String, fallback: Int, min: Int, max: Int): Int {
        return optInt(key, fallback).coerceIn(min, max)
    }
}
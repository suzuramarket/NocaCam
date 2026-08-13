package com.novacamera.app.config

data class ProcessingConfig(
    val sharpness: Float = 1f,
    val noiseReduction: Float = 1f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val highlightRecovery: Float = 1f,
    val shadowRecovery: Float = 1f,
)

data class ColorConfig(
    val temperature: Int = 5200,
    val tint: Float = 1f,
    val vibrance: Float = 1f,
)

data class HdrConfig(
    val enabled: Boolean = true,
    val frameCount: Int = 5,
    val strength: Float = 1f,
    val ghostReduction: Float = 1f,
)

data class NightConfig(
    val exposureMultiplier: Float = 1.2f,
    val noiseReduction: Float = 1.2f,
    val frameCount: Int = 8,
    val shadowRecovery: Float = 1.1f,
)

data class PortraitConfig(
    val blurStrength: Float = 0.45f,
    val edgeRefinement: Float = 1f,
    val skinProcessing: Float = 1f,
)

data class CameraConfig(
    val name: String,
    val version: String = "1.0",
    val appVersion: String = "0.1.0",
    val deviceCompatibility: List<String> = emptyList(),
    val cameraCompatibility: List<String> = emptyList(),
    val processingCompatibility: List<String> = emptyList(),
    val processing: ProcessingConfig = ProcessingConfig(),
    val color: ColorConfig = ColorConfig(),
    val hdr: HdrConfig = HdrConfig(),
    val night: NightConfig = NightConfig(),
    val portrait: PortraitConfig = PortraitConfig(),
)

object BuiltInConfigs {
    val natural = CameraConfig(name = "Natural")
    val vivid = CameraConfig(
        name = "Vivid HD",
        processing = ProcessingConfig(
            sharpness = 1.2f,
            noiseReduction = 1.1f,
            contrast = 1.08f,
            saturation = 1.16f,
            highlightRecovery = 1.15f,
            shadowRecovery = 1.08f,
        ),
        color = ColorConfig(vibrance = 1.12f),
        hdr = HdrConfig(frameCount = 8),
    )
    val cinematic = CameraConfig(
        name = "Cinematic",
        processing = ProcessingConfig(contrast = 1.12f, saturation = 0.92f, sharpness = 1.05f),
        color = ColorConfig(temperature = 4800, vibrance = 0.94f),
    )
    val nightHd = CameraConfig(
        name = "Night HD",
        night = NightConfig(exposureMultiplier = 1.35f, noiseReduction = 1.3f, frameCount = 10),
        processing = ProcessingConfig(noiseReduction = 1.35f, shadowRecovery = 1.2f),
    )
    val portraitNatural = CameraConfig(
        name = "Portrait Natural",
        portrait = PortraitConfig(blurStrength = 0.38f, edgeRefinement = 1.15f),
    )

    val all = listOf(natural, vivid, cinematic, nightHd, portraitNatural)
}
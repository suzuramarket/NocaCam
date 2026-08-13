package com.novacamera.app.config

import android.content.Context
import java.io.File

sealed interface ConfigImportResult {
    data class Success(val config: CameraConfig) : ConfigImportResult
    data class Invalid(val message: String) : ConfigImportResult
}

class ConfigManager(private val context: Context) {
    private val directory = File(context.filesDir, "configs").apply { mkdirs() }
    private var active: CameraConfig = BuiltInConfigs.natural

    fun list(): List<CameraConfig> = BuiltInConfigs.all + directory.listFiles()
        .orEmpty()
        .filter { it.extension.equals("json", ignoreCase = true) }
        .mapNotNull { ConfigParser.parse(it.readText()).getOrNull() }

    fun active(): CameraConfig = active

    fun activate(config: CameraConfig): CameraConfig {
        active = config
        return active
    }

    fun importJson(json: String): ConfigImportResult {
        val parsed = ConfigParser.parse(json)
        return parsed.fold(
            onSuccess = { config ->
                File(directory, "${config.name.sanitizeFileName()}.json").writeText(ConfigParser.serialize(config))
                ConfigImportResult.Success(config)
            },
            onFailure = { ConfigImportResult.Invalid("Config tidak valid: ${it.message ?: "format rusak"}") },
        )
    }

    fun export(config: CameraConfig): File {
        val file = File(context.cacheDir, "${config.name.sanitizeFileName()}.json")
        file.writeText(ConfigParser.serialize(config))
        return file
    }

    fun delete(config: CameraConfig): Boolean {
        if (BuiltInConfigs.all.any { it.name == config.name }) return false
        return File(directory, "${config.name.sanitizeFileName()}.json").delete()
    }

    private fun String.sanitizeFileName(): String =
        replace(Regex("[^A-Za-z0-9._-]"), "_").take(64)
}
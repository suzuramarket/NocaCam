package com.novacamera.app.core

import android.content.Context
import com.novacamera.app.model.CameraMode

class SettingsManager(context: Context) {
    private val preferences = context.getSharedPreferences("novacam-settings", Context.MODE_PRIVATE)

    var activeConfigName: String
        get() = preferences.getString("active_config", "Natural") ?: "Natural"
        set(value) = preferences.edit().putString("active_config", value).apply()

    var livePhotoEnabled: Boolean
        get() = preferences.getBoolean("live_photo", false)
        set(value) = preferences.edit().putBoolean("live_photo", value).apply()

    var shutterSoundEnabled: Boolean
        get() = preferences.getBoolean("shutter_sound", true)
        set(value) = preferences.edit().putBoolean("shutter_sound", value).apply()

    var gridEnabled: Boolean
        get() = preferences.getBoolean("grid", false)
        set(value) = preferences.edit().putBoolean("grid", value).apply()

    var lastMode: CameraMode
        get() = runCatching {
            CameraMode.valueOf(preferences.getString("last_mode", CameraMode.PHOTO.name)!!)
        }.getOrDefault(CameraMode.PHOTO)
        set(value) = preferences.edit().putString("last_mode", value.name).apply()
}
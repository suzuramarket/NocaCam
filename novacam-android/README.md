# NovaCam

NovaCam is an original Android camera project inspired by modern computational-photography workflows. It does not use Google Camera/GCam branding, code, or proprietary assets.

## Included in this package

- Native Kotlin Android project using Camera2 and Jetpack Compose
- Hardware capability inspection for lenses, zoom, flash, RAW, OIS/EIS, ISO, exposure, FPS, and sensor size
- Fullscreen camera UI with tap-to-focus, exposure region, adaptive digital zoom, flash/HDR/AI toggles, mode rail, manual control surface, settings, and gallery
- Asynchronous image processing pipeline with HDR merge, Night stacking path, color processing, and safe portrait fallback
- JSON Camera Config parser with clamping, defaults, unknown-field tolerance, import/export, built-in presets, activation, and deletion rules
- Live Photo recorder/metadata bundle class built around MediaRecorder
- MediaStore image persistence and gallery querying
- Permission handling that only requests camera and legacy storage permissions when required
- Responsive dark NovaCam visual system with warm gold accent
- The approved UI mockups copied into `design/` for visual reference

## Open and build

1. Open the `novacam-android` folder in Android Studio Ladybug or newer.
2. Allow Gradle to sync.
3. Connect an Android device or start an emulator with an available camera.
4. Run the `app` configuration.

The project uses Java 17, Kotlin 2.0.21, Android Gradle Plugin 8.5.2, compile SDK 35, and minimum SDK 26.

## Device-aware behavior

NovaCam inspects the active back camera before use. Unsupported capabilities are reported in the UI rather than enabled as fake controls. RAW, optical stabilization, electronic stabilization, high FPS, depth, and astro-quality long exposure depend on the device camera HAL.

The first native pass intentionally uses a conservative single JPEG fallback when a device cannot provide the requested multi-frame/depth stream. This keeps the preview and capture path reliable. A Camera2 burst/YUV reprocessing implementation can be added behind the existing `ImageProcessor` interfaces without changing the Compose UI.

## Camera Config example

```json
{
  "name": "Vivid HD",
  "version": "1.0",
  "processing": {
    "sharpness": 1.2,
    "noiseReduction": 1.1,
    "contrast": 1.08,
    "saturation": 1.16,
    "highlightRecovery": 1.15,
    "shadowRecovery": 1.08
  },
  "color": {
    "temperature": 5200,
    "tint": 1.0,
    "vibrance": 1.12
  },
  "hdr": {
    "enabled": true,
    "frameCount": 8
  }
}
```

Unknown keys are ignored. Numeric values are clamped to safe ranges. Invalid JSON returns an import error instead of crashing the app.

## Source map

- `app/src/main/java/com/novacamera/app/MainActivity.kt` — Compose camera experience and supporting sheets
- `app/src/main/java/com/novacamera/app/core/CameraEngine.kt` — Camera2 preview, capture, focus, and zoom
- `app/src/main/java/com/novacamera/app/core/CameraCapabilities.kt` — hardware inspection
- `app/src/main/java/com/novacamera/app/processing/` — image, HDR, Night, portrait, color, AI, and Live Photo processing
- `app/src/main/java/com/novacamera/app/config/` — config models, parser, presets, and manager
- `app/src/main/java/com/novacamera/app/core/StorageManager.kt` — MediaStore persistence
- `design/` — NovaCam visual mockups used as the UI reference

## Validation note

This environment does not have a Java runtime or Android SDK installed, so Gradle sync/build could not be executed here. The project files are included as a standard Android Studio project and should be synced and built on a machine with Android Studio and SDK 35 installed.
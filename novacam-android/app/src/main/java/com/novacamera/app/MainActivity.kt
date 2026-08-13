package com.novacamera.app

import android.os.Bundle
import android.view.TextureView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.HdrOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.novacamera.app.config.BuiltInConfigs
import com.novacamera.app.config.CameraConfig
import com.novacamera.app.config.ConfigManager
import com.novacamera.app.core.CameraCapabilities
import com.novacamera.app.core.CameraEngine
import com.novacamera.app.core.GalleryItem
import com.novacamera.app.core.GalleryManager
import com.novacamera.app.core.PermissionManager
import com.novacamera.app.core.SettingsManager
import com.novacamera.app.model.CameraMode
import kotlinx.coroutines.delay

private val NovaBlack = Color(0xFF070908)
private val NovaPanel = Color(0xCC121715)
private val NovaGold = Color(0xFFD6A84F)
private val NovaMuted = Color(0xFF9B9F98)

class MainActivity : ComponentActivity() {
    private lateinit var cameraEngine: CameraEngine

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraEngine = CameraEngine(this)
        setContent {
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions(),
            ) { }
            NovaCamApp(
                cameraEngine = cameraEngine,
                onRequestPermission = { permissionLauncher.launch(PermissionManager.requiredPermissions()) },
            )
        }
    }

    override fun onDestroy() {
        cameraEngine.shutdown()
        super.onDestroy()
    }
}

@Composable
private fun NovaCamApp(
    cameraEngine: CameraEngine,
    onRequestPermission: () -> Unit,
) {
    val context = LocalContext.current
    val configManager = remember { ConfigManager(context) }
    val galleryManager = remember { GalleryManager(context) }
    val settingsManager = remember { SettingsManager(context) }
    val hardware = remember { CameraCapabilities(context.getSystemService(android.hardware.camera2.CameraManager::class.java)).inspect() }
    var splash by remember { mutableStateOf(true) }
    var mode by remember { mutableStateOf(settingsManager.lastMode) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var flash by remember { mutableStateOf(false) }
    var hdr by remember { mutableStateOf(true) }
    var ai by remember { mutableStateOf(true) }
    var livePhoto by remember { mutableStateOf(settingsManager.livePhotoEnabled) }
    var showControls by remember { mutableStateOf(false) }
    var showGallery by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showConfig by remember { mutableStateOf(false) }
    var focusPoint by remember { mutableStateOf<Pair<Float, Float>?>(null) }
    var processing by remember { mutableStateOf<String?>(null) }
    var processingProgress by remember { mutableFloatStateOf(0f) }
    var toast by remember { mutableStateOf<String?>(null) }
    var activeConfig by remember { mutableStateOf(configManager.active()) }

    LaunchedEffect(livePhoto) {
        cameraEngine.setLivePhotoEnabled(livePhoto)
    }
    LaunchedEffect(Unit) {
        delay(1050)
        splash = false
    }
    LaunchedEffect(toast) {
        if (toast != null) {
            delay(2300)
            toast = null
        }
    }

    if (splash) {
        SplashScreen()
        return
    }

    if (!PermissionManager.cameraGranted(context)) {
        PermissionScreen(onRequestPermission)
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NovaBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        CameraPreview(
            cameraEngine = cameraEngine,
            focusPoint = focusPoint,
            onFocus = { x, y, width, height ->
                focusPoint = x to y
                cameraEngine.setFocus(x, y, width, height)
            },
        )
        CameraScrim()
        TopCameraBar(
            flash = flash,
            hdr = hdr,
            ai = ai,
            onFlash = { flash = !flash },
            onHdr = { hdr = !hdr },
            onAi = { ai = !ai },
            onConfig = { showConfig = true },
            onSettings = { showSettings = true },
            onTimer = { toast = "Timer: 3 seconds" },
        )
        if (hardware.cameraCount == 0) {
            UnsupportedBanner("Camera hardware tidak ditemukan.")
        }
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            ManualControls(
                hardware = hardware,
                config = activeConfig,
                onClose = { showControls = false },
            )
        }
        AnimatedVisibility(
            visible = !showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            BottomCameraBar(
                mode = mode,
                zoom = zoom,
                livePhoto = livePhoto,
                onZoom = {
                    zoom = it
                    cameraEngine.setZoom(it)
                },
                onLivePhoto = {
                    livePhoto = !livePhoto
                    settingsManager.livePhotoEnabled = livePhoto
                    cameraEngine.setLivePhotoEnabled(livePhoto)
                },
                onMode = {
                    mode = it
                    settingsManager.lastMode = it
                    if (it == CameraMode.ASTRO) toast = "Letakkan ponsel di permukaan stabil."
                },
                onGallery = { showGallery = true },
                onCapture = {
                    processing = "Processing image..."
                    processingProgress = 0.04f
                    cameraEngine.capture(
                        mode = mode,
                        config = activeConfig,
                        livePhoto = livePhoto,
                        onUpdate = { status -> processing = status },
                        onComplete = {
                            processing = null
                            toast = if (livePhoto) "Live Photo tersimpan ✓" else "Photo ready ✓"
                        },
                        onError = {
                            processing = null
                            toast = it
                        },
                    )
                },
                onSwitch = { toast = "Kamera depan belum tersedia dalam mode ini." },
                onControls = { showControls = true },
            )
        }
        if (focusPoint != null) {
            FocusIndicator(
                x = focusPoint!!.first,
                y = focusPoint!!.second,
                modifier = Modifier.fillMaxSize(),
            )
        }
        processing?.let { status ->
            ProcessingOverlay(status = status, progress = processingProgress)
        }
        toast?.let { message ->
            Text(
                text = message,
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 62.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.Black.copy(alpha = 0.72f))
                    .padding(horizontal = 16.dp, vertical = 9.dp),
            )
        }
    }

    if (showGallery) {
        GallerySheet(
            items = remember { galleryManager.latest() },
            onClose = { showGallery = false },
            onMessage = { toast = it },
        )
    }
    if (showSettings) {
        SettingsSheet(
            hardware = hardware,
            livePhoto = livePhoto,
            grid = settingsManager.gridEnabled,
            onLivePhoto = {
                livePhoto = it
                settingsManager.livePhotoEnabled = it
                cameraEngine.setLivePhotoEnabled(it)
            },
            onGrid = { settingsManager.gridEnabled = it },
            onClose = { showSettings = false },
            onMessage = { toast = it },
        )
    }
    if (showConfig) {
        ConfigSheet(
            configs = configManager.list(),
            active = activeConfig,
            onActivate = {
                activeConfig = configManager.activate(it)
                toast = "${it.name} aktif ✓"
                showConfig = false
            },
            onClose = { showConfig = false },
        )
    }
}

@Composable
private fun SplashScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(NovaBlack),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.dp, NovaGold.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                    .background(NovaGold.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.CameraAlt, null, tint = NovaGold, modifier = Modifier.size(38.dp))
            }
            Spacer(Modifier.height(24.dp))
            Text("NOVA SYSTEMS", color = NovaGold, fontSize = 10.sp, letterSpacing = 3.sp)
            Text("NovaCam", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Light)
            Text("COMPUTATIONAL CAMERA", color = NovaMuted, fontSize = 11.sp, letterSpacing = 2.sp)
        }
    }
}

@Composable
private fun PermissionScreen(onRequestPermission: () -> Unit) {
    Box(Modifier.fillMaxSize().background(NovaBlack), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Default.CameraAlt, null, tint = NovaGold, modifier = Modifier.size(54.dp))
            Spacer(Modifier.height(20.dp))
            Text("Camera access is needed", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Text(
                "NovaCam needs camera access to show the preview and capture photos. No permission is requested until you tap continue.",
                color = NovaMuted,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = NovaGold, contentColor = NovaBlack),
            ) {
                Text("Continue")
            }
        }
    }
}

@Composable
private fun CameraPreview(
    cameraEngine: CameraEngine,
    focusPoint: Pair<Float, Float>?,
    onFocus: (Float, Float, Int, Int) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    onFocus(offset.x, offset.y, size.width, size.height)
                }
            },
    ) {
        AndroidView(
            factory = { context ->
                TextureView(context).also { cameraEngine.attachPreview(it) }
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun CameraScrim() {
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent, Color.Black.copy(alpha = 0.76f)),
            ),
        ),
    )
}

@Composable
private fun TopCameraBar(
    flash: Boolean,
    hdr: Boolean,
    ai: Boolean,
    onFlash: () -> Unit,
    onHdr: () -> Unit,
    onAi: () -> Unit,
    onConfig: () -> Unit,
    onSettings: () -> Unit,
    onTimer: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CameraToolIcon(if (flash) Icons.Default.FlashOn else Icons.Default.FlashOff, "Flash", onFlash, flash)
        CameraToolIcon(Icons.Default.HdrOn, if (hdr) "HDR Auto" else "HDR Off", onHdr, hdr)
        CameraToolIcon(Icons.Default.AutoAwesome, if (ai) "AI On" else "AI Off", onAi, ai)
        CameraToolIcon(Icons.Default.Tune, "Config", onConfig, false)
        CameraToolIcon(Icons.Default.Settings, "Settings", onSettings, false)
        CameraToolIcon(Icons.Default.Timer, "Timer", onTimer, false)
    }
}

@Composable
private fun CameraToolIcon(icon: ImageVector, label: String, onClick: () -> Unit, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(42.dp).clip(CircleShape).background(
                if (active) NovaGold.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.22f),
            ),
        ) {
            Icon(icon, contentDescription = label, tint = if (active) NovaGold else Color.White, modifier = Modifier.size(20.dp))
        }
        Text(label, color = if (active) NovaGold else Color.White.copy(alpha = 0.72f), fontSize = 8.sp)
    }
}

@Composable
private fun BottomCameraBar(
    mode: CameraMode,
    zoom: Float,
    livePhoto: Boolean,
    onZoom: (Float) -> Unit,
    onLivePhoto: () -> Unit,
    onMode: (CameraMode) -> Unit,
    onGallery: () -> Unit,
    onCapture: () -> Unit,
    onSwitch: () -> Unit,
    onControls: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            listOf(0.6f, 1f, 2f, 3f, 5f).forEach { value ->
                Text(
                    text = "${value}x",
                    color = if (zoom == value) NovaGold else Color.White.copy(alpha = 0.75f),
                    fontSize = 13.sp,
                    fontWeight = if (zoom == value) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.clip(CircleShape).clickable { onZoom(value) }.padding(horizontal = 13.dp, vertical = 7.dp),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(22.dp, Alignment.CenterHorizontally),
        ) {
            items(CameraMode.entries) { item ->
                Text(
                    text = item.name.replace('_', ' '),
                    color = if (item == mode) Color.White else Color.White.copy(alpha = 0.45f),
                    fontSize = 11.sp,
                    fontWeight = if (item == mode) FontWeight.SemiBold else FontWeight.Normal,
                    modifier = Modifier.clickable { onMode(item) },
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 26.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RoundAction(Icons.Default.PhotoLibrary, "Gallery", onGallery)
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .border(3.dp, if (livePhoto) NovaGold else Color.White, CircleShape)
                    .padding(5.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onCapture() },
            )
            RoundAction(Icons.Default.Cameraswitch, "Switch camera", onSwitch)
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (livePhoto) "LIVE PHOTO ON" else "LIVE PHOTO OFF", color = if (livePhoto) NovaGold else NovaMuted, fontSize = 9.sp, letterSpacing = 1.sp)
            IconButton(onClick = onLivePhoto, modifier = Modifier.size(28.dp)) {
                Box(Modifier.size(12.dp).border(1.dp, if (livePhoto) NovaGold else NovaMuted, CircleShape).background(if (livePhoto) NovaGold else Color.Transparent, CircleShape))
            }
            IconButton(onClick = onControls, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.KeyboardArrowUp, "Open controls", tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun RoundAction(icon: ImageVector, label: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.35f)),
    ) {
        Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(23.dp))
    }
}

@Composable
private fun FocusIndicator(x: Float, y: Float, modifier: Modifier) {
    Box(modifier = modifier) {
        Box(
            Modifier
                .padding(start = (x - 28).dp, top = (y - 28).dp)
                .size(56.dp)
                .border(1.dp, NovaGold, RoundedCornerShape(14.dp)),
        )
    }
}

@Composable
private fun UnsupportedBanner(message: String) {
    Row(
        modifier = Modifier.align(Alignment.TopCenter).padding(top = 108.dp).clip(RoundedCornerShape(18.dp)).background(Color.Black.copy(alpha = 0.65f)).padding(horizontal = 13.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Default.Warning, null, tint = NovaGold, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(7.dp))
        Text(message, color = Color.White, fontSize = 11.sp)
    }
}

@Composable
private fun ManualControls(
    hardware: com.novacamera.app.core.HardwareSnapshot,
    config: CameraConfig,
    onClose: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(10.dp),
        shape = RoundedCornerShape(26.dp),
        color = NovaPanel,
        tonalElevation = 8.dp,
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("MANUAL CONTROLS", color = NovaGold, fontSize = 10.sp, letterSpacing = 2.sp)
                    Text("Applying ${config.name}", color = Color.White, fontSize = 13.sp)
                }
                IconButton(onClick = onClose) { Icon(Icons.Default.KeyboardArrowDown, "Close controls", tint = Color.White) }
            }
            Divider(color = Color.White.copy(alpha = 0.1f))
            ControlRow("ISO", hardware.isoRange?.let { "${it.lower}–${it.upper}" } ?: "Auto")
            ControlRow("SHUTTER", hardware.exposureRange?.let { "1/${(1_000_000_000L / it.upper.coerceAtLeast(1L))}" } ?: "Auto")
            ControlRow("EV", "0.0")
            ControlRow("WHITE BALANCE", "Auto")
            ControlRow("QUALITY", if (hardware.jpegSize != null) "Ultra · ${hardware.jpegSize.width}×${hardware.jpegSize.height}" else "Standard")
        }
    }
}

@Composable
private fun ControlRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 9.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = NovaMuted, fontSize = 10.sp, letterSpacing = 1.sp)
        Text(value, color = Color.White, fontSize = 12.sp)
    }
}

@Composable
private fun ProcessingOverlay(status: String, progress: Float) {
    Box(
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.width(270.dp).clip(RoundedCornerShape(24.dp)).background(NovaPanel).padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("PROCESSING IMAGE", color = NovaGold, fontSize = 10.sp, letterSpacing = 2.sp)
            Spacer(Modifier.height(12.dp))
            Text(status, color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(18.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0.05f, 1f) },
                color = NovaGold,
                trackColor = Color.White.copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            Text("Frames are processed off the camera thread", color = NovaMuted, fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun GallerySheet(items: List<GalleryItem>, onClose: () -> Unit, onMessage: (String) -> Unit) {
    Surface(Modifier.fillMaxSize(), color = NovaBlack) {
        Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing).padding(18.dp)) {
            SheetHeader("NOVA GALLERY", "Your captures", onClose)
            if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PhotoLibrary, null, tint = NovaGold, modifier = Modifier.size(38.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("No photos yet", color = Color.White, fontSize = 18.sp)
                        Text("Captured images will appear here.", color = NovaMuted, fontSize = 13.sp)
                    }
                }
            } else {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Live Photos", "Favorites", "RAW").forEachIndexed { index, label ->
                        FilterChip(label, index == 0)
                    }
                }
                Spacer(Modifier.height(18.dp))
                items.forEach { item ->
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(NovaPanel).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(Modifier.size(58.dp).clip(RoundedCornerShape(12.dp)).background(Brush.linearGradient(listOf(NovaGold.copy(alpha = 0.65f), Color(0xFF183B3E)))))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(item.displayName, color = Color.White, fontSize = 13.sp)
                            Text(item.mimeType, color = NovaMuted, fontSize = 11.sp)
                        }
                        IconButton(onClick = { onMessage("Export started") }) { Icon(Icons.Default.Download, "Export", tint = NovaGold) }
                        IconButton(onClick = { onMessage("Share sheet ready") }) { Icon(Icons.Default.Share, "Share", tint = Color.White) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun SettingsSheet(
    hardware: com.novacamera.app.core.HardwareSnapshot,
    livePhoto: Boolean,
    grid: Boolean,
    onLivePhoto: (Boolean) -> Unit,
    onGrid: (Boolean) -> Unit,
    onClose: () -> Unit,
    onMessage: (String) -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = NovaBlack) {
        Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing).verticalScroll(rememberScrollState()).padding(18.dp)) {
            SheetHeader("SETTINGS", "NovaCam configuration", onClose)
            SettingsGroup("CAMERA") {
                SettingToggle("Live Photo", "Save the moment around each shutter press", livePhoto, onLivePhoto)
                SettingToggle("Grid", "Show a composition grid in preview", grid, onGrid)
                SettingRow("RAW capture", if (hardware.supportsRaw) "Available" else "Not supported") {
                    onMessage(if (hardware.supportsRaw) "RAW DNG ready" else "RAW tidak didukung oleh kamera ini.")
                }
                SettingRow("Video stabilization", if (hardware.supportsEis || hardware.supportsOis) "Available" else "Not supported") {}
            }
            SettingsGroup("PROCESSING") {
                SettingRow("Image quality", "Ultra") {}
                SettingRow("HDR strength", "Auto") {}
                SettingRow("Noise reduction", "Adaptive") {}
                SettingRow("Active Config", "Natural") {}
            }
            SettingsGroup("DEVICE") {
                SettingRow("Camera count", hardware.cameraCount.toString()) {}
                SettingRow("Hardware level", hardware.hardwareLevel?.toString() ?: "Unknown") {}
                SettingRow("Sensor", hardware.sensorSize?.let { "${it.width}×${it.height}" } ?: "Unknown") {}
                SettingRow("Max zoom", "${hardware.maxDigitalZoom}x") {}
            }
        }
    }
}

@Composable
private fun ConfigSheet(
    configs: List<CameraConfig>,
    active: CameraConfig,
    onActivate: (CameraConfig) -> Unit,
    onClose: () -> Unit,
) {
    Surface(Modifier.fillMaxSize(), color = NovaBlack) {
        Column(Modifier.windowInsetsPadding(WindowInsets.safeDrawing).verticalScroll(rememberScrollState()).padding(18.dp)) {
            SheetHeader("CAMERA CONFIG", "Processing character", onClose)
            Text("Active config", color = NovaMuted, fontSize = 12.sp)
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(NovaGold.copy(alpha = 0.14f)).padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = NovaGold, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(active.name, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    Text("Applied to supported image processing", color = NovaMuted, fontSize = 11.sp)
                }
            }
            Spacer(Modifier.height(22.dp))
            configs.forEach { config ->
                ConfigCard(config, selected = config.name == active.name, onClick = { onActivate(config) })
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ConfigCard(config: CameraConfig, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).border(1.dp, if (selected) NovaGold else Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp)).background(NovaPanel).clickable(onClick = onClick).padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(58.dp).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(listOf(NovaGold.copy(alpha = 0.8f), Color(0xFF1B4041)))))
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(config.name, color = Color.White, fontSize = 15.sp)
            Text("Preview before activation", color = NovaMuted, fontSize = 11.sp)
        }
        if (selected) Icon(Icons.Default.CheckCircle, null, tint = NovaGold, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun SheetHeader(eyebrow: String, title: String, onClose: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(bottom = 22.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onClose) { Icon(Icons.Default.ArrowBack, "Back", tint = Color.White) }
        Column(Modifier.weight(1f)) {
            Text(eyebrow, color = NovaGold, fontSize = 10.sp, letterSpacing = 2.sp)
            Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Light)
        }
        IconButton(onClick = onClose) { Icon(Icons.Default.Close, "Close", tint = NovaMuted) }
    }
}

@Composable
private fun FilterChip(label: String, selected: Boolean) {
    Text(
        label,
        color = if (selected) NovaBlack else NovaMuted,
        fontSize = 11.sp,
        modifier = Modifier.clip(CircleShape).background(if (selected) NovaGold else NovaPanel).padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column(Modifier.padding(bottom = 24.dp)) {
        Text(title, color = NovaGold, fontSize = 10.sp, letterSpacing = 2.sp)
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun SettingToggle(title: String, description: String, value: Boolean, onValueChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 14.sp)
            Text(description, color = NovaMuted, fontSize = 11.sp)
        }
        Switch(checked = value, onCheckedChange = onValueChange)
    }
}

@Composable
private fun SettingRow(title: String, value: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title, color = Color.White, fontSize = 14.sp)
        Text(value, color = NovaMuted, fontSize = 13.sp)
    }
}
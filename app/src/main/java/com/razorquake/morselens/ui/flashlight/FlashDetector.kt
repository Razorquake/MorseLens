package com.razorquake.morselens.ui.flashlight

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.razorquake.morselens.ui.components.CustomSlider
import java.util.Locale
import java.util.concurrent.Executors

@Composable
fun FlashDetector(bottomPadding: Dp, topPadding: Dp) {
    val context = LocalContext.current
    var flashlightDetected by remember { mutableStateOf(false) }
    var currentMorse by remember { mutableStateOf("") }
    var decodedMessage by remember { mutableStateOf("") }
    var brightnessThreshold by remember { mutableIntStateOf(240) }
    var areaThreshold by remember { mutableIntStateOf(300) }
    var camera by remember { mutableStateOf<Camera?>(null)}
    var zoomRatio by remember { mutableFloatStateOf(1.0f) }

    // Maximum zoom supported by the device (will be updated when camera is available)
    var maxZoom by remember { mutableFloatStateOf(5.0f) }

    // For pinch-to-zoom gesture detection
    var scale by remember { mutableFloatStateOf(1f) }

    // State to track permission
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission request launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            hasCameraPermission = isGranted
        }
    )

    // Effect to request permission on first composition
    LaunchedEffect(key1 = Unit) {
        if (!hasCameraPermission) {
            launcher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, _ ->
                    // Update scale factor with the zoom
                    scale *= zoom

                    // Keep the scale within bounds (0.5 to maxZoom)
                    scale = scale.coerceIn(0.5f, maxZoom)

                    // Apply zoom to camera
                    camera?.cameraControl?.setZoomRatio(scale)
                    zoomRatio = scale
                }
            }
    ) {
        when {
            hasCameraPermission -> {
                Box(modifier = Modifier.fillMaxSize()) {

                    // Camera Preview
                    CameraPreview(
                        onFrameAnalyzed = { isDetected ->
                            flashlightDetected = isDetected
                        },
                        onMessageUpdate = { morse, message ->
                            currentMorse = morse
                            if (message.isNotEmpty()) {
                                decodedMessage = message
                            }
                        },
                        brightnessThreshold = brightnessThreshold,
                        areaThreshold = areaThreshold,
                        onCameraAvailable = { cam ->
                            camera = cam
                            maxZoom = cam.cameraInfo.zoomState.value?.maxZoomRatio ?: 5.0f
                        }
                    )
                    // Show current zoom level
                    Text(
                        text = String.format(Locale.US, "%.1fx", zoomRatio),
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .statusBarsPadding()
                    )
                    // Add DetectionCircle overlay
                    DetectionCircle(isFlashDetected = flashlightDetected)

                    // Settings sliders
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .align(Alignment.TopCenter)
                            .padding(top = topPadding)
                    ) {
                        Column(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f))

                        ) {
                            Text(
                                text = "Brightness Threshold: $brightnessThreshold",
                                color = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                            CustomSlider(
                                value = brightnessThreshold.toLong(),
                                onValueChange = { brightnessThreshold = it.toInt() },
                                valueRange = 0f..255f,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Text(
                                text = "Area Threshold: $areaThreshold",
                                color = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                            CustomSlider(
                                value = areaThreshold.toLong(),
                                onValueChange = { areaThreshold = it.toInt() },
                                valueRange = 0f..1000f,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }

                    // Status and detection overlay
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .padding(bottom = bottomPadding)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = if (flashlightDetected) "Flash Detected!" else "No Flash Detected",
                                color = if (flashlightDetected) Color.Green else Color.Red,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Current Morse: $currentMorse",
                                color = Color.White,
                                fontSize = 16.sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Decoded Text: ${if (decodedMessage.length > 30) decodedMessage.takeLast(30) else decodedMessage}",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Camera permission is required for this app")
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                        Text("Request permission")
                    }
                }
            }
        }
    }
}

@Composable
fun CameraPreview(
    onFrameAnalyzed: (Boolean) -> Unit,
    onMessageUpdate: (String, String) -> Unit,
    brightnessThreshold: Int,
    areaThreshold: Int,
    onCameraAvailable: (Camera) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = Executors.newSingleThreadExecutor()
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor, FlashlightAnalyzer(
                            onFlashlightDetected = onFrameAnalyzed,
                            onMessageUpdate = onMessageUpdate,
                            brightnessThreshold = brightnessThreshold,
                            areaThreshold = areaThreshold
                        ))
                    }

                try {
                    cameraProvider.unbindAll()
                    val camera = cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalyzer
                    )
                    // Pass camera reference back to calling function
                    onCameraAvailable(camera)
                } catch (exc: Exception) {
                    exc.printStackTrace()
                    // Handle any errors
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun DetectionCircle(
    modifier: Modifier = Modifier,
    isFlashDetected: Boolean = false // Add parameter to show detection state
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = 20f * density  // Match the detection circle radius used in analysis

        // Draw blue circle
        drawCircle(
            color = if (isFlashDetected) Color.Green else Color.Blue,
            radius = radius,
            center = center,
            style = Stroke(width = 2f * density)
        )

        // Optional: Draw filled circle when flash detected
        if (isFlashDetected) {
            drawCircle(
                color = Color.Green.copy(alpha = 0.3f),
                radius = radius,
                center = center,
                blendMode = BlendMode.Screen
            )
        }
    }
}


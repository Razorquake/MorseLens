package com.razorquake.morselens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.razorquake.morselens.morse_code_translator.components.CustomSlider
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.concurrent.Executors

@Composable
fun FlashDetector() {
    val context = LocalContext.current
    var flashlightDetected by remember { mutableStateOf(false) }
    var currentMorse by remember { mutableStateOf("") }
    var decodedMessage by remember { mutableStateOf("") }
    var brightnessThreshold by remember { mutableIntStateOf(240) }
    var areaThreshold by remember { mutableIntStateOf(300) }

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

    Box(modifier = Modifier.fillMaxSize()) {
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
                        areaThreshold = areaThreshold
                    )
                    // Add DetectionCircle overlay
                    DetectionCircle(isFlashDetected = flashlightDetected)

                    // Settings sliders
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopCenter)
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

                    // Status and detection overlay
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Spacer(modifier = Modifier.weight(1f))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .padding(16.dp)
                        ) {
                            Column {
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
    areaThreshold: Int
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
                    it.setSurfaceProvider(previewView.surfaceProvider)
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
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalyzer
                    )
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

class FlashlightAnalyzer(
    private val onFlashlightDetected: (Boolean) -> Unit,
    private val onMessageUpdate: (String, String) -> Unit,
    private val brightnessThreshold: Int,
    private val areaThreshold: Int
) : ImageAnalysis.Analyzer {
    private var lastAnalysisTimestamp = 0L
    private val ANALYSIS_INTERVAL = 100L // Analyze every 100ms
    private val morseDetector = MorseCodeDetector()
    private var detectedContour: MatOfPoint? = null

    // Morse code timing parameters (in seconds)
    private val DIT_MAX_DURATION = 0.3
    private val DASH_MIN_DURATION = 0.3
    private val LETTER_GAP = 0.7
    private val WORD_GAP = 1.3

    // State variables
    private var lastLightState = false
    private var lightStartTime = 0L
    private var lastLightEndTime = 0L
    private val detectionCircleRadius = 20 // matches Python implementation
    private var currentMorse = ""
    private var decodedText = ""

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalysisTimestamp >= ANALYSIS_INTERVAL) {
            val rotationDegrees = image.imageInfo.rotationDegrees
            val bitmap = image.toBitmap()
            val rotatedBitmap = rotateBitmap(bitmap, rotationDegrees.toFloat())

            val mat = Mat()
            Utils.bitmapToMat(rotatedBitmap, mat)

            val (isFlashlightDetected, contour) = detectMobileFlashlight(mat)
            detectedContour = contour
            onFlashlightDetected(isFlashlightDetected)

            // Process morse code signal
            processFlashSignal(isFlashlightDetected, currentTimestamp)

            lastAnalysisTimestamp = currentTimestamp
            mat.release()
        }
        image.close()
    }

    private fun detectMobileFlashlight(mat: Mat): Pair<Boolean, MatOfPoint?> {
        val width = mat.width()
        val height = mat.height()

        // Create circular mask
        val mask = Mat.zeros(height, width, org.opencv.core.CvType.CV_8UC1)
        val center = org.opencv.core.Point(width / 2.0, height / 2.0)
        Imgproc.circle(mask, center, detectionCircleRadius, org.opencv.core.Scalar(255.0), -1)

        // Convert to grayscale and apply mask
        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGB2GRAY)
        val maskedGray = Mat()
        Core.bitwise_and(gray, gray, maskedGray, mask)

        // Apply Gaussian blur
        Imgproc.GaussianBlur(maskedGray, maskedGray, Size(5.0, 5.0), 0.0)

        // Threshold to detect bright spots
        val threshold = Mat()
        Imgproc.threshold(maskedGray, threshold, brightnessThreshold.toDouble(), 255.0, Imgproc.THRESH_BINARY)

        // Find contours
        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(threshold, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        var detectedContour: MatOfPoint? = null
        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area > areaThreshold) {
                detectedContour = contour
                break
            }
        }

        // Clean up
        mask.release()
        gray.release()
        maskedGray.release()
        threshold.release()
        hierarchy.release()

        return Pair(detectedContour != null, detectedContour)
    }

    private fun processFlashSignal(isFlashlightDetected: Boolean, currentTimestamp: Long) {

        // Handle light state changes
        if (isFlashlightDetected != lastLightState) {
            if (isFlashlightDetected) {
                // Light just turned on
                lightStartTime = currentTimestamp
            } else {
                // Light just turned off
                val duration = (currentTimestamp - lightStartTime) / 1000.0
                // Classify as dit or dash
                currentMorse += when {
                    duration < DIT_MAX_DURATION -> "."
                    duration >= DASH_MIN_DURATION -> "-"
                    else -> "" // Ignore signals between DIT_MAX and DASH_MIN
                }
                lastLightEndTime = currentTimestamp
                onMessageUpdate(currentMorse, "") // Update current morse without decoding
            }
            lastLightState = isFlashlightDetected
        }

        // Check for letter gap
        else if (!isFlashlightDetected && currentMorse.isNotEmpty() &&
            ((currentTimestamp - lastLightEndTime) / 1000.0) > LETTER_GAP) {
            // Decode the current morse sequence
            val decodedLetter = morseDetector.decodeMorse(currentMorse)
            if (decodedLetter.isNotEmpty()) {
                decodedText += decodedLetter
                onMessageUpdate(currentMorse, decodedText)
            }
            currentMorse = ""
            lastLightEndTime = currentTimestamp
        }

        // Check for word gap
        else if (!isFlashlightDetected && decodedText.isNotEmpty() &&
            ((currentTimestamp - lastLightEndTime) / 1000.0) > WORD_GAP) {
            if (!decodedText.endsWith(" ")) {
                decodedText += " "
                onMessageUpdate(currentMorse, decodedText)
            }
            lastLightEndTime = currentTimestamp
        }
    }

    private fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}

class MorseCodeDetector {
    companion object {
        private val MORSE_CODE_DICT = mapOf(
            ".-" to "A", "-..." to "B", "-.-." to "C", "-.." to "D", "." to "E",
            "..-." to "F", "--." to "G", "...." to "H", ".." to "I", ".---" to "J",
            "-.-" to "K", ".-.." to "L", "--" to "M", "-." to "N", "---" to "O",
            ".--." to "P", "--.-" to "Q", ".-." to "R", "..." to "S", "-" to "T",
            "..-" to "U", "...-" to "V", ".--" to "W", "-..-" to "X", "-.--" to "Y",
            "--.." to "Z", "-----" to "0", ".----" to "1", "..---" to "2",
            "...--" to "3", "....-" to "4", "....." to "5", "-..." to "6",
            "--..." to "7", "---.." to "8", "----." to "9"
        )
    }

    fun decodeMorse(morseSequence: String): String {
        return MORSE_CODE_DICT[morseSequence] ?: ""
    }
}
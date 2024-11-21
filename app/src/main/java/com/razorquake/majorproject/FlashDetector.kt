package com.razorquake.majorproject

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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.concurrent.Executors

@Composable
fun FlashDetector() {
    val context = LocalContext.current
    var flashlightDetected by remember { mutableStateOf(false) }
    var decodedMessage by remember { mutableStateOf("") }

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
                CameraPreview(
                    onFrameAnalyzed = { isDetected ->
                        flashlightDetected = isDetected
                    },
                    onMessageDecoded = { message ->
                        decodedMessage += message
                    }
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = if (flashlightDetected) "Mobile Flashlight Detected!" else "No Mobile Flashlight Detected",
                        color = if (flashlightDetected) Color.Green else Color.Red
                    )
                    if (decodedMessage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Decoded Message: $decodedMessage",
                            color = Color.White
                        )
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
                    Button(
                        onClick = {
                            launcher.launch(Manifest.permission.CAMERA)
                        }
                    ) {
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
    onMessageDecoded: (String) -> Unit
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
                            onMessageDecoded = onMessageDecoded
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
                    // Handle any errors
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

class FlashlightAnalyzer(
    private val onFlashlightDetected: (Boolean) -> Unit,
    private val onMessageDecoded: (String) -> Unit
) : ImageAnalysis.Analyzer {
    private var lastAnalysisTimestamp = 0L
    private val ANALYSIS_INTERVAL = 100L // Analyze every 100ms for more responsive Morse detection
    private val morseDetector = MorseCodeDetector()
    private var morseSequence = ""
    private var flashOn = false
    private var flashStartTime: Long? = null

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(image: ImageProxy) {
        val currentTimestamp = System.currentTimeMillis()
        if (currentTimestamp - lastAnalysisTimestamp >= ANALYSIS_INTERVAL) {
            val rotationDegrees = image.imageInfo.rotationDegrees
            val bitmap = image.toBitmap()
            val rotatedBitmap = rotateBitmap(bitmap, rotationDegrees.toFloat())

            val mat = Mat()
            Utils.bitmapToMat(rotatedBitmap, mat)

            val isFlashlightDetected = detectMobileFlashlight(mat)
            onFlashlightDetected(isFlashlightDetected)

            // Morse code processing
            processFlashSignal(isFlashlightDetected, currentTimestamp)

            lastAnalysisTimestamp = currentTimestamp
        }
        image.close()
    }

    private fun processFlashSignal(isFlashlightDetected: Boolean, currentTimestamp: Long) {
        if (isFlashlightDetected) {
            if (!flashOn) {
                flashOn = true
                flashStartTime = currentTimestamp
            }
        } else {
            if (flashOn) {
                flashOn = false
                val duration = (currentTimestamp - (flashStartTime ?: currentTimestamp)) / 1000.0

                // Add dot or dash based on duration
                morseSequence += if (duration < MorseCodeDetector.DOT_DURATION) "." else "-"
            }

            // Check for letter space
            flashStartTime?.let { startTime ->
                if ((currentTimestamp - startTime) / 1000.0 > MorseCodeDetector.SPACE_DURATION) {
                    morseSequence += " "
                    flashStartTime = null

                    // Try to decode if we have a sequence
                    if (morseSequence.isNotEmpty()) {
                        val message = morseDetector.decodeMorse(morseSequence)
                        if (message.isNotEmpty()) {
                            onMessageDecoded(message)
                            morseSequence = ""
                        }
                    }
                }
            }
        }
    }

    private fun detectMobileFlashlight(mat: Mat): Boolean {
        val gray = Mat()
        Imgproc.cvtColor(mat, gray, Imgproc.COLOR_RGB2GRAY)
        Imgproc.GaussianBlur(gray, gray, Size(5.0, 5.0), 0.0)

        val threshold = Mat()
        Imgproc.threshold(gray, threshold, 240.0, 255.0, Imgproc.THRESH_BINARY)

        val contours = mutableListOf<MatOfPoint>()
        val hierarchy = Mat()
        Imgproc.findContours(threshold, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE)

        for (contour in contours) {
            val area = Imgproc.contourArea(contour)
            if (area > 300) {
                val rect = Imgproc.boundingRect(contour)
                val aspectRatio = rect.width.toFloat() / rect.height.toFloat()
                if (aspectRatio in 0.8f..1.2f) {
                    return true
                }
            }
        }
        return false
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
            "...--" to "3", "....-" to "4", "....." to "5", "-...." to "6",
            "--..." to "7", "---.." to "8", "----." to "9",
            ".-.-.-" to ".", "--..--" to ",", "..--.." to "?", "-..-." to "/",
            "-.--." to "(", "-.--.-" to ")", "-...-" to "=", "-....-" to "-",
            ".-.-." to "+", ".-..." to "&", "---..." to ":", "-.-.-." to ";",
            "...-..-" to "$", ".-..-." to "\"", "..--.-" to "_"
        )

        const val DOT_DURATION = 0.206 // 206ms
        const val SPACE_DURATION = 0.618 // 618ms
    }

    private var morseSequence = ""
    private var flashOn = false
    private var flashStartTime: Long? = null
    private val _decodedMessage = MutableStateFlow("")
    val decodedMessage: StateFlow<String> = _decodedMessage

    fun decodeMorse(morseSequence: String): String {
        return morseSequence.trim().split("   ").joinToString(" ") { word ->
            word.split(" ").joinToString("") { letter ->
                MORSE_CODE_DICT[letter] ?: ""
            }
        }
    }
}
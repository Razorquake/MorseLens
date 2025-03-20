package com.razorquake.morselens.ui.flashlight

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

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
    private val WORD_GAP = 1.4

    // State variables
    private var lastLightState = false
    private var lightStartTime = 0L
    private var lastLightEndTime = 0L
    private val detectionCircleRadius = 20
    private var currentMorse = ""
    private var decodedText = ""

    @ExperimentalGetImage
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
        val mask = Mat.zeros(height, width, CvType.CV_8UC1)
        val center = Point(width / 2.0, height / 2.0)
        Imgproc.circle(mask, center, detectionCircleRadius, Scalar(255.0), -1)

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
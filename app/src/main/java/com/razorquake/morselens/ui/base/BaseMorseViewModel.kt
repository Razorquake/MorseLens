package com.razorquake.morselens.ui.base

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.razorquake.morselens.data.PreferencesManager
import com.razorquake.morselens.ui.morse_code_translator.TransmissionMode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.text.iterator

abstract class BaseMorseViewModel <S: BaseState>(
    preferencesManager: PreferencesManager
) : ViewModel(){
    private val unitTime = preferencesManager.unitTimeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Companion.Eagerly,
        initialValue = 200L
    )
    protected abstract val _state: MutableStateFlow<S>
    protected var activeTransmissionJob: Job? = null
    protected val morseCodeMap = mapOf(
        'A' to ".-", 'B' to "-...", 'C' to "-.-.", 'D' to "-..",
        'E' to ".", 'F' to "..-.", 'G' to "--.", 'H' to "....",
        'I' to "..", 'J' to ".---", 'K' to "-.-", 'L' to ".-..",
        'M' to "--", 'N' to "-.", 'O' to "---", 'P' to ".--.",
        'Q' to "--.-", 'R' to ".-.", 'S' to "...", 'T' to "-",
        'U' to "..-", 'V' to "...-", 'W' to ".--", 'X' to "-..-",
        'Y' to "-.--", 'Z' to "--..", '1' to ".----", '2' to "..---",
        '3' to "...--", '4' to "....-", '5' to ".....", '6' to "-....",
        '7' to "--...", '8' to "---..", '9' to "----.", '0' to "-----",
        '.' to ".-.-.-", ',' to "--..--", '?' to "..--..", '/' to "-..-.",
        '@' to ".--.-.", '!' to "-.-.--", '(' to "-.--.", ')' to "-.--.-",
        '=' to "-...-", '-' to "-....-", '+' to ".-.-.", '&' to ".-...",
        ':' to "---...", ';' to "-.-.-.", '$' to "...-..-",
        '"' to ".-..-.", '_' to "..--.-",
    )

    protected abstract fun setTransmissionMode(mode: TransmissionMode)
    protected abstract fun setError(error: String?)

    protected suspend fun transmitMorseCode(context: Context, mode: TransmissionMode, message: String) =
        withContext(Dispatchers.IO) {
            if (_state.value.transmissionMode != TransmissionMode.NONE) {
                setError("Transmission already in progress")
                return@withContext
            }

            setTransmissionMode(mode)
            setError(null)

            val unitTime: Long = unitTime.value

            if (message.isEmpty()) {
                setError("Message cannot be empty")
                setTransmissionMode(TransmissionMode.NONE)
                return@withContext
            }

            try {
                coroutineScope {
                    val cleanupJob = launch(Dispatchers.IO) {
                        try {
                            awaitCancellation()
                        } finally {
                            withContext(NonCancellable) {
                                when (mode) {
                                    TransmissionMode.NONE -> {}
                                    TransmissionMode.FLASHLIGHT -> stopFlashlight(context)
                                    TransmissionMode.VIBRATION -> stopVibration(context)
                                    TransmissionMode.SOUND -> stopSound()
                                }
                            }
                        }
                    }
                    for (char in message.uppercase()) {
                        ensureActive()
                        if (char == ' ') {
                            delay(7 * unitTime)
                            continue
                        }

                        val morseCode = morseCodeMap[char] ?: continue
                        for (symbol in morseCode) {
                            ensureActive()
                            when (mode) {
                                TransmissionMode.NONE -> {}
                                TransmissionMode.FLASHLIGHT -> transmitFlashlight(
                                    context,
                                    symbol,
                                    unitTime
                                )

                                TransmissionMode.VIBRATION -> transmitVibration(
                                    context,
                                    symbol,
                                    unitTime
                                )

                                TransmissionMode.SOUND -> transmitSound(symbol, unitTime)
                            }
                            delay(unitTime)
                        }
                        delay(4 * unitTime)
                    }
                    cleanupJob.cancel()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                setError("Error sending Morse code: ${e.message}")
                setTransmissionMode(TransmissionMode.NONE)
            } finally {
                withContext(NonCancellable) {
                    when (mode) {
                        TransmissionMode.NONE -> {}
                        TransmissionMode.FLASHLIGHT -> stopFlashlight(context)
                        TransmissionMode.VIBRATION -> stopVibration(context)
                        TransmissionMode.SOUND -> stopSound()
                    }
                    setTransmissionMode(TransmissionMode.NONE)
                }
            }
        }

    private suspend fun transmitFlashlight(context: Context, symbol: Char, unitTime: Long) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = getCameraWithFlashlight(cameraManager) ?: return
        when (symbol) {
            '.' -> {
                safeToggleFlashlight(cameraManager, cameraId, true)
                delay(unitTime)
                safeToggleFlashlight(cameraManager, cameraId, false)
            }
            '-' -> {
                safeToggleFlashlight(cameraManager, cameraId, true)
                delay(3 * unitTime)
                safeToggleFlashlight(cameraManager, cameraId, false)
            }
        }
    }

    private suspend fun transmitVibration(context: Context, symbol: Char, unitTime: Long) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        when(symbol) {
            '.' -> {
                vibrator.vibrate(VibrationEffect.createOneShot(unitTime, VibrationEffect.DEFAULT_AMPLITUDE))
                delay(unitTime)
            }
            '-' -> {
                vibrator.vibrate(VibrationEffect.createOneShot(3 * unitTime, VibrationEffect.DEFAULT_AMPLITUDE))
                delay(3 * unitTime)
            }
        }
    }

    private suspend fun transmitSound(symbol: Char, unitTime: Long) {
        val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        when(symbol) {
            '.' -> {
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_0)
                delay(unitTime)
                toneGenerator.stopTone()
            }
            '-' -> {
                toneGenerator.startTone(ToneGenerator.TONE_DTMF_0)
                delay(3 * unitTime)
                toneGenerator.stopTone()
            }
        }
    }

    private fun stopFlashlight(context: Context) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = getCameraWithFlashlight(cameraManager) ?: return
        safeToggleFlashlight(cameraManager, cameraId, false)
    }

    private fun stopVibration(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.cancel()
    }

    private fun stopSound(){

    }

    private fun safeToggleFlashlight(
        cameraManager: CameraManager,
        cameraId: String,
        isOn: Boolean
    ){
        try {
            cameraManager.setTorchMode(cameraId, isOn)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getCameraWithFlashlight(cameraManager: CameraManager): String? {
        return try {
            cameraManager.cameraIdList.firstOrNull { cameraId ->
                cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        }  catch (e: Exception) {
            setError("Failed to access camera: ${e.message}")
            null
        }
    }

    protected fun stopTransmission(){
        activeTransmissionJob?.cancel()
        activeTransmissionJob = null
        setTransmissionMode(TransmissionMode.NONE)
    }
}
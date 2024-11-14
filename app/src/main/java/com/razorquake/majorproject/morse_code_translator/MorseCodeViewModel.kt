package com.razorquake.majorproject.morse_code_translator

import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MorseCodeViewModel: ViewModel() {
    private val _state = mutableStateOf(MorseCodeState())
    val state: State<MorseCodeState> = _state
    private fun setMessage(message: String) {
        _state.value = _state.value.copy(message = message)
    }

    private fun setUnitTime(unitTime: Long) {
        _state.value = _state.value.copy(unitTime = unitTime)
    }


    private suspend fun sendMorseCode(context: Context) {
        val unitTime: Long = _state.value.unitTime // Each unit is 200 milliseconds
        for (char in _state.value.message.uppercase()) {
            if (char == ' ') {
                delay(7 * unitTime) // Space between words
                continue
            }
            val morseCode = morseCodeMap[char] ?: continue
            for (symbol in morseCode) {
                when (symbol) {
                    '.' -> {
                        toggleFlashlight(context, true)
                        delay(unitTime) // Dot is 1 unit long
                        toggleFlashlight(context, false)
                    }
                    '-' -> {
                        toggleFlashlight(context, true)
                        delay(3 * unitTime) // Dash is 3 units long
                        toggleFlashlight(context, false)
                    }
                }
                delay(unitTime) // Space between symbols
            }
            delay(3 * unitTime) // Space between letters
        }
    }

    private fun toggleFlashlight(context: Context, isOn: Boolean) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0] // Assuming the first camera has the flashlight
        try {
            cameraManager.setTorchMode(cameraId, isOn)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private val morseCodeMap = mapOf(
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

    fun onEvent(event: MorseCodeEvent) {
        when (event) {
            is MorseCodeEvent.setUnitTime -> {
                setUnitTime(event.unitTime)
            }
            is MorseCodeEvent.setMessage -> {
                setMessage(event.message)
            }
            is MorseCodeEvent.sendMorseCode -> {
                viewModelScope.launch {
                    sendMorseCode(event.context)
                }
            }
        }
    }
}
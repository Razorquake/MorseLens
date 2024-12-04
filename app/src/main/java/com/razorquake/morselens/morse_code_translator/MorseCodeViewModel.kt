package com.razorquake.morselens.morse_code_translator

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.translate.TranslateLanguage
import com.razorquake.morselens.morse_code_translator.speech.Language
import com.razorquake.morselens.morse_code_translator.speech.MLKitTranslator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MorseCodeViewModel(
    private val translator: MLKitTranslator = MLKitTranslator()
): ViewModel() {
    private val _state = MutableStateFlow(MorseCodeState())
    val state: StateFlow<MorseCodeState> = _state.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null
    private var translationJob: Job? = null
    private var activeTransmissionJob: Job? = null

    fun startListening(context: Context) {
        if (_state.value.isListening) return

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(createRecognitionListener())
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "${_state.value.selectedLanguage.code}-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        viewModelScope.launch {
            try {
                _state.update { it.copy(isListening = true, error = null) }
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isListening = false,
                        error = "Failed to start speech recognition: ${e.message}"
                    )
                }
            }
        }
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
        _state.update { it.copy(isListening = false) }
    }

    private fun createRecognitionListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            Log.d("SpeechRecognition", "Ready for speech")
        }

        override fun onBeginningOfSpeech() {
            Log.d("SpeechRecognition", "Beginning of speech")
        }

        override fun onRmsChanged(rmsdB: Float) {
            _state.update {
                val updatedHistory: List<Float> =listOf<Float>(rmsdB)+it.rmsHistory.dropLast(1)
                it.copy(rmsHistory = updatedHistory)
            }
        }

        override fun onBufferReceived(buffer: ByteArray?) {

        }

        override fun onEndOfSpeech() {
            Log.d("SpeechRecognition", "Speech ended")
        }

        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Unknown error"
            }
            _state.update {
                it.copy(
                    isListening = false,
                    error = "Speech recognition error: $errorMessage"
                )
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull() ?: return

            _state.update { it.copy(
                recognizedText=recognizedText, isTranslating = true, isListening = false
            ) }
            translateText(recognizedText)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val recognizedText = matches?.firstOrNull() ?: return
            _state.update {
                it.copy(
                    recognizedText=recognizedText, isTranslating = true
                )
            }
            translateText(recognizedText)
        }

        override fun onEvent(eventType: Int, params: Bundle?) {
            TODO("Not yet implemented")
        }

    }

    private fun translateText(text: String) {
        translationJob?.cancel()
        translationJob = viewModelScope.launch {
            try {
                val translatedText = translator.translate(
                    text,
                    _state.value.selectedLanguage.code,
                    TranslateLanguage.ENGLISH
                )
                _state.update { it.copy(message =translatedText, error = null, isTranslating = false) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(error = "Translation failed: ${e.message}", isTranslating = false)
                }
            }
        }
    }

    fun setSelectedLanguage(language: Language) {
        _state.update { it.copy(selectedLanguage = language) }
    }

    private fun setMessage(message: String) {
        _state.update { it.copy(message = message) }
    }

    private fun setUnitTime(unitTime: Long) {
        _state.update { it.copy(unitTime = unitTime) }
    }

    private fun setError(error: String?) {
        _state.update { it.copy(error = error) }
    }

    private fun setTransmitting(isTransmitting: Boolean) {
        _state.update { it.copy(isTransmitting = isTransmitting) }
    }


    private suspend fun sendMorseCode(context: Context) =withContext(Dispatchers.IO) {

        if (_state.value.isTransmitting) {
            setError("Transmission already in progress")
            return@withContext
        }
        setTransmitting(true)
        setError(null)

        val unitTime: Long = _state.value.unitTime // Each unit is 200 milliseconds
        val message = _state.value.message

        if (message.isEmpty()) {
            setError("Message cannot be empty")
            setTransmitting(false)
            return@withContext
        }
        // Validate device has flashlight
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = getCameraWithFlashlight(cameraManager) ?: run {
            _state.update {
                it.copy(error="Device has no flashlight", isTransmitting = false)
            }
            return@withContext
        }

        try {
            coroutineScope {
                val cleanupJob = launch(Dispatchers.IO) {
                    try {
                        awaitCancellation()
                    } finally {
                        withContext(NonCancellable) {
                            safeToggleFlashlight(cameraManager, cameraId, false)
                        }
                    }
                }
                for (char in message.uppercase()) {
                    ensureActive()
                    if (char == ' ') {
                        delay(7 * unitTime) // Space between words
                        continue
                    }
                    val morseCode = morseCodeMap[char] ?: continue
                    for (symbol in morseCode) {
                        ensureActive()
                        when (symbol) {
                            '.' -> {
                                safeToggleFlashlight(cameraManager, cameraId, true)
                                delay(unitTime) // Dot is 1 unit long
                                safeToggleFlashlight(cameraManager, cameraId, false)
                            }
                            '-' -> {
                                safeToggleFlashlight(cameraManager, cameraId, true)
                                delay(2 * unitTime) // Dash is 3 units long
                                safeToggleFlashlight(cameraManager, cameraId, false)
                            }
                        }
                        delay(unitTime) // Space between symbols
                    }
                    delay(4 * unitTime) // Space between letters
                }
                cleanupJob.cancel()
            }
        } catch (e: CancellationException){
            throw e
        } catch (e: Exception){
            _state.update {
                it.copy(
                    error="Error sending Morse code: ${e.message}",
                    isTransmitting = false
                )
            }
        } finally {
            withContext(NonCancellable) {
                safeToggleFlashlight(cameraManager, cameraId, false)
                setTransmitting(false)
            }
        }
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

    private fun stopTransmission(){
        activeTransmissionJob?.cancel()
        activeTransmissionJob = null
        setTransmitting(false)
    }

    fun onEvent(event: MorseCodeEvent) {
        when (event) {
            is MorseCodeEvent.SetUnitTime -> {
                setUnitTime(event.unitTime)
            }
            is MorseCodeEvent.SetMessage -> {
                setMessage(event.message)
            }
            is MorseCodeEvent.SendMorseCode -> {
                activeTransmissionJob = viewModelScope.launch {
                    sendMorseCode(event.context)
                }
            }
            is MorseCodeEvent.StopTransmission -> {
                stopTransmission()
            }
            is MorseCodeEvent.SetSelectedLanguage -> {
                setSelectedLanguage(event.language)
            }
            is MorseCodeEvent.StartListening -> {
                startListening(event.context)
            }
            is MorseCodeEvent.StopListening -> {
                stopListening()
            }
        }
    }
}
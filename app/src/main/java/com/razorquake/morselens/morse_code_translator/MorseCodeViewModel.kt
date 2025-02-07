package com.razorquake.morselens.morse_code_translator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.translate.TranslateLanguage
import com.razorquake.morselens.data.PreferencesManager
import com.razorquake.morselens.morse_code_translator.speech.Language
import com.razorquake.morselens.morse_code_translator.speech.MLKitTranslator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MorseCodeViewModel @Inject constructor(
    preferencesManager: PreferencesManager
): BaseMorseViewModel<MorseCodeState>(
    preferencesManager = preferencesManager
) {
    private val translator: MLKitTranslator = MLKitTranslator()
    override val _state = MutableStateFlow(MorseCodeState())
    val state: StateFlow<MorseCodeState> = _state.asStateFlow()

    private var historySize: Int = 50
    private var speechRecognizer: SpeechRecognizer? = null
    private var translationJob: Job? = null

    override fun setTransmissionMode(mode: TransmissionMode){
        _state.update { it.copy(transmissionMode = mode) }
    }

    private fun updateHistorySize(size: Int){
        historySize = size
        _state.update {
            val currentHistory = it.rmsHistory
            val newHistory = when {
                currentHistory.size > historySize -> currentHistory.takeLast(historySize)
                currentHistory.size < historySize -> currentHistory + List(historySize - currentHistory.size) { 0f }
                else -> currentHistory
            }
            it.copy(rmsHistory = newHistory)
        }
    }

    private fun startListening(context: Context) {
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

    private fun stopListening() {
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
                val updatedHistory: List<Float> =listOf(rmsdB)+it.rmsHistory.dropLast(1)
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

    private fun setSelectedLanguage(language: Language) {
        _state.update { it.copy(selectedLanguage = language) }
    }

    private fun setMessage(message: String) {
        _state.update { it.copy(message = message) }
    }

    override fun setError(error: String?) {
        _state.update { it.copy(error = error) }
    }


    fun onEvent(event: MorseCodeEvent) {
        when (event) {
            is MorseCodeEvent.SetMessage -> {
                setMessage(event.message)
            }
            is MorseCodeEvent.SendMorseCode -> {
                activeTransmissionJob = viewModelScope.launch {
                    transmitMorseCode(event.context, event.mode, _state.value.message)
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
            is MorseCodeEvent.UpdateHistorySize -> {
                updateHistorySize(event.size)
            }
        }
    }
}
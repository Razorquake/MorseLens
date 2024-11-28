package com.razorquake.majorproject.morse_code_translator

import com.google.mlkit.nl.translate.TranslateLanguage
import com.razorquake.majorproject.morse_code_translator.speech.Language

data class MorseCodeState(
    val message: String = "",
    val error: String? = null,
    //Morse
    val unitTime: Long = 200L,
    val isTransmitting: Boolean = false,
    //Speech
    val isListening: Boolean = false,
    val recognizedText: String = "",
    val supportedLanguage: List<Language> = listOf(
        Language("Hindi", TranslateLanguage.HINDI),
        Language("Bengali", TranslateLanguage.BENGALI),
        Language("Gujarati", TranslateLanguage.GUJARATI),
        Language("Kannada", TranslateLanguage.KANNADA),
        Language("Marathi", TranslateLanguage.MARATHI),
    ),
    val selectedLanguage: Language = Language("Hindi", TranslateLanguage.HINDI),
)

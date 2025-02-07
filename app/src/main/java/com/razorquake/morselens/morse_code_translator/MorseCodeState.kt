package com.razorquake.morselens.morse_code_translator

import com.google.mlkit.nl.translate.TranslateLanguage
import com.razorquake.morselens.morse_code_translator.speech.Language
data class MorseCodeState(
    val message: String = "",
    override val error: String? = null,
    //Morse
    // Speech
    val isListening: Boolean = false,
    var isTranslating: Boolean = false,
    val recognizedText: String = "",
    val supportedLanguage: List<Language> = listOf(
        Language("हिंदी", TranslateLanguage.HINDI),
        Language("বাংলা", TranslateLanguage.BENGALI),
        Language("ગુજરાતી", TranslateLanguage.GUJARATI),
        Language("ಕನ್ನಡ", TranslateLanguage.KANNADA),
        Language("मराठी", TranslateLanguage.MARATHI),
        Language("தமிழ்", TranslateLanguage.TAMIL),
        Language("తెలుగు", TranslateLanguage.TELUGU),
        Language("اردو", TranslateLanguage.URDU),
    ),
    val selectedLanguage: Language = Language("हिंदी", TranslateLanguage.HINDI),
    val rmsHistory: List<Float> = emptyList<Float>(),
    override val transmissionMode: TransmissionMode = TransmissionMode.NONE
): BaseState

enum class TransmissionMode {
    NONE,
    FLASHLIGHT,
    VIBRATION,
    SOUND
}

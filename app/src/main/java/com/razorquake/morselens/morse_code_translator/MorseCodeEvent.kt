package com.razorquake.morselens.morse_code_translator

import android.content.Context
import com.razorquake.morselens.morse_code_translator.speech.Language

sealed class MorseCodeEvent {
    data class SetUnitTime(val unitTime: Long) : MorseCodeEvent()

    data class SetMessage(val message: String) : MorseCodeEvent()
    data class SendMorseCode(val context: Context, val mode: TransmissionMode) : MorseCodeEvent()
    data object StopTransmission : MorseCodeEvent()

    data class SetSelectedLanguage(val language: Language) : MorseCodeEvent()
    data class StartListening(val context: Context) : MorseCodeEvent()
    data object StopListening : MorseCodeEvent()
    data class UpdateHistorySize(val size: Int) : MorseCodeEvent()

}
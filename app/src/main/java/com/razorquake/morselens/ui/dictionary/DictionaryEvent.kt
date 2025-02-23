package com.razorquake.morselens.ui.dictionary

import android.content.Context
import com.razorquake.morselens.ui.morse_code_translator.TransmissionMode

sealed class DictionaryEvent {
    data class SendMorseCode(val context: Context, val mode: TransmissionMode, val char: Char) : DictionaryEvent()
    data object StopTransmission : DictionaryEvent()
    data class GetMorseCode(val char: Char): DictionaryEvent()
}
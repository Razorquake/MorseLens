package com.razorquake.morselens.morse_code_translator.dictionary

import android.content.Context
import com.razorquake.morselens.morse_code_translator.TransmissionMode

sealed class DictionaryEvent {
    data class SetUnitTime(val unitTime: Long) : DictionaryEvent()
    data class SendMorseCode(val context: Context, val mode: TransmissionMode, val char: Char) : DictionaryEvent()
    object StopTransmission : DictionaryEvent()
    data class GetMorseCode(val char: Char): DictionaryEvent()
}
package com.razorquake.majorproject.morse_code_translator

import android.content.Context

sealed class MorseCodeEvent {
    data class SetUnitTime(val unitTime: Long) : MorseCodeEvent()
    data class SetMessage(val message: String) : MorseCodeEvent()
    data class SendMorseCode(val context: Context) : MorseCodeEvent()
    data object StopTransmission : MorseCodeEvent()
}
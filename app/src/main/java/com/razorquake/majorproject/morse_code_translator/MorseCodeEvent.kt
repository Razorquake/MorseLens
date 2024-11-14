package com.razorquake.majorproject.morse_code_translator

import android.content.Context

sealed class MorseCodeEvent {
    data class setUnitTime(val unitTime: Long) : MorseCodeEvent()
    data class setMessage(val message: String) : MorseCodeEvent()
    data class sendMorseCode(val context: Context) : MorseCodeEvent()

}
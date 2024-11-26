package com.razorquake.majorproject.morse_code_translator

data class MorseCodeState(
    val message: String = "",
    val unitTime: Long = 200L,
    val error: String? = null,
    val isTransmitting: Boolean = false
)

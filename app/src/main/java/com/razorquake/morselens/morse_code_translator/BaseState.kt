package com.razorquake.morselens.morse_code_translator

interface BaseState {
    val error: String?
    //Morse
    val unitTime: Long
    //Speech
    val transmissionMode: TransmissionMode
}

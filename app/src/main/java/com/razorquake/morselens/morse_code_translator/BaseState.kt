package com.razorquake.morselens.morse_code_translator

interface BaseState {
    val error: String?
    //Speech
    val transmissionMode: TransmissionMode
}

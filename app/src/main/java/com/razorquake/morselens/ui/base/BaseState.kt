package com.razorquake.morselens.ui.base

import com.razorquake.morselens.ui.morse_code_translator.TransmissionMode

interface BaseState {
    val error: String?
    //Speech
    val transmissionMode: TransmissionMode
}
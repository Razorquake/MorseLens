package com.razorquake.morselens.morse_code_translator.dictionary

import com.razorquake.morselens.morse_code_translator.BaseState
import com.razorquake.morselens.morse_code_translator.TransmissionMode

data class DictionaryState(
    override val error: String?=null,
    override val transmissionMode: TransmissionMode = TransmissionMode.NONE,
    val activeCharacter: Char? = null
): BaseState
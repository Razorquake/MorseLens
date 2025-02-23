package com.razorquake.morselens.ui.dictionary

import com.razorquake.morselens.ui.base.BaseState
import com.razorquake.morselens.ui.morse_code_translator.TransmissionMode

data class DictionaryState(
    override val error: String?=null,
    override val transmissionMode: TransmissionMode = TransmissionMode.NONE,
    val activeCharacter: Char? = null
): BaseState
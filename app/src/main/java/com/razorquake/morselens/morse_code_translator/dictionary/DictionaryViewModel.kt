package com.razorquake.morselens.morse_code_translator.dictionary

import androidx.lifecycle.viewModelScope
import com.razorquake.morselens.morse_code_translator.BaseMorseViewModel
import com.razorquake.morselens.morse_code_translator.TransmissionMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DictionaryViewModel() : BaseMorseViewModel<DictionaryState>() {
    override val _state = MutableStateFlow(DictionaryState())
    override fun setTransmissionMode(mode: TransmissionMode) {
        _state.update { it.copy(transmissionMode = mode) }
    }

    override fun setError(error: String?) {
        _state.update { it.copy(error = error) }
    }

    override fun setUnitTime(unitTime: Long) {
        _state.update { it.copy(unitTime = unitTime) }
    }

    private fun setActiveCharacter(char: Char?){
        _state.update { it.copy(activeCharacter = char) }
    }

    val state = _state.asStateFlow()

    fun onEvent(event: DictionaryEvent): String? {
        when (event) {
            is DictionaryEvent.GetMorseCode -> {
                return morseCodeMap[event.char.toChar()]
            }
            is DictionaryEvent.SendMorseCode -> {
                activeTransmissionJob = viewModelScope.launch {
                    setActiveCharacter(event.char)
                    transmitMorseCode(event.context, event.mode,
                        _state.value.activeCharacter.toString()
                    )
                    setActiveCharacter(null)
                }
            }
            is DictionaryEvent.SetUnitTime -> {
                setUnitTime(event.unitTime)
            }
            DictionaryEvent.StopTransmission -> {
                stopTransmission()
                setActiveCharacter(null)
            }
        }
        return null
    }
}
package com.razorquake.morselens.ui.dictionary

import androidx.lifecycle.viewModelScope
import com.razorquake.morselens.data.PreferencesManager
import com.razorquake.morselens.ui.base.BaseMorseViewModel
import com.razorquake.morselens.ui.morse_code_translator.TransmissionMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DictionaryViewModel @Inject constructor(
    preferencesManager: PreferencesManager
) : BaseMorseViewModel<DictionaryState>(
    preferencesManager = preferencesManager
) {
    override val _state = MutableStateFlow(DictionaryState())
    override fun setTransmissionMode(mode: TransmissionMode) {
        _state.update { it.copy(transmissionMode = mode) }
    }

    override fun setError(error: String?) {
        _state.update { it.copy(error = error) }
    }

    private fun setActiveCharacter(char: Char?){
        _state.update { it.copy(activeCharacter = char) }
    }

    val state = _state.asStateFlow()

    fun onEvent(event: DictionaryEvent): String? {
        when (event) {
            is DictionaryEvent.GetMorseCode -> {
                return morseCodeMap[event.char]
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
            DictionaryEvent.StopTransmission -> {
                stopTransmission()
                setActiveCharacter(null)
            }
        }
        return null
    }
}
package com.razorquake.morselens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.razorquake.morselens.data.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
): ViewModel() {
    val unitTime: StateFlow<Long> = preferencesManager.unitTimeFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 200L
        )

    fun updateUnitTime(unitTime: Long) {
        viewModelScope.launch {
            preferencesManager.setUnitTime(unitTime)
        }
    }
}
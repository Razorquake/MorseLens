package com.razorquake.morselens

import androidx.annotation.DrawableRes
import kotlinx.serialization.Serializable

sealed class Screen {
    val text: String
        get() {
            return when (this) {
                is MorseCodeTranslator -> "Morse Code Translator"
                is FlashDetector -> "Flash Detector"
                is Dictionary -> "Dictionary"
                Settings -> "Settings"
            }
        }
    @get:DrawableRes
    val icon: Int
        get() {
            return when (this) {
                is MorseCodeTranslator -> R.drawable.morse_code_svgrepo_com
                is FlashDetector -> R.drawable.outline_camera_enhance_24
                is Dictionary -> R.drawable.book
                Settings -> R.drawable.settings
            }
        }
    @Serializable
    data object MorseCodeTranslator : Screen()
    @Serializable
    data object FlashDetector : Screen()
    @Serializable
    data object Dictionary : Screen()
    @Serializable
    data object Settings : Screen()
}
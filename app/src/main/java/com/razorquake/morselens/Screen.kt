package com.razorquake.morselens

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    data object Home : Screen()
    @Serializable
    data object MorseCodeTranslator : Screen()
    @Serializable
    data object FlashDetector : Screen()
    @Serializable
    data object Dictionary : Screen()
}
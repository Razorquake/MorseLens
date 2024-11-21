package com.razorquake.majorproject

import kotlinx.serialization.Serializable

sealed class Screen {
    @Serializable
    data object Home : Screen()
    @Serializable
    data object MorseCodeTranslator : Screen()
    @Serializable
    data object FlashDetector : Screen()
}
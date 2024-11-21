package com.razorquake.majorproject.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(
    onMorseCodeTranslator: () -> Unit,
    onFlashDetector: () -> Unit
) {
        Column(modifier = Modifier.fillMaxSize()) {
            ListItem(
                headlineContent = {Text("Morse Code Translator")},
                modifier = Modifier.clickable { onMorseCodeTranslator() }
            )
            ListItem(
                headlineContent = {Text("Flash Detector")},
                modifier = Modifier.clickable { onFlashDetector() }
            )
        }
}
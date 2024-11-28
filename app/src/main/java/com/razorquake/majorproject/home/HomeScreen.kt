package com.razorquake.majorproject.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.razorquake.majorproject.R

@Composable
fun HomeScreen(
    onMorseCodeTranslator: () -> Unit,
    onFlashDetector: () -> Unit
) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Home", style = MaterialTheme.typography.headlineMedium)
            ListItem(
                leadingContent = {
                    Icon(
                        modifier = Modifier.size(40.dp),
                        painter = painterResource(id = R.drawable._678673),
                        contentDescription = null
                    )
                },
                headlineContent = {Text("Morse Code Translator", style = MaterialTheme.typography.bodyLarge)},
                supportingContent = {Text("Translate Text to Morse Code", style = MaterialTheme.typography.bodyMedium)},
                modifier = Modifier.clickable { onMorseCodeTranslator() }
            )
            ListItem(
                leadingContent = {
                    Icon(
                        modifier = Modifier.size(40.dp),
                        painter = painterResource(id = R.drawable.outline_camera_enhance_24),
                        contentDescription = null
                    )
                },
                headlineContent = {Text("Flash Detector", style = MaterialTheme.typography.bodyLarge)},
                supportingContent = {Text("Translate Morse Code to Text", style = MaterialTheme.typography.bodyMedium)},
                modifier = Modifier.clickable { onFlashDetector() }
            )
        }
}
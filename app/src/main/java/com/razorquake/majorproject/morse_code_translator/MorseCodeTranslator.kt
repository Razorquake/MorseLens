package com.razorquake.majorproject.morse_code_translator

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts


@Composable
fun MorseCodeTranslator(state: MorseCodeState, onEvent: (MorseCodeEvent) -> Unit) {
    val context = LocalContext.current
    // State to track permission
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    // Permission request launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            hasAudioPermission = isGranted
        }
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LanguageSelector(
            languages = state.supportedLanguage,
            selectedLanguage = state.selectedLanguage,
            onLanguageSelected = { onEvent(MorseCodeEvent.SetSelectedLanguage(it)) },
            enabled = !state.isListening && !state.isTransmitting

        )
        Button(
            onClick = {
                if (!hasAudioPermission) {
                    launcher.launch(Manifest.permission.RECORD_AUDIO)
                }
                if (state.isListening) {
                    onEvent(MorseCodeEvent.StopListening)
                } else {
                    onEvent(MorseCodeEvent.StartListening(context))
                }
            },
            enabled = !state.isTransmitting,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.isListening)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )
        ) {
            Text(if (state.isListening) "Stop Listening" else "Start Listening")
        }
        TextField(
            value = state.message,
            onValueChange = {onEvent(MorseCodeEvent.SetMessage(it))},
            label = { Text("Enter message") },
            enabled = !state.isTransmitting
        )
        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (!state.isTransmitting) {
            Button(onClick = {
                onEvent(MorseCodeEvent.SendMorseCode(context))
                },
                enabled = state.message.isNotBlank()
            ) {
                Text("Send Morse Code via Flashlight")
            }
        } else {
            Button(
                onClick = {
                    onEvent(MorseCodeEvent.StopTransmission)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Stop Transmission")
            }
        }
        Text(text = "Unit Time: ${state.unitTime} ms")
        Slider(
            value = state.unitTime.toFloat(),
            onValueChange = { onEvent(MorseCodeEvent.SetUnitTime(it.toLong())) },
            valueRange = 100f..450f,
        )

    }
}
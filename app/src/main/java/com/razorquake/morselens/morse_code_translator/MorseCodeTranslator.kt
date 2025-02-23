package com.razorquake.morselens.morse_code_translator

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
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
import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.razorquake.morselens.R
import com.razorquake.morselens.ui.theme.MorseLensTheme


@Composable
fun MorseCodeTranslator(
    state: MorseCodeState,
    onEvent: (MorseCodeEvent) -> Unit,
    bottomPadding: Dp,
    topPadding: Dp
) {
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
            .padding(16.dp)
            .padding(
                top = topPadding,
                bottom = bottomPadding
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        LanguageSelector(
            languages = state.supportedLanguage,
            selectedLanguage = state.selectedLanguage,
            onLanguageSelected = { onEvent(MorseCodeEvent.SetSelectedLanguage(it)) },
            enabled = !state.isListening && state.transmissionMode == TransmissionMode.NONE
        )
        WaveformVisualizer(
            rmsHistory = state.rmsHistory,
            isListening = state.isListening,
            modifier = Modifier.padding(16.dp),
            onHistoryChanged = { onEvent(MorseCodeEvent.UpdateHistorySize(it)) }
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
            enabled = state.transmissionMode == TransmissionMode.NONE && !state.isTranslating,
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
            onValueChange = { onEvent(MorseCodeEvent.SetMessage(it)) },
            label = { Text("Enter message") },
            enabled = state.transmissionMode == TransmissionMode.NONE
        )
        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = {
                    if (state.transmissionMode == TransmissionMode.FLASHLIGHT) {
                        onEvent(MorseCodeEvent.StopTransmission)
                    } else {
                        onEvent(MorseCodeEvent.SendMorseCode(context, TransmissionMode.FLASHLIGHT))
                    }
                },
                enabled = state.message.isNotBlank() && (state.transmissionMode == TransmissionMode.NONE || state.transmissionMode == TransmissionMode.FLASHLIGHT)
            ) {
                Icon(
                    painter = painterResource(R.drawable.flash),
                    contentDescription = if (state.transmissionMode == TransmissionMode.FLASHLIGHT)
                        "Stop Flashlight" else "Start Flashlight",
                    tint = when {
                        state.transmissionMode == TransmissionMode.FLASHLIGHT ->
                            MaterialTheme.colorScheme.error
                        state.message.isBlank() || state.transmissionMode != TransmissionMode.NONE ->
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        else ->
                            MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            IconButton(
                onClick = {
                    if (state.transmissionMode == TransmissionMode.VIBRATION) {
                        onEvent(MorseCodeEvent.StopTransmission)
                    } else {
                        onEvent(MorseCodeEvent.SendMorseCode(context, TransmissionMode.VIBRATION))
                    }
                },
                enabled = state.message.isNotBlank() &&
                        (state.transmissionMode == TransmissionMode.NONE || state.transmissionMode == TransmissionMode.VIBRATION)
            ) {
                Icon(
                    painter = painterResource(R.drawable.vibration),
                    contentDescription = if (state.transmissionMode == TransmissionMode.VIBRATION)
                        "Stop Vibration" else "Start Vibration",
                    tint = when {
                        state.transmissionMode == TransmissionMode.VIBRATION ->
                            MaterialTheme.colorScheme.error
                        state.message.isBlank() || state.transmissionMode != TransmissionMode.NONE ->
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        else ->
                            MaterialTheme.colorScheme.onSurface
                    }
                )
            }
            IconButton(
                onClick = {
                    if (state.transmissionMode == TransmissionMode.SOUND) {
                        onEvent(MorseCodeEvent.StopTransmission)
                    } else {
                        onEvent(MorseCodeEvent.SendMorseCode(context, TransmissionMode.SOUND))
                    }
                },
                enabled = state.message.isNotBlank() &&
                        (state.transmissionMode == TransmissionMode.NONE || state.transmissionMode == TransmissionMode.SOUND)
            ) {
                Icon(
                    painter = painterResource(R.drawable.volume_up),
                    contentDescription = if (state.transmissionMode == TransmissionMode.SOUND)
                        "Stop Sound" else "Start Sound",
                    tint = when {
                        state.transmissionMode == TransmissionMode.SOUND ->
                            MaterialTheme.colorScheme.error
                        state.message.isBlank() || state.transmissionMode != TransmissionMode.NONE ->
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        else ->
                            MaterialTheme.colorScheme.onSurface
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MorseCodeTranslatorPreview() {
    MorseLensTheme {
        MorseCodeTranslator(
            state = MorseCodeState(),
            onEvent = {},
            bottomPadding = 0.dp,
            topPadding = 0.dp
        )
    }
}
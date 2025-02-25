package com.razorquake.morselens.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.razorquake.morselens.R
import com.razorquake.morselens.ui.morse_code_translator.TransmissionMode

@Composable
fun MorseCodeRow(
    isActive: Boolean,
    onTransmit: (TransmissionMode) -> Unit,
    onStop: () -> Unit,
    transmissionMode: TransmissionMode
){
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = {
                if (isActive&&transmissionMode == TransmissionMode.FLASHLIGHT) {
                    onStop
                } else {
                    onTransmit(TransmissionMode.FLASHLIGHT)
                }
            },
            enabled = isActive&&(transmissionMode == TransmissionMode.NONE || transmissionMode == TransmissionMode.FLASHLIGHT)
        ) {
            Icon(
                painter = painterResource(R.drawable.flash),
                contentDescription = if (transmissionMode == TransmissionMode.FLASHLIGHT)
                    "Stop Flashlight" else "Start Flashlight",
                tint = when {
                    transmissionMode == TransmissionMode.FLASHLIGHT&&isActive ->
                        MaterialTheme.colorScheme.error

                    transmissionMode != TransmissionMode.NONE ->
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

                    else ->
                        MaterialTheme.colorScheme.onSurface
                }
            )
        }
        IconButton(
            onClick = {
                if (transmissionMode == TransmissionMode.VIBRATION&&isActive) {
                    onStop
                } else {
                    onTransmit(TransmissionMode.VIBRATION)
                }
            },
            enabled =
                    isActive&&(transmissionMode == TransmissionMode.NONE || transmissionMode == TransmissionMode.VIBRATION)
        ) {
            Icon(
                painter = painterResource(R.drawable.vibration),
                contentDescription = if (transmissionMode == TransmissionMode.VIBRATION)
                    "Stop Vibration" else "Start Vibration",
                tint = when {
                    transmissionMode == TransmissionMode.VIBRATION&&isActive ->
                        MaterialTheme.colorScheme.error

                    transmissionMode != TransmissionMode.NONE ->
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

                    else ->
                        MaterialTheme.colorScheme.onSurface
                }
            )
        }
        IconButton(
            onClick = {
                if (transmissionMode == TransmissionMode.SOUND&&isActive) {
                    onStop
                } else {
                    onTransmit(TransmissionMode.SOUND)
                }
            },
            enabled =
                    isActive&&(transmissionMode == TransmissionMode.NONE || transmissionMode == TransmissionMode.SOUND)
        ) {
            Icon(
                painter = painterResource(R.drawable.volume_up),
                contentDescription = if (transmissionMode == TransmissionMode.SOUND)
                    "Stop Sound" else "Start Sound",
                tint = when {
                    transmissionMode == TransmissionMode.SOUND&&isActive ->
                        MaterialTheme.colorScheme.error

                    transmissionMode != TransmissionMode.NONE ->
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

                    else ->
                        MaterialTheme.colorScheme.onSurface
                }
            )
        }

    }
}

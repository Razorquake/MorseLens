package com.razorquake.morselens.morse_code_translator.dictionary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.razorquake.morselens.morse_code_translator.MorseCodeRow
import com.razorquake.morselens.morse_code_translator.TransmissionMode

@Composable
fun DictionaryItem(
    char: Char,
    morseCode: String,
    isActive: Boolean,
    onTransmit: (TransmissionMode) -> Unit,
    onStop: () -> Unit,
    transmissionMode: TransmissionMode
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Character and morse code
            Column {
                Text(
                    text = char.toString(),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = morseCode,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            MorseCodeRow(
                isActive = isActive,
                onTransmit = onTransmit,
                onStop = onStop,
                transmissionMode = transmissionMode
            )
        }
    }
}
package com.razorquake.morselens.ui.dictionary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.razorquake.morselens.ui.components.MorseCodeRow
import com.razorquake.morselens.ui.morse_code_translator.TransmissionMode

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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiary,

        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Character and morse code
            Column(
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ).size(
                    width = 48.dp,
                    height = 48.dp
                ),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = char.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = Color.Black
                    )
                )
                Text(
                    text = morseCode,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color.Black
                    )
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            MorseCodeRow(
                isActive = isActive,
                onTransmit = onTransmit,
                onStop = onStop,
                transmissionMode = transmissionMode
            )
        }
    }
}
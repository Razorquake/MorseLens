package com.razorquake.majorproject.morse_code_translator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MorseCodeTranslator(state: MorseCodeState, onEvent: (MorseCodeEvent) -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = state.message,
            onValueChange = {onEvent(MorseCodeEvent.setMessage(it))},
            label = { Text("Enter message") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            CoroutineScope(Dispatchers.Main).launch {
                onEvent(MorseCodeEvent.sendMorseCode(context))
            }
        }) {
            Text("Send Morse Code via Flashlight")
        }
        Text(text = "Unit Time: ${state.unitTime} ms")
        Slider(
            value = state.unitTime.toFloat(),
            onValueChange = { onEvent(MorseCodeEvent.setUnitTime(it.toLong())) },
            valueRange = 100f..1000f,
        )

    }
}





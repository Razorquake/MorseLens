package com.razorquake.morselens.ui.dictionary

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DictionaryScreen(
    state: DictionaryState,
    eventHandler: (DictionaryEvent) -> String?,
    innerPadding: PaddingValues
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                innerPadding
            )
            .padding(horizontal = 16.dp)
    ) {
        // Error message
        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Dictionary content
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Alphabets
            stickyHeader{
                Surface(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .clip(
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Text(
                        "Alphabets",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(
                                start = 4.dp
                            )
                    )
                }
            }
            items(('A'..'Z').toList()) { char ->
                DictionaryItem(
                    char = char,
                    morseCode = eventHandler(DictionaryEvent.GetMorseCode(char)) ?: "",
                    isActive = state.activeCharacter == char||state.activeCharacter==null,
                    onTransmit = { mode ->
                        eventHandler(DictionaryEvent.SendMorseCode(context, mode, char))
                    },
                    onStop = {
                        eventHandler(DictionaryEvent.StopTransmission)
                    },
                    transmissionMode = state.transmissionMode
                )
            }

            // Numbers
            stickyHeader {
                Surface(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .clip(
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Text(
                        "Numbers",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(
                                start = 4.dp
                            )
                    )
                }
            }
            items(('0'..'9').toList()) { char ->
                DictionaryItem(
                    char = char,
                    morseCode = eventHandler(DictionaryEvent.GetMorseCode(char))?: "",
                    isActive = state.activeCharacter == char||state.activeCharacter==null,
                    onTransmit = { mode ->
                        eventHandler(DictionaryEvent.SendMorseCode(context, mode, char))
                    },
                    onStop = {
                        eventHandler(DictionaryEvent.StopTransmission)
                    },
                    transmissionMode = state.transmissionMode
                )
            }

            // Special characters
            stickyHeader {
                Surface(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .clip(
                            shape = RoundedCornerShape(4.dp)
                        )
                ) {
                    Text(
                        "Special Characters",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .padding(
                                start = 4.dp
                            )
                    )
                }
            }
            val specialChars = ".,?/@!()=+-&:;\$\"_"
            items(specialChars.toList()) { char ->
                DictionaryItem(
                    char = char,
                    morseCode = eventHandler(DictionaryEvent.GetMorseCode(char))?:"",
                    isActive = state.activeCharacter == char||state.activeCharacter==null,
                    onTransmit = { mode ->
                        eventHandler(DictionaryEvent.SendMorseCode(context, mode, char))
                    },
                    onStop = {
                        eventHandler(DictionaryEvent.StopTransmission)
                    },
                    transmissionMode = state.transmissionMode
                )
            }
        }
    }
}
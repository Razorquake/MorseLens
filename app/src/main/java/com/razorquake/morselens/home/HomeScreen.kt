package com.razorquake.morselens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.razorquake.morselens.R

@Composable
fun HomeScreen(
    onMorseCodeTranslator: () -> Unit,
    onFlashDetector: () -> Unit,
    onDictionary: () -> Unit,
    onSettings: () -> Unit
) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text("Home", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = {onSettings()},
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings Icon"
                    )
                }
            }
            ListItem(
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .offset((-4).dp, (-4).dp) // Shift the icon up and left
                    ) {
                        // Shadow box
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .shadow(8.dp, RoundedCornerShape(12.dp))
                                .background(colorResource(R.color.input_background))
                        )

                        // Icon box
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colorResource(R.color.input_background)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.morse_code_svgrepo_com),
                                contentDescription = "Sign Language Icon",
                                tint = colorResource(R.color.text_title),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                },
                headlineContent = {Text("Morse Code Translator", style = MaterialTheme.typography.bodyLarge)},
                supportingContent = {Text("Translate Text to Morse Code", style = MaterialTheme.typography.bodyMedium)},
                modifier = Modifier.clickable { onMorseCodeTranslator() }
            )
            ListItem(
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .offset((-4).dp, (-4).dp) // Shift the icon up and left
                    ) {
                        // Shadow box
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .shadow(8.dp, RoundedCornerShape(12.dp))
                                .background(colorResource(R.color.input_background))
                        )

                        // Icon box
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colorResource(R.color.input_background)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.outline_camera_enhance_24),
                                contentDescription = "Sign Language Icon",
                                tint = colorResource(R.color.text_title),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                },
                headlineContent = {Text("Flash Detector", style = MaterialTheme.typography.bodyLarge)},
                supportingContent = {Text("Translate Morse Code to Text", style = MaterialTheme.typography.bodyMedium)},
                modifier = Modifier.clickable { onFlashDetector() }
            )
            ListItem(
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .offset((-4).dp, (-4).dp) // Shift the icon up and left
                    ) {
                        // Shadow box
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .shadow(8.dp, RoundedCornerShape(12.dp))
                                .background(colorResource(R.color.input_background))
                        )

                        // Icon box
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colorResource(R.color.input_background)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.book),
                                contentDescription = "Sign Language Icon",
                                tint = colorResource(R.color.text_title),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                },
                headlineContent = {Text("Dictionary", style = MaterialTheme.typography.bodyLarge)},
                supportingContent = {Text("Learn Morse Code", style = MaterialTheme.typography.bodyMedium)},
                modifier = Modifier.clickable {
                    onDictionary()
                }
            )
        }
}
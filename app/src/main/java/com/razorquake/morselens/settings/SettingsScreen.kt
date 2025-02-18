package com.razorquake.morselens.settings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.razorquake.morselens.data.PreferencesManager

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val unitTime = viewModel.unitTime.collectAsState().value
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Slider(
            value = unitTime.toFloat(),
            onValueChange = {viewModel.updateUnitTime(it.toLong()) },
            valueRange = 100f..450f,
        )
        Text(text = "Unit Time: ${unitTime} ms")
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(viewModel = SettingsViewModel(preferencesManager  = PreferencesManager(context = LocalContext.current)))
}
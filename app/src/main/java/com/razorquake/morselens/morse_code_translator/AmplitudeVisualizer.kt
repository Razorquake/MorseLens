package com.razorquake.morselens.morse_code_translator

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import com.razorquake.morselens.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Composable
fun WaveformVisualizer(
    rmsHistory: List<Float>,
    isListening: Boolean,
    modifier: Modifier = Modifier,
    barColor: Color = Color.White
) {
    val barWidth = 4.dp
    val spacing = 2.dp

    Row(
        modifier = modifier
            .height(60.dp)
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .background(Color.Black, shape = RoundedCornerShape(15.dp))
            .border(width =1.dp, color =colorResource(R.color.input_background), shape = RoundedCornerShape(15.dp) ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        rmsHistory.forEachIndexed { index, rms ->
            // Calculate height based on position and RMS value
            val heightMultiplier = remember(index) {
                // Create a smoother wave pattern
                kotlin.math.sin(index * (Math.PI / rmsHistory.size)).toFloat() * 0.5f + 0.5f
            }
            // Animate the height of each bar
            val targetHeight = if (isListening) {
                val normalizedRms = (abs(rms) / 10f).coerceIn(0f, 1f)
                normalizedRms *heightMultiplier * 60.dp.value
            } else {
                0f
            }

            val animatedHeight by animateFloatAsState(
                targetValue = targetHeight,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "height"
            )

            Box(
                modifier = Modifier
                    .padding(horizontal = spacing)
                    .width(barWidth)
                    .height(animatedHeight.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(barColor)
            )
        }
    }
}

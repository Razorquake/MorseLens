package com.razorquake.morselens.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.razorquake.morselens.ui.theme.MorseLensTheme

@Composable
fun CustomSlider(
    value: Long,
    onValueChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    var isDragging by remember { mutableStateOf(false) }

    val offsetHeight by animateFloatAsState(
        targetValue = with(density) { if (isDragging) 36.dp.toPx() else 0.dp.toPx() },
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioLowBouncy
        ), label = "offsetHeight"
    )

    Box(
        modifier = modifier
            .height(66.dp)
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false }
                ) { change, _ ->
                    val xPos = change.position.x
                    val newValue = (xPos / size.width) *
                            (valueRange.endInclusive - valueRange.start) + valueRange.start
                    onValueChange(newValue.coerceIn(valueRange).toLong())
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {

            val activeWidth = ((value - valueRange.start)
                    / (valueRange.endInclusive - valueRange.start)) * size.width
            val midPoint = size.height / 2
            val curveHeight = midPoint - offsetHeight
            val beyondBounds = size.width * 2
            val ramp = with(density) { 72.dp.toPx() }

            val path = Path().apply {
                moveTo(beyondBounds, midPoint)
                lineTo(activeWidth + ramp, midPoint)
                cubicTo(
                    x1 = activeWidth + (ramp / 2),
                    y1 = midPoint,
                    x2 = activeWidth + (ramp / 2),
                    y2 = curveHeight,
                    x3 = activeWidth,
                    y3 = curveHeight
                )
                cubicTo(
                    x1 = activeWidth - (ramp / 2),
                    y1 = curveHeight,
                    x2 = activeWidth - (ramp / 2),
                    y2 = midPoint,
                    x3 = activeWidth - ramp,
                    y3 = midPoint
                )
                lineTo(-beyondBounds, midPoint)
                lineTo(-beyondBounds, midPoint + 0.1f)
                lineTo(activeWidth - ramp, midPoint + 0.1f)
                cubicTo(
                    x1 = activeWidth - (ramp / 2),
                    y1 = midPoint + 0.1f,
                    x2 = activeWidth - (ramp / 2),
                    y2 = curveHeight + 0.1f,
                    x3 = activeWidth,
                    y3 = curveHeight + 0.1f
                )
                cubicTo(
                    x1 = activeWidth + (ramp / 2),
                    y1 = curveHeight + 0.1f,
                    x2 = activeWidth + (ramp / 2),
                    y2 = midPoint + 0.1f,
                    x3 = activeWidth + ramp,
                    y3 = midPoint + 0.1f
                )
                lineTo(beyondBounds, midPoint + 0.1f)
            }

            val exclude = Path().apply {
                addRect(Rect(-beyondBounds, -beyondBounds, 0f, beyondBounds))
                addRect(Rect(size.width, -beyondBounds, beyondBounds, beyondBounds))
            }

            val trimmedPath = Path().apply {
                op(path, exclude, PathOperation.Difference)
            }

            // Draw active segment
            clipRect(left = -beyondBounds, right = activeWidth) {
                drawPath(
                    trimmedPath,
                    color = Color.White,
                    style = Stroke(
                        10f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round)
                )
            }
            // Draw inactive segment
            clipRect(left = activeWidth, right = beyondBounds) {
                drawPath(
                    trimmedPath,
                    color = Color.Gray.copy(alpha = 0.2f),
                    style = Stroke(10f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round)
                )
            }

                val pathMeasure = PathMeasure().apply { setPath(trimmedPath, false) }
                val graduations = steps + 1
                for (i in 0..graduations) {
                    val pos = pathMeasure.getPosition((i / graduations.toFloat()) * pathMeasure.length/2)
                    if (pos.x.isNaN()) continue
                    if (i == 0 || i == graduations) {
                        drawCircle(
                            color = Color.White,
                            radius = 10f,
                            center = pos
                        )
                    } else {
                        drawLine(
                            color = Color.White,
                            start = pos + Offset(0f, 10f),
                            end = pos + Offset(0f, -10f),
                            strokeWidth = if (pos.x < activeWidth) 4f else 2f
                        )
                    }
                }


            drawCircle(
                color = Color(0xFFFFD700), // Yellow color
                radius = 20.dp.toPx(),
                center = Offset(activeWidth, curveHeight)
            )

            val text = textMeasurer.measure(value.toString(), style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ))
            drawText(
                textLayoutResult = text,
                topLeft = Offset(
                    activeWidth-text.size.width/2,
                    curveHeight - text.size.height/2
                )
            )
        }
    }
}


@Preview()
@Preview(showBackground = true, uiMode = 3)
@Composable
fun CustomSliderPreview(){
    MorseLensTheme {
            CustomSlider(value = 1, onValueChange = {})

    }
}
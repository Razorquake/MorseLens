package com.razorquake.morselens.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffset
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.razorquake.morselens.Screen

@Composable
fun AnimatedNavigationRail(
    buttons: List<Screen>,
    selectedItem: Int,
    onItemClick: (Int) -> Unit,
    railColor: Color = MaterialTheme.colorScheme.surface,
    circleColor: Color = Color(0xFFFFD700),
    selectedColor: Color = Color.Black,
    unselectedColor: Color = Color(0xFFFFD700)
) {
    val circleRadius = 26.dp
    val navigationBarsInsets = WindowInsets.navigationBars
    val cutoutInsets = WindowInsets.displayCutout

    // Get left inset values
    val navigationBarsLeft = with(LocalDensity.current) {
        navigationBarsInsets.getLeft(this, LayoutDirection.Ltr)
    }
    val cutoutLeft = with(LocalDensity.current) {
        cutoutInsets.getLeft(this, LayoutDirection.Ltr)
    }

    // Create custom WindowInsets for left side only
    val leftInsets = remember(navigationBarsLeft, cutoutLeft) {
        if (navigationBarsLeft > 0 && cutoutLeft > 0) {
            // If both affect the left, use the larger one
            val maxLeft = maxOf(navigationBarsLeft, cutoutLeft)
            WindowInsets(left = maxLeft)
        } else if (navigationBarsLeft > 0) {
            WindowInsets(left = navigationBarsLeft)
        } else if (cutoutLeft > 0) {
            WindowInsets(left = cutoutLeft)
        } else {
            WindowInsets(0)
        }
    }

    var railSize by remember {
        mutableStateOf(IntSize(0, 0))
    }

    val offsetStep = remember(railSize) {
        railSize.height.toFloat() / (buttons.size * 2)
    }

    val offset = remember(selectedItem, offsetStep) {
        offsetStep + selectedItem * 2 * offsetStep
    }

    val circleRadiusPx = LocalDensity.current.run { circleRadius.toPx().toInt() }
    val offsetTransition = updateTransition(offset)
    val animation = spring<Float>(dampingRatio = 0.5f, stiffness = Spring.StiffnessVeryLow)
    val cutoutOffset by offsetTransition.animateFloat(
        transitionSpec = {
            if (this.initialState == 0f)
                snap()
            else
                animation
        }
    ) { it }
    val circleOffset by offsetTransition.animateIntOffset(
        transitionSpec = {
            if (this.initialState == 0f)
                snap()
            else
                spring(animation.dampingRatio, animation.stiffness)
        }
    ) {
        IntOffset(railSize.width - circleRadiusPx, it.toInt() - circleRadiusPx)
    }
    val railShape = remember(cutoutOffset) {
        RailShape(
            offset = cutoutOffset,
            circleRadius = circleRadius,
            cornerRadius = 25.dp,
        )
    }
    // Add combined system insets
    Box{
        Circle(
            modifier = Modifier
                .offset { circleOffset }
                .zIndex(1f),
            color = circleColor,
            radius = circleRadius,
            button = buttons[selectedItem],
            iconColor = selectedColor,
        )

        Column(
            modifier = Modifier
                .onPlaced { railSize = it.size }
                .graphicsLayer {
                    shape = railShape
                    clip = true
                }
                .fillMaxHeight()
                .background(railColor)
                .windowInsetsPadding(leftInsets),
            verticalArrangement = Arrangement.SpaceAround,
        ) {
            buttons.forEachIndexed { index, button ->
                val isSelected = index == selectedItem
                NavigationRailItem(
                    selected = isSelected,
                    onClick = { onItemClick(index) },
                    icon = {
                        val iconAlpha by animateFloatAsState(
                            targetValue = if (isSelected) 0f else 1f,
                            label = "Navbar item icon"
                        )
                        Icon(
                            painterResource(button.icon),
                            contentDescription = button.text,
                            modifier = Modifier.alpha(iconAlpha)
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        unselectedIconColor = unselectedColor,
                        unselectedTextColor = unselectedColor,
                        indicatorColor = Color.Transparent,
                    )
                )
            }
        }
    }
}

private class RailShape(
    private val offset: Float,
    private val circleRadius: Dp,
    private val cornerRadius: Dp,
    private val circleGap: Dp = 5.dp,
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(getPath(size, density))
    }

    private fun getPath(size: Size, density: Density): Path {
        val cutoutCenterY = offset
        val cutoutRadius = density.run { (circleRadius + circleGap).toPx() }
        val cornerRadiusPx = density.run { cornerRadius.toPx() }
        val cornerDiameter = cornerRadiusPx * 2

        return Path().apply {
            val cutoutEdgeOffset = cutoutRadius * 1.5f
            val cutoutTopY = cutoutCenterY - cutoutEdgeOffset
            val cutoutBottomY = cutoutCenterY + cutoutEdgeOffset

            // top left
            moveTo(x = 0f, y = 0f)

            // top right (with potential corner)
            lineTo(x = size.width - cornerRadiusPx, y = 0f)
            arcTo(
                rect = Rect(
                    left = size.width - cornerDiameter,
                    top = 0f,
                    right = size.width,
                    bottom = cornerDiameter
                ),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // right side before cutout
            lineTo(x = size.width, y = cutoutTopY)

            // cutout
            cubicTo(
                x1 = size.width,
                y1 = cutoutCenterY - cutoutRadius,
                x2 = size.width - cutoutRadius,
                y2 = cutoutCenterY - cutoutRadius,
                x3 = size.width - cutoutRadius,
                y3 = cutoutCenterY,
            )
            cubicTo(
                x1 = size.width - cutoutRadius,
                y1 = cutoutCenterY + cutoutRadius,
                x2 = size.width,
                y2 = cutoutCenterY + cutoutRadius,
                x3 = size.width,
                y3 = cutoutBottomY,
            )

            // right side after cutout
            lineTo(x = size.width, y = size.height - cornerRadiusPx)

            // bottom right corner
            arcTo(
                rect = Rect(
                    left = size.width - cornerDiameter,
                    top = size.height - cornerDiameter,
                    right = size.width,
                    bottom = size.height
                ),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // bottom side
            lineTo(x = cornerRadiusPx, y = size.height)

            // bottom left corner
            arcTo(
                rect = Rect(
                    left = 0f,
                    top = size.height - cornerDiameter,
                    right = cornerDiameter,
                    bottom = size.height
                ),
                startAngleDegrees = 90f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )

            // left side
            lineTo(x = 0f, y = 0f)

            close()
        }
    }
}
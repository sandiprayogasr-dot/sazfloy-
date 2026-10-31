package com.example.editor.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ColorWheelTonal
import com.example.model.ColorWheelsData
import com.example.ui.theme.SazfloyAmber
import com.example.ui.theme.SazfloyBackground
import com.example.ui.theme.SazfloyBorder
import com.example.ui.theme.SazfloyCyan
import com.example.ui.theme.SazfloyPink
import com.example.ui.theme.SazfloySurface
import com.example.ui.theme.SazfloySurfaceElevated
import com.example.ui.theme.SazfloySurfaceVariant
import com.example.ui.theme.SazfloyTextMuted
import com.example.ui.theme.SazfloyTextPrimary
import com.example.ui.theme.SazfloyTextSecondary
import com.example.ui.theme.SazfloyToska
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

enum class WheelTonalRange(val title: String, val subtitle: String, val accentColor: Color) {
    LIFT("Lift", "Shadows", Color(0xFF64B5F6)),
    GAMMA("Gamma", "Midtones", Color(0xFF81C784)),
    GAIN("Gain", "Highlights", Color(0xFFFFB74D)),
    OFFSET("Offset", "Master", Color(0xFFBA68C8))
}

@Composable
fun DaVinciColorWheelsComponent(
    colorWheels: ColorWheelsData,
    onUpdateWheels: (ColorWheelsData) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeRange by remember { mutableStateOf(WheelTonalRange.GAMMA) }

    val currentTonal = when (activeRange) {
        WheelTonalRange.LIFT -> colorWheels.lift
        WheelTonalRange.GAMMA -> colorWheels.gamma
        WheelTonalRange.GAIN -> colorWheels.gain
        WheelTonalRange.OFFSET -> colorWheels.offset
    }

    fun updateCurrentTonal(newTonal: ColorWheelTonal) {
        val updated = when (activeRange) {
            WheelTonalRange.LIFT -> colorWheels.copy(lift = newTonal)
            WheelTonalRange.GAMMA -> colorWheels.copy(gamma = newTonal)
            WheelTonalRange.GAIN -> colorWheels.copy(gain = newTonal)
            WheelTonalRange.OFFSET -> colorWheels.copy(offset = newTonal)
        }
        onUpdateWheels(updated)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("davinci_color_wheels_component"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Range Switcher Tabs (Lift, Gamma, Gain, Offset)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            WheelTonalRange.values().forEach { range ->
                val isSel = activeRange == range
                val tonalData = when (range) {
                    WheelTonalRange.LIFT -> colorWheels.lift
                    WheelTonalRange.GAMMA -> colorWheels.gamma
                    WheelTonalRange.GAIN -> colorWheels.gain
                    WheelTonalRange.OFFSET -> colorWheels.offset
                }
                val hasAdjustment = tonalData.saturation > 0.01f || Math.abs(tonalData.luminance) > 0.01f

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) range.accentColor.copy(alpha = 0.25f) else SazfloySurfaceVariant)
                        .border(
                            1.dp,
                            if (isSel) range.accentColor else if (hasAdjustment) range.accentColor.copy(alpha = 0.5f) else Color.Transparent,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { activeRange = range }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = range.title,
                            color = if (isSel) Color.White else SazfloyTextPrimary,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp
                        )
                        Text(
                            text = range.subtitle,
                            color = if (isSel) range.accentColor else SazfloyTextMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            // Quick Reset Current Wheel Button
            IconButton(
                onClick = { updateCurrentTonal(ColorWheelTonal()) },
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(SazfloySurfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Wheel",
                    tint = SazfloyTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Active Color Wheel & Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SazfloySurfaceElevated, RoundedCornerShape(12.dp))
                .border(1.dp, SazfloyBorder, RoundedCornerShape(12.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Interactive 2D Wheel Canvas
            InteractiveColorWheelDisc(
                tonal = currentTonal,
                accentColor = activeRange.accentColor,
                onPuckMoved = { hue, sat ->
                    updateCurrentTonal(currentTonal.copy(hueDegrees = hue, saturation = sat))
                },
                modifier = Modifier.size(120.dp)
            )

            // Right side: Luminance Master Slider & RGB Readouts
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${activeRange.title} Master Y",
                        style = MaterialTheme.typography.labelSmall,
                        color = activeRange.accentColor,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "%+.2f".format(currentTonal.luminance),
                        style = MaterialTheme.typography.labelSmall,
                        color = SazfloyTextPrimary,
                        fontSize = 10.sp
                    )
                }

                Slider(
                    value = currentTonal.luminance,
                    onValueChange = { updateCurrentTonal(currentTonal.copy(luminance = it)) },
                    valueRange = -1.0f..1.0f,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = activeRange.accentColor,
                        activeTrackColor = activeRange.accentColor,
                        inactiveTrackColor = SazfloySurfaceVariant
                    )
                )

                // Numeric readouts
                val rad = Math.toRadians(currentTonal.hueDegrees.toDouble())
                val rShift = (cos(rad) * currentTonal.saturation * 100).toInt()
                val bShift = (sin(rad) * currentTonal.saturation * 100).toInt()
                val gShift = (-0.5 * (cos(rad) + sin(rad)) * currentTonal.saturation * 100).toInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TonalRgbTag("R", rShift, SazfloyPink)
                    TonalRgbTag("G", gShift, SazfloyToska)
                    TonalRgbTag("B", bShift, SazfloyCyan)
                }
            }
        }
    }
}

@Composable
private fun TonalRgbTag(label: String, value: Int, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$label: %s%d".format(if (value > 0) "+" else "", value),
            color = color,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun InteractiveColorWheelDisc(
    tonal: ColorWheelTonal,
    accentColor: Color,
    onPuckMoved: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .testTag("color_wheel_disc")
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val center = size.width / 2f
                    val dx = offset.x - center
                    val dy = offset.y - center
                    val distance = sqrt(dx * dx + dy * dy)
                    val maxRadius = center - 8f

                    val sat = (distance / maxRadius).coerceIn(0f, 1f)
                    var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    if (angleDeg < 0) angleDeg += 360f

                    onPuckMoved(angleDeg, sat)
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val center = size.width / 2f
                    val dx = change.position.x - center
                    val dy = change.position.y - center
                    val distance = sqrt(dx * dx + dy * dy)
                    val maxRadius = center - 8f

                    val sat = (distance / maxRadius).coerceIn(0f, 1f)
                    var angleDeg = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                    if (angleDeg < 0) angleDeg += 360f

                    onPuckMoved(angleDeg, sat)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.width / 2f) - 6f

            // Outer ring
            drawCircle(
                color = SazfloyBorder,
                radius = radius,
                center = center,
                style = Stroke(width = 3f)
            )

            // Crosshairs
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(center.x - radius, center.y),
                end = Offset(center.x + radius, center.y),
                strokeWidth = 1f
            )
            drawLine(
                color = Color.White.copy(alpha = 0.15f),
                start = Offset(center.x, center.y - radius),
                end = Offset(center.x, center.y + radius),
                strokeWidth = 1f
            )

            // Inner circle reference (50% sat)
            drawCircle(
                color = Color.White.copy(alpha = 0.08f),
                radius = radius * 0.5f,
                center = center,
                style = Stroke(width = 1f)
            )

            // Calculate Puck position from hue & sat
            val rad = Math.toRadians(tonal.hueDegrees.toDouble())
            val puckDist = tonal.saturation * radius
            val puckX = (center.x + cos(rad) * puckDist).toFloat()
            val puckY = (center.y + sin(rad) * puckDist).toFloat()

            // Connecting vector line from center to puck
            if (tonal.saturation > 0.02f) {
                drawLine(
                    color = accentColor.copy(alpha = 0.6f),
                    start = center,
                    end = Offset(puckX, puckY),
                    strokeWidth = 2f
                )
            }

            // Puck shadow & outer ring
            drawCircle(
                color = Color.Black.copy(alpha = 0.5f),
                radius = 8.dp.toPx(),
                center = Offset(puckX, puckY)
            )
            drawCircle(
                color = accentColor,
                radius = 6.dp.toPx(),
                center = Offset(puckX, puckY)
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(puckX, puckY)
            )
        }
    }
}

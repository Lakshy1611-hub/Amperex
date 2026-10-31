package com.amperex.batteryintelligence.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amperex.batteryintelligence.domain.model.ChargingStatus
import com.amperex.batteryintelligence.ui.theme.AmberWarning
import com.amperex.batteryintelligence.ui.theme.CrimsonAlert
import com.amperex.batteryintelligence.ui.theme.ElectricLime
import com.amperex.batteryintelligence.ui.theme.MintAccent

@Composable
fun BatteryLevelRing(
    percentage: Int,
    status: ChargingStatus,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    strokeWidth: Dp = 14.dp
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage.coerceIn(0, 100).toFloat(),
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "BatteryRingAnimation"
    )

    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.62f)
    val ringColor = when {
        status == ChargingStatus.CHARGING -> MintAccent
        percentage <= 15 -> CrimsonAlert
        percentage <= 25 -> AmberWarning
        else -> ElectricLime
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            // Background track
            drawArc(
                color = trackColor,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Active percentage arc
            val sweep = (animatedPercentage / 100f) * 270f
            if (sweep > 0f) {
                drawArc(
                    color = ringColor,
                    startAngle = 135f,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when (status) {
                    ChargingStatus.CHARGING -> "CHARGING"
                    ChargingStatus.DISCHARGING -> "DISCHARGING"
                    ChargingStatus.FULL -> "FULL CHARGE"
                    ChargingStatus.NOT_CHARGING -> "NOT CHARGING"
                    ChargingStatus.UNKNOWN -> "MONITORING"
                },
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp),
                color = ringColor
            )
        }
    }
}

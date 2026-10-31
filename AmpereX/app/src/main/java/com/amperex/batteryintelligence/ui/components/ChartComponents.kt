package com.amperex.batteryintelligence.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amperex.batteryintelligence.domain.model.BatterySample
import com.amperex.batteryintelligence.domain.model.ChargingStatus
import com.amperex.batteryintelligence.ui.theme.ElectricLime
import com.amperex.batteryintelligence.ui.theme.GraphiteCard
import com.amperex.batteryintelligence.ui.theme.GraphiteCardBorder
import com.amperex.batteryintelligence.ui.theme.MintAccent
import com.amperex.batteryintelligence.ui.theme.TextMuted
import com.amperex.batteryintelligence.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InteractiveBatteryChart(
    samples: List<BatterySample>,
    modifier: Modifier = Modifier,
    height: Int = 220
) {
    if (samples.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(GraphiteCard)
                .border(1.dp, GraphiteCardBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No recorded observations in this timeframe",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "AmpereX only displays actual recorded data.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted.copy(alpha = 0.7f)
                )
            }
        }
        return
    }

    val sorted = remember(samples) { samples.sortedBy { it.timestamp } }
    val minTime = sorted.first().timestamp
    val maxTime = sorted.last().timestamp
    val timeSpan = (maxTime - minTime).coerceAtLeast(1000L)

    var inspectedIndex by remember { mutableStateOf<Int?>(null) }
    val inspectedSample = inspectedIndex?.let { sorted.getOrNull(it) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GraphiteCard)
            .border(1.dp, GraphiteCardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Inspection HUD Header
        if (inspectedSample != null) {
            val sdf = remember { SimpleDateFormat("HH:mm:ss • MMM dd", Locale.getDefault()) }
            val timeStr = sdf.format(Date(inspectedSample.timestamp))
            val prevSample = if (inspectedIndex!! > 0) sorted[inspectedIndex!! - 1] else null
            val delta = if (prevSample != null) inspectedSample.percentage - prevSample.percentage else null

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "${inspectedSample.percentage}%",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (inspectedSample.status == ChargingStatus.CHARGING) MintAccent else ElectricLime
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = inspectedSample.status.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (inspectedSample.status == ChargingStatus.CHARGING) MintAccent else TextSecondary
                    )
                    if (delta != null) {
                        val sign = if (delta > 0) "+$delta%" else "$delta%"
                        Text(
                            text = "Step: $sign",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (delta > 0) MintAccent else TextMuted
                        )
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOUCH & DRAG TO SCRUB",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
                Text(
                    text = "${sorted.size} observations",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Canvas Graph Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height((height - 70).dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(sorted) {
                        detectTapGestures(
                            onPress = { offset ->
                                val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                val targetTime = minTime + (fraction * timeSpan).toLong()
                                inspectedIndex = sorted.indices.minByOrNull {
                                    kotlin.math.abs(sorted[it].timestamp - targetTime)
                                }
                            }
                        )
                    }
                    .pointerInput(sorted) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                val targetTime = minTime + (fraction * timeSpan).toLong()
                                inspectedIndex = sorted.indices.minByOrNull {
                                    kotlin.math.abs(sorted[it].timestamp - targetTime)
                                }
                            },
                            onDrag = { change, _ ->
                                val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                val targetTime = minTime + (fraction * timeSpan).toLong()
                                inspectedIndex = sorted.indices.minByOrNull {
                                    kotlin.math.abs(sorted[it].timestamp - targetTime)
                                }
                            }
                        )
                    }
            ) {
                val w = size.width
                val h = size.height

                // Draw horizontal guide lines (0%, 25%, 50%, 75%, 100%)
                val guideColor = Color(0xFF252529)
                for (pct in listOf(0, 25, 50, 75, 100)) {
                    val y = h - (pct / 100f * h)
                    drawLine(
                        color = guideColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Plot path
                val path = Path()
                val points = sorted.map { sample ->
                    val x = ((sample.timestamp - minTime).toFloat() / timeSpan.toFloat()) * w
                    val y = h - (sample.percentage / 100f * h)
                    Offset(x, y)
                }

                if (points.isNotEmpty()) {
                    path.moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        path.lineTo(points[i].x, points[i].y)
                    }
                    drawPath(
                        path = path,
                        color = ElectricLime,
                        style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Draw data nodes
                    points.forEachIndexed { idx, pt ->
                        val isChg = sorted[idx].status == ChargingStatus.CHARGING
                        drawCircle(
                            color = if (isChg) MintAccent else ElectricLime,
                            radius = if (inspectedIndex == idx) 6.dp.toPx() else 3.dp.toPx(),
                            center = pt
                        )
                    }
                }

                // Draw scrub indicator line
                if (inspectedIndex != null && inspectedIndex!! in points.indices) {
                    val scrubPt = points[inspectedIndex!!]
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(scrubPt.x, 0f),
                        end = Offset(scrubPt.x, h),
                        strokeWidth = 1.5.dp.toPx()
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 7.dp.toPx(),
                        center = scrubPt
                    )
                }
            }
        }
    }
}

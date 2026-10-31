package com.amperex.batteryintelligence.ui.charging

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.ElectricMeter
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amperex.batteryintelligence.domain.model.ChargingSession
import com.amperex.batteryintelligence.domain.model.PlugType
import com.amperex.batteryintelligence.ui.components.DisclaimerBanner
import com.amperex.batteryintelligence.ui.components.MetricCard
import com.amperex.batteryintelligence.ui.theme.ElectricLime
import com.amperex.batteryintelligence.ui.theme.MaterialTheme.colorScheme.surface
import com.amperex.batteryintelligence.ui.theme.MaterialTheme.colorScheme.outline
import com.amperex.batteryintelligence.ui.theme.MintAccent
import com.amperex.batteryintelligence.ui.theme.TextMuted
import com.amperex.batteryintelligence.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChargingScreen(
    viewModel: ChargingViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val session = state.activeSession
    val pred = state.prediction

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Charging Analytics",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Real-time intake rate, session duration, and completion predictions",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Active Session Status Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.BatteryChargingFull,
                            contentDescription = null,
                            tint = if (state.isCharging) MintAccent else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.isCharging) "ACTIVE CHARGING SESSION" else "STANDBY / UNPLUGGED",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (state.isCharging) MintAccent else TextSecondary
                        )
                    }

                    val plugName = when (state.currentSample?.plugType) {
                        PlugType.AC -> "AC Fast"
                        PlugType.USB -> "USB Port"
                        PlugType.WIRELESS -> "Wireless Qi"
                        else -> "None"
                    }
                    Text(
                        text = plugName,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "${state.currentSample?.percentage ?: 0}%",
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Current Battery Level",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    if (session != null) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "+${session.percentageGain}%",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MintAccent
                            )
                            Text(
                                text = "Gained since ${session.startPercentage}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                    }
                }

                if (pred?.explanation != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = pred.explanation,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 16.sp),
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Time to 80% & Time to Full Cards
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val to80Text = when {
                !state.isCharging -> "Not charging"
                pred?.estimatedMinutesTo80 == 0L -> "Reached 80%"
                pred?.estimatedMinutesTo80 != null -> "${pred.estimatedMinutesTo80} min"
                else -> "Measuring..."
            }
            MetricCard(
                title = "Est. Time to 80%",
                value = to80Text,
                subtitle = "Optimal Li-ion health target",
                icon = Icons.Rounded.HourglassBottom,
                accentColor = MintAccent,
                modifier = Modifier.weight(1f)
            )

            val toFullText = when {
                !state.isCharging -> "Not charging"
                pred?.estimatedMinutesToFull == 0L -> "100% Full"
                pred?.estimatedMinutesToFull != null -> {
                    val hrs = pred.estimatedMinutesToFull / 60
                    val mins = pred.estimatedMinutesToFull % 60
                    if (hrs > 0) "${hrs}h ${mins}m" else "${mins}m"
                }
                else -> "Measuring..."
            }
            MetricCard(
                title = "Est. Time to Full",
                value = toFullText,
                subtitle = "Includes 80-100% taper",
                icon = Icons.Rounded.Bolt,
                accentColor = ElectricLime,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Rate & Wattage Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val rateStr = if (pred != null && pred.observedRatePerHour > 0f) {
                "${String.format("%.1f", pred.observedRatePerHour)}%/hr"
            } else "N/A"
            MetricCard(
                title = "Charging Rate",
                value = rateStr,
                subtitle = "Observed rate of gain",
                icon = Icons.Rounded.Speed,
                modifier = Modifier.weight(1f)
            )

            val currStr = state.currentSample?.currentMa?.let { "$it mA" } ?: "Not supported"
            MetricCard(
                title = "Intake Current",
                value = currStr,
                subtitle = "BatteryManager reading",
                icon = Icons.Rounded.ElectricMeter,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Historical Charging Sessions Header
        Text(
            text = "Recent Charging Sessions",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (state.recentSessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No charging sessions recorded yet. Connect a charger to track real sessions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.recentSessions.forEach { item ->
                    ChargingSessionItem(item)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        DisclaimerBanner(
            text = "Cycle Count Note: A partial charge session is not 1 full cycle. A standard battery cycle equals 100% cumulative discharge/recharge."
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun ChargingSessionItem(session: ChargingSession) {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    val dateStr = sdf.format(Date(session.startTime))
    val durationMins = (session.durationMs / 60_000L).coerceAtLeast(1L)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = dateStr,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${session.plugType.name} • ${durationMins} minutes",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            val gain = session.percentageGain
            Text(
                text = if (gain >= 0) "+$gain%" else "$gain%",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (gain > 0) MintAccent else TextSecondary
            )
            Text(
                text = "${session.startPercentage}% → ${session.endPercentage ?: session.currentPercentage}%",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
        }
    }
}

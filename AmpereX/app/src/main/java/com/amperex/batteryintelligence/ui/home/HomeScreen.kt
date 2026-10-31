package com.amperex.batteryintelligence.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DeviceThermostat
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amperex.batteryintelligence.domain.model.ChargingStatus
import com.amperex.batteryintelligence.domain.model.EstimationConfidence
import com.amperex.batteryintelligence.domain.model.PlugType
import com.amperex.batteryintelligence.ui.components.BatteryLevelRing
import com.amperex.batteryintelligence.ui.components.DisclaimerBanner
import com.amperex.batteryintelligence.ui.components.MetricCard
import com.amperex.batteryintelligence.ui.theme.BlueAccent
import com.amperex.batteryintelligence.ui.theme.ElectricLime
import com.amperex.batteryintelligence.ui.theme.MintAccent
import com.amperex.batteryintelligence.ui.theme.VioletAccent
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onOpenSettings: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val sample = state.currentSample

    // Refresh while this screen is visible. Live battery broadcasts update the UI between polls.
    LaunchedEffect(viewModel) {
        while (true) {
            delay(30_000L)
            viewModel.refreshBatteryData(force = false)
        }
    }

    val pageBrush = if (MaterialTheme.colorScheme.background == androidx.compose.ui.graphics.Color(0xFF080B16)) {
        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.background))
    } else {
        Brush.verticalGradient(listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)))
    }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val updateTimeStr = timeFormat.format(Date(state.lastRefreshedMillis))
    val prediction = state.backupPrediction
    val predictionText = prediction?.estimatedMinutesRemaining?.let {
        "${it / 60}h ${it % 60}m"
    } ?: "Learning your usage"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(pageBrush)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Brush.linearGradient(listOf(ElectricLime, VioletAccent))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.BatteryChargingFull, contentDescription = null, tint = androidx.compose.ui.graphics.Color(0xFF08121D), modifier = Modifier.size(25.dp))
                }
                Spacer(Modifier.width(11.dp))
                Column {
                    Text("AMPEREX", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp))
                    Text("BATTERY INTELLIGENCE", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.1.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 10.dp, end = 2.dp)) {
                    Box(Modifier.size(7.dp).clip(CircleShape).background(MintAccent))
                    Spacer(Modifier.width(6.dp))
                    Text("LIVE", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    IconButton(onClick = { viewModel.refreshBatteryData(force = true) }) {
                        if (state.isRefreshing) CircularProgressIndicator(Modifier.size(17.dp), strokeWidth = 2.dp)
                        else Icon(Icons.Rounded.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        Spacer(Modifier.height(26.dp))

        // Premium battery hero panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(Brush.linearGradient(listOf(ColorStart(), ColorEnd())))
                .padding(vertical = 22.dp, horizontal = 18.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.BatteryChargingFull, contentDescription = null, tint = ElectricLime, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(7.dp))
                    Text("BATTERY OVERVIEW", style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold), color = androidx.compose.ui.graphics.Color(0xFFB8C8E4))
                }
                Spacer(Modifier.height(15.dp))
                BatteryLevelRing(
                    percentage = sample?.percentage ?: 0,
                    status = sample?.status ?: ChargingStatus.UNKNOWN,
                    size = 214.dp,
                    strokeWidth = 15.dp
                )
                Spacer(Modifier.height(13.dp))
                Text(
                    text = when (sample?.status) {
                        ChargingStatus.CHARGING -> "Power flowing in"
                        ChargingStatus.DISCHARGING -> "Running on battery"
                        ChargingStatus.FULL -> "Fully charged"
                        ChargingStatus.NOT_CHARGING -> "Connected · not charging"
                        else -> "Waiting for battery signal"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = androidx.compose.ui.graphics.Color(0xFFD5E1F5)
                )
                Spacer(Modifier.height(17.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeroStat("STATUS", sample?.status?.name?.replace("_", " ")?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Unknown")
                    Box(Modifier.width(1.dp).height(30.dp).background(androidx.compose.ui.graphics.Color.White.copy(alpha = 0.16f)))
                    HeroStat("TEMPERATURE", sample?.temperatureCelsius?.let { "${"%.1f".format(it)}°C" } ?: "--")
                }
            }
        }

        Spacer(Modifier.height(19.dp))

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column {
                Text("Power insights", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                Text("A live snapshot of your device", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.11f), shape = RoundedCornerShape(50)) {
                Text("AUTO SYNC", modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(12.dp))

        MetricCard(
            title = "Estimated backup",
            value = predictionText,
            subtitle = prediction?.methodologyNote ?: "Building a discharge pattern from your device",
            icon = Icons.Rounded.Bolt,
            accentColor = VioletAccent,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            val voltage = sample?.voltageMv?.let { "${"%.2f".format(it / 1000f)} V" } ?: "Unavailable"
            MetricCard("Voltage", voltage, "Battery terminal", Icons.Rounded.Speed, BlueAccent, Modifier.weight(1f))
            val current = sample?.currentMa?.let { "${it} mA" } ?: "Not supported"
            MetricCard("Current", current, if (sample?.status == ChargingStatus.CHARGING) "Charging flow" else "Discharge flow", Icons.Rounded.Power, MintAccent, Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard("Thermal", sample?.temperatureCelsius?.let { "${"%.1f".format(it)}°C" } ?: "Unavailable", "Battery sensor", Icons.Rounded.DeviceThermostat, PinkAccent, Modifier.weight(1f))
            val source = when (sample?.plugType) {
                PlugType.AC -> "AC power"
                PlugType.USB -> "USB"
                PlugType.WIRELESS -> "Wireless"
                PlugType.DOCK -> "Dock"
                PlugType.NONE -> "Unplugged"
                else -> "Unknown"
            }
            MetricCard("Power source", source, "Connection status", Icons.Rounded.BatteryChargingFull, ElectricLime, Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(7.dp))
            Text("Updated $updateTimeStr", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.weight(1f))
            Text("Every 30 sec", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(10.dp))
        DisclaimerBanner(
            text = "AmpereX uses real Android battery readings. Some hardware metrics depend on device support; estimates become more useful as your phone records more usage history."
        )
        Spacer(Modifier.height(26.dp))
    }
}

@Composable
private fun HeroStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp), color = androidx.compose.ui.graphics.Color(0xFF9FB1D0))
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = androidx.compose.ui.graphics.Color.White)
    }
}

@Composable
private fun ColorStart() = androidx.compose.ui.graphics.Color(0xFF172B4D)

@Composable
private fun ColorEnd() = androidx.compose.ui.graphics.Color(0xFF32245B)

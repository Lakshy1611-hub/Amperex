package com.amperex.batteryintelligence.ui.home

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.DeviceThermostat
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amperex.batteryintelligence.domain.model.ChargingStatus
import com.amperex.batteryintelligence.domain.model.EstimationConfidence
import com.amperex.batteryintelligence.domain.model.PlugType
import com.amperex.batteryintelligence.ui.components.BatteryLevelRing
import com.amperex.batteryintelligence.ui.components.DisclaimerBanner
import com.amperex.batteryintelligence.ui.components.MetricCard
import com.amperex.batteryintelligence.ui.theme.ElectricLime
import com.amperex.batteryintelligence.ui.theme.MintAccent
import com.amperex.batteryintelligence.ui.theme.TextMuted
import com.amperex.batteryintelligence.ui.theme.TextSecondary
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AMPEREX",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    ),
                    color = ElectricLime
                )
                Text(
                    text = "Battery Intelligence",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = ElectricLime
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                IconButton(onClick = { viewModel.refreshBatteryData(force = true) }) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh battery reading",
                        tint = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Central Battery Ring Indicator
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            BatteryLevelRing(
                percentage = sample?.percentage ?: 0,
                status = sample?.status ?: ChargingStatus.UNKNOWN
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Backup Runtime Prediction Card
        val prediction = state.backupPrediction
        val predictionText = if (prediction?.estimatedMinutesRemaining != null) {
            val hours = prediction.estimatedMinutesRemaining / 60
            val mins = prediction.estimatedMinutesRemaining % 60
            "${hours}h ${mins}m"
        } else {
            "Collecting data"
        }

        MetricCard(
            title = "Estimated Remaining Runtime",
            value = predictionText,
            subtitle = prediction?.methodologyNote,
            icon = Icons.Rounded.Bolt,
            accentColor = if (prediction?.confidence == EstimationConfidence.HIGH) ElectricLime else MintAccent,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hardware Telemetry Grid (2 columns)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val tempText = sample?.temperatureCelsius?.let { "${String.format("%.1f", it)}°C" } ?: "Unavailable"
            MetricCard(
                title = "Temperature",
                value = tempText,
                subtitle = "Battery thermal sensor",
                icon = Icons.Rounded.DeviceThermostat,
                modifier = Modifier.weight(1f)
            )

            val voltText = sample?.voltageMv?.let { "${String.format("%.2f", it / 1000f)} V" } ?: "Unavailable"
            MetricCard(
                title = "Voltage",
                value = voltText,
                subtitle = "Cell terminal voltage",
                icon = Icons.Rounded.Speed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val currentText = when {
                sample?.currentMa != null -> "${sample.currentMa} mA"
                else -> "Not supported"
            }
            MetricCard(
                title = "Current Flow",
                value = currentText,
                subtitle = if (sample?.status == ChargingStatus.CHARGING) "Charge rate" else "Discharge draw",
                icon = Icons.Rounded.Power,
                modifier = Modifier.weight(1f)
            )

            val plugText = when (sample?.plugType) {
                PlugType.AC -> "AC Fast Charger"
                PlugType.USB -> "USB Port"
                PlugType.WIRELESS -> "Qi Wireless"
                PlugType.DOCK -> "Charging Dock"
                PlugType.NONE -> "Unplugged"
                else -> "Unavailable"
            }
            MetricCard(
                title = "Power Source",
                value = plugText,
                subtitle = "Hardware connection",
                icon = Icons.Rounded.BatteryAlert,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Last Updated Footer
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        val updateTimeStr = timeFormat.format(Date(state.lastRefreshedMillis))

        DisclaimerBanner(
            text = "Last hardware sync: $updateTimeStr. AmpereX reads genuine Android kernel battery telemetry without fabricating values."
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

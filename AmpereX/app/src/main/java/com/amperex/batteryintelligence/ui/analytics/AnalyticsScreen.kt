package com.amperex.batteryintelligence.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.DeviceThermostat
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.TrendingDown
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
import com.amperex.batteryintelligence.ui.components.DisclaimerBanner
import com.amperex.batteryintelligence.ui.components.InteractiveBatteryChart
import com.amperex.batteryintelligence.ui.components.MetricCard
import com.amperex.batteryintelligence.ui.theme.ElectricLime
import com.amperex.batteryintelligence.ui.theme.MaterialTheme.colorScheme.surface
import com.amperex.batteryintelligence.ui.theme.MaterialTheme.colorScheme.outline
import com.amperex.batteryintelligence.ui.theme.MintAccent
import com.amperex.batteryintelligence.ui.theme.TextMuted
import com.amperex.batteryintelligence.ui.theme.TextSecondary

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val summary = state.todaySummary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Battery History & Analytics",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Real observed charge-discharge timeline and calculations",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Time Period Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TimePeriod.values().forEach { period ->
                val isSelected = state.selectedPeriod == period
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) ElectricLime else Color.Transparent)
                        .clickable { viewModel.selectPeriod(period) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = period.label,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (isSelected) Color.Black else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Interactive Battery Timeline Chart
        InteractiveBatteryChart(
            samples = state.samples,
            height = 240
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Daily Battery Report Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Daily Report",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (summary?.isPartialData == true) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "PARTIAL COVERAGE",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Daily Report Stats Grid
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricCard(
                title = "Gross Discharge",
                value = "${summary?.grossDischarge ?: 0}%",
                subtitle = "Sum of all battery drops",
                icon = Icons.Rounded.TrendingDown,
                accentColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Charging Gain",
                value = "+${summary?.grossChargingGain ?: 0}%",
                subtitle = "Sum of charging input",
                icon = Icons.Rounded.BatteryChargingFull,
                accentColor = MintAccent,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val netSign = if ((summary?.netChange ?: 0) > 0) "+${summary?.netChange}%" else "${summary?.netChange ?: 0}%"
            MetricCard(
                title = "Net Change",
                value = netSign,
                subtitle = "Ending % minus Starting %",
                icon = Icons.Rounded.ShowChart,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Charging Sessions",
                value = "${summary?.chargingSessionCount ?: 0}",
                subtitle = "Cable plug events",
                icon = Icons.Rounded.FlashOn,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val rateText = summary?.averageDischargeRatePerHour?.let { "${String.format("%.1f", it)}%/hr" } ?: "Unavailable"
            MetricCard(
                title = "Avg Discharge Rate",
                value = rateText,
                subtitle = "Observed active rate",
                modifier = Modifier.weight(1f)
            )

            val peakText = summary?.peakTemperatureCelsius?.let { "${String.format("%.1f", it)}°C" } ?: "Unavailable"
            MetricCard(
                title = "Peak Temperature",
                value = peakText,
                subtitle = "Maximum thermal point",
                icon = Icons.Rounded.DeviceThermostat,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        DisclaimerBanner(
            text = "Gross discharge separately accounts for all observed downward steps and charging boosts. AmpereX never assumes historical data before installation."
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

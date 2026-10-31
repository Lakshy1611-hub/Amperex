package com.amperex.batteryintelligence.ui.health

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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material.icons.rounded.ReportProblem
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
import com.amperex.batteryintelligence.domain.model.BatteryHealthState
import com.amperex.batteryintelligence.ui.components.DisclaimerBanner
import com.amperex.batteryintelligence.ui.components.MetricCard
import com.amperex.batteryintelligence.ui.theme.ElectricLime
import com.amperex.batteryintelligence.ui.theme.MaterialTheme.colorScheme.surface
import com.amperex.batteryintelligence.ui.theme.MaterialTheme.colorScheme.outline
import com.amperex.batteryintelligence.ui.theme.MintAccent
import com.amperex.batteryintelligence.ui.theme.TextMuted
import com.amperex.batteryintelligence.ui.theme.TextSecondary

@Composable
fun HealthScreen(
    viewModel: HealthViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val report = state.report

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Battery Health",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Transparent degradation & system capacity analytics",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main Battery Health Card
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
                    Text(
                        text = "CAPACITY RETENTION",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (report.isReliableCapacityAvailable) ElectricLime.copy(alpha = 0.15f) else Color(0xFF25252A))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (report.isReliableCapacityAvailable) "HARDWARE VERIFIED" else "ESTIMATE / UNAVAILABLE",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = if (report.isReliableCapacityAvailable) ElectricLime else TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (report.capacityHealthPercentage != null) {
                    Text(
                        text = "${String.format("%.1f", report.capacityHealthPercentage)}%",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = ElectricLime
                    )
                } else {
                    Text(
                        text = "Unavailable",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Battery capacity health cannot be measured reliably on this device.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = report.explanation,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, lineHeight = 17.sp),
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Android Reported State Card
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val statusColor = when (report.androidReportedHealth) {
                BatteryHealthState.GOOD -> MintAccent
                BatteryHealthState.OVERHEAT, BatteryHealthState.OVER_VOLTAGE -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.tertiary
            }

            MetricCard(
                title = "Android Reported State",
                value = report.androidReportedHealth.name,
                subtitle = "BatteryManager kernel status",
                icon = Icons.Rounded.CheckCircle,
                accentColor = statusColor,
                modifier = Modifier.weight(1f)
            )

            val cycleText = report.cycleCount?.let { "$it cycles" } ?: "Not exposed"
            MetricCard(
                title = "Cycle Count",
                value = cycleText,
                subtitle = "BATTERY_PROPERTY_CYCLE_COUNT",
                icon = Icons.Rounded.Loop,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Capacity Details
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            val measuredText = report.measuredFullCapacityMah?.let { "$it mAh" } ?: "Unavailable"
            MetricCard(
                title = "Full Charge Capacity",
                value = measuredText,
                subtitle = "Current maximum Coulomb charge",
                icon = Icons.Rounded.Speed,
                modifier = Modifier.weight(1f)
            )

            val designText = report.designCapacityMah?.let { "$it mAh" } ?: "Unavailable"
            MetricCard(
                title = "Factory Design Capacity",
                value = designText,
                subtitle = "OEM nominal specification",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        DisclaimerBanner(
            text = "Honesty Guarantee: Standard Android public APIs do not expose factory design capacity on all phones. Unlike apps that fabricate 98% or 100%, AmpereX only reports authentic hardware measurements."
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

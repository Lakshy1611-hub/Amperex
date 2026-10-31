package com.amperex.batteryintelligence.ui.settings

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
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.PrivacyTip
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.amperex.batteryintelligence.ui.theme.CrimsonAlert
import com.amperex.batteryintelligence.ui.theme.ElectricLime

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val colors = MaterialTheme.colorScheme

    if (state.showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.setClearConfirmationVisible(false) },
            title = { Text("Clear All Battery History", fontWeight = FontWeight.Bold) },
            text = {
                Text("This permanently wipes recorded battery observations, charging sessions, and daily summaries stored on this device. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearAllHistory() },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonAlert)
                ) {
                    Text("Delete Everything", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setClearConfirmationVisible(false) }) {
                    Text("Cancel")
                }
            },
            containerColor = colors.surface
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Settings & Privacy",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = colors.onSurface
        )
        Text(
            text = "Local controls, data preferences, and exports",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("PREFERENCES", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Appearance", fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                        Text(
                            "Follows your phone's Light or Dark theme automatically",
                            fontSize = 12.sp,
                            color = colors.onSurfaceVariant
                        )
                    }
                    Text("SYSTEM", color = colors.primary, style = MaterialTheme.typography.labelMedium)
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Thermostat,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Fahrenheit Temperature", fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                            Text("Display °F instead of °C", fontSize = 12.sp, color = colors.onSurfaceVariant)
                        }
                    }
                    Switch(
                        checked = state.useFahrenheit,
                        onCheckedChange = { viewModel.toggleTemperatureUnit(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = colors.onPrimary,
                            checkedTrackColor = colors.primary
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("DATA PORTABILITY & EXPORT", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))

        SettingsCard {
            Column {
                Text("Export Historical Observations", fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                Text(
                    "Export recorded battery percentages, voltage, and charging events to your device.",
                    fontSize = 12.sp,
                    color = colors.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { viewModel.exportCsv() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Rounded.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export CSV", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.exportJson() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.secondaryContainer,
                            contentColor = colors.onSecondaryContainer
                        ),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Export JSON", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("SYSTEM & PRIVACY", style = MaterialTheme.typography.labelSmall, color = colors.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))

        DisclaimerBanner(
            text = "AmpereX stores its battery records locally on this device. No account is required."
        )
        Spacer(modifier = Modifier.height(10.dp))
        DisclaimerBanner(
            text = "Android may limit background activity while the device is idle. Battery history is collected when Android allows the app to receive updates."
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { viewModel.setClearConfirmationVisible(true) },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonAlert),
            border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = androidx.compose.ui.graphics.SolidColor(CrimsonAlert.copy(alpha = 0.5f))
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Rounded.DeleteForever, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Clear All Local History", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .border(1.dp, colors.outlineVariant, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        content()
    }
}

package com.amperex.batteryintelligence.ui.apps

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.amperex.batteryintelligence.domain.model.AppUsageInfo
import com.amperex.batteryintelligence.ui.components.DisclaimerBanner
import com.amperex.batteryintelligence.ui.theme.BlueAccent
import com.amperex.batteryintelligence.ui.theme.ElectricLime
import com.amperex.batteryintelligence.ui.theme.MintAccent
import com.amperex.batteryintelligence.ui.theme.VioletAccent
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AppsScreen(viewModel: AppsViewModel) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    DisposableEffect(context, viewModel) {
        val owner = context as? LifecycleOwner
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.checkPermissionAndLoad()
        }
        owner?.lifecycle?.addObserver(observer)
        onDispose { owner?.lifecycle?.removeObserver(observer) }
    }

    LaunchedEffect(viewModel, state.hasPermission) {
        if (state.hasPermission) {
            while (true) {
                delay(30_000L)
                viewModel.refresh()
            }
        }
    }

    val totalForeground = state.appUsageList.sumOf { it.foregroundTimeMs }.coerceAtLeast(1L)
    val session = state.session
    val startedText = remember(session?.startedAt) {
        session?.startedAt?.let {
            SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(it))
        } ?: "Not started"
    }
    val elapsedText = session?.startedAt?.let { formatDuration(System.currentTimeMillis() - it) } ?: "—"
    val batteryDrop = session?.startBatteryPercent?.let {
        (it - session.currentBatteryPercent).coerceAtLeast(0)
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("APP INTELLIGENCE", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.8.sp, fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(5.dp))
                Text("Since last charge", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text("See what you've used after unplugging", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                IconButton(onClick = { viewModel.refresh() }) {
                    if (state.isLoading) CircularProgressIndicator(Modifier.size(19.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Rounded.Refresh, contentDescription = "Refresh app usage", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        if (!state.hasPermission) {
            Surface(shape = RoundedCornerShape(26.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(42.dp))
                    Spacer(Modifier.height(12.dp))
                    Text("Usage access required", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(7.dp))
                    Text("Allow AmpereX under Android Settings → Special app access → Usage access. This lets the app read app foreground activity on your device.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.openUsageAccessSettings() }, modifier = Modifier.fillMaxWidth()) { Text("Enable usage access") }
                }
            }
        } else if (session?.isCharging == true) {
            SessionStatusCard(
                title = "Phone is charging",
                subtitle = "Your next app-usage session starts automatically when you unplug the charger.",
                icon = Icons.Rounded.BatteryChargingFull
            )
        } else {
            Surface(shape = RoundedCornerShape(28.dp), color = Color.Transparent, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.background(Brush.linearGradient(listOf(Color(0xFF173B42), Color(0xFF26385A), Color(0xFF3B3267)))).padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(9.dp).clip(CircleShape).background(MintAccent))
                        Spacer(Modifier.width(8.dp))
                        Text("LIVE DISCHARGE SESSION", style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp, fontWeight = FontWeight.Bold), color = Color(0xFFC5F4E9))
                        Spacer(Modifier.weight(1f))
                        Text("AUTO TRACKING", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.White.copy(alpha = 0.82f))
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Column(Modifier.weight(1f)) {
                            Text("Battery now", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                            Text("${session?.currentBatteryPercent ?: 0}%", color = Color.White, style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Dropped since unplug", color = Color.White.copy(alpha = 0.75f), style = MaterialTheme.typography.bodySmall)
                            Text(batteryDrop?.let { "$it%" } ?: "—", color = Color(0xFF8DE6C9), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        SessionMetric("STARTED", startedText, Modifier.weight(1f))
                        SessionMetric("ELAPSED", elapsedText, Modifier.weight(1f))
                        SessionMetric("START LEVEL", session?.startBatteryPercent?.let { "$it%" } ?: "—", Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(22.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text("App activity", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                    Text("Ranked by foreground time this session", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape = RoundedCornerShape(30.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)) {
                    Text("${state.appUsageList.size} APPS", modifier = Modifier.padding(horizontal = 11.dp, vertical = 7.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                }
            }
            Spacer(Modifier.height(12.dp))

            when {
                state.isLoading -> Box(Modifier.fillMaxWidth().padding(34.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                state.appUsageList.isEmpty() -> Surface(shape = RoundedCornerShape(22.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
                    Column(Modifier.fillMaxWidth().padding(22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Rounded.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(9.dp))
                        Text("No app activity recorded yet", fontWeight = FontWeight.SemiBold)
                        Text("As you use apps, their active time will appear here automatically.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                }
                else -> state.appUsageList.forEachIndexed { index, app ->
                    AppUsageItem(rank = index + 1, app = app, share = app.foregroundTimeMs.toFloat() / totalForeground)
                    Spacer(Modifier.height(10.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            DisclaimerBanner(text = "Battery drop above is the phone's measured session-level change. Android does not provide ordinary apps with reliable per-app mAh / battery-percent attribution on every device. App rows show actual active time and share of tracked app time—not invented battery-drain percentages.")
        }

        Spacer(Modifier.height(12.dp))
        Text("Updates automatically while this screen is open · refreshes every 30 seconds", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(22.dp))
    }
}

@Composable
private fun SessionMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.6.sp), color = Color.White.copy(alpha = 0.68f), maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SessionStatusCard(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(38.dp))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun AppUsageItem(rank: Int, app: AppUsageInfo, share: Float) {
    val context = LocalContext.current
    val icon = remember(app.packageName) {
        runCatching {
            context.packageManager.getApplicationIcon(app.packageName).toBitmap(width = 112, height = 112).asImageBitmap()
        }.getOrNull()
    }
    val sharePct = (share * 100f).toInt().coerceIn(0, 100)
    val accent = when (rank % 3) { 1 -> ElectricLime; 2 -> VioletAccent; else -> BlueAccent }

    Surface(shape = RoundedCornerShape(21.dp), color = MaterialTheme.colorScheme.surface, border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(24.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Text("$rank", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.width(11.dp))
                if (icon != null) {
                    androidx.compose.foundation.Image(bitmap = icon, contentDescription = "${app.appName} icon", modifier = Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)))
                } else {
                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Apps, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(app.appName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(app.packageName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatDuration(app.foregroundTimeMs), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                    Text("ACTIVE TIME", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, letterSpacing = 0.7.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(Modifier.height(13.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Share of app time", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text("$sharePct%", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = accent)
            }
            Spacer(Modifier.height(7.dp))
            Box(Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)) {
                Box(Modifier.fillMaxWidth(share.coerceIn(0f, 1f)).height(6.dp).clip(CircleShape).background(accent))
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val minutes = durationMs.coerceAtLeast(0L) / 60_000L
    val hours = minutes / 60
    val remainder = minutes % 60
    return if (hours > 0) "${hours}h ${remainder}m" else "${remainder}m"
}

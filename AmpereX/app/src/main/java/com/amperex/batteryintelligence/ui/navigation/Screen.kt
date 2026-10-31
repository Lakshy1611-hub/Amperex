package com.amperex.batteryintelligence.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Analytics
import androidx.compose.material.icons.rounded.BatteryChargingFull
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GridApps
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Rounded.Bolt)
    object Analytics : Screen("analytics", "Analytics", Icons.Rounded.Analytics)
    object Health : Screen("health", "Health", Icons.Rounded.Favorite)
    object Apps : Screen("apps", "Apps", Icons.Rounded.GridApps)
    object Charging : Screen("charging", "Charging", Icons.Rounded.BatteryChargingFull)
    object Settings : Screen("settings", "Settings", Icons.Rounded.Settings)

    companion object {
        val bottomNavItems = listOf(Home, Analytics, Health, Apps, Charging)
    }
}

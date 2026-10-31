package com.amperex.batteryintelligence.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.amperex.batteryintelligence.ui.analytics.AnalyticsScreen
import com.amperex.batteryintelligence.ui.analytics.AnalyticsViewModel
import com.amperex.batteryintelligence.ui.apps.AppsScreen
import com.amperex.batteryintelligence.ui.apps.AppsViewModel
import com.amperex.batteryintelligence.ui.charging.ChargingScreen
import com.amperex.batteryintelligence.ui.charging.ChargingViewModel
import com.amperex.batteryintelligence.ui.health.HealthScreen
import com.amperex.batteryintelligence.ui.health.HealthViewModel
import com.amperex.batteryintelligence.ui.home.HomeScreen
import com.amperex.batteryintelligence.ui.home.HomeViewModel
import com.amperex.batteryintelligence.ui.settings.SettingsScreen
import com.amperex.batteryintelligence.ui.settings.SettingsViewModel
import com.amperex.batteryintelligence.ui.theme.AmoledBlack
import com.amperex.batteryintelligence.ui.theme.ElectricLime
import com.amperex.batteryintelligence.ui.theme.GraphiteSurface
import com.amperex.batteryintelligence.ui.theme.TextMuted

@Composable
fun AmpereXNavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    analyticsViewModel: AnalyticsViewModel,
    healthViewModel: HealthViewModel,
    appsViewModel: AppsViewModel,
    chargingViewModel: ChargingViewModel,
    settingsViewModel: SettingsViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = AmoledBlack,
        bottomBar = {
            NavigationBar(
                containerColor = GraphiteSurface,
                tonalElevation = 0.dp
            ) {
                Screen.bottomNavItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(imageVector = screen.icon, contentDescription = screen.title)
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                color = if (selected) ElectricLime else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            indicatorColor = ElectricLime,
                            unselectedIconColor = TextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = homeViewModel,
                    onOpenSettings = { navController.navigate(Screen.Settings.route) }
                )
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(viewModel = analyticsViewModel)
            }
            composable(Screen.Health.route) {
                HealthScreen(viewModel = healthViewModel)
            }
            composable(Screen.Apps.route) {
                AppsScreen(viewModel = appsViewModel)
            }
            composable(Screen.Charging.route) {
                ChargingScreen(viewModel = chargingViewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = settingsViewModel)
            }
        }
    }
}

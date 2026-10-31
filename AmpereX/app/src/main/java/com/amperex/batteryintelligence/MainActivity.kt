package com.amperex.batteryintelligence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.amperex.batteryintelligence.ui.analytics.AnalyticsViewModel
import com.amperex.batteryintelligence.ui.apps.AppsViewModel
import com.amperex.batteryintelligence.ui.charging.ChargingViewModel
import com.amperex.batteryintelligence.ui.health.HealthViewModel
import com.amperex.batteryintelligence.ui.home.HomeViewModel
import com.amperex.batteryintelligence.ui.navigation.AmpereXNavGraph
import com.amperex.batteryintelligence.ui.settings.SettingsViewModel
import com.amperex.batteryintelligence.ui.theme.AmpereXTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AmpereXApplication

        val homeViewModel = HomeViewModel(app.batteryRepository)
        val analyticsViewModel = AnalyticsViewModel(app.batteryRepository, app.database.dailySummaryDao())
        val healthViewModel = HealthViewModel(app.healthRepository)
        val appsViewModel = AppsViewModel(this, app.usageStatsRepository)
        val chargingViewModel = ChargingViewModel(app.batteryRepository, app.chargingRepository)
        val settingsViewModel = SettingsViewModel(app.batteryRepository, app.exportManager)

        setContent {
            AmpereXTheme(darkTheme = true) {
                val navController = rememberNavController()
                AmpereXNavGraph(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    analyticsViewModel = analyticsViewModel,
                    healthViewModel = healthViewModel,
                    appsViewModel = appsViewModel,
                    chargingViewModel = chargingViewModel,
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

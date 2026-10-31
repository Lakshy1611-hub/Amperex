/**
 * Map of all native Android Studio project files for inspection and ZIP packaging.
 */

export interface AndroidFile {
  path: string;
  name: string;
  type: 'kotlin' | 'gradle' | 'xml' | 'toml' | 'properties' | 'markdown';
  category: 'Configuration' | 'Manifest' | 'Database' | 'Calculators' | 'UI' | 'Tests' | 'Documentation';
  content: string;
}

export const ANDROID_PROJECT_FILES: AndroidFile[] = [
  {
    path: 'AmpereX/settings.gradle.kts',
    name: 'settings.gradle.kts',
    type: 'gradle',
    category: 'Configuration',
    content: `pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\\\.android.*")
                includeGroupByRegex("com\\\\.google.*")
                includeGroupByRegex("androidx\\\\..*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AmpereX"
include(":app")`
  },
  {
    path: 'AmpereX/build.gradle.kts',
    name: 'build.gradle.kts (Root)',
    type: 'gradle',
    category: 'Configuration',
    content: `plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ksp) apply false
}`
  },
  {
    path: 'AmpereX/gradle/libs.versions.toml',
    name: 'libs.versions.toml',
    type: 'toml',
    category: 'Configuration',
    content: `[versions]
agp = "8.7.2"
kotlin = "2.0.21"
coreKtx = "1.15.0"
junit = "4.13.2"
lifecycleRuntimeKtx = "2.8.7"
activityCompose = "1.9.3"
composeBom = "2024.11.00"
navigationCompose = "2.8.4"
room = "2.6.1"
ksp = "2.0.21-1.0.28"
coroutines = "1.9.0"
serialization = "1.7.3"
datastore = "1.1.1"
workManager = "2.10.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-work-runtime-ktx = { group = "androidx.work", name = "work-runtime-ktx", version.ref = "workManager" }
junit = { group = "junit", name = "junit", version.ref = "junit" }`
  },
  {
    path: 'AmpereX/app/build.gradle.kts',
    name: 'app/build.gradle.kts',
    type: 'gradle',
    category: 'Configuration',
    content: `plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.amperex.batteryintelligence"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.amperex.batteryintelligence"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)

    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // WorkManager & Background Tasks
    implementation(libs.androidx.work.runtime.ktx)

    testImplementation(libs.junit)
}`
  },
  {
    path: 'AmpereX/app/src/main/AndroidManifest.xml',
    name: 'AndroidManifest.xml',
    type: 'xml',
    category: 'Manifest',
    content: `<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_SPECIAL_USE" />

    <!-- Granted via Settings > Apps > Special App Access -->
    <uses-permission
        android:name="android.permission.PACKAGE_USAGE_STATS"
        tools:ignore="ProtectedPermissions" />

    <application
        android:name=".AmpereXApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.AmpereX">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.AmpereX">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <receiver
            android:name=".receiver.BatteryStateReceiver"
            android:exported="false">
            <intent-filter>
                <action android:name="android.intent.action.ACTION_POWER_CONNECTED" />
                <action android:name="android.intent.action.ACTION_POWER_DISCONNECTED" />
                <action android:name="android.intent.action.BATTERY_LOW" />
                <action android:name="android.intent.action.BATTERY_OKAY" />
            </intent-filter>
        </receiver>

        <service
            android:name=".service.BatteryMonitoringService"
            android:exported="false"
            android:foregroundServiceType="specialUse">
            <property
                android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
                android:value="User-initiated continuous battery charging analytics" />
        </service>

    </application>
</manifest>`
  },
  {
    path: 'AmpereX/app/src/main/java/com/amperex/batteryintelligence/data/local/AmpereXDatabase.kt',
    name: 'AmpereXDatabase.kt',
    type: 'kotlin',
    category: 'Database',
    content: `package com.amperex.batteryintelligence.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.amperex.batteryintelligence.data.local.dao.*
import com.amperex.batteryintelligence.data.local.entities.*

@Database(
    entities = [
        BatterySampleEntity::class,
        ChargingSessionEntity::class,
        DailyBatterySummaryEntity::class,
        BatteryHealthObservationEntity::class,
        AppUsageSnapshotEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AmpereXDatabase : RoomDatabase() {
    abstract fun batteryDao(): BatteryDao
    abstract fun chargingSessionDao(): ChargingSessionDao
    abstract fun dailySummaryDao(): DailySummaryDao
    abstract fun batteryHealthDao(): BatteryHealthDao

    companion object {
        @Volatile
        private var INSTANCE: AmpereXDatabase? = null

        fun getInstance(context: Context): AmpereXDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AmpereXDatabase::class.java,
                    "amperex_battery_intelligence.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}`
  },
  {
    path: 'AmpereX/app/src/main/java/com/amperex/batteryintelligence/domain/calculator/DailyReportCalculator.kt',
    name: 'DailyReportCalculator.kt',
    type: 'kotlin',
    category: 'Calculators',
    content: `package com.amperex.batteryintelligence.domain.calculator

import com.amperex.batteryintelligence.domain.model.BatterySample
import com.amperex.batteryintelligence.domain.model.ChargingStatus
import com.amperex.batteryintelligence.domain.model.DailyBatterySummary
import kotlin.math.max

object DailyReportCalculator {
    /**
     * Accurately calculates gross discharge and charging gain separately.
     * Never calculates gross discharge solely by (ending - starting).
     */
    fun computeDailySummary(
        dateEpochDay: Long,
        daySamples: List<BatterySample>,
        trackedScreenOnMinutes: Long? = null
    ): DailyBatterySummary {
        if (daySamples.isEmpty()) {
            return DailyBatterySummary(
                dateEpochDay = dateEpochDay,
                startingPercentage = null,
                endingPercentage = null,
                grossDischarge = 0,
                grossChargingGain = 0,
                netChange = 0,
                trackedScreenOnMinutes = trackedScreenOnMinutes,
                chargingSessionCount = 0,
                averageDischargeRatePerHour = null,
                peakTemperatureCelsius = null,
                isPartialData = true,
                totalSamples = 0
            )
        }

        val sorted = daySamples.sortedBy { it.timestamp }
        val startingPct = sorted.first().percentage
        val endingPct = sorted.last().percentage

        var grossDischarge = 0
        var grossChargingGain = 0
        var chargingSessions = 0
        var inChargingSession = false
        var peakTemp: Float? = null

        for (i in 0 until sorted.size) {
            val curr = sorted[i]
            if (curr.temperatureCelsius != null) {
                peakTemp = if (peakTemp == null) curr.temperatureCelsius else max(peakTemp, curr.temperatureCelsius)
            }

            val isChargingNow = curr.status == ChargingStatus.CHARGING || curr.status == ChargingStatus.FULL
            if (isChargingNow && !inChargingSession) {
                chargingSessions++
                inChargingSession = true
            } else if (!isChargingNow && inChargingSession) {
                inChargingSession = false
            }

            if (i > 0) {
                val prev = sorted[i - 1]
                val delta = curr.percentage - prev.percentage
                if (delta < 0) {
                    grossDischarge += -delta
                } else if (delta > 0) {
                    grossChargingGain += delta
                }
            }
        }

        val netChange = endingPct - startingPct
        val isPartial = sorted.size < 12

        return DailyBatterySummary(
            dateEpochDay = dateEpochDay,
            startingPercentage = startingPct,
            endingPercentage = endingPct,
            grossDischarge = grossDischarge,
            grossChargingGain = grossChargingGain,
            netChange = netChange,
            trackedScreenOnMinutes = trackedScreenOnMinutes,
            chargingSessionCount = chargingSessions,
            averageDischargeRatePerHour = null,
            peakTemperatureCelsius = peakTemp,
            isPartialData = isPartial,
            totalSamples = sorted.size
        )
    }
}`
  },
  {
    path: 'AmpereX/app/src/main/java/com/amperex/batteryintelligence/domain/calculator/BatteryHealthCalculator.kt',
    name: 'BatteryHealthCalculator.kt',
    type: 'kotlin',
    category: 'Calculators',
    content: `package com.amperex.batteryintelligence.domain.calculator

import com.amperex.batteryintelligence.domain.model.BatteryHealthReport
import com.amperex.batteryintelligence.domain.model.BatteryHealthState

object BatteryHealthCalculator {
    fun evaluateHealth(
        androidReportedHealth: BatteryHealthState,
        designCapacityMah: Int?,
        measuredFullCapacityMah: Int?,
        cycleCount: Int?,
        historicalObservationsCount: Int
    ): BatteryHealthReport {
        val isValidDesign = designCapacityMah != null && designCapacityMah in 500..15000
        val isValidMeasured = measuredFullCapacityMah != null && measuredFullCapacityMah in 400..15000
        val isReliable = isValidDesign && isValidMeasured

        val capacityHealthPercentage: Float? = if (isReliable) {
            val ratio = (measuredFullCapacityMah!!.toFloat() / designCapacityMah!!.toFloat()) * 100f
            if (ratio in 30f..115f) Math.round(ratio * 10f) / 10f else null
        } else null

        val explanation = when {
            capacityHealthPercentage != null ->
                "Measured via hardware full-charge capacity ($measuredFullCapacityMah mAh) relative to factory design capacity ($designCapacityMah mAh)."
            !isReliable ->
                "Battery capacity health cannot be measured reliably on this device. Standard Android public APIs do not expose verified factory design capacity on this hardware."
            else ->
                "Capacity readings from system counters were out of expected operational bounds."
        }

        return BatteryHealthReport(
            androidReportedHealth = androidReportedHealth,
            designCapacityMah = if (isValidDesign) designCapacityMah else null,
            measuredFullCapacityMah = if (isValidMeasured) measuredFullCapacityMah else null,
            capacityHealthPercentage = capacityHealthPercentage,
            cycleCount = if (cycleCount != null && cycleCount in 0..10000) cycleCount else null,
            isReliableCapacityAvailable = capacityHealthPercentage != null,
            historicalReadingsCount = historicalObservationsCount,
            explanation = explanation
        )
    }
}`
  },
  {
    path: 'AmpereX/app/src/main/java/com/amperex/batteryintelligence/MainActivity.kt',
    name: 'MainActivity.kt',
    type: 'kotlin',
    category: 'UI',
    content: `package com.amperex.batteryintelligence

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.amperex.batteryintelligence.ui.navigation.AmpereXNavGraph
import com.amperex.batteryintelligence.ui.theme.AmpereXTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AmpereXApplication

        setContent {
            AmpereXTheme(darkTheme = true) {
                val navController = rememberNavController()
                // Sets up Scaffold with Bottom Navigation for Home, Analytics, Health, Apps, Charging
                AmpereXNavGraph(navController = navController)
            }
        }
    }
}`
  },
  {
    path: 'AmpereX/app/src/test/java/com/amperex/batteryintelligence/CalculatorsTest.kt',
    name: 'CalculatorsTest.kt',
    type: 'kotlin',
    category: 'Tests',
    content: `package com.amperex.batteryintelligence

import com.amperex.batteryintelligence.domain.calculator.*
import com.amperex.batteryintelligence.domain.model.*
import org.junit.Assert.*
import org.junit.Test

class CalculatorsTest {
    @Test
    fun testGrossDischargeAndChargingGainSeparation() {
        val baseTime = 1700000000000L
        val samples = listOf(
            BatterySample(1, baseTime + 0, 80, ChargingStatus.DISCHARGING, PlugType.NONE, 28f, 4100, -350),
            BatterySample(2, baseTime + 3600_000L * 4, 40, ChargingStatus.DISCHARGING, PlugType.NONE, 29f, 3750, -350),
            BatterySample(3, baseTime + 3600_000L * 6, 70, ChargingStatus.CHARGING, PlugType.AC, 32f, 4250, 1800),
            BatterySample(4, baseTime + 3600_000L * 8, 50, ChargingStatus.DISCHARGING, PlugType.NONE, 27f, 3850, -320)
        )

        val summary = DailyReportCalculator.computeDailySummary(19680L, samples)
        assertEquals(60, summary.grossDischarge)
        assertEquals(30, summary.grossChargingGain)
        assertEquals(-30, summary.netChange)
    }

    @Test
    fun testBatteryHealthCalculationsHonesty() {
        val validReport = BatteryHealthCalculator.evaluateHealth(
            BatteryHealthState.GOOD, 4500, 4050, 180, 25
        )
        assertTrue(validReport.isReliableCapacityAvailable)
        assertEquals(90.0f, validReport.capacityHealthPercentage ?: 0f, 0.1f)

        val unavailableReport = BatteryHealthCalculator.evaluateHealth(
            BatteryHealthState.GOOD, null, null, null, 10
        )
        assertFalse(unavailableReport.isReliableCapacityAvailable)
        assertNull(unavailableReport.capacityHealthPercentage)
    }
}`
  },
  {
    path: 'AmpereX/README.md',
    name: 'README.md',
    type: 'markdown',
    category: 'Documentation',
    content: `# AmpereX — Battery Intelligence
Complete native Android application built with Kotlin, Jetpack Compose, Material 3, Room, and battery analytics engine.`
  }
];

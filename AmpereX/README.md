# AmpereX — Battery Intelligence
**Native Android Application built with Kotlin & Jetpack Compose**

AmpereX is a battery intelligence application engineered for genuine Android hardware telemetry. It provides real battery analytics, historical cycle tracking, charging wattage estimations, transparent battery health measurements, and application usage analysis—without fabricating statistics or guessing battery life.

---

## ⚡ Tech Stack & Architecture

- **Language:** Kotlin 2.0.21
- **UI Toolkit:** Jetpack Compose + Material 3 (AMOLED Pure Black & Electric Lime design)
- **Architecture:** Clean MVVM (Model-View-ViewModel) + Coroutines & StateFlow
- **Local Persistence:** Room Database 2.6.1 (with migrations & custom TypeConverters)
- **Background Work:** WorkManager 2.10.0 (Daily midnight aggregations) + Intent BroadcastReceivers
- **System Telemetry:** Android `BatteryManager`, `UsageStatsManager`
- **Serialization:** Kotlinx.Serialization JSON & CSV exporter
- **Testing:** JUnit 4 unit tests covering calculations, day-boundary splits, and capacity health formulas

---

## 📁 Project Structure

```
AmpereX/
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/amperex/batteryintelligence/
│       │   │   ├── AmpereXApplication.kt
│       │   │   ├── MainActivity.kt
│       │   │   ├── data/
│       │   │   │   ├── local/
│       │   │   │   │   ├── AmpereXDatabase.kt
│       │   │   │   │   ├── Converters.kt
│       │   │   │   │   ├── dao/ (BatteryDao, ChargingSessionDao, DailySummaryDao, ...)
│       │   │   │   │   └── entities/ (BatterySampleEntity, ChargingSessionEntity, ...)
│       │   │   │   └── repository/ (BatteryRepository, HealthRepository, UsageStatsRepository, ...)
│       │   │   ├── domain/
│       │   │   │   ├── model/ (BatteryModels.kt)
│       │   │   │   └── calculator/
│       │   │   │       ├── BackupPredictionCalculator.kt
│       │   │   │       ├── BatteryHealthCalculator.kt
│       │   │   │       ├── ChargingAnalyticsCalculator.kt
│       │   │   │       ├── DailyReportCalculator.kt
│       │   │   │       └── DischargeRateCalculator.kt
│       │   │   ├── receiver/ (BatteryStateReceiver.kt)
│       │   │   ├── service/ (BatteryMonitoringService.kt)
│       │   │   ├── worker/ (DailySummaryWorker.kt)
│       │   │   ├── export/ (DataExportManager.kt)
│       │   │   └── ui/
│       │   │       ├── theme/ (Color.kt, Theme.kt, Type.kt)
│       │   │       ├── navigation/ (Screen.kt, AmpereXNavGraph.kt)
│       │   │       ├── components/ (BatteryLevelRing, MetricCard, InteractiveBatteryChart, ...)
│       │   │       ├── home/ (HomeScreen.kt, HomeViewModel.kt)
│       │   │       ├── analytics/ (AnalyticsScreen.kt, AnalyticsViewModel.kt)
│       │   │       ├── health/ (HealthScreen.kt, HealthViewModel.kt)
│       │   │       ├── apps/ (AppsScreen.kt, AppsViewModel.kt)
│       │   │       ├── charging/ (ChargingScreen.kt, ChargingViewModel.kt)
│       │   │       └── settings/ (SettingsScreen.kt, SettingsViewModel.kt)
│       │   └── res/
│       └── test/java/com/amperex/batteryintelligence/
│           └── CalculatorsTest.kt
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
│       └── gradle-wrapper.properties
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## 🚀 How to Build in Android Studio

1. **Prerequisites:**
   - Android Studio (Hedgehog 2023.1.1, Iguana 2023.2.1, Koala 2024.1.1, or Ladybug 2024.2.1+)
   - JDK 17 (bundled automatically in Android Studio)
   - Android SDK Platform 35 (minSdk 26, targetSdk 35)

2. **Open the Project:**
   - Launch Android Studio.
   - Select **Open** and browse to the `/AmpereX` directory.
   - Wait for Gradle sync to complete.

3. **Run Unit Tests:**
   In terminal:
   ```bash
   ./gradlew test
   ```
   Or right-click `CalculatorsTest.kt` in Android Studio and click **Run 'CalculatorsTest'**.

4. **Build APK:**
   ```bash
   ./gradlew assembleDebug
   ```
   The output APK will be located at:
   `app/build/outputs/apk/debug/app-debug.apk`

---

## 🔒 Permissions & Privacy Principles

1. **No Cloud Sync or Accounts:**
   AmpereX stores all battery samples, charging intervals, and summaries locally in an encrypted Room SQLite database.
2. **Usage Access (`PACKAGE_USAGE_STATS`):**
   Required to display foreground screen time per application via Android's `UsageStatsManager`.
   *Notice:* Foreground time is transparently reported as active duration and is never converted into fake mAh numbers.
3. **No Unnecessary Accessibility Services:**
   AmpereX never abuses Accessibility Services to inspect user screens.
4. **Android Doze & Battery Conservation:**
   Rather than holding an aggressive wake-lock that wastes battery, AmpereX listens for genuine system broadcasts (`ACTION_BATTERY_CHANGED`, `ACTION_POWER_CONNECTED`, `ACTION_POWER_DISCONNECTED`).

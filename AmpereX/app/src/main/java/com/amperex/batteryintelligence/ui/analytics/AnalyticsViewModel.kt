package com.amperex.batteryintelligence.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amperex.batteryintelligence.data.local.dao.DailySummaryDao
import com.amperex.batteryintelligence.data.repository.BatteryRepository
import com.amperex.batteryintelligence.domain.calculator.DailyReportCalculator
import com.amperex.batteryintelligence.domain.model.BatterySample
import com.amperex.batteryintelligence.domain.model.DailyBatterySummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

enum class TimePeriod(val label: String, val durationHours: Long) {
    ONE_HOUR("1h", 1),
    SIX_HOURS("6h", 6),
    TWENTY_FOUR_HOURS("24h", 24),
    SEVEN_DAYS("7d", 7 * 24),
    THIRTY_DAYS("30d", 30 * 24)
}

data class AnalyticsUiState(
    val selectedPeriod: TimePeriod = TimePeriod.TWENTY_FOUR_HOURS,
    val samples: List<BatterySample> = emptyList(),
    val todaySummary: DailyBatterySummary? = null,
    val isLoading: Boolean = false
)

class AnalyticsViewModel(
    private val batteryRepository: BatteryRepository,
    private val dailySummaryDao: DailySummaryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadDataForPeriod(TimePeriod.TWENTY_FOUR_HOURS)
    }

    fun selectPeriod(period: TimePeriod) {
        _uiState.update { it.copy(selectedPeriod = period) }
        loadDataForPeriod(period)
    }

    private fun loadDataForPeriod(period: TimePeriod) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val sinceTs = System.currentTimeMillis() - (period.durationHours * 3600_000L)
            val samples = batteryRepository.getSamplesSince(sinceTs)

            // Compute today's daily report
            val todayEpochDay = LocalDate.now().toEpochDay()
            val dayStartTs = todayEpochDay * 86_400_000L
            val daySamples = samples.filter { it.timestamp >= dayStartTs }
            val summary = DailyReportCalculator.computeDailySummary(
                dateEpochDay = todayEpochDay,
                daySamples = daySamples,
                trackedScreenOnMinutes = null
            )

            _uiState.update {
                it.copy(
                    samples = samples,
                    todaySummary = summary,
                    isLoading = false
                )
            }
        }
    }
}

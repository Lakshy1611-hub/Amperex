package com.amperex.batteryintelligence.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amperex.batteryintelligence.data.repository.HealthRepository
import com.amperex.batteryintelligence.domain.model.BatteryHealthReport
import com.amperex.batteryintelligence.domain.model.BatteryHealthState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HealthUiState(
    val report: BatteryHealthReport = BatteryHealthReport(
        androidReportedHealth = BatteryHealthState.GOOD,
        designCapacityMah = null,
        measuredFullCapacityMah = null,
        capacityHealthPercentage = null,
        cycleCount = null,
        isReliableCapacityAvailable = false,
        historicalReadingsCount = 0,
        explanation = "Checking battery hardware health..."
    ),
    val isLoading: Boolean = false
)

class HealthViewModel(
    private val healthRepository: HealthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    init {
        loadHealthData()
    }

    fun loadHealthData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val report = healthRepository.getHealthReport()
            _uiState.update { it.copy(report = report, isLoading = false) }
        }
    }
}

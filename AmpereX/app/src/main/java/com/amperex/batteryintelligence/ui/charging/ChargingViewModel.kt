package com.amperex.batteryintelligence.ui.charging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amperex.batteryintelligence.data.repository.BatteryRepository
import com.amperex.batteryintelligence.data.repository.ChargingRepository
import com.amperex.batteryintelligence.domain.calculator.ChargingAnalyticsCalculator
import com.amperex.batteryintelligence.domain.calculator.ChargingPrediction
import com.amperex.batteryintelligence.domain.model.BatterySample
import com.amperex.batteryintelligence.domain.model.ChargingSession
import com.amperex.batteryintelligence.domain.model.ChargingStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChargingUiState(
    val currentSample: BatterySample? = null,
    val activeSession: ChargingSession? = null,
    val recentSessions: List<ChargingSession> = emptyList(),
    val prediction: ChargingPrediction? = null,
    val isCharging: Boolean = false
)

class ChargingViewModel(
    private val batteryRepository: BatteryRepository,
    private val chargingRepository: ChargingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChargingUiState())
    val uiState: StateFlow<ChargingUiState> = _uiState.asStateFlow()

    init {
        observeCharging()
    }

    private fun observeCharging() {
        viewModelScope.launch {
            batteryRepository.latestSampleFlow.collect { sample ->
                if (sample != null) {
                    val isChg = sample.status == ChargingStatus.CHARGING
                    val active = chargingRepository.getActiveSession()
                    val recentSamples = if (isChg) batteryRepository.getRecentSamples(15) else emptyList()
                    val pred = ChargingAnalyticsCalculator.calculateChargingPrediction(
                        currentPercentage = sample.percentage,
                        isCharging = isChg,
                        sessionSamples = recentSamples
                    )

                    _uiState.update {
                        it.copy(
                            currentSample = sample,
                            activeSession = active,
                            prediction = pred,
                            isCharging = isChg
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            chargingRepository.getRecentSessions(10).collect { sessions ->
                _uiState.update { it.copy(recentSessions = sessions) }
            }
        }
    }
}

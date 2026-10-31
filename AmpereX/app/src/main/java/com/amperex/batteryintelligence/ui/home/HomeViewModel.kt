package com.amperex.batteryintelligence.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amperex.batteryintelligence.data.repository.BatteryRepository
import com.amperex.batteryintelligence.domain.calculator.BackupPredictionCalculator
import com.amperex.batteryintelligence.domain.model.BackupPrediction
import com.amperex.batteryintelligence.domain.model.BatterySample
import com.amperex.batteryintelligence.domain.model.ChargingStatus
import com.amperex.batteryintelligence.domain.model.EstimationConfidence
import com.amperex.batteryintelligence.domain.model.PlugType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val currentSample: BatterySample? = null,
    val backupPrediction: BackupPrediction? = null,
    val isRefreshing: Boolean = false,
    val lastRefreshedMillis: Long = System.currentTimeMillis()
)

class HomeViewModel(
    private val batteryRepository: BatteryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshBatteryData(force = false)
        observeLiveBattery()
    }

    private fun observeLiveBattery() {
        viewModelScope.launch {
            batteryRepository.latestSampleFlow.collect { sample ->
                if (sample != null) {
                    val recent = batteryRepository.getRecentSamples(20)
                    val prediction = BackupPredictionCalculator.predictRemainingRuntime(
                        currentPercentage = sample.percentage,
                        isCharging = sample.status == ChargingStatus.CHARGING,
                        recentSamples = recent
                    )
                    _uiState.update {
                        it.copy(
                            currentSample = sample,
                            backupPrediction = prediction,
                            lastRefreshedMillis = sample.timestamp
                        )
                    }
                }
            }
        }
    }

    fun refreshBatteryData(force: Boolean = true) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val sample = batteryRepository.captureCurrentSample(forceRecord = force)
            val recent = batteryRepository.getRecentSamples(20)
            val prediction = BackupPredictionCalculator.predictRemainingRuntime(
                currentPercentage = sample.percentage,
                isCharging = sample.status == ChargingStatus.CHARGING,
                recentSamples = recent
            )
            _uiState.update {
                it.copy(
                    currentSample = sample,
                    backupPrediction = prediction,
                    isRefreshing = false,
                    lastRefreshedMillis = System.currentTimeMillis()
                )
            }
        }
    }
}

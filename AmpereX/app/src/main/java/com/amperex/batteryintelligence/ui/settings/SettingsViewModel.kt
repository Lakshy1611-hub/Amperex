package com.amperex.batteryintelligence.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amperex.batteryintelligence.data.repository.BatteryRepository
import com.amperex.batteryintelligence.export.DataExportManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class SettingsUiState(
    val isDarkMode: Boolean = true,
    val useFahrenheit: Boolean = false,
    val exportFile: File? = null,
    val showClearConfirmation: Boolean = false,
    val isExporting: Boolean = false
)

class SettingsViewModel(
    private val batteryRepository: BatteryRepository,
    private val exportManager: DataExportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleDarkMode(enabled: Boolean) {
        _uiState.update { it.copy(isDarkMode = enabled) }
    }

    fun toggleTemperatureUnit(useFahrenheit: Boolean) {
        _uiState.update { it.copy(useFahrenheit = useFahrenheit) }
    }

    fun setClearConfirmationVisible(visible: Boolean) {
        _uiState.update { it.copy(showClearConfirmation = visible) }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            batteryRepository.clearHistory()
            _uiState.update { it.copy(showClearConfirmation = false) }
        }
    }

    fun exportCsv() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val file = exportManager.exportAsCsv()
            _uiState.update { it.copy(exportFile = file, isExporting = false) }
        }
    }

    fun exportJson() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }
            val file = exportManager.exportAsJson()
            _uiState.update { it.copy(exportFile = file, isExporting = false) }
        }
    }
}

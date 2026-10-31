package com.amperex.batteryintelligence.ui.apps

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amperex.batteryintelligence.data.repository.DischargeSessionSnapshot
import com.amperex.batteryintelligence.data.repository.UsageStatsRepository
import com.amperex.batteryintelligence.domain.model.AppUsageInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppsUiState(
    val hasPermission: Boolean = false,
    val appUsageList: List<AppUsageInfo> = emptyList(),
    val session: DischargeSessionSnapshot? = null,
    val isLoading: Boolean = false,
    val lastUpdatedAt: Long = 0L
)

class AppsViewModel(
    private val context: Context,
    private val usageStatsRepository: UsageStatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppsUiState())
    val uiState: StateFlow<AppsUiState> = _uiState.asStateFlow()

    init { checkPermissionAndLoad() }

    fun checkPermissionAndLoad() {
        val granted = usageStatsRepository.hasUsageStatsPermission()
        _uiState.update { it.copy(hasPermission = granted) }
        if (granted) refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val session = usageStatsRepository.getDischargeSessionSnapshot()
            val list = if (session.isCharging) emptyList()
                else usageStatsRepository.getAppUsageList(sinceTimestamp = session.startedAt)
            _uiState.update {
                it.copy(
                    session = session,
                    appUsageList = list,
                    isLoading = false,
                    lastUpdatedAt = System.currentTimeMillis()
                )
            }
        }
    }

    fun openUsageAccessSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

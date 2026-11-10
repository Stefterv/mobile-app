package io.music_assistant.client.ui.compose.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.music_assistant.client.data.HeadphoneMonitor
import io.music_assistant.client.data.MainDataSource
import io.music_assistant.client.data.model.client.PlayerData
import io.music_assistant.client.settings.HeadphoneTransfer
import io.music_assistant.client.settings.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Backs Settings → Headphones: the stored [HeadphoneTransfer] and the players to choose from.
 * A change is saved at once.
 */
class HeadphoneTransferViewModel(
    private val settings: SettingsRepository,
    dataSource: MainDataSource,
    monitor: HeadphoneMonitor,
) : ViewModel() {
    val isSupported = monitor.isSupported

    val transfer: StateFlow<HeadphoneTransfer> = settings.headphoneTransfer

    val players: StateFlow<List<PlayerData>> = dataSource.playersData
        .map { it.dataOrNull.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    /** Turning it on for the first time preselects this phone as the target. */
    fun setEnabled(enabled: Boolean) {
        val current = transfer.value
        val target = current.targetPlayerId
            ?: settings.sendspinEffectivePlayerId.value.takeIf { enabled && players.value.any { p -> p.playerId == it } }
        settings.setHeadphoneTransfer(current.copy(enabled = enabled, targetPlayerId = target))
    }

    fun setSource(playerId: String) = settings.setHeadphoneTransfer(transfer.value.copy(sourcePlayerId = playerId))

    fun setTarget(playerId: String) = settings.setHeadphoneTransfer(transfer.value.copy(targetPlayerId = playerId))

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}

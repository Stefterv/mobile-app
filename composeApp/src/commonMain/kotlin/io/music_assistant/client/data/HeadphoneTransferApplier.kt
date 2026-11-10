package io.music_assistant.client.data

import co.touchlab.kermit.Logger
import io.music_assistant.client.api.ServiceClient
import io.music_assistant.client.data.model.client.PlayerData
import io.music_assistant.client.settings.SettingsRepository
import io.music_assistant.client.ui.compose.common.DataState
import io.music_assistant.client.ui.compose.common.action.QueueAction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Moves the playback from the chosen source player to the chosen target player when headphones
 * connect ([SettingsRepository.headphoneTransfer]). Only a source that plays is moved. The target
 * then becomes the selected player. Eagerly created (Koin `createdAtStart`).
 *
 * The state at launch is not an edge: headphones that are already connected when the app starts
 * do not move the playback.
 */
class HeadphoneTransferApplier(
    private val serviceClient: ServiceClient,
    monitor: HeadphoneMonitor,
    private val dataSource: MainDataSource,
    private val localPlayer: LocalPlayerAdapter,
    private val settings: SettingsRepository,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
) {
    private val logger = Logger.withTag("HeadphoneTransfer")

    init {
        scope.launch {
            monitor.connected // StateFlow already conflates duplicate values
                .drop(1)
                .filter { it }
                .collect {
                    logger.i { "Headphones connected: ${settings.headphoneTransfer.value}" }
                    transfer()
                }
        }
    }

    private suspend fun transfer() {
        val config = settings.headphoneTransfer.value
        if (!config.enabled) return
        val sourceId = config.sourcePlayerId ?: return
        val targetId = config.targetPlayerId ?: return
        if (sourceId == targetId) return
        // The edge often comes with a return to the foreground, so the session can still connect.
        val source = withTimeoutOrNull(READY_TIMEOUT_MS) {
            serviceClient.isReadyForCommands.first { it }
            dataSource.playersData
                .mapNotNull { state -> (state as? DataState.Data)?.data?.firstOrNull { it.playerId == sourceId } }
                .first()
        }
        if (source == null) {
            logger.i { "Headphones connected: source player not available within ${READY_TIMEOUT_MS}ms" }
            return
        }
        if (!source.player.isPlaying) {
            logger.i { "Headphones connected: ${source.player.name} does not play, nothing to move" }
            return
        }
        move(source, targetId)
    }

    private suspend fun move(source: PlayerData, targetId: String) {
        // A target that is the local player can hold a local pause; lift it and wait for its link.
        localPlayer.prepareQueue(targetId)
        logger.i { "Headphones connected: moving the playback from ${source.player.name} to $targetId" }
        dataSource.queueAction(
            QueueAction.Transfer(sourceId = source.queueOrPlayerId, targetId = targetId, autoplay = true),
        )
        // Show the target in the player pager, like a transfer from the player menu does.
        dataSource.playersData.value.dataOrNull
            ?.firstOrNull { it.playerId == targetId }
            ?.let { dataSource.selectPlayer(it.player) }
    }

    private companion object {
        const val READY_TIMEOUT_MS = 15_000L
    }
}

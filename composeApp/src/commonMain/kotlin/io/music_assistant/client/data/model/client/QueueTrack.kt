package io.music_assistant.client.data.model.client

import io.music_assistant.client.data.model.client.items.PlayableItem
import io.music_assistant.client.data.model.server.AudioFormat
import io.music_assistant.client.data.model.server.AudioProcessingChain

data class QueueTrack(
    val id: String,
    val track: PlayableItem,
    val isPlayable: Boolean,
    val format: AudioFormat?,
    val provider: String?,
    val audioProcessingChain: AudioProcessingChain? = null,
)

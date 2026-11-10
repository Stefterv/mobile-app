package io.music_assistant.client.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Tells if the audio output of the phone is a pair of headphones: wired, or Bluetooth (AirPods and
 * other Bluetooth audio). It drives [HeadphoneTransferApplier].
 */
interface HeadphoneMonitor {
    /** False on a platform without a headphone signal. The settings then hide the feature. */
    val isSupported: Boolean

    val connected: StateFlow<Boolean>
}

/** The monitor of a platform that does not support the feature yet. */
object NoHeadphoneMonitor : HeadphoneMonitor {
    override val isSupported = false
    override val connected: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()
}

package io.music_assistant.client.di

import co.touchlab.kermit.Logger
import io.music_assistant.client.data.HeadphoneMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionPortBluetoothA2DP
import platform.AVFAudio.AVAudioSessionPortBluetoothHFP
import platform.AVFAudio.AVAudioSessionPortBluetoothLE
import platform.AVFAudio.AVAudioSessionPortDescription
import platform.AVFAudio.AVAudioSessionPortHeadphones
import platform.AVFAudio.AVAudioSessionRouteChangeNotification
import platform.AVFAudio.currentRoute
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSOperationQueue
import platform.UIKit.UIApplicationWillEnterForegroundNotification

/**
 * iOS [HeadphoneMonitor]: reads the output ports of the current audio route.
 *
 * iOS sends the route change only to a running app. A suspended app gets nothing, so the monitor
 * reads the route again when the app returns to the foreground. Headphones that connected while
 * the app was suspended then count as a new connection.
 *
 * iOS cannot tell Bluetooth headphones from a Bluetooth speaker, so every Bluetooth audio output
 * counts. CarPlay has its own port type and does not count.
 */
class IosHeadphoneMonitor : HeadphoneMonitor {
    override val isSupported = true

    private val logger = Logger.withTag("HeadphoneMonitor")

    private val _connected = MutableStateFlow(readRoute("launch"))
    override val connected: StateFlow<Boolean> = _connected.asStateFlow()

    init {
        val center = NSNotificationCenter.defaultCenter
        // The monitor is a Koin singleton for the life of the process, so the observers stay.
        listOf(AVAudioSessionRouteChangeNotification, UIApplicationWillEnterForegroundNotification).forEach { name ->
            center.addObserverForName(name = name, `object` = null, queue = NSOperationQueue.mainQueue) { _ ->
                _connected.value = readRoute(name.toString())
            }
        }
    }

    private fun readRoute(trigger: String): Boolean {
        val ports = AVAudioSession.sharedInstance().currentRoute.outputs
            .filterIsInstance<AVAudioSessionPortDescription>()
            .map { it.portType }
        val connected = ports.any { it in HEADPHONE_PORTS }
        logger.d { "Route read ($trigger): outputs $ports, headphones $connected" }
        return connected
    }

    private companion object {
        val HEADPHONE_PORTS = setOf(
            AVAudioSessionPortHeadphones,
            AVAudioSessionPortBluetoothA2DP,
            AVAudioSessionPortBluetoothLE,
            AVAudioSessionPortBluetoothHFP,
        )
    }
}

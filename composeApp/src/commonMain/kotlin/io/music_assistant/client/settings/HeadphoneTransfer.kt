package io.music_assistant.client.settings

/**
 * Move the playback to a different player when headphones connect to the phone.
 *
 * @property enabled the user turned the setting on.
 * @property sourcePlayerId the player to take the playback from, if it plays.
 * @property targetPlayerId the player that gets the playback, usually this phone.
 */
data class HeadphoneTransfer(
    val enabled: Boolean = false,
    val sourcePlayerId: String? = null,
    val targetPlayerId: String? = null,
)

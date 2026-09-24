package io.music_assistant.client.ui.compose.settings.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.music_assistant.client.settings.SettingsRepository
import io.music_assistant.client.ui.compose.common.OverflowMenuButton
import io.music_assistant.client.ui.compose.common.OverflowMenuOption
import io.music_assistant.client.ui.compose.common.localizedTitle
import io.music_assistant.client.ui.compose.settings.SectionCard
import io.music_assistant.client.ui.compose.settings.SectionTitle
import io.music_assistant.sendspin.api.AudioCodec
import musicassistantclient.composeapp.generated.resources.Res
import musicassistantclient.composeapp.generated.resources.cd_select_codec
import musicassistantclient.composeapp.generated.resources.settings_buffer_size
import musicassistantclient.composeapp.generated.resources.settings_codec_preference
import musicassistantclient.composeapp.generated.resources.settings_custom_sendspin
import musicassistantclient.composeapp.generated.resources.settings_disable_local_player
import musicassistantclient.composeapp.generated.resources.settings_enable_local_player
import musicassistantclient.composeapp.generated.resources.settings_host
import musicassistantclient.composeapp.generated.resources.settings_local_player_disabled
import musicassistantclient.composeapp.generated.resources.settings_local_player_enabled
import musicassistantclient.composeapp.generated.resources.settings_path
import musicassistantclient.composeapp.generated.resources.settings_player_name
import musicassistantclient.composeapp.generated.resources.settings_port_default
import musicassistantclient.composeapp.generated.resources.settings_use_tls_wss
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@Composable
fun SendspinSection(
    modifier: Modifier = Modifier,
    enabled: Boolean = false,
    deviceName: String = "",
    useCustomConnection: Boolean = false,
    port: Int = 8097,
    path: String = "",
    codecPreference: AudioCodec = AudioCodec.OPUS,
    bufferCapacityMb: Int = SettingsRepository.BUFFER_MB_DEFAULT,
    host: String = "",
    useTls: Boolean = false,
    onEnabledChange: (Boolean) -> Unit = {},
    onDeviceNameChange: (String) -> Unit = {},
    onUseCustomConnectionChange: (Boolean) -> Unit = {},
    onPortChange: (Int) -> Unit = {},
    onPathChange: (String) -> Unit = {},
    onCodecPreferenceChange: (AudioCodec) -> Unit = {},
    onBufferCapacityMbChange: (Int) -> Unit = {},
    onHostChange: (String) -> Unit = {},
    onUseTlsChange: (Boolean) -> Unit = {},
) {
    val focusManager = LocalFocusManager.current
    val settingsEditable = !enabled

    SectionCard(modifier = modifier) {
        SectionTitle(
            if (enabled) {
                stringResource(
                    Res.string.settings_local_player_enabled,
                )
            } else {
                stringResource(Res.string.settings_local_player_disabled)
            },
        )

        // Text fields on top - disabled when player is running
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            value = deviceName,
            onValueChange = onDeviceNameChange,
            label = { Text(stringResource(Res.string.settings_player_name)) },
            singleLine = true,
            enabled = settingsEditable,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            colors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                disabledTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            ),
        )

        // Codec selection
        OverflowMenuButton(
            options = SettingsRepository.CODECS.map { item ->
                OverflowMenuOption(
                    title = item.localizedTitle(),
                ) { onCodecPreferenceChange(item) }
            },
            buttonContent = { onClick ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = settingsEditable) { onClick() }
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(Res.string.settings_codec_preference),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = codecPreference.localizedTitle(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (enabled) {
                                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            } else {
                                MaterialTheme.colorScheme.onBackground
                            },
                        )
                    }
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = stringResource(Res.string.cd_select_codec),
                        tint = if (enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            },
        )

        // Buffer size (advertised buffer_capacity in MB). Connect-time config, so locked while
        // the local player is running — takes effect on the next connect.
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(Res.string.settings_buffer_size),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "$bufferCapacityMb MB",
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onBackground
                    },
                )
            }
            Slider(
                value = bufferCapacityMb.toFloat(),
                onValueChange = { onBufferCapacityMbChange(it.roundToInt()) },
                valueRange = SettingsRepository.BUFFER_MB_MIN.toFloat()..SettingsRepository.BUFFER_MB_MAX.toFloat(),
                steps = (SettingsRepository.BUFFER_MB_MAX - SettingsRepository.BUFFER_MB_MIN) /
                        SettingsRepository.BUFFER_MB_STEP - 1,
                enabled = settingsEditable,
            )
        }

        // Custom connection toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = useCustomConnection,
                onCheckedChange = onUseCustomConnectionChange,
                enabled = settingsEditable,
            )
            Text(
                text = stringResource(Res.string.settings_custom_sendspin),
                color = if (enabled) {
                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                } else {
                    MaterialTheme.colorScheme.onBackground
                },
            )
        }

        // Require-encryption toggle: refuse the legacy cleartext protocol
        // when the server is too old for encrypted Sendspin.

        // Connection fields (only shown when using custom connection)
        if (useCustomConnection) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                value = host,
                onValueChange = onHostChange,
                label = { Text(stringResource(Res.string.settings_host)) },
                singleLine = true,
                enabled = settingsEditable,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                ),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                    disabledTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 12.dp),
                    value = port.toString(),
                    onValueChange = {
                        it.toIntOrNull()?.let(onPortChange)
                    },
                    label = { Text(stringResource(Res.string.settings_port_default)) },
                    singleLine = true,
                    enabled = settingsEditable,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Next) },
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        disabledTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    ),
                )

                TextField(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 12.dp),
                    value = path,
                    onValueChange = onPathChange,
                    label = { Text(stringResource(Res.string.settings_path)) },
                    singleLine = true,
                    enabled = settingsEditable,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                        disabledTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    ),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = useTls,
                    onCheckedChange = onUseTlsChange,
                    enabled = settingsEditable,
                )
                Text(
                    text = stringResource(Res.string.settings_use_tls_wss),
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onBackground
                    },
                )
            }
        }

        // Toggle button on the bottom
        if (enabled) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEnabledChange(false) },
            ) {
                Text(stringResource(Res.string.settings_disable_local_player))
            }
        } else {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onEnabledChange(true) },
            ) {
                Text(stringResource(Res.string.settings_enable_local_player))
            }
        }
    }
}


@Composable
@Preview
fun SendspinSectionPreview() {
    SendspinSection(
        modifier = Modifier.padding(16.dp),
    )
}
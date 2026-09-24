package io.music_assistant.client.ui.compose.settings.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.music_assistant.client.settings.SettingsRepository
import io.music_assistant.client.ui.compose.common.OverflowMenuButton
import io.music_assistant.client.ui.compose.common.OverflowMenuOption
import io.music_assistant.client.ui.compose.common.localizedTitle
import io.music_assistant.client.ui.compose.settings.SectionCard
import io.music_assistant.client.utils.platformDeviceName
import io.music_assistant.sendspin.api.AudioCodec
import musicassistantclient.composeapp.generated.resources.Res
import musicassistantclient.composeapp.generated.resources.cd_select_codec
import musicassistantclient.composeapp.generated.resources.settings_buffer_size
import musicassistantclient.composeapp.generated.resources.settings_codec_preference
import musicassistantclient.composeapp.generated.resources.settings_custom_sendspin
import musicassistantclient.composeapp.generated.resources.settings_disable_local_player
import musicassistantclient.composeapp.generated.resources.settings_enable_local_player
import musicassistantclient.composeapp.generated.resources.settings_host
import musicassistantclient.composeapp.generated.resources.settings_local_player
import musicassistantclient.composeapp.generated.resources.settings_local_player_clear_name
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

    Column {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                stringResource(
                    Res.string.settings_local_player
                ),
                style = MaterialTheme.typography.titleLargeEmphasized,
            )
            Switch(
                checked = enabled,
                onCheckedChange = { onEnabledChange(it) },
            )
        }
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            ),
            shape = RoundedCornerShape(12.dp),
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                value = deviceName,
                onValueChange = onDeviceNameChange,
                label = { Text(stringResource(Res.string.settings_player_name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                trailingIcon = {
                    if(deviceName != platformDeviceName()) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(Res.string.settings_local_player_clear_name),
                            modifier = Modifier
                                .clickable { onDeviceNameChange(platformDeviceName()) }
                        )
                    }
                }
            )


            // Codec selection
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.settings_codec_preference),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    SettingsRepository.CODECS.forEachIndexed { index, codec ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = SettingsRepository.CODECS.size
                            ),
                            onClick = {
                                onCodecPreferenceChange(codec)
                            },
                            selected = codec == codecPreference,
                            label = { Text(codec.localizedTitle().substringBefore(" ")) }
                        )
                    }
                }
                Text(
                    text = codecPreference.localizedTitle()
                        .substringAfter(" ")
                        .replace("(", "")
                        .replace(")", ""),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            var showAdvancedConfig by remember { mutableStateOf(true) }
            ListItem(
                headlineContent = {
                    Text("Advanced Configuration")
                },
                supportingContent = {
                    Text("50mb buffer, custom connection, and more")
                },
                trailingContent = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = if (showAdvancedConfig) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = stringResource(Res.string.cd_select_codec),
                    )
                },
                modifier = Modifier.clickable {
                    showAdvancedConfig = !showAdvancedConfig
                },
                colors = ListItemDefaults.colors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                ),
            )
            AnimatedVisibility(
                visible = showAdvancedConfig,
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = stringResource(Res.string.settings_custom_sendspin),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                            )
                            Checkbox(
                                checked = useCustomConnection,
                                onCheckedChange = onUseCustomConnectionChange,
                            )
                        }
                        SingleChoiceSegmentedButtonRow() {
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = 0,
                                    count = 2
                                ),
                                onClick = {
                                },
                                selected = !useTls,
                                enabled = useCustomConnection,
                                label = { Text("ws://") }
                            )
                            SegmentedButton(
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = 1,
                                    count = 2
                                ),
                                onClick = {
                                },
                                selected = useTls,
                                enabled = useCustomConnection,
                                label = { Text("wss://") }
                            )

                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            var isHostFocused by remember { mutableStateOf(false) }
                            var isPortFocused by remember { mutableStateOf(false) }
                            var isPathFocused by remember { mutableStateOf(false) }

                            OutlinedTextField(
                                modifier = Modifier
                                    .onFocusChanged { isHostFocused = it.isFocused }
                                    .weight(if (isHostFocused) 2f else 1f)
                                    .padding(bottom = 12.dp),
                                value = host,
                                onValueChange = onHostChange,
                                label = {
                                    Text(
                                        text = stringResource(Res.string.settings_host),
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip,
                                    )
                                },
                                singleLine = true,
                                placeholder = {
                                    Text(
                                        text = "example.com",
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip,
                                    )
                                },
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                ),
                                enabled = useCustomConnection,
                            )
                            OutlinedTextField(
                                modifier = Modifier
                                    .onFocusChanged { isPortFocused = it.isFocused }
                                    .weight(if (isPortFocused) 2f else 1f)
                                    .padding(bottom = 12.dp),
                                value = port.toString(),
                                onValueChange = {
                                    it.toIntOrNull()?.let(onPortChange)
                                },
                                label = {
                                    Text(
                                        text = stringResource(Res.string.settings_port_default),
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip,
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                ),
                                placeholder = {
                                    Text(
                                        text = "8095",
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip,
                                    )
                                },
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Next) },
                                ),
                                enabled = useCustomConnection,
                            )

                            OutlinedTextField(
                                modifier = Modifier
                                    .onFocusChanged { isPathFocused = it.isFocused }
                                    .weight(if (isPathFocused) 2f else 1f)
                                    .padding(bottom = 12.dp),
                                value = path,
                                onValueChange = onPathChange,
                                placeholder = {
                                    Text(
                                        text = "/sendspin",
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip,
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(Res.string.settings_path),
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Clip,
                                    )
                                },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                enabled = useCustomConnection,
                            )
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                        )
                        Text(
                            text = "Larger values can prevent issues on unreliable connection, but increases app memory usage",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}


@Composable
@Preview
fun SendspinSectionPreview() {
    var enabled by remember { mutableStateOf(false) }
    var deviceName by remember { mutableStateOf("Living Room") }
    var useCustomConnection by remember { mutableStateOf(false) }
    var port by remember { mutableStateOf(8097) }
    var path by remember { mutableStateOf("/music-assistant") }
    var codecPreference by remember { mutableStateOf(AudioCodec.OPUS) }
    var bufferCapacityMb by remember { mutableStateOf(SettingsRepository.BUFFER_MB_DEFAULT) }
    var host by remember { mutableStateOf("192.168.1.42") }
    var useTls by remember { mutableStateOf(true) }

    SendspinSection(
        modifier = Modifier.padding(16.dp),
        enabled = enabled,
        deviceName = deviceName,
        useCustomConnection = useCustomConnection,
        port = port,
        path = path,
        codecPreference = codecPreference,
        bufferCapacityMb = bufferCapacityMb,
        host = host,
        useTls = useTls,
        onEnabledChange = { enabled = it },
        onDeviceNameChange = { deviceName = it },
        onUseCustomConnectionChange = { useCustomConnection = it },
        onPortChange = { port = it },
        onPathChange = { path = it },
        onCodecPreferenceChange = { codecPreference = it },
        onBufferCapacityMbChange = { bufferCapacityMb = it },
        onHostChange = { host = it },
        onUseTlsChange = { useTls = it },
    )
}
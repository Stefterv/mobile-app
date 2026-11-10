package io.music_assistant.client.ui.compose.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.music_assistant.client.data.model.client.PlayerData
import io.music_assistant.client.ui.compose.home.players.iconRes
import musicassistantclient.composeapp.generated.resources.Res
import musicassistantclient.composeapp.generated.resources.settings_headphone_transfer
import musicassistantclient.composeapp.generated.resources.settings_headphone_transfer_choose
import musicassistantclient.composeapp.generated.resources.settings_headphone_transfer_from
import musicassistantclient.composeapp.generated.resources.settings_headphone_transfer_hint
import musicassistantclient.composeapp.generated.resources.settings_headphone_transfer_to
import musicassistantclient.composeapp.generated.resources.settings_headphone_transfer_unavailable
import musicassistantclient.composeapp.generated.resources.settings_headphones
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

/**
 * Settings → Headphones: move the playback from one player to another when headphones connect.
 * Shows nothing on a platform without a headphone signal.
 */
@Composable
fun HeadphoneTransferSection() {
    val viewModel = koinViewModel<HeadphoneTransferViewModel>()
    if (!viewModel.isSupported) return
    val transfer by viewModel.transfer.collectAsStateWithLifecycle()
    val players by viewModel.players.collectAsStateWithLifecycle()

    SectionCard {
        SectionTitle(stringResource(Res.string.settings_headphones))
        SettingSwitch(
            title = stringResource(Res.string.settings_headphone_transfer),
            hint = stringResource(Res.string.settings_headphone_transfer_hint),
            checked = transfer.enabled,
            onCheckedChange = viewModel::setEnabled,
        )
        if (transfer.enabled) {
            Column(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PlayerChoice(
                    label = stringResource(Res.string.settings_headphone_transfer_from),
                    selectedId = transfer.sourcePlayerId,
                    players = players,
                    onSelect = viewModel::setSource,
                )
                PlayerChoice(
                    label = stringResource(Res.string.settings_headphone_transfer_to),
                    selectedId = transfer.targetPlayerId,
                    players = players,
                    onSelect = viewModel::setTarget,
                )
            }
        }
    }
}

@Composable
private fun PlayerChoice(
    label: String,
    selectedId: String?,
    players: List<PlayerData>,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelLarge)
        PlayerDropdown(selectedId = selectedId, players = players, onSelect = onSelect)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerDropdown(
    selectedId: String?,
    players: List<PlayerData>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = players.firstOrNull { it.playerId == selectedId }
    // A stored player that the server does not list now (offline or removed) keeps its place.
    val selectedLabel = selected?.player?.nameAndSuffix
        ?: selectedId?.let { stringResource(Res.string.settings_headphone_transfer_unavailable) }
        ?: stringResource(Res.string.settings_headphone_transfer_choose)
    val shape = RoundedCornerShape(4.dp)
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, shape)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            selected?.let { PlayerIcon(it) }
            Text(
                text = selectedLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            players.forEach { playerData ->
                DropdownMenuItem(
                    text = {
                        Text(playerData.player.nameAndSuffix, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    leadingIcon = { PlayerIcon(playerData) },
                    onClick = {
                        onSelect(playerData.playerId)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun PlayerIcon(playerData: PlayerData) {
    Icon(
        painter = painterResource(playerData.iconRes()),
        contentDescription = null,
        modifier = Modifier.size(24.dp),
    )
}

package io.music_assistant.client.ui.compose.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SplitButtonDefaults.LeadingButton
import androidx.compose.material3.SplitButtonDefaults.TrailingButton
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.music_assistant.client.data.model.client.ClickContext
import io.music_assistant.client.data.model.client.PlayerData
import io.music_assistant.client.data.model.client.QueueOption
import io.music_assistant.client.data.model.client.itemKind
import io.music_assistant.client.data.model.client.items.AppMediaItem
import io.music_assistant.client.data.model.server.PlayMediaSupport
import io.music_assistant.client.data.play.PlayMode
import io.music_assistant.client.ui.MAX_DIALOG_HEIGHT
import io.music_assistant.client.ui.compose.common.OverflowMenuButton
import io.music_assistant.client.ui.compose.common.OverflowMenuOption
import io.music_assistant.client.ui.compose.common.items.DefaultClickActionsDialog
import io.music_assistant.client.ui.compose.common.items.ItemAction
import io.music_assistant.client.ui.compose.common.items.LocalClickActionConfig
import io.music_assistant.client.ui.compose.common.items.icon
import io.music_assistant.client.ui.compose.common.items.resolvePlayButtonActions
import io.music_assistant.client.ui.compose.common.items.title
import io.music_assistant.client.ui.compose.common.items.toOverflowOption
import io.music_assistant.client.ui.compose.common.items.toPlayMode
import io.music_assistant.client.ui.compose.home.players.iconRes
import io.music_assistant.client.ui.contentColorByLuminance
import musicassistantclient.composeapp.generated.resources.Res
import musicassistantclient.composeapp.generated.resources.action_play_now_on_player
import musicassistantclient.composeapp.generated.resources.action_play_on
import musicassistantclient.composeapp.generated.resources.cd_play_options
import musicassistantclient.composeapp.generated.resources.common_cancel
import musicassistantclient.composeapp.generated.resources.play_on_title
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun ItemPlayButton(
    item: AppMediaItem,
    onPlayClick: (PlayMode) -> Unit,
    playTargets: ItemDetailsViewModel.PlayTargets = ItemDetailsViewModel.PlayTargets(),
    onPlayOnClick: (PlayerData, PlayMode) -> Unit = { _, _ -> },
    tint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier,
) {
    if (!item.isPlayable) return

    // Art-derived control tint as the button fill; black/white content per its luminance.
    val onTint = tint.contentColorByLuminance()
    val buttonColors = ButtonDefaults.buttonColors(
        containerColor = tint,
        contentColor = onTint,
    )

    // The detail header is wrapped in ProvideClickActions(DETAIL), so the config resolves
    // this item's DETAIL default. effectiveActionFor is non-null here (item is playable).
    val clickActionConfig = LocalClickActionConfig.current
    val effective = clickActionConfig.effectiveActionFor(item)
        ?: ItemAction.Play(QueueOption.REPLACE)
    val kind = item.itemKind()

    var showCustomizeDialog by remember { mutableStateOf(false) }
    var showPlayOnDialog by remember { mutableStateOf(false) }

    val runPlayAction: (ItemAction) -> Unit = { action ->
        action.toPlayMode()?.let(onPlayClick)
    }

    SplitButtonLayout(
        modifier = modifier,
        leadingButton = {
            val actionLabel = stringResource(effective.title())
            // The icon shows the action, and the text names the target, so it is clear where
            // the item will play. The content description keeps both.
            val playerName = playTargets.selected?.player?.name
            val label = playerName ?: actionLabel
            val description = playerName
                ?.let { stringResource(Res.string.action_play_now_on_player, actionLabel, it) }
                ?: actionLabel
            LeadingButton(
                modifier = Modifier.semantics { contentDescription = description },
                onClick = { runPlayAction(effective) },
                colors = buttonColors,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = effective.icon(clickActionConfig.context), contentDescription = null)
                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        trailingButton = {
            PlayOverflow(
                item = item,
                default = effective,
                // Customize opens the per-kind table — meaningless for kindless items (e.g. Genre).
                onCustomize = if (kind != null) ({ showCustomizeDialog = true }) else null,
                onPlayAction = runPlayAction,
                // Offered only when there is another player to pick.
                onPlayOn = if (playTargets.players.any { it.playerId != playTargets.selected?.playerId }) {
                    ({ showPlayOnDialog = true })
                } else {
                    null
                },
                context = clickActionConfig.context,
                playMediaSupport = clickActionConfig.playMediaSupport,
            ) { onClick ->
                TrailingButton(onClick = onClick, colors = buttonColors) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = stringResource(Res.string.cd_play_options),
                    )
                }
            }
        },
    )

    if (showPlayOnDialog) {
        PlayOnDialog(
            players = playTargets.players,
            selectedPlayerId = playTargets.selected?.playerId,
            onSelect = { player ->
                showPlayOnDialog = false
                effective.toPlayMode()?.let { onPlayOnClick(player, it) }
            },
            onDismiss = { showPlayOnDialog = false },
        )
    }

    if (showCustomizeDialog && kind != null) {
        DefaultClickActionsDialog(
            itemKind = kind,
            onDismiss = { showCustomizeDialog = false },
        )
    }
}

@Composable
private fun PlayOverflow(
    item: AppMediaItem,
    default: ItemAction,
    onCustomize: (() -> Unit)?,
    onPlayAction: (ItemAction) -> Unit,
    onPlayOn: (() -> Unit)?,
    context: ClickContext?,
    playMediaSupport: PlayMediaSupport,
    button: @Composable (() -> Unit) -> Unit,
) {
    val actions = resolvePlayButtonActions(item, default, playMediaSupport, onCustomize != null)
    val playOnOption = onPlayOn?.let {
        OverflowMenuOption(
            title = stringResource(Res.string.action_play_on),
            icon = Icons.Default.Speaker,
            onClick = it,
        )
    }
    val options = listOfNotNull(playOnOption) + actions.map { action ->
        action.toOverflowOption(context) {
            if (it == ItemAction.Customize) onCustomize?.invoke() else onPlayAction(it)
        }
    }
    OverflowMenuButton(
        options = options,
        buttonContent = button,
    )
}

/** The players that "Play on…" offers. A tap plays the item there and selects the player. */
@Composable
private fun PlayOnDialog(
    players: List<PlayerData>,
    selectedPlayerId: String?,
    onSelect: (PlayerData) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.play_on_title)) },
        text = {
            LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = MAX_DIALOG_HEIGHT)) {
                items(players, key = { it.playerId }) { player ->
                    val selected = player.playerId == selectedPlayerId
                    ListItem(
                        modifier = Modifier.clickable { onSelect(player) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        leadingContent = {
                            Icon(
                                painter = painterResource(player.iconRes()),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        },
                        headlineContent = {
                            Text(
                                text = player.player.name,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = if (selected) FontWeight.Bold else null,
                            )
                        },
                        trailingContent = if (selected) {
                            { Icon(Icons.Default.Check, contentDescription = null) }
                        } else {
                            null
                        },
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(Res.string.common_cancel)) }
        },
    )
}

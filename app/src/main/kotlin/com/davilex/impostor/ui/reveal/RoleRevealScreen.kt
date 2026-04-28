package com.davilex.impostor.ui.reveal

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davilex.impostor.domain.model.GameMode
import com.davilex.impostor.domain.model.PlayerAssignment
import com.davilex.impostor.domain.model.PlayerRole
import com.davilex.impostor.ui.components.PlayerAvatar
import com.davilex.impostor.ui.components.PrimaryButton
import com.davilex.impostor.ui.components.SwipeRevealCard
import com.davilex.impostor.ui.components.colorAssignments
import com.davilex.impostor.ui.components.colorForName
import com.davilex.impostor.ui.theme.CivilBadge
import com.davilex.impostor.ui.theme.ImpostorBadge

@Composable
fun RoleRevealScreen(
    onAbort: () -> Unit,
    onAllRolesRevealed: () -> Unit,
    viewModel: RoleRevealViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showAbortDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.finished) {
        if (state.finished) onAllRolesRevealed()
    }

    BackHandler { showAbortDialog = true }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(20.dp)
    ) {
        RevealContent(
            state = state,
            onClose = { showAbortDialog = true },
            onRevealed = { viewModel.markRevealed() },
            onNext = { viewModel.next() }
        )
    }

    if (showAbortDialog) {
        AlertDialog(
            onDismissRequest = { showAbortDialog = false },
            title = { Text("¿Salir de la partida?") },
            text = { Text("Se perderá el progreso actual.") },
            confirmButton = {
                TextButton(onClick = {
                    showAbortDialog = false
                    onAbort()
                }) { Text("Salir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showAbortDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun RevealContent(
    state: RevealUiState,
    onClose: () -> Unit,
    onRevealed: () -> Unit,
    onNext: () -> Unit
) {
    val current = state.current ?: return
    val playerColors = remember(state.allPlayerNames) { colorAssignments(state.allPlayerNames) }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Salir", tint = MaterialTheme.colorScheme.onBackground)
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "${state.index + 1} / ${state.total}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleSmall
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            PlayerAvatar(
                name = current.playerName,
                size = 56,
                color = playerColors[current.playerName] ?: colorForName(current.playerName)
            )
            Spacer(Modifier.size(12.dp))
            Text(
                text = current.playerName,
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        ) {
            key(state.index) {
                SwipeRevealCard(
                    onRevealed = onRevealed,
                    modifier = Modifier.fillMaxSize(),
                    revealed = { RevealedContent(assignment = current, mode = state.mode) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Pasa al siguiente jugador.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            textAlign = TextAlign.Center
        )

        PrimaryButton(
            text = if (state.isLast) "Jugar" else "Jugador siguiente",
            enabled = state.revealed,
            onClick = onNext
        )

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun RevealedContent(assignment: PlayerAssignment, mode: GameMode) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (val role = assignment.role) {
            is PlayerRole.Civil -> {
                RoleBadge(text = "Civil", color = CivilBadge)
                Spacer(Modifier.height(20.dp))
                LabeledWord(label = "Tu palabra es", word = role.word)
            }
            is PlayerRole.Impostor -> {
                RoleBadge(text = "Impostor", color = ImpostorBadge)
                Spacer(Modifier.height(20.dp))
                if (role.hint != null) {
                    LabeledWord(label = "Tu pista es", word = role.hint)
                } else {
                    Text(
                        text = "No tienes palabra. Pasa al siguiente jugador.",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }
            is PlayerRole.WordOnly -> {
                LabeledWord(label = "Tu palabra es", word = role.word)
            }
        }
    }
}

@Composable
private fun RoleBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = color,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun LabeledWord(label: String, word: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = "«$word»",
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
    )
}


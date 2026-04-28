package com.davilex.impostor.ui.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davilex.impostor.domain.model.GameMode
import com.davilex.impostor.ui.components.PlayerAvatar
import com.davilex.impostor.ui.components.PrimaryButton
import com.davilex.impostor.ui.components.SecondaryButton
import com.davilex.impostor.ui.components.colorAssignments
import com.davilex.impostor.ui.components.colorForName

@Composable
fun ResultScreen(
    onNewGame: () -> Unit,
    onHome: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val emoji = if (state.civilsWin) "🏆" else "🕵️"
                Box(
                    modifier = Modifier
                        .size(112.dp)
                        .clip(CircleShape)
                        .background(
                            (if (state.civilsWin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                                .copy(alpha = 0.18f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, style = MaterialTheme.typography.displayLarge)
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text = if (state.civilsWin) "¡Civiles ganan!" else "¡Los Impostores ganan!",
                    style = MaterialTheme.typography.displaySmall,
                    color = if (state.civilsWin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                if (state.civilsWin) {
                    InfoCard(label = "La palabra era", value = "«${state.civilWord}»")
                    if (showImpostorWordOnReveal(state)) {
                        Spacer(Modifier.height(12.dp))
                        InfoCard(
                            label = if (state.mode == GameMode.CLASICO) "Pista del impostor" else "Palabra del impostor",
                            value = "«${state.impostorWord}»"
                        )
                    }
                } else {
                    Text(
                        text = "Los civiles no han identificado a todos los impostores.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "Impostores",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    val playerColors = androidx.compose.runtime.remember(state.allPlayerNames) {
                        colorAssignments(state.allPlayerNames)
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.impostorNames.forEach { name ->
                            ImpostorChip(
                                name = name,
                                color = playerColors[name] ?: colorForName(name)
                            )
                        }
                    }
                }
            }
            Column {
                PrimaryButton(text = "Nueva Partida", onClick = {
                    viewModel.restartGame(onReady = onNewGame)
                })
                Spacer(Modifier.height(8.dp))
                SecondaryButton(text = "Inicio", onClick = {
                    viewModel.clearSession()
                    onHome()
                })
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

private fun showImpostorWordOnReveal(state: ResultUiState): Boolean {
    return when (state.mode) {
        GameMode.CLASICO -> state.showHint
        GameMode.TODOS_CON_PALABRA -> true
    }
}

@Composable
private fun InfoCard(label: String, value: String) {
    Box(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(20.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = androidx.compose.ui.Modifier.fillMaxWidth()) {
            Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleSmall)
            Spacer(androidx.compose.ui.Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ImpostorChip(name: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = androidx.compose.ui.Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        PlayerAvatar(name = name, size = 28, color = color)
        Spacer(androidx.compose.ui.Modifier.size(8.dp))
        Text(name, color = Color.White, style = MaterialTheme.typography.titleSmall)
    }
}

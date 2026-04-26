package com.davilex.impostor.ui.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.davilex.impostor.data.local.PlayerEntity
import com.davilex.impostor.domain.model.GameMode
import com.davilex.impostor.ui.components.PlayerAvatar
import com.davilex.impostor.ui.components.PrimaryButton

@Composable
fun SetupScreen(
    onBack: () -> Unit,
    onOpenCategories: () -> Unit,
    onStartGame: () -> Unit,
    viewModel: SetupViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            TopBar(onBack = onBack)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Configurar Partida",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(20.dp))

            ModeSelector(
                mode = state.mode,
                onChange = viewModel::setMode
            )

            Spacer(Modifier.height(24.dp))

            PlayersSection(
                players = state.players,
                onAdd = viewModel::addPlayer,
                onRemove = viewModel::removePlayer,
                onRename = viewModel::renamePlayer
            )

            Spacer(Modifier.height(24.dp))

            ImpostorCounter(
                value = state.impostorCount,
                max = state.maxImpostors,
                enabled = state.players.size >= 3,
                onChange = viewModel::setImpostorCount
            )

            if (state.mode == GameMode.CLASICO) {
                Spacer(Modifier.height(16.dp))
                ToggleRow(
                    title = "Pista para Impostores",
                    description = "Muestra una palabra relacionada al impostor.",
                    checked = state.showHint,
                    onCheckedChange = viewModel::setShowHint
                )
            }

            Spacer(Modifier.height(16.dp))

            TimerSection(
                enabled = state.timerEnabled,
                durationSeconds = state.timerDurationSeconds,
                onEnabledChange = viewModel::setTimerEnabled,
                onDurationChange = viewModel::setTimerDuration
            )

            Spacer(Modifier.height(16.dp))

            CategoriesRow(
                selected = state.selectedCategoryIds.size,
                total = state.totalCategories,
                onClick = onOpenCategories
            )

            Spacer(Modifier.height(28.dp))

            PrimaryButton(
                text = "Iniciar Juego",
                enabled = state.canStart,
                leadingIcon = Icons.Filled.PlayArrow,
                onClick = { viewModel.startGame(onReady = onStartGame) }
            )

            Spacer(Modifier.height(24.dp))
        }
    }

    state.errorMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = viewModel::consumeError,
            confirmButton = {
                TextButton(onClick = viewModel::consumeError) { Text("Entendido") }
            },
            title = { Text("Aviso") },
            text = { Text(msg) }
        )
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onBackground)
        }
    }
}

@Composable
private fun ModeSelector(mode: GameMode, onChange: (GameMode) -> Unit) {
    Column {
        SectionTitle(title = "Modo de juego")
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(4.dp)
        ) {
            ModeChip(
                title = "Clásico",
                selected = mode == GameMode.CLASICO,
                onClick = { onChange(GameMode.CLASICO) },
                modifier = Modifier.weight(1f)
            )
            ModeChip(
                title = "Todos con palabra",
                selected = mode == GameMode.TODOS_CON_PALABRA,
                onClick = { onChange(GameMode.TODOS_CON_PALABRA) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ModeChip(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (selected) MaterialTheme.colorScheme.secondary else Color.Transparent
    val fg = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        TextButton(onClick = onClick) {
            Text(text = title, color = fg, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun PlayersSection(
    players: List<PlayerEntity>,
    onAdd: (String) -> Unit,
    onRemove: (Int) -> Unit,
    onRename: (Int, String) -> Unit
) {
    var newName by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<Int?>(null) }
    var editingName by remember { mutableStateOf("") }

    Column {
        SectionTitle(title = "Jugadores", subtitle = "${players.size} jugadores · mín. 3, máx. 24")
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
        ) {
            Column {
                players.forEach { p ->
                    val isEditing = editingId == p.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlayerAvatar(name = p.name, size = 36)
                        Spacer(Modifier.width(12.dp))
                        if (isEditing) {
                            OutlinedTextField(
                                value = editingName,
                                onValueChange = { editingName = it },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                colors = textFieldColors(),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    onRename(p.id, editingName.trim())
                                    editingId = null
                                })
                            )
                            IconButton(onClick = {
                                onRename(p.id, editingName.trim())
                                editingId = null
                            }) {
                                Icon(Icons.Filled.Check, contentDescription = "Confirmar", tint = MaterialTheme.colorScheme.primary)
                            }
                        } else {
                            Text(
                                text = p.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = {
                                editingId = p.id
                                editingName = p.name
                            }) {
                                Icon(Icons.Filled.Edit, contentDescription = "Renombrar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { onRemove(p.id) }) {
                                Icon(Icons.Filled.Close, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                if (players.size < 24) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            placeholder = { Text("Nombre del jugador") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = textFieldColors(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                if (newName.isNotBlank()) {
                                    onAdd(newName)
                                    newName = ""
                                }
                            })
                        )
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (newName.isNotBlank()) {
                                    onAdd(newName)
                                    newName = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = "Añadir jugador",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImpostorCounter(
    value: Int,
    max: Int,
    enabled: Boolean,
    onChange: (Int) -> Unit
) {
    Column {
        SectionTitle(title = "Número de Impostores", subtitle = "Entre 1 y $max")
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Impostores", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
            CounterButton(icon = Icons.Filled.Remove, enabled = enabled && value > 1) {
                onChange((value - 1).coerceAtLeast(1))
            }
            Text(
                text = "$value",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            CounterButton(icon = Icons.Filled.Add, enabled = enabled && value < max) {
                onChange((value + 1).coerceAtMost(max))
            }
        }
    }
}

@Composable
private fun CounterButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val bg = if (enabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    val fg = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bg)
    ) {
        Icon(icon, contentDescription = null, tint = fg)
    }
}

@Composable
private fun TimerSection(
    enabled: Boolean,
    durationSeconds: Int,
    onEnabledChange: (Boolean) -> Unit,
    onDurationChange: (Int) -> Unit
) {
    Column {
        ToggleRow(
            title = "Cronómetro",
            description = "Tiempo de discusión por ronda.",
            checked = enabled,
            onCheckedChange = onEnabledChange
        )
        if (enabled) {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val mins = durationSeconds / 60
                val secs = durationSeconds % 60
                val label = if (secs == 0) "${mins} min" else "${mins} min ${secs}s"
                Text("Duración", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                CounterButton(icon = Icons.Filled.Remove, enabled = durationSeconds > 60) {
                    onDurationChange((durationSeconds - 30).coerceAtLeast(60))
                }
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                CounterButton(icon = Icons.Filled.Add, enabled = durationSeconds < 600) {
                    onDurationChange((durationSeconds + 30).coerceAtMost(600))
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
            Text(
                description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun CategoriesRow(
    selected: Int,
    total: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text("Paquetes de palabras", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleSmall)
            Text(
                text = "$selected de $total seleccionados",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        TextButton(onClick = onClick) {
            Text("Editar", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SectionTitle(title: String, subtitle: String? = null) {
    Column {
        Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
        if (subtitle != null) {
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    cursorColor = MaterialTheme.colorScheme.primary,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline
)

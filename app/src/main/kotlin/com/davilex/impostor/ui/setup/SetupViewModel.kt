package com.davilex.impostor.ui.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davilex.impostor.data.assets.CategoryRepository
import com.davilex.impostor.data.local.PlayerEntity
import com.davilex.impostor.data.local.PlayerRepository
import com.davilex.impostor.data.preferences.SettingsRepository
import com.davilex.impostor.domain.model.Category
import com.davilex.impostor.domain.model.GameConfig
import com.davilex.impostor.domain.model.GameMode
import com.davilex.impostor.domain.usecase.GameSession
import com.davilex.impostor.domain.usecase.StartGameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SetupUiState(
    val mode: GameMode = GameMode.CLASICO,
    val players: List<PlayerEntity> = emptyList(),
    val impostorCount: Int = 1,
    val showHint: Boolean = true,
    val timerEnabled: Boolean = true,
    val timerDurationSeconds: Int = 180,
    val selectedCategoryIds: Set<String> = emptySet(),
    val totalCategories: Int = 0,
    val errorMessage: String? = null,
    val canStart: Boolean = false,
    val isStarting: Boolean = false
) {
    val maxImpostors: Int
        get() = ((players.size - 1) / 2).coerceAtLeast(1)
}

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val settingsRepository: SettingsRepository,
    private val categoryRepository: CategoryRepository,
    private val startGameUseCase: StartGameUseCase,
    private val gameSession: GameSession
) : ViewModel() {

    private val transient = MutableStateFlow(TransientState())

    private val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())

    val uiState: StateFlow<SetupUiState> = combine(
        playerRepository.observePlayers(),
        settingsRepository.settings,
        categoriesFlow,
        transient
    ) { players, settings, categories, transient ->
        val maxImpostors = ((players.size - 1) / 2).coerceAtLeast(1)
        val clampedImpostors = settings.impostorCount.coerceIn(1, maxImpostors)
        val effectiveSelected = if (settings.selectedCategoryIds.isEmpty() && categories.isNotEmpty()) {
            categories.map { it.id }.toSet()
        } else settings.selectedCategoryIds.intersect(categories.map { it.id }.toSet())

        val canStart = players.size >= 3 &&
            clampedImpostors in 1..maxImpostors &&
            effectiveSelected.isNotEmpty() &&
            !transient.isStarting

        SetupUiState(
            mode = settings.mode,
            players = players,
            impostorCount = clampedImpostors,
            showHint = settings.showHint,
            timerEnabled = settings.timerEnabled,
            timerDurationSeconds = settings.timerDurationSeconds,
            selectedCategoryIds = effectiveSelected,
            totalCategories = categories.size,
            errorMessage = transient.errorMessage,
            canStart = canStart,
            isStarting = transient.isStarting
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SetupUiState()
    )

    init {
        viewModelScope.launch {
            categoriesFlow.value = categoryRepository.getCategories()
            ensureDefaultCategories()
        }
    }

    private suspend fun ensureDefaultCategories() {
        val firstSettings = settingsRepository.settings.first()
        if (firstSettings.selectedCategoryIds.isEmpty() && categoriesFlow.value.isNotEmpty()) {
            settingsRepository.setSelectedCategories(categoriesFlow.value.map { it.id }.toSet())
        }
    }

    fun setMode(mode: GameMode) = viewModelScope.launch {
        settingsRepository.setMode(mode)
    }

    fun addPlayer(name: String) = viewModelScope.launch {
        val result = playerRepository.add(name)
        if (result.isFailure) {
            transient.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
        }
    }

    fun removePlayer(id: Int) = viewModelScope.launch {
        playerRepository.remove(id)
    }

    fun renamePlayer(id: Int, newName: String) = viewModelScope.launch {
        val result = playerRepository.rename(id, newName)
        if (result.isFailure) {
            transient.update { it.copy(errorMessage = result.exceptionOrNull()?.message) }
        }
    }

    fun setImpostorCount(count: Int) = viewModelScope.launch {
        settingsRepository.setImpostorCount(count)
    }

    fun setShowHint(value: Boolean) = viewModelScope.launch {
        settingsRepository.setShowHint(value)
    }

    fun setTimerEnabled(value: Boolean) = viewModelScope.launch {
        settingsRepository.setTimerEnabled(value)
    }

    fun setTimerDuration(seconds: Int) = viewModelScope.launch {
        settingsRepository.setTimerDuration(seconds)
    }

    fun consumeError() {
        transient.update { it.copy(errorMessage = null) }
    }

    fun startGame(onReady: () -> Unit) = viewModelScope.launch {
        val state = uiState.value
        if (!state.canStart) return@launch
        transient.update { it.copy(isStarting = true) }
        val config = GameConfig(
            mode = state.mode,
            playerNames = state.players.map { it.name },
            impostorCount = state.impostorCount,
            showHint = state.showHint,
            timerEnabled = state.timerEnabled,
            timerDurationSeconds = state.timerDurationSeconds,
            selectedCategoryIds = state.selectedCategoryIds.toList()
        )
        val result = startGameUseCase(config)
        transient.update { it.copy(isStarting = false) }
        result.fold(
            onSuccess = {
                gameSession.store(it)
                onReady()
            },
            onFailure = { err ->
                transient.update { it.copy(errorMessage = err.message) }
            }
        )
    }
}

private data class TransientState(
    val isStarting: Boolean = false,
    val errorMessage: String? = null
)

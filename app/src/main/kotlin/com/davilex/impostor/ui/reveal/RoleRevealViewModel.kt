package com.davilex.impostor.ui.reveal

import androidx.lifecycle.ViewModel
import com.davilex.impostor.domain.model.GameMode
import com.davilex.impostor.domain.model.GameState
import com.davilex.impostor.domain.model.PlayerAssignment
import com.davilex.impostor.domain.usecase.GameSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class RevealUiState(
    val playerName: String = "",
    val current: PlayerAssignment? = null,
    val mode: GameMode = GameMode.CLASICO,
    val index: Int = 0,
    val total: Int = 0,
    val allPlayerNames: List<String> = emptyList(),
    val revealed: Boolean = false,
    val finished: Boolean = false
) {
    val isLast: Boolean get() = index == (total - 1)
}

@HiltViewModel
class RoleRevealViewModel @Inject constructor(
    private val gameSession: GameSession
) : ViewModel() {

    private val _state = MutableStateFlow(RevealUiState())
    val state: StateFlow<RevealUiState> = _state.asStateFlow()

    init {
        val game = gameSession.current
        if (game != null) {
            applyAssignment(game, 0)
        }
    }

    private fun applyAssignment(game: GameState, index: Int) {
        val assignment = game.assignments[index]
        _state.value = RevealUiState(
            playerName = assignment.playerName,
            current = assignment,
            mode = game.config.mode,
            index = index,
            total = game.assignments.size,
            allPlayerNames = game.config.playerNames,
            revealed = false,
            finished = false
        )
    }

    fun markRevealed() {
        _state.update { it.copy(revealed = true) }
    }

    fun next(): Boolean {
        val game = gameSession.current ?: return true
        val current = _state.value
        if (!current.revealed) return false
        if (current.isLast) {
            _state.update { it.copy(finished = true) }
            return true
        }
        applyAssignment(game, current.index + 1)
        return false
    }
}

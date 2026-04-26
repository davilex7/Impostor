package com.davilex.impostor.ui.voting

import androidx.lifecycle.ViewModel
import com.davilex.impostor.domain.usecase.GameSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class VotingUiState(
    val players: List<String> = emptyList(),
    val maxSelections: Int = 1,
    val selected: Set<String> = emptySet()
) {
    val canConfirm: Boolean get() = selected.size == maxSelections
}

@HiltViewModel
class VotingViewModel @Inject constructor(
    private val gameSession: GameSession
) : ViewModel() {

    private val _state = MutableStateFlow(VotingUiState())
    val state: StateFlow<VotingUiState> = _state.asStateFlow()

    init {
        val game = gameSession.current
        if (game != null) {
            _state.value = VotingUiState(
                players = game.config.playerNames,
                maxSelections = game.config.impostorCount,
                selected = emptySet()
            )
        }
    }

    fun toggle(name: String) {
        _state.update { current ->
            val next = current.selected.toMutableSet()
            if (name in next) {
                next.remove(name)
            } else {
                if (current.maxSelections == 1) {
                    next.clear()
                    next.add(name)
                } else if (next.size < current.maxSelections) {
                    next.add(name)
                }
            }
            current.copy(selected = next)
        }
    }

    fun confirm() {
        gameSession.votedImpostors = _state.value.selected.toList()
    }
}

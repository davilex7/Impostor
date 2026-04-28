package com.davilex.impostor.ui.result

import androidx.lifecycle.ViewModel
import com.davilex.impostor.domain.model.GameMode
import com.davilex.impostor.domain.usecase.GameSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class ResultUiState(
    val civilsWin: Boolean = false,
    val civilWord: String = "",
    val impostorWord: String = "",
    val impostorNames: List<String> = emptyList(),
    val votedNames: List<String> = emptyList(),
    val allPlayerNames: List<String> = emptyList(),
    val mode: GameMode = GameMode.CLASICO,
    val showHint: Boolean = false
)

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val gameSession: GameSession
) : ViewModel() {

    private val _state = MutableStateFlow(ResultUiState())
    val state: StateFlow<ResultUiState> = _state.asStateFlow()

    init {
        val game = gameSession.current
        val voted = gameSession.votedImpostors
        if (game != null) {
            val impostorNames = game.impostorNames.toSet()
            val civilsWin = voted.toSet() == impostorNames
            _state.value = ResultUiState(
                civilsWin = civilsWin,
                civilWord = game.civilWord,
                impostorWord = game.impostorWord,
                impostorNames = game.impostorNames,
                votedNames = voted,
                allPlayerNames = game.config.playerNames,
                mode = game.config.mode,
                showHint = game.config.showHint
            )
        }
    }

    fun clearSession() {
        gameSession.clear()
    }
}

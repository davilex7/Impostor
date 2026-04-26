package com.davilex.impostor.ui.game

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davilex.impostor.domain.usecase.GameSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DebateUiState(
    val starterName: String = "",
    val timerEnabled: Boolean = false,
    val totalSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val running: Boolean = false,
    val finished: Boolean = false
)

@HiltViewModel
class DebateViewModel @Inject constructor(
    private val gameSession: GameSession
) : ViewModel() {

    private val _state = MutableStateFlow(DebateUiState())
    val state: StateFlow<DebateUiState> = _state.asStateFlow()

    private var tickerJob: Job? = null

    init {
        val game = gameSession.current
        if (game != null) {
            _state.value = DebateUiState(
                starterName = game.starterName,
                timerEnabled = game.config.timerEnabled,
                totalSeconds = game.config.timerDurationSeconds,
                remainingSeconds = game.config.timerDurationSeconds,
                running = game.config.timerEnabled,
                finished = false
            )
            if (game.config.timerEnabled) startTicker()
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (_state.value.remainingSeconds > 0 && _state.value.running) {
                delay(1_000)
                _state.update {
                    val next = (it.remainingSeconds - 1).coerceAtLeast(0)
                    it.copy(remainingSeconds = next, finished = next == 0)
                }
            }
        }
    }

    fun toggleTimer() {
        if (!_state.value.timerEnabled) return
        if (_state.value.finished) return
        val nextRunning = !_state.value.running
        _state.update { it.copy(running = nextRunning) }
        if (nextRunning) startTicker() else tickerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        tickerJob?.cancel()
    }
}

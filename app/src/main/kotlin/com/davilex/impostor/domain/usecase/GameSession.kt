package com.davilex.impostor.domain.usecase

import com.davilex.impostor.domain.model.GameState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameSession @Inject constructor() {
    @Volatile
    var current: GameState? = null
        private set

    @Volatile
    var votedImpostors: List<String> = emptyList()

    fun store(state: GameState) {
        current = state
        votedImpostors = emptyList()
    }

    fun clear() {
        current = null
        votedImpostors = emptyList()
    }
}

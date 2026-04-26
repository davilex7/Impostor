package com.davilex.impostor.domain.model

data class GameState(
    val config: GameConfig,
    val assignments: List<PlayerAssignment>,
    val starterName: String,
    val civilWord: String,
    val impostorWord: String
) {
    val impostorNames: List<String>
        get() = assignments.filter { it.isImpostor }.map { it.playerName }
}

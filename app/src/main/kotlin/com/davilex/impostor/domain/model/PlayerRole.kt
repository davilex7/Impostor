package com.davilex.impostor.domain.model

sealed class PlayerRole {
    data class Civil(val word: String) : PlayerRole()
    data class Impostor(val hint: String?) : PlayerRole()
    data class WordOnly(val word: String, val isImpostor: Boolean) : PlayerRole()
}

data class PlayerAssignment(
    val playerName: String,
    val role: PlayerRole
) {
    val isImpostor: Boolean
        get() = when (role) {
            is PlayerRole.Impostor -> true
            is PlayerRole.WordOnly -> role.isImpostor
            is PlayerRole.Civil -> false
        }
}

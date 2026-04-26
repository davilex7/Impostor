package com.davilex.impostor.domain.model

data class GameConfig(
    val mode: GameMode,
    val playerNames: List<String>,
    val impostorCount: Int,
    val showHint: Boolean,
    val timerEnabled: Boolean,
    val timerDurationSeconds: Int,
    val selectedCategoryIds: List<String>
) {
    val maxImpostors: Int
        get() = ((playerNames.size - 1) / 2).coerceAtLeast(1)

    fun validate(): Result<Unit> {
        if (playerNames.size < 3) {
            return Result.failure(IllegalStateException("Se necesitan al menos 3 jugadores"))
        }
        if (impostorCount < 1 || impostorCount > maxImpostors) {
            return Result.failure(IllegalStateException("Número de impostores inválido"))
        }
        if (selectedCategoryIds.isEmpty()) {
            return Result.failure(IllegalStateException("Selecciona al menos una categoría"))
        }
        if (timerEnabled && (timerDurationSeconds < 60 || timerDurationSeconds > 600)) {
            return Result.failure(IllegalStateException("Duración del cronómetro inválida"))
        }
        return Result.success(Unit)
    }
}

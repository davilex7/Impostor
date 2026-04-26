package com.davilex.impostor.domain.usecase

import com.davilex.impostor.data.assets.CategoryRepository
import com.davilex.impostor.domain.model.Category
import com.davilex.impostor.domain.model.GameConfig
import com.davilex.impostor.domain.model.GameMode
import com.davilex.impostor.domain.model.GameState
import com.davilex.impostor.domain.model.PlayerAssignment
import com.davilex.impostor.domain.model.PlayerRole
import com.davilex.impostor.domain.model.WordPair
import javax.inject.Inject
import kotlin.random.Random

class StartGameUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(
        config: GameConfig,
        random: Random = Random.Default
    ): Result<GameState> {
        config.validate().onFailure { return Result.failure(it) }

        val allCategories = categoryRepository.getCategories()
        return invokeWithCategories(config, allCategories, random)
    }

    fun invokeWithCategories(
        config: GameConfig,
        allCategories: List<Category>,
        random: Random = Random.Default
    ): Result<GameState> {
        val pool: List<WordPair> = allCategories
            .filter { it.id in config.selectedCategoryIds }
            .flatMap { it.words }
            .distinct()

        if (pool.isEmpty()) {
            return Result.failure(IllegalStateException("No hay palabras en las categorías seleccionadas"))
        }

        val pair = pool.random(random)

        val shuffledNames = config.playerNames.shuffled(random)
        val impostorNames = shuffledNames.take(config.impostorCount).toSet()

        val assignments = shuffledNames.map { name ->
            val role = buildRole(
                isImpostor = name in impostorNames,
                mode = config.mode,
                showHint = config.showHint,
                pair = pair
            )
            PlayerAssignment(playerName = name, role = role)
        }

        val revealOrder = assignments.shuffled(random)
        val starter = config.playerNames.random(random)

        return Result.success(
            GameState(
                config = config,
                assignments = revealOrder,
                starterName = starter,
                civilWord = pair.civil,
                impostorWord = pair.impostor
            )
        )
    }

    private fun buildRole(
        isImpostor: Boolean,
        mode: GameMode,
        showHint: Boolean,
        pair: WordPair
    ): PlayerRole {
        return when (mode) {
            GameMode.CLASICO -> if (isImpostor) {
                PlayerRole.Impostor(hint = if (showHint) pair.impostor else null)
            } else {
                PlayerRole.Civil(word = pair.civil)
            }
            GameMode.TODOS_CON_PALABRA -> {
                val word = if (isImpostor) pair.impostor else pair.civil
                PlayerRole.WordOnly(word = word, isImpostor = isImpostor)
            }
        }
    }
}

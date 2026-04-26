package com.davilex.impostor.domain.usecase

import com.davilex.impostor.data.assets.CategoryRepository
import com.davilex.impostor.domain.model.Category
import com.davilex.impostor.domain.model.GameConfig
import com.davilex.impostor.domain.model.GameMode
import com.davilex.impostor.domain.model.PlayerRole
import com.davilex.impostor.domain.model.WordPair
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class StartGameUseCaseTest {

    private val categories = listOf(
        Category(
            id = "vida",
            name = "Vida",
            emoji = "⏰",
            words = listOf(
                WordPair("Champú", "Sábila"),
                WordPair("Despertador", "Café")
            )
        ),
        Category(
            id = "tech",
            name = "Tecnología",
            emoji = "💻",
            words = listOf(
                WordPair("Teclado", "Ratón"),
                WordPair("Móvil", "Tablet")
            )
        )
    )

    private val useCase = StartGameUseCase(mockk())

    private fun baseConfig(
        mode: GameMode = GameMode.CLASICO,
        players: List<String> = listOf("Ana", "Beatriz", "Carlos", "Diana"),
        impostorCount: Int = 1,
        showHint: Boolean = true,
        timerEnabled: Boolean = true,
        timerSeconds: Int = 180,
        categoryIds: List<String> = listOf("vida", "tech")
    ) = GameConfig(
        mode = mode,
        playerNames = players,
        impostorCount = impostorCount,
        showHint = showHint,
        timerEnabled = timerEnabled,
        timerDurationSeconds = timerSeconds,
        selectedCategoryIds = categoryIds
    )

    @Test
    fun `validation fails with fewer than 3 players`() {
        val config = baseConfig(players = listOf("Ana", "Beatriz"))
        val result = useCase.invokeWithCategories(config, categories, Random(42))
        assertTrue(result.isFailure)
    }

    @Test
    fun `validation fails with no categories`() {
        val config = baseConfig(categoryIds = emptyList())
        val result = useCase.invokeWithCategories(config, categories, Random(42))
        assertTrue(result.isFailure)
    }

    @Test
    fun `validation fails when impostor count exceeds half`() {
        val config = baseConfig(players = listOf("A", "B", "C", "D"), impostorCount = 3)
        val result = useCase.invokeWithCategories(config, categories, Random(42))
        assertTrue(result.isFailure)
    }

    @Test
    fun `classic mode assigns one civil with the civil word and the impostor with the hint`() {
        val config = baseConfig(impostorCount = 1, showHint = true)
        val result = useCase.invokeWithCategories(config, categories, Random(123))
        assertTrue(result.isSuccess)
        val state = result.getOrThrow()

        val impostors = state.assignments.filter { it.isImpostor }
        val civils = state.assignments.filter { !it.isImpostor }
        assertEquals(1, impostors.size)
        assertEquals(3, civils.size)

        val civilWord = state.civilWord
        val hint = state.impostorWord
        civils.forEach { a ->
            val role = a.role as PlayerRole.Civil
            assertEquals(civilWord, role.word)
        }
        impostors.forEach { a ->
            val role = a.role as PlayerRole.Impostor
            assertEquals(hint, role.hint)
        }
    }

    @Test
    fun `classic mode without hint hides the impostor word`() {
        val config = baseConfig(showHint = false)
        val result = useCase.invokeWithCategories(config, categories, Random(7))
        val state = result.getOrThrow()
        val impostor = state.assignments.first { it.isImpostor }
        val role = impostor.role as PlayerRole.Impostor
        assertNull(role.hint)
    }

    @Test
    fun `todos con palabra mode shows civil word to civils and impostor word to impostors`() {
        val config = baseConfig(mode = GameMode.TODOS_CON_PALABRA, impostorCount = 1)
        val result = useCase.invokeWithCategories(config, categories, Random(99))
        val state = result.getOrThrow()
        state.assignments.forEach { a ->
            val role = a.role as PlayerRole.WordOnly
            if (a.isImpostor) {
                assertEquals(state.impostorWord, role.word)
                assertTrue(role.isImpostor)
            } else {
                assertEquals(state.civilWord, role.word)
                assertTrue(!role.isImpostor)
            }
        }
    }

    @Test
    fun `selecting an unknown category id is ignored gracefully`() {
        val config = baseConfig(categoryIds = listOf("vida"))
        val result = useCase.invokeWithCategories(config, categories, Random(0))
        assertTrue(result.isSuccess)
        val state = result.getOrThrow()
        // Word must come from "vida" pool only
        val vida = categories.first { it.id == "vida" }.words
        val match = vida.any { it.civil == state.civilWord && it.impostor == state.impostorWord }
        assertTrue("Word pair should be from selected category", match)
    }

    @Test
    fun `assignments include all configured players exactly once`() {
        val players = listOf("A", "B", "C", "D", "E", "F")
        val config = baseConfig(players = players, impostorCount = 2)
        val result = useCase.invokeWithCategories(config, categories, Random(1))
        val state = result.getOrThrow()
        assertEquals(players.toSet(), state.assignments.map { it.playerName }.toSet())
        assertEquals(players.size, state.assignments.size)
    }

    @Test
    fun `starter is one of the configured players`() {
        val players = listOf("A", "B", "C", "D")
        val config = baseConfig(players = players)
        val result = useCase.invokeWithCategories(config, categories, Random(1))
        val state = result.getOrThrow()
        assertNotNull(state.starterName)
        assertTrue(state.starterName in players)
    }

    @Test
    fun `game state preserves impostor count`() {
        val config = baseConfig(
            players = listOf("A", "B", "C", "D", "E"),
            impostorCount = 2
        )
        val result = useCase.invokeWithCategories(config, categories, Random(13))
        val state = result.getOrThrow()
        assertEquals(2, state.impostorNames.size)
    }
}

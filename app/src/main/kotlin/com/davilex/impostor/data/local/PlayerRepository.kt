package com.davilex.impostor.data.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerRepository @Inject constructor(
    private val dao: PlayerDao
) {
    fun observePlayers(): Flow<List<PlayerEntity>> = dao.observeAll()

    fun observeNames(): Flow<List<String>> = dao.observeAll().map { list -> list.map { it.name } }

    suspend fun add(name: String): Result<Unit> {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("El nombre no puede estar vacío"))
        }
        if (dao.countByName(trimmed) > 0) {
            return Result.failure(IllegalStateException("Ese jugador ya existe"))
        }
        dao.insert(PlayerEntity(name = trimmed))
        return Result.success(Unit)
    }

    suspend fun rename(id: Int, newName: String): Result<Unit> {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) {
            return Result.failure(IllegalArgumentException("El nombre no puede estar vacío"))
        }
        if (dao.countByName(trimmed) > 0) {
            return Result.failure(IllegalStateException("Ese jugador ya existe"))
        }
        dao.update(PlayerEntity(id = id, name = trimmed))
        return Result.success(Unit)
    }

    suspend fun remove(id: Int) {
        dao.deleteById(id)
    }
}

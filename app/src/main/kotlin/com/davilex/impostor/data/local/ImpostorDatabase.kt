package com.davilex.impostor.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [PlayerEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ImpostorDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao

    companion object {
        const val NAME = "impostor.db"
    }
}

package com.davilex.impostor.data.assets

import android.content.Context
import com.davilex.impostor.domain.model.CategoriesPayload
import com.davilex.impostor.domain.model.Category
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val mutex = Mutex()
    private var cache: List<Category>? = null

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getCategories(): List<Category> {
        cache?.let { return it }
        return mutex.withLock {
            cache ?: loadFromAssets().also { cache = it }
        }
    }

    private suspend fun loadFromAssets(): List<Category> = withContext(Dispatchers.IO) {
        context.assets.open(ASSET_PATH).bufferedReader().use { reader ->
            val text = reader.readText()
            json.decodeFromString(CategoriesPayload.serializer(), text).categories
        }
    }

    companion object {
        private const val ASSET_PATH = "categories.json"
    }
}

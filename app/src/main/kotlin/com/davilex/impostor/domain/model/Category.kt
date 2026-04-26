package com.davilex.impostor.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WordPair(
    val civil: String,
    val impostor: String
)

@Serializable
data class Category(
    val id: String,
    val name: String,
    val emoji: String,
    val words: List<WordPair>
)

@Serializable
data class CategoriesPayload(
    val categories: List<Category>
)

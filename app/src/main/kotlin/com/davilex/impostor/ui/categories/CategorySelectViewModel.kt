package com.davilex.impostor.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.davilex.impostor.data.assets.CategoryRepository
import com.davilex.impostor.data.preferences.SettingsRepository
import com.davilex.impostor.domain.model.Category
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val selectedIds: Set<String> = emptySet()
) {
    val canContinue: Boolean get() = selectedIds.isNotEmpty()
}

@HiltViewModel
class CategorySelectViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val categoriesFlow = MutableStateFlow<List<Category>>(emptyList())

    val uiState: StateFlow<CategoryUiState> = combine(
        categoriesFlow,
        settingsRepository.settings
    ) { categories, settings ->
        CategoryUiState(
            categories = categories,
            selectedIds = settings.selectedCategoryIds
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CategoryUiState()
    )

    init {
        viewModelScope.launch {
            categoriesFlow.value = categoryRepository.getCategories()
        }
    }

    fun toggle(id: String) = viewModelScope.launch {
        val current = uiState.value.selectedIds.toMutableSet()
        if (id in current) current.remove(id) else current.add(id)
        if (current.isNotEmpty()) {
            settingsRepository.setSelectedCategories(current)
        } else {
            settingsRepository.setSelectedCategories(setOf(id))
        }
    }

    fun selectAll() = viewModelScope.launch {
        settingsRepository.setSelectedCategories(uiState.value.categories.map { it.id }.toSet())
    }
}

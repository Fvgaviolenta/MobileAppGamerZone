package com.example.appgamerzone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appgamerzone.data.model.Product
import com.example.appgamerzone.data.repository.FirebaseProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProductsUiState(
    val products: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String? = null,
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class FirebaseProductViewModel(
    private val repository: FirebaseProductRepository = FirebaseProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductsUiState())
    val uiState: StateFlow<ProductsUiState> = _uiState.asStateFlow()

    init {
        loadProducts()
        loadCategories()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = repository.getAllProducts()
            result.onSuccess { products ->
                _uiState.value = _uiState.value.copy(
                    products = products,
                    filteredProducts = products,
                    isLoading = false
                )
                filterProducts()
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val result = repository.getCategories()
            result.onSuccess { categories ->
                _uiState.value = _uiState.value.copy(categories = categories)
            }
        }
    }

    fun selectCategory(category: String?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
        filterProducts()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        filterProducts()
    }

    private fun filterProducts() {
        val currentState = _uiState.value
        var filtered = currentState.products

        // Filtrar por categoría
        currentState.selectedCategory?.let { category ->
            filtered = filtered.filter { it.category == category }
        }

        // Filtrar por búsqueda
        if (currentState.searchQuery.isNotEmpty()) {
            filtered = filtered.filter {
                it.name.contains(currentState.searchQuery, ignoreCase = true) ||
                it.description.contains(currentState.searchQuery, ignoreCase = true) ||
                it.category.contains(currentState.searchQuery, ignoreCase = true)
            }
        }

        _uiState.value = currentState.copy(filteredProducts = filtered)
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            selectedCategory = null,
            searchQuery = ""
        )
        filterProducts()
    }
}


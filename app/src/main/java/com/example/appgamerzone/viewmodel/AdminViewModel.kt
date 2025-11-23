package com.example.appgamerzone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appgamerzone.data.model.Product
import com.example.appgamerzone.data.repository.FirebaseProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AdminProductsUiState(
    val products: List<Product> = emptyList(),
    val categories: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val selectedProduct: Product? = null,
    val isEditing: Boolean = false
)

class AdminViewModel(
    private val repository: FirebaseProductRepository = FirebaseProductRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProductsUiState())
    val uiState: StateFlow<AdminProductsUiState> = _uiState.asStateFlow()

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
                    isLoading = false
                )
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

    fun selectProduct(product: Product) {
        _uiState.value = _uiState.value.copy(
            selectedProduct = product,
            isEditing = true
        )
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(
            selectedProduct = null,
            isEditing = false
        )
    }

    fun createProduct(product: Product) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)

            val result = repository.createProduct(product)
            result.onSuccess {
                loadProducts()
                loadCategories()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Producto creado exitosamente"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }

    fun updateProduct(product: Product) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)

            val result = repository.updateProduct(product)
            result.onSuccess {
                loadProducts()
                loadCategories()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Producto actualizado exitosamente",
                    selectedProduct = null,
                    isEditing = false
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)

            val result = repository.deleteProduct(productId)
            result.onSuccess {
                loadProducts()
                loadCategories()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Producto eliminado exitosamente"
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
    }
}


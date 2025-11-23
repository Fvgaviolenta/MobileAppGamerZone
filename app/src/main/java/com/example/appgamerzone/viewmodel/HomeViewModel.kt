package com.example.appgamerzone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.appgamerzone.data.model.ProductCategory
import com.example.appgamerzone.data.model.Product

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun loadFeaturedProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                featuredProducts = getSampleFeaturedProducts(),
                isLoading = false
            )
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                categories = getSampleCategories()
            )
        }
    }

    private fun getSampleFeaturedProducts(): List<Product> {
        return listOf(
            Product(
                id = "1",
                name = "PlayStation 5",
                price = 549990.0,
                category = "Consolas"
            ),
            Product(
                id = "2",
                name = "PC Gamer ASUS ROG",
                price = 1299990.0,
                category = "Computadores Gamers"
            )
        )
    }

    // En HomeViewModel.kt - actualizar getSampleCategories()
    private fun getSampleCategories(): List<ProductCategory> {
        return listOf(
            ProductCategory("Consolas", "🎮"),
            ProductCategory("Computadores", "💻"),
            ProductCategory("Accesorios", "🎧"),
            ProductCategory("Sillas", "🪑")
        )
    }
}

data class HomeUiState(
    val featuredProducts: List<Product> = emptyList(),
    val categories: List<ProductCategory> = emptyList(),
    val isLoading: Boolean = true,
    val userPoints: Int = 0
)
package com.example.appgamerzone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.appgamerzone.data.model.ProductCategory
import com.example.appgamerzone.data.model.Product
import com.example.appgamerzone.data.model.DolarResponse
import com.example.appgamerzone.data.api.DolarApiService
import android.util.Log

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val dolarApiService = DolarApiService.create()

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

    fun loadDolarPrice() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDolarLoading = true)
            try {
                val dolarData = dolarApiService.getCotizacionUSD()
                _uiState.value = _uiState.value.copy(
                    dolarData = dolarData,
                    isDolarLoading = false,
                    dolarError = null
                )
                Log.d("HomeViewModel", "Dólar cargado: Compra=${dolarData.compra}, Venta=${dolarData.venta}")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDolarLoading = false,
                    dolarError = e.message ?: "Error al cargar cotización"
                )
                Log.e("HomeViewModel", "Error al cargar dólar", e)
            }
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
    val userPoints: Int = 0,
    val dolarData: DolarResponse? = null,
    val isDolarLoading: Boolean = false,
    val dolarError: String? = null
)
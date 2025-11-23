package com.example.appgamerzone.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appgamerzone.data.model.CartItem
import com.example.appgamerzone.data.model.Order
import com.example.appgamerzone.data.repository.FirebaseAuthRepository
import com.example.appgamerzone.data.repository.FirebaseCartRepository
import com.example.appgamerzone.data.repository.FirebaseProductRepository
import com.example.appgamerzone.data.session.UserSessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val total: Double = 0.0,
    val discountCode: String = "",
    val isDiscountApplied: Boolean = false,
    val checkoutSuccess: Boolean = false,
    val lastOrder: Order? = null
) {
    val totalItems: Int
        get() = items.sumOf { it.quantity }
}

class CartViewModel(
    private val context: Context,
    private val cartRepository: FirebaseCartRepository = FirebaseCartRepository(FirebaseProductRepository()),
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "CartViewModel"
    }

    private val sessionManager = UserSessionManager(context)

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        // Cargar el carrito al inicializar el ViewModel
        Log.d(TAG, "🔵 CartViewModel inicializado")

        // Verificar que hay un usuario en sesión
        viewModelScope.launch {
            sessionManager.currentUserId.collect { userId ->
                Log.d(TAG, "🔵 USUARIO EN SESIÓN: ${userId ?: "NULL - NO HAY SESIÓN"}")
                if (userId != null) {
                    Log.d(TAG, "🔵 Cargando carrito para usuario: $userId")
                    loadCart()
                } else {
                    Log.e(TAG, "❌ NO HAY USUARIO EN SESIÓN - No se puede cargar carrito")
                }
            }
        }
    }

    fun loadCart() {
        viewModelScope.launch {
            Log.d(TAG, "")
            Log.d(TAG, "📥 ==========================================")
            Log.d(TAG, "📥 CARGANDO CARRITO")
            Log.d(TAG, "📥 ==========================================")

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val userId = sessionManager.currentUserId.first()
            Log.d(TAG, "🔑 userId: '$userId'")
            Log.d(TAG, "🔑 userId es null: ${userId == null}")
            Log.d(TAG, "🔑 userId es vacío: ${userId?.isEmpty() == true}")
            Log.d(TAG, "🔑 userId length: ${userId?.length ?: 0}")

            if (userId == null || userId.isEmpty()) {
                Log.e(TAG, "❌❌❌ ERROR: Usuario no autenticado o userId vacío ❌❌❌")
                Log.e(TAG, "❌ Por favor, cierra sesión y vuelve a iniciar sesión")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Usuario no autenticado. Por favor, inicia sesión nuevamente."
                )
                return@launch
            }

            Log.d(TAG, "✅ Usuario autenticado con ID válido, cargando desde Firebase...")

            val result = cartRepository.getCart(userId)
            result.onSuccess { items ->
                Log.d(TAG, "✅✅✅ CARRITO CARGADO: ${items.size} items ✅✅✅")
                items.forEachIndexed { index, item ->
                    Log.d(TAG, "  📦 Item $index: ${item.productName} x${item.quantity} - $${item.unitPrice}")
                }
                calculateTotals(items)
                _uiState.value = _uiState.value.copy(
                    items = items,
                    isLoading = false
                )
            }.onFailure { error ->
                Log.e(TAG, "❌❌❌ ERROR al cargar: ${error.message} ❌❌❌", error)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
            Log.d(TAG, "📥 ========================================== FIN")
            Log.d(TAG, "")
        }
    }

    fun addToCart(productId: String, quantity: Int = 1) {
        viewModelScope.launch {
            Log.d(TAG, "")
            Log.d(TAG, "🛒 ==========================================")
            Log.d(TAG, "🛒 INTENTANDO AGREGAR AL CARRITO")
            Log.d(TAG, "🛒 productId: $productId, quantity: $quantity")
            Log.d(TAG, "🛒 ==========================================")

            val userId = sessionManager.currentUserId.first()
            Log.d(TAG, "🔑 userId obtenido: '$userId'")
            Log.d(TAG, "🔑 userId es null: ${userId == null}")
            Log.d(TAG, "🔑 userId es vacío: ${userId?.isEmpty() == true}")
            Log.d(TAG, "🔑 userId length: ${userId?.length ?: 0}")

            if (userId == null || userId.isEmpty()) {
                Log.e(TAG, "❌❌❌ ERROR: Usuario no autenticado o userId vacío ❌❌❌")
                Log.e(TAG, "❌ Por favor, cierra sesión y vuelve a iniciar sesión")
                _uiState.value = _uiState.value.copy(
                    error = "Usuario no autenticado. Por favor, inicia sesión nuevamente."
                )
                return@launch
            }

            Log.d(TAG, "✅ Usuario autenticado con ID válido, llamando al repositorio...")

            val result = cartRepository.addToCart(userId, productId, quantity)
            result.onSuccess { items ->
                Log.d(TAG, "✅✅✅ ÉXITO! Items en carrito: ${items.size} ✅✅✅")
                items.forEachIndexed { index, item ->
                    Log.d(TAG, "  📦 Item $index: ${item.productName} x${item.quantity}")
                }
                calculateTotals(items)
                _uiState.value = _uiState.value.copy(
                    items = items,
                    error = null
                )
                Log.d(TAG, "🛒 Estado actualizado con ${items.size} items")
            }.onFailure { error ->
                Log.e(TAG, "❌❌❌ ERROR al agregar: ${error.message} ❌❌❌", error)
                _uiState.value = _uiState.value.copy(
                    error = error.message ?: "Error al agregar al carrito"
                )
            }
            Log.d(TAG, "🛒 ========================================== FIN")
            Log.d(TAG, "")
        }
    }

    fun updateQuantity(productId: String, newQuantity: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val userId = sessionManager.currentUserId.first()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Usuario no autenticado"
                )
                return@launch
            }

            val result = cartRepository.updateCartItemQuantity(userId, productId, newQuantity)
            result.onSuccess { items ->
                calculateTotals(items)
                _uiState.value = _uiState.value.copy(
                    items = items,
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

    fun removeFromCart(productId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val userId = sessionManager.currentUserId.first()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Usuario no autenticado"
                )
                return@launch
            }

            val result = cartRepository.removeFromCart(userId, productId)
            result.onSuccess { items ->
                calculateTotals(items)
                _uiState.value = _uiState.value.copy(
                    items = items,
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

    fun applyDiscountCode(code: String) {
        val isValid = cartRepository.validateDiscountCode(code)

        if (isValid) {
            _uiState.value = _uiState.value.copy(
                discountCode = code,
                isDiscountApplied = true,
                error = null
            )
            calculateTotals(_uiState.value.items, code)
        } else {
            _uiState.value = _uiState.value.copy(
                error = "Código de descuento inválido",
                discountCode = "",
                isDiscountApplied = false
            )
            calculateTotals(_uiState.value.items)
        }
    }

    fun checkout() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val userId = sessionManager.currentUserId.first()
            if (userId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Usuario no autenticado"
                )
                return@launch
            }

            val userResult = authRepository.getUserById(userId)
            userResult.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message
                )
                return@launch
            }

            val user = userResult.getOrNull()!!
            val discountCode = if (_uiState.value.isDiscountApplied) {
                _uiState.value.discountCode
            } else {
                null
            }

            val result = cartRepository.checkout(userId, user, discountCode)
            result.onSuccess { order ->
                _uiState.value = CartUiState(
                    checkoutSuccess = true,
                    lastOrder = order
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }

    fun resetCheckoutState() {
        _uiState.value = CartUiState()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun calculateTotals(items: List<CartItem>, discountCode: String? = null) {
        val subtotal = items.sumOf { it.subtotal }
        val discount = when (discountCode?.uppercase()) {
            "GAMER10" -> subtotal * 0.10
            "GAMER20" -> subtotal * 0.20
            "DUOC50" -> subtotal * 0.50
            else -> 0.0
        }
        val total = subtotal - discount

        _uiState.value = _uiState.value.copy(
            subtotal = subtotal,
            discount = discount,
            total = total
        )
    }
}


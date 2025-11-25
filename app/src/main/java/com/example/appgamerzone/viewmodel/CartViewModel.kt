package com.example.appgamerzone.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appgamerzone.data.model.CartItem
import com.example.appgamerzone.data.model.Order
import com.example.appgamerzone.data.repository.FirebaseAuthRepository
import com.example.appgamerzone.data.repository.FirebaseCartRepository
import com.example.appgamerzone.data.repository.FirebaseDiscountRepository
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
    val discountPercentage: Double = 0.0,
    val total: Double = 0.0,
    val discountCode: String = "",
    val discountCodeId: String = "",
    val isDiscountApplied: Boolean = false,
    val discountError: String? = null,
    val checkoutSuccess: Boolean = false,
    val lastOrder: Order? = null
) {
    val totalItems: Int
        get() = items.sumOf { it.quantity }
}

class CartViewModel(
    private val context: Context,
    private val cartRepository: FirebaseCartRepository = FirebaseCartRepository(FirebaseProductRepository()),
    private val authRepository: FirebaseAuthRepository = FirebaseAuthRepository(),
    private val discountRepository: FirebaseDiscountRepository = FirebaseDiscountRepository()
) : ViewModel() {

    companion object {
        private const val TAG = "CartViewModel"
    }

    private val sessionManager = UserSessionManager(context)

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        // Cargar el carrito al inicializar el ViewModel
        Log.d(TAG, "CartViewModel inicializado")
        loadCart()
    }

    fun loadCart() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val userId = sessionManager.currentUserId.first()
            Log.d(TAG, "loadCart - userId: $userId")

            if (userId == null) {
                Log.e(TAG, "loadCart - Usuario no autenticado")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Usuario no autenticado"
                )
                return@launch
            }

            val result = cartRepository.getCart(userId)
            result.onSuccess { items ->
                Log.d(TAG, "loadCart - Items cargados: ${items.size}")
                items.forEachIndexed { index, item ->
                    Log.d(TAG, "  Item $index: ${item.productName} - qty: ${item.quantity}")
                }

                _uiState.value = _uiState.value.copy(
                    items = items,
                    isLoading = false
                )

                // Recalcular totales con descuento actual si existe
                calculateTotals(items, _uiState.value.discountPercentage)
            }.onFailure { error ->
                Log.e(TAG, "loadCart - Error: ${error.message}", error)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }

    fun addToCart(productId: String, quantity: Int = 1) {
        viewModelScope.launch {
            Log.d(TAG, "addToCart - productId: $productId, quantity: $quantity")

            val userId = sessionManager.currentUserId.first()
            Log.d(TAG, "addToCart - userId: $userId")

            if (userId == null) {
                Log.e(TAG, "addToCart - Usuario no autenticado")
                _uiState.value = _uiState.value.copy(
                    error = "Usuario no autenticado"
                )
                return@launch
            }

            val result = cartRepository.addToCart(userId, productId, quantity)
            result.onSuccess { items ->
                Log.d(TAG, "addToCart - Éxito! Items en carrito: ${items.size}")
                _uiState.value = _uiState.value.copy(
                    items = items,
                    error = null
                )
                calculateTotals(items, _uiState.value.discountPercentage)
            }.onFailure { error ->
                Log.e(TAG, "addToCart - Error: ${error.message}", error)
                _uiState.value = _uiState.value.copy(
                    error = error.message ?: "Error al agregar al carrito"
                )
            }
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
                _uiState.value = _uiState.value.copy(
                    items = items,
                    isLoading = false
                )
                calculateTotals(items, _uiState.value.discountPercentage)
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
                _uiState.value = _uiState.value.copy(
                    items = items,
                    isLoading = false
                )
                calculateTotals(items, _uiState.value.discountPercentage)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }

    fun applyDiscountCode(code: String) {
        viewModelScope.launch {
            Log.d(TAG, "applyDiscountCode - Código ingresado: $code")

            // Validar que el código no esté vacío
            if (code.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    discountError = "Ingrese un código de descuento",
                    isDiscountApplied = false
                )
                return@launch
            }

            // Validar el código con Firebase
            val result = discountRepository.validateDiscountCode(code)

            result.onSuccess { discountCode ->
                if (discountCode != null) {
                    // Código válido - aplicar descuento
                    Log.d(TAG, "Código válido: ${discountCode.code} - ${discountCode.discountPercentage}%")

                    _uiState.value = _uiState.value.copy(
                        discountCode = discountCode.code,
                        discountCodeId = discountCode.id,
                        discountPercentage = discountCode.discountPercentage,
                        isDiscountApplied = true,
                        discountError = null
                    )

                    // Recalcular totales con descuento
                    calculateTotals(_uiState.value.items, discountCode.discountPercentage)
                } else {
                    // Código no válido
                    Log.d(TAG, "Código inválido: $code")

                    _uiState.value = _uiState.value.copy(
                        discountError = "Código de descuento inválido o expirado",
                        discountCode = "",
                        discountCodeId = "",
                        discountPercentage = 0.0,
                        isDiscountApplied = false
                    )

                    // Recalcular sin descuento
                    calculateTotals(_uiState.value.items, 0.0)
                }
            }.onFailure { error ->
                Log.e(TAG, "Error al validar código: ${error.message}", error)

                _uiState.value = _uiState.value.copy(
                    discountError = "Error al validar el código",
                    discountCode = "",
                    discountCodeId = "",
                    discountPercentage = 0.0,
                    isDiscountApplied = false
                )

                calculateTotals(_uiState.value.items, 0.0)
            }
        }
    }

    /**
     * Remueve el descuento aplicado y recalcula los totales
     */
    fun removeDiscount() {
        Log.d(TAG, "removeDiscount - Quitando descuento")

        _uiState.value = _uiState.value.copy(
            discountCode = "",
            discountCodeId = "",
            discountPercentage = 0.0,
            isDiscountApplied = false,
            discountError = null
        )

        // Recalcular totales sin descuento
        calculateTotals(_uiState.value.items, 0.0)
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

            val discountPercentage = if (_uiState.value.isDiscountApplied) {
                _uiState.value.discountPercentage
            } else {
                0.0
            }

            val result = cartRepository.checkout(userId, user, discountCode, discountPercentage)
            result.onSuccess { order ->
                // Si se usó un código de descuento, incrementar su contador
                if (_uiState.value.isDiscountApplied && _uiState.value.discountCodeId.isNotEmpty()) {
                    viewModelScope.launch {
                        discountRepository.incrementUsageCount(_uiState.value.discountCodeId)
                    }
                }

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

    private fun calculateTotals(items: List<CartItem>, discountPercentage: Double = 0.0) {
        val subtotal = items.sumOf { it.subtotal }
        val discount = if (discountPercentage > 0) {
            subtotal * (discountPercentage / 100.0)
        } else {
            0.0
        }
        val total = subtotal - discount

        _uiState.value = _uiState.value.copy(
            subtotal = subtotal,
            discount = discount,
            total = total
        )

        Log.d(TAG, "Totales calculados - Subtotal: $subtotal, Descuento: $discount ($discountPercentage%), Total: $total")
    }
}


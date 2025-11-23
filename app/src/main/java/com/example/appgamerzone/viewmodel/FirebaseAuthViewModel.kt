// viewmodel/AuthViewModel.kt
package com.example.appgamerzone.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appgamerzone.data.model.User
import com.example.appgamerzone.data.model.UserRole
import com.example.appgamerzone.data.repository.FirebaseAuthRepository
import com.example.appgamerzone.data.session.UserSessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AuthUiState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val phone: String = "",
    val address: String = "",
    val fullNameError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,
    val isRegistrationSuccessful: Boolean = false,
    val isLoginSuccessful: Boolean = false,
    val loginError: String? = null,
    val isLoading: Boolean = false,
    val currentUser: User? = null
)

class AuthViewModel(
    private val context: Context,
    private val repository: FirebaseAuthRepository = FirebaseAuthRepository()
) : ViewModel() {

    private val sessionManager = UserSessionManager(context)

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isAdmin = sessionManager.isAdmin
    val isLoggedIn = sessionManager.isLoggedIn

    fun updateFullName(fullName: String) {
        _uiState.value = _uiState.value.copy(fullName = fullName, fullNameError = null)
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email, emailError = null, loginError = null)
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password, passwordError = null, loginError = null)
    }

    fun updateConfirmPassword(confirmPassword: String) {
        _uiState.value = _uiState.value.copy(
            confirmPassword = confirmPassword,
            confirmPasswordError = null
        )
    }

    fun updatePhone(phone: String) {
        _uiState.value = _uiState.value.copy(phone = phone)
    }

    fun updateAddress(address: String) {
        _uiState.value = _uiState.value.copy(address = address)
    }

    fun validateForm(): Boolean {
        val currentState = _uiState.value
        var isValid = true

        if (currentState.fullName.isBlank()) {
            _uiState.value = currentState.copy(fullNameError = "El nombre completo es requerido")
            isValid = false
        }
        if (currentState.email.isBlank()) {
            _uiState.value = currentState.copy(emailError = "El email es requerido")
            isValid = false
        } else if (!isValidEmail(currentState.email)) {
            _uiState.value = currentState.copy(emailError = "Ingresa un email válido")
            isValid = false
        }
        if (currentState.password.isBlank()) {
            _uiState.value = currentState.copy(passwordError = "La contraseña es requerida")
            isValid = false
        } else if (currentState.password.length < 6) {
            _uiState.value = currentState.copy(passwordError = "La contraseña debe tener al menos 6 caracteres")
            isValid = false
        }
        if (currentState.confirmPassword != currentState.password) {
            _uiState.value = currentState.copy(confirmPasswordError = "Las contraseñas no coinciden")
            isValid = false
        }
        return isValid
    }

    fun validateLogin(): Boolean {
        val currentState = _uiState.value
        var isValid = true

        if (currentState.email.isBlank()) {
            _uiState.value = currentState.copy(emailError = "El email es requerido")
            isValid = false
        } else if (!isValidEmail(currentState.email)) {
            _uiState.value = currentState.copy(emailError = "Ingresa un email válido")
            isValid = false
        }
        if (currentState.password.isBlank()) {
            _uiState.value = currentState.copy(passwordError = "La contraseña es requerida")
            isValid = false
        }
        return isValid
    }

    fun registerUser(age: Int = 18, isDuocStudent: Boolean = false, referralCode: String? = null) {
        val current = _uiState.value
        if (!validateForm()) return

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true)

            val user = User(
                email = current.email,
                password = current.password,
                fullName = current.fullName,
                age = age,
                phone = current.phone,
                address = current.address,
                role = UserRole.USER,
                isDuocStudent = isDuocStudent,
                referralCode = referralCode
            )

            val startTime = System.currentTimeMillis()
            val result = repository.register(user)
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < 2000) {
                delay(2000 - elapsed)
            }

            result.onSuccess { registeredUser ->
                sessionManager.saveUser(registeredUser)
                _uiState.value = current.copy(
                    isRegistrationSuccessful = true,
                    isLoading = false,
                    currentUser = registeredUser
                )
            }.onFailure {
                _uiState.value = current.copy(emailError = it.message, isLoading = false)
            }
        }
    }

    fun loginUser() {
        val current = _uiState.value
        if (!validateLogin()) return

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true, loginError = null)

            val startTime = System.currentTimeMillis()
            val result = repository.login(current.email, current.password)
            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed < 2000) {
                delay(2000 - elapsed)
            }

            result.onSuccess { user ->
                sessionManager.saveUser(user)
                _uiState.value = current.copy(
                    isLoginSuccessful = true,
                    isLoading = false,
                    currentUser = user
                )
            }.onFailure {
                _uiState.value = current.copy(loginError = it.message, isLoading = false)
            }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = sessionManager.currentUserId.first()
            if (userId != null) {
                val result = repository.getUserById(userId)
                result.onSuccess { user ->
                    _uiState.value = _uiState.value.copy(
                        currentUser = user,
                        fullName = user.fullName,
                        email = user.email,
                        phone = user.phone,
                        address = user.address
                    )
                }
            }
        }
    }

    fun updateProfile(
        fullName: String? = null,
        phone: String? = null,
        address: String? = null,
        newPassword: String? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val userId = sessionManager.currentUserId.first()
            if (userId != null) {
                val result = repository.updateProfile(
                    userId = userId,
                    fullName = fullName,
                    phone = phone,
                    address = address,
                    password = newPassword
                )

                result.onSuccess { updatedUser ->
                    sessionManager.saveUser(updatedUser)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentUser = updatedUser,
                        fullName = updatedUser.fullName,
                        phone = updatedUser.phone,
                        address = updatedUser.address
                    )
                }.onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _uiState.value = AuthUiState()
        }
    }

    fun resetRegistrationState() {
        _uiState.value = AuthUiState()
    }

    fun resetLoginState() {
        val current = _uiState.value
        _uiState.value = current.copy(isLoginSuccessful = false, loginError = null, isLoading = false)
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}


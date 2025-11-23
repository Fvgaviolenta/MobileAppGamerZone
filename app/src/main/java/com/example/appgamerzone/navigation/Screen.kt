// navigation/Screen.kt
package com.example.appgamerzone.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Catalog : Screen("catalog")
    object Login : Screen("login")
    object Register : Screen("register")
    object Profile : Screen("profile")
    object ProductManagement : Screen("product_management")
    object Cart : Screen("cart")
    object ProductDetail : Screen("product_detail")

    // Helpers para rutas con parámetros
    fun withCategory(category: String): String {
        return "$route/$category"
    }

    fun withId(id: Long): String {
        return "$route/$id"
    }
}
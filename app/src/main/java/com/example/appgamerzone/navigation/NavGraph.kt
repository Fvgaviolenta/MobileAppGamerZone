// navigation/NavGraph.kt
package com.example.appgamerzone.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.appgamerzone.view.auth.LoginScreen
import com.example.appgamerzone.view.auth.RegisterScreen
import com.example.appgamerzone.view.home.HomeScreen
import com.example.appgamerzone.view.catalog.CatalogScreen
import com.example.appgamerzone.view.profile.ProfileScreen
import com.example.appgamerzone.view.admin.ProductManagementScreen
import com.example.appgamerzone.view.cart.CartScreen
import com.example.appgamerzone.view.cart.QRScannerScreen
import com.example.appgamerzone.viewmodel.CartViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    onOpenDrawer: () -> Unit = {},
    startRoute: String = Screen.Register.route
) {
    // Crear instancia compartida de CartViewModel para todas las pantallas
    val context = LocalContext.current
    val sharedCartViewModel: CartViewModel = viewModel {
        CartViewModel(context)
    }

    val validRoutes = setOf(
        Screen.Register.route,
        Screen.Login.route,
        Screen.Home.route,
        Screen.Catalog.route,
        Screen.Profile.route,
        Screen.ProductManagement.route,
        Screen.Cart.route
    )
    val resolvedStart = when {
        startRoute in validRoutes -> startRoute
        // Permitir categorías dinámicas en catálogo: "catalog/<categoria>"
        startRoute.startsWith(Screen.Catalog.route) -> startRoute
        else -> Screen.Register.route
    }

    NavHost(
        navController = navController,
        startDestination = resolvedStart
    ) {
        // ==================== PANTALLAS SIN DRAWER ====================
        composable(route = Screen.Register.route) {
            RegisterScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(route = Screen.Login.route) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        // ==================== PANTALLAS CON DRAWER ====================
        composable(route = Screen.Home.route) {
            HomeScreen(
                onOpenDrawer = onOpenDrawer,
                onCategoryClick = { category ->
                    navController.navigate("${Screen.Catalog.route}/$category")
                }
            )
        }

        composable(route = Screen.Catalog.route) {
            CatalogScreen(
                category = null,
                onOpenDrawer = onOpenDrawer,
                onBackClick = { navController.popBackStack() },
                onProductClick = { productId ->
                    // TODO: Navegar a detalle de producto si se implementa
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                },
                cartViewModel = sharedCartViewModel
            )
        }

        composable(route = Screen.Catalog.route + "/{category}") { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category")
            CatalogScreen(
                category = category,
                onOpenDrawer = onOpenDrawer,
                onBackClick = { navController.popBackStack() },
                onProductClick = { productId ->
                    // TODO: Navegar a detalle de producto si se implementa
                },
                onNavigateToCart = {
                    navController.navigate(Screen.Cart.route)
                },
                cartViewModel = sharedCartViewModel
            )
        }

        composable(route = Screen.Profile.route) {
            ProfileScreen(
                onOpenDrawer = onOpenDrawer
            )
        }

        composable(route = Screen.ProductManagement.route) {
            ProductManagementScreen(
                onOpenDrawer = onOpenDrawer
            )
        }

        composable(route = Screen.Cart.route) {
            CartScreen(
                onOpenDrawer = onOpenDrawer,
                onBackClick = { navController.popBackStack() },
                onScanQR = {
                    navController.navigate("qr_scanner")
                },
                onGoToCatalog = {
                    navController.navigate(Screen.Catalog.route)
                },
                viewModel = sharedCartViewModel
            )
        }

        composable(route = "qr_scanner") {
            QRScannerScreen(
                onBackClick = { navController.popBackStack() },
                onQRScanned = { code ->
                    // El código se maneja en CartScreen
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("scanned_code", code)
                }
            )
        }
    }
}
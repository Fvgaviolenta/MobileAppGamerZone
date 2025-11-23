// navigation/MainApp.kt
package com.example.appgamerzone.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch
import com.example.appgamerzone.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(startRoute: String? = null) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun navigateWithDrawer(route: String) {
        scope.launch {
            drawerState.close()
            navController.navigate(route)
        }
    }

    val openDrawer: () -> Unit = {
        scope.launch { drawerState.open() }
    }

    val backStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = backStackEntry?.destination?.route

    // Mostrar drawer en todas las pantallas principales excepto Login y Register
    val showDrawer = currentRoute != Screen.Login.route &&
                     currentRoute != Screen.Register.route

    if (showDrawer) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                NavigationDrawerContent(
                    onItemClick = { route -> navigateWithDrawer(route) },
                    onClose = { scope.launch { drawerState.close() } },
                    onLogout = {
                        // Navegar a la pantalla de registro y limpiar el back stack
                        navController.navigate(Screen.Register.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        ) {
            NavGraph(
                navController = navController,
                onOpenDrawer = openDrawer,
                startRoute = startRoute ?: Screen.Register.route
            )
        }
    } else {
        NavGraph(
            navController = navController,
            onOpenDrawer = {},
            startRoute = startRoute ?: Screen.Register.route
        )
    }
}
package com.example.appgamerzone.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.material3.HorizontalDivider
import com.example.appgamerzone.data.session.UserSessionManager
import kotlinx.coroutines.launch

@Composable
fun NavigationDrawerContent(
    onItemClick: (String) -> Unit,
    onClose: () -> Unit,
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = UserSessionManager(context)
    val isAdmin by sessionManager.isAdmin.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            // Header del drawer
            Text(
                "Gamer Zone",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 20.dp, horizontal = 12.dp)
            )

            HorizontalDivider()

            Spacer(modifier = Modifier.height(16.dp))

            // Sección Principal
            Text(
                "MENÚ PRINCIPAL",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            NavigationDrawerItem(
                label = { Text("Inicio") },
                icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                selected = false,
                onClick = { onItemClick(Screen.Home.route) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            NavigationDrawerItem(
                label = { Text("Mi Perfil") },
                icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                selected = false,
                onClick = { onItemClick(Screen.Profile.route) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            NavigationDrawerItem(
                label = { Text("Catálogo") },
                icon = { Icon(Icons.Default.ShoppingBag, contentDescription = "Catálogo") },
                selected = false,
                onClick = { onItemClick(Screen.Catalog.route) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            NavigationDrawerItem(
                label = { Text("Carrito de Compras") },
                icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito") },
                selected = false,
                onClick = { onItemClick(Screen.Cart.route) },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )

            // Sección Administración (solo para ADMIN)
            if (isAdmin) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "ADMINISTRACIÓN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )

                NavigationDrawerItem(
                    label = { Text("Gestión de Productos") },
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Productos") },
                    selected = false,
                    onClick = { onItemClick(Screen.ProductManagement.route) },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider()

            NavigationDrawerItem(
                label = { Text("Cerrar Sesión") },
                icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar Sesión") },
                selected = false,
                onClick = {
                    scope.launch {
                        // Limpiar sesión
                        sessionManager.clearSession()
                        android.util.Log.d("NavigationDrawer", "🔓 Sesión cerrada")
                        onClose()
                        onLogout()
                    }
                },
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }
    }
}

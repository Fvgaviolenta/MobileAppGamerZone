package com.example.appgamerzone.view.cart

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.appgamerzone.data.model.CartItem
import com.example.appgamerzone.viewmodel.CartViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onOpenDrawer: () -> Unit = {},
    onBackClick: () -> Unit,
    onScanQR: () -> Unit,
    onGoToCatalog: () -> Unit,
    viewModel: CartViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var discountCodeInput by remember { mutableStateOf("") }
    var showCheckoutDialog by remember { mutableStateOf(false) }

    // Recargar el carrito cada vez que se muestre esta pantalla
    LaunchedEffect(Unit) {
        viewModel.loadCart()
    }

    // Observar cambios en el carrito para recargarlo
    val cartItemsCount = uiState.items.size
    LaunchedEffect(cartItemsCount) {
        // Si hay cambios, esto ayuda a mantener la UI actualizada
    }

    LaunchedEffect(uiState.checkoutSuccess) {
        if (uiState.checkoutSuccess) {
            showCheckoutDialog = true
        }
    }

    if (showCheckoutDialog && uiState.lastOrder != null) {
        AlertDialog(
            onDismissRequest = {
                showCheckoutDialog = false
                viewModel.resetCheckoutState()
                onBackClick()
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("¡Compra realizada!") },
            text = {
                Column {
                    Text("Tu pedido ha sido procesado exitosamente.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Total: ${formatPrice(uiState.lastOrder!!.total)}")
                    if (uiState.lastOrder!!.discount > 0) {
                        Text("Descuento aplicado: ${formatPrice(uiState.lastOrder!!.discount)}")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showCheckoutDialog = false
                    viewModel.resetCheckoutState()
                    onBackClick()
                }) {
                    Text("Aceptar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Carrito de Compras") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, "Menú")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (uiState.items.isEmpty() && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Tu carrito está vacío",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Button(onClick = onGoToCatalog) {
                        Text("Ir a comprar")
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Lista de productos
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.items, key = { it.productId }) { item ->
                        CartItemCard(
                            item = item,
                            onIncreaseQuantity = { viewModel.updateQuantity(item.productId, item.quantity + 1) },
                            onDecreaseQuantity = { viewModel.updateQuantity(item.productId, item.quantity - 1) },
                            onRemove = { viewModel.removeFromCart(item.productId) }
                        )
                    }

                    // Sección de código de descuento
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "¿Tienes un código de descuento?",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = discountCodeInput,
                                        onValueChange = { discountCodeInput = it.uppercase() },
                                        label = { Text("Código") },
                                        modifier = Modifier.weight(1f),
                                        singleLine = true,
                                        enabled = !uiState.isDiscountApplied,
                                        isError = uiState.discountError != null,
                                        supportingText = {
                                            if (uiState.discountError != null) {
                                                Text(
                                                    text = uiState.discountError!!,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            errorBorderColor = MaterialTheme.colorScheme.error,
                                            errorLabelColor = MaterialTheme.colorScheme.error
                                        )
                                    )

                                    Button(
                                        onClick = { viewModel.applyDiscountCode(discountCodeInput) },
                                        enabled = !uiState.isDiscountApplied && discountCodeInput.isNotEmpty()
                                    ) {
                                        Text("Aplicar")
                                    }
                                }

                                // Mostrar descuento aplicado
                                if (uiState.isDiscountApplied) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "✓ Código aplicado: ${uiState.discountCode}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        TextButton(onClick = {
                                            discountCodeInput = ""
                                            viewModel.removeDiscount()
                                        }) {
                                            Text("Quitar")
                                        }
                                    }
                                }

                                Button(
                                    onClick = onScanQR,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, "Escanear QR")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Escanear código QR")
                                }

                                if (uiState.isDiscountApplied) {
                                    Text(
                                        "✓ Descuento aplicado: ${uiState.discountCode}",
                                        color = MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // Resumen y botón de compra
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Subtotal:", style = MaterialTheme.typography.bodyLarge)
                            Text(formatPrice(uiState.subtotal), style = MaterialTheme.typography.bodyLarge)
                        }

                        if (uiState.discount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Descuento:",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    "-${formatPrice(uiState.discount)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Total:",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                formatPrice(uiState.total),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Button(
                            onClick = { viewModel.checkout() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !uiState.isLoading && uiState.items.isNotEmpty()
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Realizar compra", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }
            }
        }

        // Mensaje de error
        uiState.error?.let { error ->
            Snackbar(
                modifier = Modifier.padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                }
            ) {
                Text(error)
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onIncreaseQuantity: () -> Unit,
    onDecreaseQuantity: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Imagen del producto
            AsyncImage(
                model = item.productImage,
                contentDescription = item.productName,
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Crop
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = formatPrice(item.unitPrice),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Disponible: ${item.availableStock}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onDecreaseQuantity,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Remove, "Disminuir", modifier = Modifier.size(20.dp))
                    }

                    Text(
                        text = item.quantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onIncreaseQuantity,
                        modifier = Modifier.size(32.dp),
                        enabled = item.quantity < item.availableStock
                    ) {
                        Icon(Icons.Default.Add, "Aumentar", modifier = Modifier.size(20.dp))
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Text(
                        text = formatPrice(item.subtotal),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    "Eliminar",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

private fun formatPrice(price: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.Builder().setLanguage("es").setRegion("CL").build())
    return format.format(price)
}


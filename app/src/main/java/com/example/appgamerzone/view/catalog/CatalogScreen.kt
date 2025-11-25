package com.example.appgamerzone.view.catalog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.appgamerzone.viewmodel.FirebaseProductViewModel
import com.example.appgamerzone.viewmodel.CartViewModel
import com.example.appgamerzone.data.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    category: String? = null,
    onOpenDrawer: () -> Unit = {},
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onNavigateToCart: () -> Unit = {},
    cartViewModel: CartViewModel
) {
    val context = LocalContext.current
    val viewModel: FirebaseProductViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    val cartUiState by cartViewModel.uiState.collectAsState()

    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var showProductDialog by remember { mutableStateOf(false) }
    var showAddToCartFeedback by remember { mutableStateOf(false) }
    var addToCartMessage by remember { mutableStateOf("") }

    LaunchedEffect(category) {
        if (category != null) {
            viewModel.selectCategory(category)
        } else {
            viewModel.selectCategory(null)
        }
    }

    // Mostrar feedback cuando se agregó al carrito
    LaunchedEffect(showAddToCartFeedback) {
        if (showAddToCartFeedback) {
            kotlinx.coroutines.delay(2000)
            showAddToCartFeedback = false
        }
    }

    // Dialog de detalle de producto
    if (showProductDialog && selectedProduct != null) {
        ProductDetailDialog(
            product = selectedProduct!!,
            onDismiss = { showProductDialog = false },
            onAddToCart = { product, quantity ->
                cartViewModel.addToCart(product.id, quantity)
                showProductDialog = false
                addToCartMessage = "${product.name} agregado al carrito (x$quantity)"
                showAddToCartFeedback = true
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        category?.let { "Catálogo - $it" } ?: "Catálogo de Productos"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                },
                actions = {
                    // Badge mostrando cantidad de items en el carrito
                    IconButton(onClick = onNavigateToCart) {
                        BadgedBox(
                            badge = {
                                if (cartUiState.totalItems > 0) {
                                    Badge {
                                        Text(text = "${cartUiState.totalItems}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Ir al carrito"
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = {
            if (showAddToCartFeedback) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(addToCartMessage)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Chips de categorías
            if (uiState.categories.isNotEmpty()) {
                Text(
                    text = "Categorías",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = uiState.selectedCategory == null,
                            onClick = { viewModel.selectCategory(null) },
                            label = { Text("Todos") },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }

                    items(uiState.categories) { cat ->
                        FilterChip(
                            selected = uiState.selectedCategory == cat,
                            onClick = { viewModel.selectCategory(cat) },
                            label = { Text(cat) },
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Lista de productos
            if (uiState.isLoading) {
                Text("Cargando productos…", style = MaterialTheme.typography.bodyMedium)
            } else {
                if (uiState.filteredProducts.isEmpty()) {
                    Text(
                        "Sin productos para esta categoría",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    LazyColumn {
                        items(uiState.filteredProducts) { product ->
                            ProductListItem(
                                product = product,
                                onProductClick = {
                                    selectedProduct = product
                                    showProductDialog = true
                                },
                                onAddToCartClick = {
                                    cartViewModel.addToCart(product.id, 1)
                                    addToCartMessage = "${product.name} agregado al carrito"
                                    showAddToCartFeedback = true
                                }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductListItem(
    product: Product,
    onProductClick: () -> Unit,
    onAddToCartClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick() }
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${product.category} • $${String.format("%.0f", product.price)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (product.stock > 0) {
                    Text(
                        text = "Stock: ${product.stock}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Text(
                        text = "Agotado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Botón de agregar al carrito
            if (product.stock > 0) {
                IconButton(
                    onClick = onAddToCartClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Agregar al carrito",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductDetailDialog(
    product: Product,
    onDismiss: () -> Unit,
    onAddToCart: (Product, Int) -> Unit
) {
    var quantity by remember { mutableStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header con botón de cerrar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Detalle del Producto",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Imagen del producto
                if (product.imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Nombre
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Categoría y Precio
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "$${String.format("%.0f", product.price)}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Descripción
                Text(
                    text = "Descripción:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = product.description.ifBlank { "Sin descripción disponible" },
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stock
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stock disponible: ",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "${product.stock}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (product.stock > 0) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Selector de cantidad
                if (product.stock > 0) {
                    Text(
                        text = "Cantidad:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Botón menos
                        OutlinedButton(
                            onClick = { if (quantity > 1) quantity-- },
                            enabled = quantity > 1
                        ) {
                            Text("-")
                        }

                        // Cantidad
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )

                        // Botón más
                        OutlinedButton(
                            onClick = { if (quantity < product.stock) quantity++ },
                            enabled = quantity < product.stock
                        ) {
                            Text("+")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Botón de agregar al carrito
                    Button(
                        onClick = { onAddToCart(product, quantity) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar al Carrito ($quantity)")
                    }
                } else {
                    // Producto agotado
                    Text(
                        text = "Producto agotado",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
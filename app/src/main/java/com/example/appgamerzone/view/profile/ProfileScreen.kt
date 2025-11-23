package com.example.appgamerzone.view.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appgamerzone.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<AuthViewModel> {
        AuthViewModel(context)
    }
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPasswordFields by remember { mutableStateOf(false) }

    var passwordError by remember { mutableStateOf<String?>(null) }
    var isInitialized by remember { mutableStateOf(false) }

    // Cargar usuario actual al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadCurrentUser()
    }

    // Actualizar campos cuando se carga el usuario
    LaunchedEffect(uiState.currentUser) {
        uiState.currentUser?.let { user ->
            if (!isInitialized) {
                fullName = user.fullName
                phone = user.phone
                address = user.address
                isInitialized = true
            }
        }
    }

    // Mostrar mensaje cuando se actualiza
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && isInitialized && uiState.currentUser != null) {
            snackbarHostState.showSnackbar("Perfil actualizado correctamente")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Información del usuario
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Información de la cuenta",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Email: ${uiState.currentUser?.email ?: ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Rol: ${uiState.currentUser?.role?.name ?: ""}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Nivel: ${uiState.currentUser?.level ?: 1}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Puntos: ${uiState.currentUser?.levelUpPoints ?: 0}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Formulario de edición
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Editar información",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nombre completo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Dirección") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 3
                    )

                    // Botón para mostrar campos de contraseña
                    TextButton(
                        onClick = { showPasswordFields = !showPasswordFields }
                    ) {
                        Text(
                            if (showPasswordFields) "Ocultar cambio de contraseña"
                            else "Cambiar contraseña"
                        )
                    }

                    if (showPasswordFields) {
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                passwordError = null
                            },
                            label = { Text("Nueva contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            isError = passwordError != null
                        )

                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                passwordError = null
                            },
                            label = { Text("Confirmar contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            isError = passwordError != null
                        )

                        passwordError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Button(
                        onClick = {
                            // Validar contraseñas si se van a cambiar
                            if (showPasswordFields && newPassword.isNotEmpty()) {
                                if (newPassword.length < 6) {
                                    passwordError = "La contraseña debe tener al menos 6 caracteres"
                                    return@Button
                                }
                                if (newPassword != confirmPassword) {
                                    passwordError = "Las contraseñas no coinciden"
                                    return@Button
                                }
                            }

                            viewModel.updateProfile(
                                fullName = if (fullName != uiState.currentUser?.fullName) fullName else null,
                                phone = if (phone != uiState.currentUser?.phone) phone else null,
                                address = if (address != uiState.currentUser?.address) address else null,
                                newPassword = if (showPasswordFields && newPassword.isNotEmpty()) newPassword else null
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Guardar cambios")
                        }
                    }
                }
            }
        }
    }
}


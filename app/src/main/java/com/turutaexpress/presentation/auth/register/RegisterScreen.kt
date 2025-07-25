package com.turutaexpress.presentation.auth.register

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.turutaexpress.presentation.auth.AuthViewModel
import com.turutaexpress.presentation.auth.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("") }
    var selectedColonia by remember { mutableStateOf("") }

    val roles = listOf("Cliente", "Mototaxista", "Sitio")
    val colonias = listOf("Colonia Centro", "Fraccionamiento Las Américas", "Residencial del Parque")
    var roleExpanded by remember { mutableStateOf(false) }
    var coloniaExpanded by remember { mutableStateOf(false) }

    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(authState) {
        when(val state = authState) {
            is AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                authViewModel.resetAuthState()
            }
            else -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (authState == AuthState.Loading) {
            CircularProgressIndicator()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Crear Cuenta", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Correo Electrónico") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Número de Teléfono") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Contraseña") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

                ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = !roleExpanded }) {
                    OutlinedTextField(
                        value = selectedRole,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Soy un...") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        roles.forEach { role ->
                            DropdownMenuItem(text = { Text(role) }, onClick = {
                                selectedRole = role
                                roleExpanded = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(expanded = coloniaExpanded, onExpandedChange = { coloniaExpanded = !coloniaExpanded }) {
                    OutlinedTextField(
                        value = selectedColonia,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Colonia / Fraccionamiento") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = coloniaExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = coloniaExpanded, onDismissRequest = { coloniaExpanded = false }) {
                        colonias.forEach { colonia ->
                            DropdownMenuItem(text = { Text(colonia) }, onClick = {
                                selectedColonia = colonia
                                coloniaExpanded = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (name.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && password.isNotBlank() && selectedRole.isNotBlank() && selectedColonia.isNotBlank()) {
                            authViewModel.register(name, email, phone, password, selectedRole, selectedColonia)
                        } else {
                            Toast.makeText(context, "Por favor, llena todos los campos", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrarme")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "¿Ya tienes cuenta? Inicia Sesión",
                    modifier = Modifier.clickable { navController.popBackStack() },
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
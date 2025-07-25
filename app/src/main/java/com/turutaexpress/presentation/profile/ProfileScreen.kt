package com.turutaexpress.presentation.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel = viewModel()) {
    val user by viewModel.user.collectAsState()
    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var address by remember(user) { mutableStateOf(user?.address ?: "") }

    var verificationCode by remember { mutableStateOf("") }
    val showVerificationSection = user?.isPhoneVerified == false

    val updateStatus by viewModel.updateStatus.collectAsState()
    val verificationStatus by viewModel.verificationStatus.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(updateStatus) {
        updateStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearUpdateStatus()
        }
    }

    LaunchedEffect(verificationStatus) {
        verificationStatus?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mi Perfil") }) }
    ) { paddingValues ->
        if (user == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            Column(modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)) {

                Text("Información Personal", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = user!!.email, onValueChange = {}, label = { Text("Correo Electrónico") }, readOnly = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Dirección (Calle y Número)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(24.dp))
                Button(onClick = { viewModel.updateUser(name, address) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Guardar Cambios")
                }

                Divider(modifier = Modifier.padding(vertical = 24.dp))

                Text("Seguridad", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))

                if (showVerificationSection) {
                    Text("Tu número de teléfono no está verificado.")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = verificationCode,
                        onValueChange = { verificationCode = it },
                        label = { Text("Código de Verificación") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row {
                        Button(onClick = { viewModel.sendVerificationCode(context) }) {
                            Text("Enviar Código")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.submitVerificationCode(verificationCode) },
                            enabled = verificationCode.isNotBlank()
                        ) {
                            Text("Verificar")
                        }
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = "Verificado", tint = Color(0xFF4CAF50))
                        Spacer(Modifier.width(8.dp))
                        Text("Tu número de teléfono ha sido verificado.")
                    }
                }
            }
        }
    }
}
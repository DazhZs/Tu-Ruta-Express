package com.turutaexpress.presentation.client.request

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.turutaexpress.navigation.AppScreens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestServiceScreen(
    navController: NavController,
    viewModel: RequestViewModel // Recibimos el ViewModel que comparte el grafo de navegación
) {
    val driverName = viewModel.driverName // Obtenemos el nombre del conductor desde el ViewModel

    val serviceTypes = listOf("Transporte", "Mandado", "Pago de Servicios")
    val (selectedType, onTypeSelected) = remember { mutableStateOf(serviceTypes[0]) }
    var destination by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is RequestUiState.Success -> {
                Toast.makeText(context, "Solicitud Enviada", Toast.LENGTH_SHORT).show()
                // Navegamos a la pantalla de estado, que está en el mismo sub-grafo.
                navController.navigate(AppScreens.RequestStatusScreen.createRoute(state.requestId)) {
                    // Limpiamos el stack hasta la pantalla de inicio del sub-grafo para que "atrás" no vuelva aquí.
                    popUpTo(AppScreens.RequestServiceScreen.route) { inclusive = true }
                }
                viewModel.resetUiState()
            }
            is RequestUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetUiState()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Solicitar a $driverName") }) }
    ) { paddingValues ->
        if (uiState is RequestUiState.Loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                Text("Tipo de Servicio", style = MaterialTheme.typography.titleMedium)
                serviceTypes.forEach { type ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = (type == selectedType),
                                onClick = { onTypeSelected(type) }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = (type == selectedType), onClick = { onTypeSelected(type) })
                        Text(text = type, modifier = Modifier.padding(start = 16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedType == "Transporte" || selectedType == "Mandado") {
                    OutlinedTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        label = { Text("Punto de Destino") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("Detalles Adicionales") },
                    placeholder = {
                        Text(
                            when(selectedType) {
                                "Mandado" -> "Ej: Comprar 1L de leche en la tienda de la esquina."
                                "Pago de Servicios" -> "Ej: Pagar recibo de luz en CFE."
                                else -> "Ej: Llevo una mochila pequeña."
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        viewModel.createRequest(selectedType, destination.ifBlank { null }, details)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Enviar Solicitud")
                }
            }
        }
    }
}
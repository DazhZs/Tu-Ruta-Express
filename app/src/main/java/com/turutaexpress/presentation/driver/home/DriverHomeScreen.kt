package com.turutaexpress.presentation.driver.home // Asegúrate que el package name sea el tuyo

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.turutaexpress.data.model.MototaxiProfile // Asegúrate que el package name sea el tuyo
import com.turutaexpress.data.model.ServiceRequest
import com.turutaexpress.data.model.ServiceStatus
import com.turutaexpress.navigation.AppScreens
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverHomeScreen(navController: NavController, viewModel: DriverHomeViewModel = viewModel()) {
    val profile by viewModel.mototaxiProfile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val pendingRequests by viewModel.pendingRequests.collectAsState()
    val activeRequest by viewModel.activeRequest.collectAsState()

    LaunchedEffect(navController.currentBackStackEntry) {
        viewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Mototaxista") },
                actions = {
                    IconButton(onClick = { navController.navigate(AppScreens.HistoryScreen.route) }) {
                        Icon(Icons.Default.History, contentDescription = "Historial")
                    }
                    IconButton(onClick = { navController.navigate(AppScreens.ProfileScreen.route) }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                AvailabilityControl(
                    profile = profile,
                    isLoading = isLoading,
                    onToggle = { viewModel.toggleAvailability(it) },
                    navController = navController
                )
                Divider(modifier = Modifier.padding(vertical = 16.dp))
            }

            activeRequest?.let { request ->
                item {
                    ActiveServiceCard(
                        request = request,
                        onUpdateStatus = { newStatus -> viewModel.updateActiveServiceStatus(newStatus) }
                    )
                    Divider(modifier = Modifier.padding(vertical = 16.dp))
                }
            }

            if (activeRequest == null) {
                item {
                    Text(
                        "Solicitudes Pendientes",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                if (pendingRequests.isEmpty()) {
                    item {
                        Text(
                            "No tienes solicitudes nuevas por el momento.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    items(pendingRequests) { request ->
                        PendingRequestCard(
                            request = request,
                            onAccept = { viewModel.acceptRequest(request.id) },
                            onReject = { viewModel.rejectRequest(request.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AvailabilityControl(
    profile: MototaxiProfile?,
    isLoading: Boolean,
    onToggle: (Boolean) -> Unit,
    navController: NavController
) {
    val membershipActive = profile?.membershipStatus == "ACTIVE"
    val expiryDate = profile?.membershipExpiryDate?.toDate()
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Mi Estado", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        if (profile != null) {
            Text(
                text = "Membresía: ${profile.membershipStatus}",
                color = if(membershipActive) Color.DarkGray else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            if(expiryDate != null) {
                Text("Vence: ${sdf.format(expiryDate)}", style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (profile.isActive) "DISPONIBLE" else "NO DISPONIBLE",
                    color = if (profile.isActive) Color(0xFF4CAF50) else Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                if (isLoading && profile.membershipStatus == "ACTIVE") {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Switch(
                        checked = profile.isActive && membershipActive,
                        onCheckedChange = onToggle,
                        enabled = membershipActive
                    )
                }
            }

            if (!membershipActive) {
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navController.navigate(AppScreens.MembershipScreen.route) }) {
                    Text("Activar Membresía")
                }
            }
        } else if (isLoading) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun PendingRequestCard(
    request: ServiceRequest,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("¡Nueva Solicitud!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("De: ${request.clientName}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Servicio: ${request.serviceType}", fontWeight = FontWeight.SemiBold)
            Text("Destino: ${request.destinationAddress ?: "N/A"}")
            Text("Detalles: ${request.details}")
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Button(
                    onClick = onAccept,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) { Text("Aceptar") }
                Button(
                    onClick = onReject,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Rechazar") }
            }
        }
    }
}

@Composable
fun ActiveServiceCard(
    request: ServiceRequest,
    onUpdateStatus: (ServiceStatus) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Servicio en Progreso", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Cliente: ${request.clientName}", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Estado Actual: ${request.status.name}", fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))

            when (request.status) {
                ServiceStatus.ACEPTADA -> {
                    Button(onClick = { onUpdateStatus(ServiceStatus.EN_CAMINO) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Iniciar Viaje / Recoger Mandado")
                    }
                }
                ServiceStatus.EN_CAMINO -> {
                    Button(onClick = { onUpdateStatus(ServiceStatus.FINALIZADA) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Marcar como Finalizado / Entregado")
                    }
                }
                else -> { }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { /* TODO: Implementar llamada o chat */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Call, contentDescription = "Llamar")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Contactar a ${request.clientName}")
            }
        }
    }
}
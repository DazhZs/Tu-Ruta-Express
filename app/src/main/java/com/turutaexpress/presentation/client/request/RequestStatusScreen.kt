package com.turutaexpress.presentation.client.request

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.turutaexpress.data.model.ServiceRequest
import com.turutaexpress.data.model.ServiceStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestStatusScreen(
    navController: NavController,
    viewModel: RequestViewModel, // Recibimos el ViewModel compartido
    requestId: String           // Seguimos necesitando el ID para iniciar el rastreo
) {
    val request by viewModel.serviceRequest.collectAsState()

    LaunchedEffect(requestId) {
        viewModel.trackRequest(requestId)
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Estado de tu Solicitud") }) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (request == null) {
                CircularProgressIndicator()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Solicitud a ${request!!.driverName}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    StatusTimeline(status = request!!.status)

                    Spacer(modifier = Modifier.height(32.dp))

                    ServiceDetailsCard(request = request!!)
                }
            }
        }
    }
}

@Composable
fun StatusTimeline(status: ServiceStatus) {
    val statuses = ServiceStatus.values().filter { it != ServiceStatus.CANCELADA }
    Column {
        statuses.forEachIndexed { index, s ->
            val isCurrent = s == status
            val isCompleted = s.ordinal < status.ordinal

            StatusStep(
                stepName = s.name.replace("_", " "),
                isCurrent = isCurrent,
                isCompleted = isCompleted
            )
            if (index < statuses.size - 1) {
                Divider(
                    modifier = Modifier
                        .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
                        .width(2.dp)
                        .height(32.dp),
                    color = if(isCompleted) MaterialTheme.colorScheme.primary else Color.Gray
                )
            }
        }
    }
}

@Composable
fun StatusStep(stepName: String, isCurrent: Boolean, isCompleted: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val (icon, color) = when {
            isCompleted -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
            isCurrent -> Icons.Default.HourglassEmpty to MaterialTheme.colorScheme.primary
            else -> Icons.Default.RadioButtonUnchecked to Color.Gray
        }
        Icon(imageVector = icon, contentDescription = null, tint = color)
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stepName.lowercase().replaceFirstChar { it.uppercase() },
            fontWeight = if (isCurrent || isCompleted) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent || isCompleted) MaterialTheme.colorScheme.onSurface else Color.Gray
        )
    }
}

@Composable
fun ServiceDetailsCard(request: ServiceRequest) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Detalles:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Servicio: ${request.serviceType}")
            Text("Origen: ${request.originAddress}")
            request.destinationAddress?.let { Text("Destino: $it") }
            Text("Indicaciones: ${request.details}")
            if (request.cost > 0) {
                Text("Costo: $%.2f".format(request.cost), fontWeight = FontWeight.Bold)
            }
        }
    }
}
package com.turutaexpress.presentation.history

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.turutaexpress.data.model.ServiceRequest
import com.turutaexpress.data.model.ServiceStatus
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController, viewModel: HistoryViewModel = viewModel()) {
    val state by viewModel.historyState.collectAsState()
    val ratingState by viewModel.ratingState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    LaunchedEffect(ratingState) {
        ratingState?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mi Historial de Servicios") }) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = state) {
                is HistoryUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is HistoryUiState.Error -> Text(currentState.message, modifier = Modifier.align(Alignment.Center))
                is HistoryUiState.Success -> {
                    if (currentState.history.isEmpty()) {
                        Text("Aún no tienes servicios en tu historial.", modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(currentState.history) { request ->
                                HistoryItemCard(request, viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryItemCard(request: ServiceRequest, viewModel: HistoryViewModel) {
    val currentUser = FirebaseAuth.getInstance().currentUser!!
    var showRatingDialog by remember { mutableStateOf(false) }

    val needsRating = request.status == ServiceStatus.FINALIZADA &&
            ((currentUser.uid == request.clientId && !request.clientHasRated) ||
                    (currentUser.uid == request.driverId && !request.driverHasRated))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            Text(sdf.format(request.createdAt.toDate()), style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(8.dp))

            Text(request.serviceType, style = MaterialTheme.typography.titleLarge)
            Text("Con: ${if(currentUser.uid == request.clientId) request.driverName else request.clientName}")
            Text("Estado: ${request.status.name}")
            if(request.cost > 0) Text("Costo: $%.2f".format(request.cost))

            if (needsRating) {
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { showRatingDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Calificar Servicio")
                }
            }
        }
    }

    if (showRatingDialog) {
        RatingDialog(
            onDismiss = { showRatingDialog = false },
            onSubmit = { rating, comment ->
                viewModel.submitRatingForService(request, rating, comment)
                showRatingDialog = false
            }
        )
    }
}
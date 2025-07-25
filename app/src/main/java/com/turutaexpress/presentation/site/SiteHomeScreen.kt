package com.turutaexpress.presentation.site

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.turutaexpress.data.model.User
import com.turutaexpress.navigation.AppScreens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteHomeScreen(navController: NavController, viewModel: SiteAdminViewModel = viewModel()) {
    val site by viewModel.site.collectAsState()
    val drivers by viewModel.drivers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(site?.name ?: "Panel de Sitio") },
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (site == null) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No has registrado un sitio.",
                        style = MaterialTheme.typography.headlineSmall,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Para poder gestionar a tus mototaxistas, primero debes crear tu sitio. Esta acción se realiza una sola vez.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { /* TODO: Navegar a pantalla de creación de sitio */ }) {
                        Text("Crear Mi Sitio Ahora")
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    item {
                        Text("Mis Mototaxistas Registrados", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(16.dp))
                    }
                    if (drivers.isEmpty()) {
                        item { Text("Aún no hay mototaxistas registrados en tu sitio.") }
                    } else {
                        items(drivers) { driver ->
                            DriverInfoCard(driver)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DriverInfoCard(driver: User) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(driver.name, style = MaterialTheme.typography.titleMedium)
                Text(driver.phone, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
package com.turutaexpress.presentation.driver.membership

import androidx.compose.ui.graphics.Color
import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

data class MembershipPlan(
    val name: String,
    val price: String,
    val months: Int,
    val description: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MembershipScreen(navController: NavController, viewModel: MembershipViewModel = viewModel()) {
    val plans = listOf(
        MembershipPlan("Semestral", "$150.00 MXN", 6, "Acceso por 6 meses."),
        MembershipPlan("Anual", "$250.00 MXN", 12, "El mejor valor, acceso por 12 meses.")
    )

    var selectedPlan by remember { mutableStateOf<MembershipPlan?>(null) }
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uiState) {
        when(val state = uiState) {
            is MembershipUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                // Regresar a la pantalla anterior después del éxito
                navController.popBackStack()
            }
            is MembershipUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Activar Membresía") }) }
    ) { paddingValues ->
        if (uiState is MembershipUiState.Loading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Elige tu Plan",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Para poder recibir viajes, necesitas una membresía activa.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                plans.forEach { plan ->
                    PlanCard(
                        plan = plan,
                        isSelected = selectedPlan == plan,
                        onSelect = { selectedPlan = plan }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { selectedPlan?.let { viewModel.purchaseMembership(it.months) } },
                    enabled = selectedPlan != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Activar Plan ${selectedPlan?.name ?: ""}")
                }
            }
        }
    }
}

@Composable
fun PlanCard(plan: MembershipPlan, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onSelect() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(plan.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(plan.price, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Text(plan.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
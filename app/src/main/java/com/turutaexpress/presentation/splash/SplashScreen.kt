package com.turutaexpress.presentation.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.turutaexpress.R
import com.turutaexpress.navigation.AppScreens
import com.turutaexpress.presentation.auth.AuthViewModel
import com.turutaexpress.presentation.auth.AuthState
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()

    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500), // Animación de 1.5 segundos
        label = "alphaAnimation"
    )

    // Este LaunchedEffect controla tanto la animación como la lógica de navegación.
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(500) // Una pequeña espera antes de verificar para que la animación empiece
        authViewModel.checkCurrentUser()
    }

    // Este otro LaunchedEffect maneja la navegación cuando el estado de autenticación está listo.
    LaunchedEffect(authState) {
        if (authState is AuthState.Success || authState is AuthState.Error) {
            // Esperamos a que la animación de fade in termine antes de navegar
            delay(1000)

            authViewModel.resetAuthState()

            val destination = when (val state = authState) {
                is AuthState.Success -> {
                    when (state.user.role) {
                        "Cliente" -> AppScreens.ClientHomeScreen.route
                        "Mototaxista" -> AppScreens.DriverHomeScreen.route
                        "Sitio" -> AppScreens.SiteHomeScreen.route
                        else -> AppScreens.LoginScreen.route
                    }
                }
                is AuthState.Error -> AppScreens.LoginScreen.route
                else -> AppScreens.LoginScreen.route
            }

            navController.navigate(destination) {
                popUpTo(AppScreens.SplashScreen.route) { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background), // Fondo con el nuevo color del tema
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_logo_motorcycle),
            contentDescription = "Logo de la App",
            modifier = Modifier
                .size(150.dp)
                .alpha(alphaAnim.value), // Aplicamos la animación de opacidad
            tint = MaterialTheme.colorScheme.primary // El ícono tomará nuestro color primario ámbar
        )
    }
}
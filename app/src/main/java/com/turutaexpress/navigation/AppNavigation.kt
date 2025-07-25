package com.turutaexpress.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.turutaexpress.presentation.auth.AuthViewModel
import com.turutaexpress.presentation.auth.login.LoginScreen
import com.turutaexpress.presentation.auth.register.RegisterScreen
import com.turutaexpress.presentation.client.home.ClientHomeScreen
import com.turutaexpress.presentation.client.request.RequestServiceScreen
import com.turutaexpress.presentation.client.request.RequestStatusScreen
import com.turutaexpress.presentation.driver.home.DriverHomeScreen
import com.turutaexpress.presentation.history.HistoryScreen
import com.turutaexpress.presentation.profile.ProfileScreen
import com.turutaexpress.presentation.site.SiteHomeScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = AppScreens.LoginScreen.route,
        modifier = modifier
    ) {
        composable(route = AppScreens.LoginScreen.route) {
            LoginScreen(navController, authViewModel)
        }
        composable(route = AppScreens.RegisterScreen.route) {
            RegisterScreen(navController, authViewModel)
        }
        composable(route = AppScreens.ClientHomeScreen.route) {
            ClientHomeScreen(navController)
        }
        composable(route = AppScreens.DriverHomeScreen.route) {
            DriverHomeScreen(navController)
        }
        composable(route = AppScreens.SiteHomeScreen.route) {
            SiteHomeScreen(navController)
        }
        composable(route = AppScreens.HistoryScreen.route) {
            HistoryScreen(navController)
        }
        composable(route = AppScreens.ProfileScreen.route) {
            ProfileScreen(navController)
        }
        composable(
            route = AppScreens.RequestServiceScreen.route,
            arguments = listOf(
                navArgument("driverId") { type = NavType.StringType },
                navArgument("driverName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            RequestServiceScreen(
                navController = navController,
                driverId = backStackEntry.arguments?.getString("driverId")!!,
                driverName = backStackEntry.arguments?.getString("driverName")!!
            )
        }
        composable(
            route = AppScreens.RequestStatusScreen.route,
            arguments = listOf(navArgument("requestId") { type = NavType.StringType })
        ) { backStackEntry ->
            RequestStatusScreen(
                navController = navController,
                requestId = backStackEntry.arguments?.getString("requestId")!!
            )
        }
    }
}
package com.turutaexpress.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.turutaexpress.presentation.auth.AuthViewModel
import com.turutaexpress.presentation.auth.login.LoginScreen
import com.turutaexpress.presentation.auth.register.RegisterScreen
import com.turutaexpress.presentation.client.home.ClientHomeScreen
import com.turutaexpress.presentation.client.request.RequestServiceScreen
import com.turutaexpress.presentation.client.request.RequestStatusScreen
import com.turutaexpress.presentation.client.request.RequestViewModel
import com.turutaexpress.presentation.driver.home.DriverHomeScreen
import com.turutaexpress.presentation.history.HistoryScreen
import com.turutaexpress.presentation.profile.ProfileScreen
import com.turutaexpress.presentation.site.SiteHomeScreen
import com.turutaexpress.presentation.splash.SplashScreen
import com.turutaexpress.presentation.driver.membership.MembershipScreen

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppScreens.SplashScreen.route,
        modifier = modifier
    ) {
        composable(route = AppScreens.SplashScreen.route) {
            SplashScreen(navController)
        }
        composable(route = AppScreens.LoginScreen.route) {
            // Aquí se crea una instancia de AuthViewModel para Login y Register
            LoginScreen(navController, viewModel())
        }
        composable(route = AppScreens.RegisterScreen.route) {
            RegisterScreen(navController, viewModel())
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
        composable(route = AppScreens.MembershipScreen.route) {
            MembershipScreen(navController)
        }

        navigation(
            route = "request_flow/{driverId}/{driverName}",
            arguments = listOf(
                navArgument("driverId") { type = NavType.StringType },
                navArgument("driverName") { type = NavType.StringType }
            ),
            startDestination = AppScreens.RequestServiceScreen.route
        ) {
            composable(route = AppScreens.RequestServiceScreen.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("request_flow/{driverId}/{driverName}")
                }
                val requestViewModel: RequestViewModel = viewModel(parentEntry)

                RequestServiceScreen(
                    navController = navController,
                    viewModel = requestViewModel
                )
            }
            composable(
                route = AppScreens.RequestStatusScreen.route,
                arguments = listOf(navArgument("requestId") { type = NavType.StringType })
            ) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("request_flow/{driverId}/{driverName}")
                }
                val requestViewModel: RequestViewModel = viewModel(parentEntry)
                val requestId = backStackEntry.arguments?.getString("requestId")!!

                RequestStatusScreen(
                    navController = navController,
                    viewModel = requestViewModel,
                    requestId = requestId
                )
            }
        }
    }
}
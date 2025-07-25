package com.turutaexpress.navigation

sealed class AppScreens(val route: String) {
    object LoginScreen : AppScreens("login_screen")
    object RegisterScreen : AppScreens("register_screen")
    object ClientHomeScreen : AppScreens("client_home_screen")
    object DriverHomeScreen : AppScreens("driver_home_screen")
    object SiteHomeScreen : AppScreens("site_home_screen")
    object HistoryScreen : AppScreens("history_screen")
    object ProfileScreen : AppScreens("profile_screen")

    object RequestServiceScreen : AppScreens("request_service_screen/{driverId}/{driverName}") {
        fun createRoute(driverId: String, driverName: String) = "request_service_screen/$driverId/$driverName"
    }

    object RequestStatusScreen : AppScreens("request_status_screen/{requestId}") {
        fun createRoute(requestId: String) = "request_status_screen/$requestId"
    }
}
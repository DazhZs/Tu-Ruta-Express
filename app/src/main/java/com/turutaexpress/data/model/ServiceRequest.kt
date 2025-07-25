package com.turutaexpress.data.model

import com.google.firebase.Timestamp

enum class ServiceStatus {
    PENDIENTE,
    ACEPTADA,
    EN_CAMINO,
    FINALIZADA,
    CANCELADA
}

data class ServiceRequest(
    var id: String = "",
    val clientId: String = "",
    val clientName: String = "",
    val driverId: String = "",
    val driverName: String = "",
    val serviceType: String = "",
    val originAddress: String = "",
    val destinationAddress: String? = null,
    val details: String = "",
    var status: ServiceStatus = ServiceStatus.PENDIENTE,
    var cost: Double = 0.0,
    val createdAt: Timestamp = Timestamp.now(),
    var clientRating: Float? = null,
    var clientComment: String? = null,
    var driverRating: Float? = null,
    var driverComment: String? = null,
    var clientHasRated: Boolean = false,
    var driverHasRated: Boolean = false
)
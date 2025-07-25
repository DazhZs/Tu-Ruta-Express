package com.turutaexpress.data.model

import com.google.firebase.Timestamp

data class MototaxiProfile(
    val uid: String = "",
    var isActive: Boolean = false,
    var rating: Float = 0.0f,
    var totalTrips: Int = 0,
    var services: List<String> = emptyList(),
    var siteId: String? = null,
    var membershipStatus: String = "INACTIVE",
    var membershipExpiryDate: Timestamp? = null
)
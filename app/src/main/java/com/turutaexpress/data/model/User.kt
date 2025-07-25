package com.turutaexpress.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "",
    val colonia: String = "",
    var address: String = "",
    val isPhoneVerified: Boolean = false,
    var profileImageUrl: String? = null,
    val totalTripsAsClient: Int = 0,
    val ratingAsClient: Float = 0.0f,
    var fcmToken: String? = null,
    var verificationCode: String? = null
)
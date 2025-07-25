package com.turutaexpress.presentation.profile

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turutaexpress.R
import com.turutaexpress.data.model.User
import com.turutaexpress.data.repository.AuthRepository
import com.turutaexpress.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class ProfileViewModel : ViewModel() {
    private val authRepository = AuthRepository()
    private val userRepository = UserRepository()
    private val currentUser = FirebaseAuth.getInstance().currentUser!!

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _updateStatus = MutableStateFlow<String?>(null)
    val updateStatus: StateFlow<String?> = _updateStatus

    private val _verificationStatus = MutableStateFlow<String?>(null)
    val verificationStatus: StateFlow<String?> = _verificationStatus

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val result = authRepository.getUserData(currentUser.uid)
            result.onSuccess { _user.value = it }
        }
    }

    fun updateUser(name: String, address: String) {
        viewModelScope.launch {
            val result = userRepository.updateUserProfile(currentUser.uid, name, address)
            result.onSuccess {
                _updateStatus.value = "Perfil actualizado con éxito."
                loadUserProfile()
            }.onFailure {
                _updateStatus.value = "Error: ${it.message}"
            }
        }
    }

    fun saveProfileImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _updateStatus.value = "Guardando imagen..."
            val result = userRepository.saveProfileImageLocally(context, currentUser.uid, imageUri)
            result.onSuccess {
                _updateStatus.value = "Imagen de perfil actualizada."
                loadUserProfile()
            }.onFailure {
                _updateStatus.value = "Error al guardar la imagen: ${it.message}"
            }
        }
    }

    fun sendVerificationCode(context: Context) {
        viewModelScope.launch {
            _verificationStatus.value = "Generando código..."
            val result = authRepository.requestVerificationCodeLocally()
            result.onSuccess { code ->
                _verificationStatus.value = "Revisa tu barra de notificaciones."
                showLocalNotification(context, "Tu Código de Verificación", "Tu código para Tu Ruta Express es: $code")
            }.onFailure {
                _verificationStatus.value = "Error: ${it.message}"
            }
        }
    }

    private fun showLocalNotification(context: Context, title: String, body: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "verification_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Códigos de Verificación",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(Random.nextInt(), notificationBuilder.build())
    }

    fun submitVerificationCode(code: String) {
        viewModelScope.launch {
            val result = authRepository.submitVerificationCode(code)
            result.onSuccess {
                _verificationStatus.value = "¡Teléfono verificado con éxito!"
                loadUserProfile()
            }.onFailure {
                _verificationStatus.value = "Error: ${it.message}"
            }
        }
    }

    fun clearUpdateStatus() {
        _updateStatus.value = null
    }
}
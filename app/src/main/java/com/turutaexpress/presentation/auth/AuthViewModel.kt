package com.turutaexpress.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turutaexpress.data.model.User
import com.turutaexpress.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun checkCurrentUser() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val currentUser = repository.getCurrentUser()
            if (currentUser != null) {
                val userDataResult = repository.getUserData(currentUser.uid)
                userDataResult.onSuccess { user ->
                    repository.updateFCMToken()
                    _authState.value = AuthState.Success(user)
                }.onFailure {
                    // Si hay usuario en Auth pero no en DB, es un error. Mandar a Login.
                    _authState.value = AuthState.Error(it.message ?: "Usuario no encontrado en la base de datos.")
                }
            } else {
                // Si no hay usuario en Auth, es normal. Mandar a Login.
                _authState.value = AuthState.Error("No hay sesión activa.")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.login(email, password)
            result.onSuccess { firebaseUser ->
                val userDataResult = repository.getUserData(firebaseUser.uid)
                userDataResult.onSuccess { user ->
                    repository.updateFCMToken()
                    _authState.value = AuthState.Success(user)
                }.onFailure {
                    _authState.value = AuthState.Error(it.message ?: "Error al obtener datos del usuario.")
                }
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Error desconocido en el login.")
            }
        }
    }

    fun register(name: String, email: String, phone: String, password: String, role: String, colonia: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val newUser = User(name = name, email = email, phone = phone, role = role, colonia = colonia, isPhoneVerified = false)
            val result = repository.register(newUser, password)
            result.onSuccess {
                login(email, password)
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Error desconocido en el registro.")
            }
        }
    }

    fun resetAuthState() {
        _authState.value = AuthState.Idle
    }
}
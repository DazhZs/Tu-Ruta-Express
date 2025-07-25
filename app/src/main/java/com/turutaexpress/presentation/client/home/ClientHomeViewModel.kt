package com.turutaexpress.presentation.client.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turutaexpress.data.repository.AuthRepository
import com.turutaexpress.data.repository.MototaxistaDisponible
import com.turutaexpress.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ClientHomeViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val authRepository = AuthRepository()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private val _availableMototaxistas = MutableStateFlow<List<MototaxistaDisponible>>(emptyList())
    val availableMototaxistas: StateFlow<List<MototaxistaDisponible>> = _availableMototaxistas

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadAvailableDrivers()
    }

    private fun loadAvailableDrivers() {
        viewModelScope.launch {
            _isLoading.value = true
            val userResult = authRepository.getUserData(currentUser!!.uid)
            userResult.onSuccess { user ->
                userRepository.getAvailableMototaxistas(user.colonia)
                    .catch {
                        _isLoading.value = false
                    }
                    .collect { drivers ->
                        _availableMototaxistas.value = drivers
                        _isLoading.value = false
                    }
            }.onFailure {
                _isLoading.value = false
            }
        }
    }
}
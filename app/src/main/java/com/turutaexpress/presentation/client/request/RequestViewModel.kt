package com.turutaexpress.presentation.client.request

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turutaexpress.data.model.ServiceRequest
import com.turutaexpress.data.repository.AuthRepository
import com.turutaexpress.data.repository.ServiceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class RequestUiState {
    object Idle : RequestUiState()
    object Loading : RequestUiState()
    data class Success(val requestId: String) : RequestUiState()
    data class Error(val message: String) : RequestUiState()
}

class RequestViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val serviceRepository = ServiceRepository()
    private val authRepository = AuthRepository()

    val driverId: String = savedStateHandle.get<String>("driverId")!!
    val driverName: String = savedStateHandle.get<String>("driverName")!!

    private val _uiState = MutableStateFlow<RequestUiState>(RequestUiState.Idle)
    val uiState: StateFlow<RequestUiState> = _uiState.asStateFlow()

    private val _serviceRequest = MutableStateFlow<ServiceRequest?>(null)
    val serviceRequest: StateFlow<ServiceRequest?> = _serviceRequest.asStateFlow()

    fun createRequest(serviceType: String, destination: String?, details: String) {
        viewModelScope.launch {
            _uiState.value = RequestUiState.Loading
            val currentUser = authRepository.getCurrentUser()!!
            val clientDataResult = authRepository.getUserData(currentUser.uid)

            clientDataResult.onSuccess { client ->
                val request = ServiceRequest(
                    clientId = client.uid,
                    clientName = client.name,
                    driverId = driverId,
                    driverName = driverName,
                    serviceType = serviceType,
                    originAddress = client.colonia,
                    destinationAddress = destination,
                    details = details
                )

                val result = serviceRepository.createServiceRequest(request)
                result.onSuccess { requestId ->
                    _uiState.value = RequestUiState.Success(requestId)
                }.onFailure {
                    _uiState.value = RequestUiState.Error(it.message ?: "Error al crear la solicitud")
                }
            }.onFailure {
                _uiState.value = RequestUiState.Error(it.message ?: "Error al obtener datos del cliente")
            }
        }
    }

    fun trackRequest(requestId: String) {
        viewModelScope.launch {
            serviceRepository.getServiceRequestStream(requestId).collect { request ->
                _serviceRequest.value = request
            }
        }
    }

    fun resetUiState() {
        _uiState.value = RequestUiState.Idle
    }
}
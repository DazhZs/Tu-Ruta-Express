package com.turutaexpress.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turutaexpress.data.model.ServiceRequest
import com.turutaexpress.data.repository.AuthRepository
import com.turutaexpress.data.repository.ServiceRepository
import com.turutaexpress.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val history: List<ServiceRequest>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

class HistoryViewModel : ViewModel() {
    private val serviceRepository = ServiceRepository()
    private val userRepository = UserRepository()
    private val authRepository = AuthRepository()
    private val currentUser = FirebaseAuth.getInstance().currentUser!!

    private val _historyState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val historyState: StateFlow<HistoryUiState> = _historyState

    private val _ratingState = MutableStateFlow<String?>(null)
    val ratingState: StateFlow<String?> = _ratingState

    fun loadHistory() {
        viewModelScope.launch {
            _historyState.value = HistoryUiState.Loading
            val userResult = authRepository.getUserData(currentUser.uid)
            userResult.onSuccess { user ->
                val historyResult = serviceRepository.getServiceHistory(user.uid, user.role)
                historyResult.onSuccess { historyList ->
                    _historyState.value = HistoryUiState.Success(historyList)
                }.onFailure {
                    _historyState.value = HistoryUiState.Error(it.message ?: "Error al cargar historial.")
                }
            }.onFailure {
                _historyState.value = HistoryUiState.Error(it.message ?: "Error al obtener datos de usuario.")
            }
        }
    }

    fun submitRatingForService(request: ServiceRequest, rating: Float, comment: String) {
        viewModelScope.launch {
            val isClientSubmitting = (currentUser.uid == request.clientId)

            val ratedUserId = if (isClientSubmitting) request.driverId else request.clientId
            val ratedUserRole = if (isClientSubmitting) "Mototaxista" else "Cliente"

            val result = userRepository.submitRating(request.id, ratedUserId, ratedUserRole, rating, comment.ifBlank { null }, isClientSubmitting)

            result.onSuccess {
                _ratingState.value = "¡Gracias por tu calificación!"
                loadHistory()
            }.onFailure {
                _ratingState.value = "Error al enviar calificación: ${it.message}"
            }
        }
    }
}
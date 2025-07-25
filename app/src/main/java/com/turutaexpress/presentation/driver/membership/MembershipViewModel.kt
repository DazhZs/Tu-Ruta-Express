package com.turutaexpress.presentation.driver.membership

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turutaexpress.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MembershipUiState {
    object Idle : MembershipUiState()
    object Loading : MembershipUiState()
    data class Success(val message: String) : MembershipUiState()
    data class Error(val message: String) : MembershipUiState()
}

class MembershipViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val currentUser = FirebaseAuth.getInstance().currentUser!!

    private val _uiState = MutableStateFlow<MembershipUiState>(MembershipUiState.Idle)
    val uiState: StateFlow<MembershipUiState> = _uiState

    fun purchaseMembership(planMonths: Int) {
        viewModelScope.launch {
            _uiState.value = MembershipUiState.Loading
            val result = userRepository.activateMembership(currentUser.uid, planMonths)
            result.onSuccess {
                _uiState.value = MembershipUiState.Success("¡Membresía activada con éxito!")
            }.onFailure {
                _uiState.value = MembershipUiState.Error(it.message ?: "Error al procesar la membresía.")
            }
        }
    }
}
package com.turutaexpress.presentation.driver.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turutaexpress.data.model.MototaxiProfile
import com.turutaexpress.data.model.ServiceRequest
import com.turutaexpress.data.model.ServiceStatus
import com.turutaexpress.data.repository.ServiceRepository
import com.turutaexpress.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DriverHomeViewModel : ViewModel() {
    private val userRepository = UserRepository()
    private val serviceRepository = ServiceRepository()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    private val _mototaxiProfile = MutableStateFlow<MototaxiProfile?>(null)
    val mototaxiProfile: StateFlow<MototaxiProfile?> = _mototaxiProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _pendingRequests = MutableStateFlow<List<ServiceRequest>>(emptyList())
    val pendingRequests: StateFlow<List<ServiceRequest>> = _pendingRequests

    private val _activeRequest = MutableStateFlow<ServiceRequest?>(null)
    val activeRequest: StateFlow<ServiceRequest?> = _activeRequest

    init {
        loadProfile()
        listenForRequests()
    }

    private fun loadProfile() {
        _isLoading.value = true
        viewModelScope.launch {
            currentUser?.uid?.let { uid ->
                val result = userRepository.getMototaxiProfile(uid)
                result.onSuccess { profile ->
                    _mototaxiProfile.value = profile
                }.onFailure {
                    // Handle error
                }
            }
            _isLoading.value = false
        }
    }

    private fun listenForRequests() {
        currentUser?.uid?.let { driverId ->
            viewModelScope.launch {
                serviceRepository.getPendingRequestsForDriver(driverId)
                    .catch { /* Handle error */ }
                    .collect { requests -> _pendingRequests.value = requests }
            }
            viewModelScope.launch {
                serviceRepository.getActiveRequestForDriver(driverId)
                    .catch { /* Handle error */ }
                    .collect { request -> _activeRequest.value = request }
            }
        }
    }

    fun toggleAvailability(isActive: Boolean) {
        _isLoading.value = true
        viewModelScope.launch {
            currentUser?.uid?.let { uid ->
                val result = userRepository.updateMototaxiStatus(uid, isActive)
                result.onSuccess {
                    _mototaxiProfile.value = _mototaxiProfile.value?.copy(isActive = isActive)
                }.onFailure {
                    // Handle error
                }
            }
            _isLoading.value = false
        }
    }

    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            serviceRepository.acceptRequest(requestId)
        }
    }

    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            serviceRepository.rejectRequest(requestId)
        }
    }

    fun updateActiveServiceStatus(newStatus: ServiceStatus) {
        _activeRequest.value?.let { request ->
            viewModelScope.launch {
                serviceRepository.updateServiceStatus(request.id, newStatus)
            }
        }
    }
}
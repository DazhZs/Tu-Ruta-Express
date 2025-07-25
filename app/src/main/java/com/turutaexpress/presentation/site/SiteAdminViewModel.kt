package com.turutaexpress.presentation.site

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turutaexpress.data.model.Site
import com.turutaexpress.data.model.User
import com.turutaexpress.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SiteAdminViewModel : ViewModel() {
    private val repository = UserRepository()
    private val currentUser = FirebaseAuth.getInstance().currentUser!!

    private val _site = MutableStateFlow<Site?>(null)
    val site: StateFlow<Site?> = _site

    private val _drivers = MutableStateFlow<List<User>>(emptyList())
    val drivers: StateFlow<List<User>> = _drivers

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadSiteData()
    }

    fun loadSiteData() {
        viewModelScope.launch {
            _isLoading.value = true
            val siteResult = repository.getSiteByAdmin(currentUser.uid)
            siteResult.onSuccess { currentSite ->
                _site.value = currentSite
                currentSite?.let {
                    val driversResult = repository.getDriversForSite(it.id)
                    driversResult.onSuccess { driverList ->
                        _drivers.value = driverList
                    }
                }
            }.onFailure {
                // Handle error
            }
            _isLoading.value = false
        }
    }
}
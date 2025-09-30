package com.example.myapplication.ui.emergencycontacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.data.repository.EmergencyContactRepository

class EmergencyContactsViewModelFactory(
    private val repository: EmergencyContactRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmergencyContactsViewModel::class.java)) {
            return EmergencyContactsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
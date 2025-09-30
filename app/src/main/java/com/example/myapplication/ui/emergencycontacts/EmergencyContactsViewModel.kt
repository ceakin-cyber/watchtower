package com.example.myapplication.ui.emergencycontacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.EmergencyContact
import com.example.myapplication.data.repository.EmergencyContactRepository
import kotlinx.coroutines.launch

class EmergencyContactsViewModel(private val repository: EmergencyContactRepository) : ViewModel() {
    
    val allContacts: LiveData<List<EmergencyContact>> = repository.getAllContacts()
    
    fun insertContact(contact: EmergencyContact) {
        viewModelScope.launch {
            repository.insertContact(contact)
        }
    }
    
    fun updateContact(contact: EmergencyContact) {
        viewModelScope.launch {
            repository.updateContact(contact)
        }
    }
    
    fun deleteContact(contact: EmergencyContact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }
}
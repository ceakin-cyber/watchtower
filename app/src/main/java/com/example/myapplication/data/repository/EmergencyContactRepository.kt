package com.example.myapplication.data.repository

import androidx.lifecycle.LiveData
import com.example.myapplication.data.database.EmergencyContact
import com.example.myapplication.data.database.EmergencyContactDao

class EmergencyContactRepository(private val emergencyContactDao: EmergencyContactDao) {
    
    fun getAllContacts(): LiveData<List<EmergencyContact>> {
        return emergencyContactDao.getAllContacts()
    }
    
    suspend fun getContactById(id: Long): EmergencyContact? {
        return emergencyContactDao.getContactById(id)
    }
    
    suspend fun insertContact(contact: EmergencyContact): Long {
        return emergencyContactDao.insertContact(contact)
    }
    
    suspend fun updateContact(contact: EmergencyContact) {
        emergencyContactDao.updateContact(contact)
    }
    
    suspend fun deleteContact(contact: EmergencyContact) {
        emergencyContactDao.deleteContact(contact)
    }
    
    suspend fun deleteContactById(id: Long) {
        emergencyContactDao.deleteContactById(id)
    }
}
package com.example.myapplication.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EmergencyContactDao {
    
    @Query("SELECT * FROM emergency_contacts ORDER BY isPrimary DESC, name ASC")
    fun getAllContacts(): LiveData<List<EmergencyContact>>
    
    @Query("SELECT * FROM emergency_contacts WHERE id = :id")
    suspend fun getContactById(id: Long): EmergencyContact?
    
    @Insert
    suspend fun insertContact(contact: EmergencyContact): Long
    
    @Update
    suspend fun updateContact(contact: EmergencyContact)
    
    @Delete
    suspend fun deleteContact(contact: EmergencyContact)
    
    @Query("DELETE FROM emergency_contacts WHERE id = :id")
    suspend fun deleteContactById(id: Long)
}
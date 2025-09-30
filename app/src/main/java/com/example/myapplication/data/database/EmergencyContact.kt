package com.example.myapplication.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.data.security.EncryptedString
import java.io.Serializable

@Entity(tableName = "emergency_contacts")
data class EmergencyContact(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: EncryptedString,
    val phoneNumber: EncryptedString,
    val relationship: EncryptedString,
    val isPrimary: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : Serializable
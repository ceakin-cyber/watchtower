package com.example.myapplication.data.database

import androidx.room.TypeConverter
import com.example.myapplication.data.security.DatabaseEncryption

class EncryptionConverters {
    
    companion object {
        private var encryption: DatabaseEncryption? = null
        
        fun setEncryption(databaseEncryption: DatabaseEncryption) {
            encryption = databaseEncryption
        }
        
        fun encryptString(value: String?): String? {
            return encryption?.encryptIfNotNull(value) ?: value
        }
        
        fun decryptString(value: String?): String? {
            return encryption?.decryptIfNotNull(value) ?: value
        }
    }
}
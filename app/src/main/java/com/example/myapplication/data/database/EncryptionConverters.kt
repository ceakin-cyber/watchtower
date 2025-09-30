package com.example.myapplication.data.database

import androidx.room.TypeConverter
import com.example.myapplication.data.security.DatabaseEncryption
import com.example.myapplication.data.security.EncryptedString

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
    
    @TypeConverter
    fun fromEncryptedString(encryptedString: EncryptedString?): String? {
        return encryptedString?.getEncryptedValue()
    }
    
    @TypeConverter
    fun toEncryptedString(value: String?): EncryptedString {
        return EncryptedString.fromEncryptedValue(value)
    }
}
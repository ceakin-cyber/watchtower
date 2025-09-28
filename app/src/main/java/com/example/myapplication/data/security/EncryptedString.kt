package com.example.myapplication.data.security

data class EncryptedString(
    private val encryptedValue: String? = null,
    private val plainValue: String? = null
) {
    companion object {
        private var encryption: DatabaseEncryption? = null
        
        fun setEncryption(databaseEncryption: DatabaseEncryption) {
            encryption = databaseEncryption
        }
        
        fun fromPlainText(value: String?): EncryptedString {
            return if (value != null) {
                val encrypted = encryption?.encryptIfNotNull(value) ?: value
                EncryptedString(encryptedValue = encrypted)
            } else {
                EncryptedString()
            }
        }
        
        fun fromEncryptedValue(encrypted: String?): EncryptedString {
            return EncryptedString(encryptedValue = encrypted)
        }
    }
    
    fun toPlainText(): String? {
        return if (encryptedValue != null) {
            encryption?.decryptIfNotNull(encryptedValue) ?: encryptedValue
        } else {
            plainValue
        }
    }
    
    fun getEncryptedValue(): String? = encryptedValue
}
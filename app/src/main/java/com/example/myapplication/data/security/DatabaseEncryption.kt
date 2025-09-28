package com.example.myapplication.data.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import java.io.IOException

class DatabaseEncryption private constructor(context: Context) {
    
    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
        
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "encrypted_database_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    companion object {
        @Volatile
        private var INSTANCE: DatabaseEncryption? = null
        private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
        
        fun getInstance(context: Context): DatabaseEncryption {
            return INSTANCE ?: synchronized(this) {
                val instance = DatabaseEncryption(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
    
    private fun getOrCreateEncryptionKey(): SecretKey {
        val keyAlias = "database_encryption_key"
        val existingKey = getStoredKey(keyAlias)
        
        return if (existingKey != null) {
            existingKey
        } else {
            generateAndStoreKey(keyAlias)
        }
    }
    
    private fun getStoredKey(keyAlias: String): SecretKey? {
        return try {
            val encodedKey = encryptedPrefs.getString(keyAlias, null)
            if (encodedKey != null) {
                val keyBytes = Base64.decode(encodedKey, Base64.DEFAULT)
                javax.crypto.spec.SecretKeySpec(keyBytes, "AES")
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun generateAndStoreKey(keyAlias: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        val secretKey = keyGenerator.generateKey()
        
        val encodedKey = Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)
        encryptedPrefs.edit().putString(keyAlias, encodedKey).apply()
        
        return secretKey
    }
    
    @Throws(GeneralSecurityException::class, IOException::class)
    fun encrypt(plaintext: String?): String? {
        if (plaintext.isNullOrEmpty()) return plaintext
        
        try {
            val secretKey = getOrCreateEncryptionKey()
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            
            // Combine IV and encrypted data
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
            
            return Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            throw GeneralSecurityException("Encryption failed", e)
        }
    }
    
    @Throws(GeneralSecurityException::class, IOException::class)
    fun decrypt(encryptedText: String?): String? {
        if (encryptedText.isNullOrEmpty()) return encryptedText
        
        try {
            val secretKey = getOrCreateEncryptionKey()
            val combined = Base64.decode(encryptedText, Base64.DEFAULT)
            
            // Extract IV and encrypted data
            val iv = ByteArray(GCM_IV_LENGTH)
            val encryptedBytes = ByteArray(combined.size - GCM_IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH)
            System.arraycopy(combined, GCM_IV_LENGTH, encryptedBytes, 0, encryptedBytes.size)
            
            val cipher = Cipher.getInstance(AES_TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            throw GeneralSecurityException("Decryption failed", e)
        }
    }
    
    fun encryptIfNotNull(value: String?): String? {
        return if (value != null) {
            try {
                encrypt(value)
            } catch (e: Exception) {
                value
            }
        } else {
            null
        }
    }
    
    fun decryptIfNotNull(value: String?): String? {
        return if (value != null) {
            try {
                decrypt(value)
            } catch (e: Exception) {
                value
            }
        } else {
            null
        }
    }
}
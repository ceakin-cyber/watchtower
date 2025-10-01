package com.example.myapplication

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.security.DatabaseEncryption
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*
import android.util.Base64

@RunWith(AndroidJUnit4::class)
class DatabaseEncryptionTest {
    
    private lateinit var context: Context
    private lateinit var encryption: DatabaseEncryption
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        encryption = DatabaseEncryption.getInstance(context)
    }
    
    @Test
    fun testBasicEncryptionDecryption() = runTest {
        val originalText = "Sensitive incident location data"
        
        val encryptedText = encryption.encrypt(originalText)
        assertNotNull("Encrypted text should not be null", encryptedText)
        assertNotEquals("Encrypted text should differ from original", originalText, encryptedText)
        
        val decryptedText = encryption.decrypt(encryptedText)
        assertEquals("Decrypted text should match original", originalText, decryptedText)
    }
    
    @Test
    fun testEncryptionProducesBase64() = runTest {
        val originalText = "Test data"
        val encryptedText = encryption.encrypt(originalText)
        
        // Verify it's valid Base64
        try {
            Base64.decode(encryptedText, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            fail("Encrypted text should be valid Base64: $encryptedText")
        }
    }
    
    @Test
    fun testEncryptionIsConsistentlyDifferent() = runTest {
        val originalText = "Same input text"
        
        val encrypted1 = encryption.encrypt(originalText)
        val encrypted2 = encryption.encrypt(originalText)
        
        // Should be different due to random IV
        assertNotEquals("Encrypting same text twice should produce different ciphertext", encrypted1, encrypted2)
        
        // But both should decrypt to same original
        assertEquals(originalText, encryption.decrypt(encrypted1))
        assertEquals(originalText, encryption.decrypt(encrypted2))
    }
    
    @Test
    fun testNullAndEmptyHandling() = runTest {
        // Null handling
        assertNull("Null input should return null", encryption.encryptIfNotNull(null))
        assertNull("Null input should return null", encryption.decryptIfNotNull(null))
        
        // Empty string handling
        val emptyString = ""
        val encryptedEmpty = encryption.encrypt(emptyString)
        assertNotNull("Empty string should be encrypted", encryptedEmpty)
        assertEquals("Empty string should decrypt correctly", emptyString, encryption.decrypt(encryptedEmpty))
    }
    
    @Test
    fun testSpecialCharacters() = runTest {
        val specialText = "Location: 123 Main St. (Apt #5) - Building A, Floor 2 🏢"
        
        val encryptedText = encryption.encrypt(specialText)
        val decryptedText = encryption.decrypt(encryptedText)
        
        assertEquals("Special characters should be preserved", specialText, decryptedText)
    }
    
    @Test
    fun testLargeDataEncryption() = runTest {
        val largeText = buildString {
            repeat(1000) {
                append("This is a very detailed incident description with lots of sensitive information. ")
            }
        }
        
        val encryptedText = encryption.encrypt(largeText)
        assertNotNull("Large text should be encrypted", encryptedText)
        
        val decryptedText = encryption.decrypt(encryptedText)
        assertEquals("Large text should decrypt correctly", largeText, decryptedText)
    }
    
    @Test
    fun testEncryptionKeyConsistency() = runTest {
        // Test that the same instance produces consistent results
        val testText = "Consistency test"
        
        val encrypted1 = encryption.encrypt(testText)
        val decrypted1 = encryption.decrypt(encrypted1)
        
        val encrypted2 = encryption.encrypt(testText)
        val decrypted2 = encryption.decrypt(encrypted2)
        
        assertEquals("Should decrypt consistently", testText, decrypted1)
        assertEquals("Should decrypt consistently", testText, decrypted2)
    }
    
    @Test
    fun testInvalidEncryptedDataHandling() = runTest {
        // Test with invalid Base64
        try {
            encryption.decrypt("invalid-base64-data!!!")
            fail("Should throw exception for invalid encrypted data")
        } catch (e: Exception) {
            // Expected - should handle gracefully
            assertTrue("Should throw appropriate exception", e is java.security.GeneralSecurityException)
        }
    }
}
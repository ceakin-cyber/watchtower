package com.example.myapplication

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.database.IncidentDatabase
import com.example.myapplication.data.repository.EncryptedIncidentRepository
import com.example.myapplication.data.repository.EncryptedEvidenceRepository
import com.example.myapplication.ui.incident.Incident
import com.example.myapplication.ui.incident.SeverityLevel
import com.example.myapplication.data.database.EvidenceAttachment
import com.example.myapplication.data.database.FileType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class DatabaseEncryptionVerificationTest {
    
    private lateinit var context: Context
    private lateinit var database: IncidentDatabase
    private lateinit var encryptedIncidentRepository: EncryptedIncidentRepository
    private lateinit var encryptedEvidenceRepository: EncryptedEvidenceRepository
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = IncidentDatabase.getDatabase(context)
        encryptedIncidentRepository = EncryptedIncidentRepository(context)
        encryptedEvidenceRepository = EncryptedEvidenceRepository(context)
    }
    
    @After
    fun tearDown() {
        // Clean up test data
        runTest {
            val incidents = encryptedIncidentRepository.getAllIncidents()
            incidents.forEach { encryptedIncidentRepository.deleteIncident(it) }
        }
    }
    
    @Test
    fun testDataIsActuallyEncryptedInDatabase() = runTest {
        val sensitiveLocation = "Secret Government Facility Building 7"
        val sensitiveDescription = "Classified security breach with confidential details"
        val sensitiveCaseNumber = "TOP-SECRET-CASE-12345"
        
        // Store incident using encrypted repository
        val incident = Incident(
            incident_type = "Security Breach",
            location = sensitiveLocation,
            description = sensitiveDescription,
            severity_level = SeverityLevel.CRITICAL,
            case_number = sensitiveCaseNumber
        )
        
        encryptedIncidentRepository.insertIncident(incident)
        
        // Now directly query the database to see raw stored data
        val rawIncidents = database.incidentDao().getAllIncidentsSuspend()
        assertEquals("Should have one incident in database", 1, rawIncidents.size)
        
        val rawIncident = rawIncidents.first()
        
        // Verify that sensitive data is NOT stored in plain text
        assertNotEquals("Location should be encrypted in database", 
            sensitiveLocation, rawIncident.location)
        assertNotEquals("Description should be encrypted in database", 
            sensitiveDescription, rawIncident.description)
        assertNotEquals("Case number should be encrypted in database", 
            sensitiveCaseNumber, rawIncident.case_number)
        
        // Verify non-sensitive data is stored normally
        assertEquals("Non-sensitive incident type should be plain text", 
            "Security Breach", rawIncident.incident_type)
        assertEquals("Severity level should be plain text", 
            SeverityLevel.CRITICAL, rawIncident.severity_level)
        
        // Verify encrypted data looks like Base64 encoded data
        assertTrue("Encrypted location should look like Base64", 
            isBase64(rawIncident.location))
        assertTrue("Encrypted description should look like Base64", 
            isBase64(rawIncident.description))
        rawIncident.case_number?.let { caseNumber ->
            assertTrue("Encrypted case number should look like Base64", isBase64(caseNumber))
        }
        
        // Verify we can still retrieve correct data through encrypted repository
        val retrievedIncidents = encryptedIncidentRepository.getAllIncidents()
        val retrievedIncident = retrievedIncidents.first()
        
        assertEquals("Decrypted location should match original", 
            sensitiveLocation, retrievedIncident.location)
        assertEquals("Decrypted description should match original", 
            sensitiveDescription, retrievedIncident.description)
        assertEquals("Decrypted case number should match original", 
            sensitiveCaseNumber, retrievedIncident.case_number)
    }
    
    @Test
    fun testEvidenceDataIsActuallyEncryptedInDatabase() = runTest {
        // First create an incident
        val incident = Incident(
            incident_type = "Test Incident",
            location = "Test Location",
            description = "Test Description",
            severity_level = SeverityLevel.LOW
        )
        encryptedIncidentRepository.insertIncident(incident)
        
        val sensitiveFilePath = "/classified/evidence/top_secret_document.pdf"
        val sensitiveDescription = "Highly classified document containing state secrets"
        
        // Store evidence using encrypted repository
        val evidence = EvidenceAttachment(
            incident_id = incident.id,
            file_name = "document.pdf",
            file_path = sensitiveFilePath,
            file_type = FileType.DOCUMENT,
            description = sensitiveDescription
        )
        
        encryptedEvidenceRepository.insertEvidence(evidence)
        
        // Query database directly to see raw stored data
        val rawEvidence = database.evidenceAttachmentDao().getAllEvidenceSuspend()
        assertEquals("Should have one evidence item", 1, rawEvidence.size)
        
        val rawEvidenceItem = rawEvidence.first()
        
        // Verify sensitive data is encrypted
        assertNotEquals("File path should be encrypted in database", 
            sensitiveFilePath, rawEvidenceItem.file_path)
        assertNotEquals("Description should be encrypted in database", 
            sensitiveDescription, rawEvidenceItem.description)
        
        // Verify non-sensitive data is plain text
        assertEquals("File name should be plain text", 
            "document.pdf", rawEvidenceItem.file_name)
        assertEquals("File type should be plain text", 
            FileType.DOCUMENT, rawEvidenceItem.file_type)
        
        // Verify encrypted data looks like Base64
        assertTrue("Encrypted file path should look like Base64", 
            isBase64(rawEvidenceItem.file_path))
        rawEvidenceItem.description?.let { desc ->
            assertTrue("Encrypted description should look like Base64", isBase64(desc))
        }
        
        // Verify we can retrieve correct data through encrypted repository
        val retrievedEvidence = encryptedEvidenceRepository.getEvidenceByIncidentId(incident.id)
        val retrievedItem = retrievedEvidence.first()
        
        assertEquals("Decrypted file path should match original", 
            sensitiveFilePath, retrievedItem.file_path)
        assertEquals("Decrypted description should match original", 
            sensitiveDescription, retrievedItem.description)
    }
    
    @Test
    fun testDifferentInstancesProduceDifferentEncryption() = runTest {
        val sensitiveText = "Same sensitive location data"
        
        // Create two identical incidents
        val incident1 = Incident(
            incident_type = "Test 1",
            location = sensitiveText,
            description = "Description 1",
            severity_level = SeverityLevel.LOW
        )
        
        val incident2 = Incident(
            incident_type = "Test 2", 
            location = sensitiveText, // Same location
            description = "Description 2",
            severity_level = SeverityLevel.LOW
        )
        
        encryptedIncidentRepository.insertIncident(incident1)
        encryptedIncidentRepository.insertIncident(incident2)
        
        // Get raw database data
        val rawIncidents = database.incidentDao().getAllIncidentsSuspend()
        assertEquals("Should have two incidents", 2, rawIncidents.size)
        
        val raw1 = rawIncidents.find { it.incident_type == "Test 1" }!!
        val raw2 = rawIncidents.find { it.incident_type == "Test 2" }!!
        
        // Even though they have the same location, the encrypted values should be different
        // (due to random IV in AES-GCM)
        assertNotEquals("Same plaintext should encrypt to different ciphertext", 
            raw1.location, raw2.location)
        
        // But both should decrypt to the same original value
        val retrieved = encryptedIncidentRepository.getAllIncidents()
        val retrieved1 = retrieved.find { it.incident_type == "Test 1" }!!
        val retrieved2 = retrieved.find { it.incident_type == "Test 2" }!!
        
        assertEquals("Both should decrypt to same original", 
            sensitiveText, retrieved1.location)
        assertEquals("Both should decrypt to same original", 
            sensitiveText, retrieved2.location)
    }
    
    private fun isBase64(str: String): Boolean {
        return try {
            android.util.Base64.decode(str, android.util.Base64.DEFAULT)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }
    
    @Test
    fun testEncryptionDoesNotAffectNonSensitiveFields() = runTest {
        val incident = Incident(
            incident_type = "Public Information", 
            location = "Secret Location",  // This will be encrypted
            description = "Confidential details", // This will be encrypted
            severity_level = SeverityLevel.MEDIUM,
            reported_to_authorities = true,
            case_number = "SECRET-123" // This will be encrypted
        )
        
        encryptedIncidentRepository.insertIncident(incident)
        
        val rawIncident = database.incidentDao().getAllIncidentsSuspend().first()
        
        // Non-sensitive fields should be stored as-is
        assertEquals("Incident type should be plain text", 
            "Public Information", rawIncident.incident_type)
        assertEquals("Severity level should be plain text", 
            SeverityLevel.MEDIUM, rawIncident.severity_level)
        assertEquals("Boolean field should be plain text", 
            true, rawIncident.reported_to_authorities)
        
        // Sensitive fields should be encrypted
        assertNotEquals("Location should be encrypted", 
            "Secret Location", rawIncident.location)
        assertNotEquals("Description should be encrypted", 
            "Confidential details", rawIncident.description)
        assertNotEquals("Case number should be encrypted", 
            "SECRET-123", rawIncident.case_number)
    }
}
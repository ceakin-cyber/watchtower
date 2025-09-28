package com.example.myapplication

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.database.IncidentDatabase
import com.example.myapplication.data.migration.EncryptionMigrationHelper
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
class EncryptionMigrationTest {
    
    private lateinit var context: Context
    private lateinit var database: IncidentDatabase
    private lateinit var migrationHelper: EncryptionMigrationHelper
    private lateinit var encryptedIncidentRepository: EncryptedIncidentRepository
    private lateinit var encryptedEvidenceRepository: EncryptedEvidenceRepository
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = IncidentDatabase.getDatabase(context)
        migrationHelper = EncryptionMigrationHelper(context)
        encryptedIncidentRepository = EncryptedIncidentRepository(context)
        encryptedEvidenceRepository = EncryptedEvidenceRepository(context)
        
        // Clear any existing migration state for testing
        clearMigrationState()
    }
    
    @After
    fun tearDown() {
        runTest {
            // Clean up test data
            val incidents = encryptedIncidentRepository.getAllIncidents()
            incidents.forEach { encryptedIncidentRepository.deleteIncident(it) }
        }
        clearMigrationState()
    }
    
    private fun clearMigrationState() {
        val prefs = context.getSharedPreferences("encryption_migration", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
    
    @Test
    fun testMigrationDetection() = runTest {
        // Initially, migration should be needed
        assertTrue("Migration should be needed initially", migrationHelper.shouldRunMigration())
        
        // After running migration, it should not be needed
        val result = migrationHelper.migrateToEncryptedStorage()
        assertTrue("Migration should succeed", result)
        
        assertFalse("Migration should not be needed after completion", migrationHelper.shouldRunMigration())
    }
    
    @Test 
    fun testMigrationIdempotency() = runTest {
        // Run migration multiple times
        val result1 = migrationHelper.migrateToEncryptedStorage()
        val result2 = migrationHelper.migrateToEncryptedStorage() 
        val result3 = migrationHelper.migrateToEncryptedStorage()
        
        assertTrue("First migration should succeed", result1)
        assertTrue("Second migration should succeed (idempotent)", result2)
        assertTrue("Third migration should succeed (idempotent)", result3)
        
        assertFalse("Migration should not be needed after completion", migrationHelper.shouldRunMigration())
    }
    
    @Test
    fun testMigrationWithExistingPlainTextData() = runTest {
        // First, add some plain text data directly to database (simulating pre-encryption data)
        val plainTextIncident = Incident(
            incident_type = "Legacy Incident",
            location = "Plain Text Location",
            description = "Plain Text Description", 
            severity_level = SeverityLevel.MEDIUM,
            case_number = "PLAIN-CASE-001"
        )
        
        val plainTextEvidence = EvidenceAttachment(
            incident_id = plainTextIncident.id,
            file_name = "legacy.jpg",
            file_path = "/plain/text/path/legacy.jpg",
            file_type = FileType.PHOTO,
            description = "Plain text evidence description"
        )
        
        // Insert directly using DAO to simulate legacy data
        database.incidentDao().insertIncident(plainTextIncident)
        database.evidenceAttachmentDao().insertAttachment(plainTextEvidence)
        
        // Verify data is stored as plain text initially
        val rawIncident = database.incidentDao().getAllIncidentsSuspend().first()
        val rawEvidence = database.evidenceAttachmentDao().getAllEvidenceSuspend().first()
        
        assertEquals("Initially should be plain text", "Plain Text Location", rawIncident.location)
        assertEquals("Initially should be plain text", "Plain Text Description", rawIncident.description)
        assertEquals("Initially should be plain text", "/plain/text/path/legacy.jpg", rawEvidence.file_path)
        
        // Run migration
        assertTrue("Migration should be needed", migrationHelper.shouldRunMigration())
        val migrationResult = migrationHelper.migrateToEncryptedStorage()
        assertTrue("Migration should succeed", migrationResult)
        
        // Verify data is now encrypted in database
        val migratedRawIncident = database.incidentDao().getAllIncidentsSuspend().first()
        val migratedRawEvidence = database.evidenceAttachmentDao().getAllEvidenceSuspend().first()
        
        assertNotEquals("Location should be encrypted after migration", 
            "Plain Text Location", migratedRawIncident.location)
        assertNotEquals("Description should be encrypted after migration", 
            "Plain Text Description", migratedRawIncident.description)
        assertNotEquals("File path should be encrypted after migration", 
            "/plain/text/path/legacy.jpg", migratedRawEvidence.file_path)
        
        // Verify we can still read the data correctly through encrypted repository
        val retrievedIncidents = encryptedIncidentRepository.getAllIncidents()
        val retrievedEvidence = encryptedEvidenceRepository.getAllEvidence()
        
        assertEquals("Should retrieve correct number of incidents", 1, retrievedIncidents.size)
        assertEquals("Should retrieve correct number of evidence", 1, retrievedEvidence.size)
        
        val retrievedIncident = retrievedIncidents.first()
        val retrievedEvidenceItem = retrievedEvidence.first()
        
        assertEquals("Location should decrypt correctly", "Plain Text Location", retrievedIncident.location)
        assertEquals("Description should decrypt correctly", "Plain Text Description", retrievedIncident.description)
        assertEquals("Case number should decrypt correctly", "PLAIN-CASE-001", retrievedIncident.case_number)
        assertEquals("File path should decrypt correctly", "/plain/text/path/legacy.jpg", retrievedEvidenceItem.file_path)
        assertEquals("Evidence description should decrypt correctly", "Plain text evidence description", retrievedEvidenceItem.description)
    }
    
    @Test
    fun testMigrationWithMixedData() = runTest {
        // Add some plain text data (legacy)
        val legacyIncident = Incident(
            incident_type = "Legacy",
            location = "Legacy Location",
            description = "Legacy Description",
            severity_level = SeverityLevel.LOW,
            case_number = "LEGACY-001"
        )
        
        database.incidentDao().insertIncident(legacyIncident)
        
        // Add some encrypted data (new)
        val newIncident = Incident(
            incident_type = "New",
            location = "New Location",
            description = "New Description", 
            severity_level = SeverityLevel.HIGH,
            case_number = "NEW-001"
        )
        
        encryptedIncidentRepository.insertIncident(newIncident)
        
        // Run migration
        val migrationResult = migrationHelper.migrateToEncryptedStorage()
        assertTrue("Migration should succeed with mixed data", migrationResult)
        
        // Verify all data can be read correctly
        val allIncidents = encryptedIncidentRepository.getAllIncidents()
        assertEquals("Should have both incidents", 2, allIncidents.size)
        
        val legacyRetrieved = allIncidents.find { it.incident_type == "Legacy" }!!
        val newRetrieved = allIncidents.find { it.incident_type == "New" }!!
        
        assertEquals("Legacy data should be readable", "Legacy Location", legacyRetrieved.location)
        assertEquals("New data should be readable", "New Location", newRetrieved.location)
    }
    
    @Test
    fun testMigrationWithNullValues() = runTest {
        // Create incident with null sensitive field
        val incidentWithNull = Incident(
            incident_type = "Test Null",
            location = "Test Location",
            description = "Test Description",
            severity_level = SeverityLevel.MEDIUM,
            case_number = null // Null case number
        )
        
        database.incidentDao().insertIncident(incidentWithNull)
        
        val evidenceWithNull = EvidenceAttachment(
            incident_id = incidentWithNull.id,
            file_name = "test.jpg", 
            file_path = "/test/path",
            file_type = FileType.PHOTO,
            description = null // Null description
        )
        
        database.evidenceAttachmentDao().insertAttachment(evidenceWithNull)
        
        // Run migration
        val migrationResult = migrationHelper.migrateToEncryptedStorage()
        assertTrue("Migration should handle null values", migrationResult)
        
        // Verify null values are preserved
        val retrievedIncidents = encryptedIncidentRepository.getAllIncidents()
        val retrievedEvidence = encryptedEvidenceRepository.getAllEvidence()
        
        val incident = retrievedIncidents.first()
        val evidence = retrievedEvidence.first()
        
        assertNull("Null case number should remain null", incident.case_number)
        assertNull("Null description should remain null", evidence.description)
        assertEquals("Non-null fields should work", "Test Location", incident.location)
        assertEquals("Non-null fields should work", "/test/path", evidence.file_path)
    }
    
    @Test
    fun testMigrationFailureRecovery() = runTest {
        // This test simulates what happens if migration encounters an error
        // For this test, we'll just verify the state remains consistent
        
        val incident = Incident(
            incident_type = "Test Recovery",
            location = "Test Location",
            description = "Test Description",
            severity_level = SeverityLevel.LOW
        )
        
        database.incidentDao().insertIncident(incident)
        
        // Even if something goes wrong, the data should still be accessible
        val migrationResult = migrationHelper.migrateToEncryptedStorage()
        
        // Whether migration succeeds or fails, we should be able to read the data
        val retrievedIncidents = encryptedIncidentRepository.getAllIncidents()
        assertEquals("Should retrieve incident regardless of migration outcome", 1, retrievedIncidents.size)
        
        val retrieved = retrievedIncidents.first()
        assertEquals("Should be able to read data", "Test Recovery", retrieved.incident_type)
    }
}
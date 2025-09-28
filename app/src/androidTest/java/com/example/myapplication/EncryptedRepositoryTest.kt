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
class EncryptedRepositoryTest {
    
    private lateinit var context: Context
    private lateinit var database: IncidentDatabase
    private lateinit var incidentRepository: EncryptedIncidentRepository
    private lateinit var evidenceRepository: EncryptedEvidenceRepository
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        
        // Create an in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(context, IncidentDatabase::class.java)
            .allowMainThreadQueries()
            .build()
            
        incidentRepository = EncryptedIncidentRepository(context)
        evidenceRepository = EncryptedEvidenceRepository(context)
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun testEncryptedIncidentStorage() = runTest {
        // Create test incident with sensitive data
        val originalIncident = Incident(
            incident_type = "Security Breach",
            location = "Building A, Room 101 - Confidential Location",
            description = "Unauthorized access detected with sensitive details about the breach",
            severity_level = SeverityLevel.HIGH,
            reported_to_authorities = true,
            case_number = "CONFIDENTIAL-CASE-2024-001"
        )
        
        // Store the incident
        incidentRepository.insertIncident(originalIncident)
        
        // Retrieve and verify
        val retrievedIncidents = incidentRepository.getAllIncidents()
        assertEquals("Should retrieve one incident", 1, retrievedIncidents.size)
        
        val retrievedIncident = retrievedIncidents.first()
        assertEquals("Location should match", originalIncident.location, retrievedIncident.location)
        assertEquals("Description should match", originalIncident.description, retrievedIncident.description)
        assertEquals("Case number should match", originalIncident.case_number, retrievedIncident.case_number)
        assertEquals("Non-sensitive fields should match", originalIncident.incident_type, retrievedIncident.incident_type)
    }
    
    @Test
    fun testEncryptedEvidenceStorage() = runTest {
        // First create an incident
        val incident = Incident(
            incident_type = "Test Incident",
            location = "Test Location",
            description = "Test Description",
            severity_level = SeverityLevel.MEDIUM
        )
        incidentRepository.insertIncident(incident)
        
        // Create evidence with sensitive data
        val originalEvidence = EvidenceAttachment(
            incident_id = incident.id,
            file_name = "evidence.jpg",
            file_path = "/secure/confidential/path/to/evidence.jpg",
            file_type = FileType.PHOTO,
            file_size = 1024000,
            description = "Confidential evidence showing sensitive information"
        )
        
        // Store the evidence
        evidenceRepository.insertEvidence(originalEvidence)
        
        // Retrieve and verify
        val retrievedEvidence = evidenceRepository.getEvidenceByIncidentId(incident.id)
        assertEquals("Should retrieve one evidence item", 1, retrievedEvidence.size)
        
        val evidence = retrievedEvidence.first()
        assertEquals("File path should match", originalEvidence.file_path, evidence.file_path)
        assertEquals("Description should match", originalEvidence.description, evidence.description)
        assertEquals("Non-sensitive fields should match", originalEvidence.file_name, evidence.file_name)
    }
    
    @Test
    fun testMultipleIncidentsWithEncryption() = runTest {
        val incidents = listOf(
            Incident(
                incident_type = "Type A",
                location = "Secret Location A",
                description = "Confidential Description A",
                severity_level = SeverityLevel.LOW,
                case_number = "CASE-A-001"
            ),
            Incident(
                incident_type = "Type B", 
                location = "Secret Location B",
                description = "Confidential Description B",
                severity_level = SeverityLevel.HIGH,
                case_number = "CASE-B-002"
            )
        )
        
        // Store all incidents
        incidents.forEach { incidentRepository.insertIncident(it) }
        
        // Retrieve all
        val retrievedIncidents = incidentRepository.getAllIncidents()
        assertEquals("Should retrieve all incidents", incidents.size, retrievedIncidents.size)
        
        // Verify each incident's sensitive data
        retrievedIncidents.forEachIndexed { index, retrieved ->
            val original = incidents.find { it.incident_type == retrieved.incident_type }
            assertNotNull("Should find matching incident", original)
            assertEquals("Location should be decrypted correctly", original!!.location, retrieved.location)
            assertEquals("Description should be decrypted correctly", original.description, retrieved.description)
            assertEquals("Case number should be decrypted correctly", original.case_number, retrieved.case_number)
        }
    }
    
    @Test
    fun testNullSensitiveFieldsHandling() = runTest {
        val incident = Incident(
            incident_type = "Test",
            location = "Test Location",
            description = "Test Description", 
            severity_level = SeverityLevel.LOW,
            case_number = null // Test null handling
        )
        
        incidentRepository.insertIncident(incident)
        
        val retrieved = incidentRepository.getAllIncidents().first()
        assertNull("Null case number should remain null", retrieved.case_number)
        assertEquals("Other fields should work normally", incident.location, retrieved.location)
    }
    
    @Test
    fun testUpdateEncryptedData() = runTest {
        // Create and store initial incident
        val originalIncident = Incident(
            incident_type = "Test",
            location = "Original Location",
            description = "Original Description",
            severity_level = SeverityLevel.MEDIUM,
            case_number = "ORIGINAL-001"
        )
        
        incidentRepository.insertIncident(originalIncident)
        
        // Update with new sensitive data
        val updatedIncident = originalIncident.copy(
            location = "Updated Secret Location",
            description = "Updated Confidential Description",
            case_number = "UPDATED-002"
        )
        
        incidentRepository.updateIncident(updatedIncident)
        
        // Verify update
        val retrieved = incidentRepository.getIncidentById(originalIncident.id)
        assertNotNull("Should find updated incident", retrieved)
        assertEquals("Location should be updated", updatedIncident.location, retrieved!!.location)
        assertEquals("Description should be updated", updatedIncident.description, retrieved.description)
        assertEquals("Case number should be updated", updatedIncident.case_number, retrieved.case_number)
    }
    
    @Test
    fun testSearchThroughEncryptedData() = runTest {
        val incidents = listOf(
            Incident(
                incident_type = "Security",
                location = "Building Alpha",
                description = "Contains keyword: database",
                severity_level = SeverityLevel.HIGH
            ),
            Incident(
                incident_type = "Safety", 
                location = "Building Beta",
                description = "Contains keyword: server",
                severity_level = SeverityLevel.MEDIUM
            ),
            Incident(
                incident_type = "Physical",
                location = "Building Gamma", 
                description = "No relevant keywords here",
                severity_level = SeverityLevel.LOW
            )
        )
        
        incidents.forEach { incidentRepository.insertIncident(it) }
        
        // Test searching through decrypted data
        val allIncidents = incidentRepository.getAllIncidents()
        
        val databaseIncidents = allIncidents.filter { 
            it.description.contains("database", ignoreCase = true) 
        }
        assertEquals("Should find database incidents", 1, databaseIncidents.size)
        assertEquals("Should find correct incident", "Security", databaseIncidents.first().incident_type)
        
        val serverIncidents = allIncidents.filter { 
            it.description.contains("server", ignoreCase = true) 
        }
        assertEquals("Should find server incidents", 1, serverIncidents.size)
        assertEquals("Should find correct incident", "Safety", serverIncidents.first().incident_type)
    }
}
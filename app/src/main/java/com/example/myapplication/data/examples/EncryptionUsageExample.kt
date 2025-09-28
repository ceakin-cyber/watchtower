package com.example.myapplication.data.examples

import android.content.Context
import com.example.myapplication.data.repository.EncryptedIncidentRepository
import com.example.myapplication.data.repository.EncryptedEvidenceRepository
import com.example.myapplication.data.migration.EncryptionMigrationHelper
import com.example.myapplication.ui.incident.Incident
import com.example.myapplication.ui.incident.SeverityLevel
import com.example.myapplication.data.database.EvidenceAttachment
import com.example.myapplication.data.database.FileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Example showing how to use the encrypted database repositories.
 * This class demonstrates best practices for handling sensitive data.
 */
class EncryptionUsageExample(private val context: Context) {
    
    private val incidentRepository = EncryptedIncidentRepository(context)
    private val evidenceRepository = EncryptedEvidenceRepository(context)
    private val migrationHelper = EncryptionMigrationHelper(context)
    
    /**
     * Initialize the encrypted database system.
     * Call this once when the app starts.
     */
    suspend fun initializeEncryption() = withContext(Dispatchers.IO) {
        // Check if migration is needed for existing data
        if (migrationHelper.shouldRunMigration()) {
            val migrationSuccess = migrationHelper.migrateToEncryptedStorage()
            if (!migrationSuccess) {
                // Handle migration failure
                throw Exception("Failed to migrate existing data to encrypted storage")
            }
        }
    }
    
    /**
     * Example of storing an incident with encrypted sensitive data.
     */
    suspend fun storeSecureIncident() {
        val incident = Incident(
            incident_type = "Security Breach",
            location = "Building A, Floor 3, Room 301", // This will be encrypted
            description = "Unauthorized access detected in server room. Multiple failed login attempts observed.", // This will be encrypted
            severity_level = SeverityLevel.HIGH,
            reported_to_authorities = true,
            case_number = "CASE-2024-001" // This will be encrypted
        )
        
        // Data is automatically encrypted when stored
        incidentRepository.insertIncident(incident)
    }
    
    /**
     * Example of storing evidence with encrypted file paths and descriptions.
     */
    suspend fun storeSecureEvidence(incidentId: String) {
        val evidence = EvidenceAttachment(
            incident_id = incidentId,
            file_name = "security_footage.mp4",
            file_path = "/secure/storage/evidence/security_footage.mp4", // This will be encrypted
            file_type = FileType.VIDEO,
            file_size = 1024000,
            mime_type = "video/mp4",
            description = "Security camera footage showing unauthorized entry", // This will be encrypted
            hash_value = "sha256:abc123...",
            is_encrypted = true
        )
        
        // Data is automatically encrypted when stored
        evidenceRepository.insertEvidence(evidence)
    }
    
    /**
     * Example of retrieving and working with encrypted data.
     */
    suspend fun retrieveAndDisplayIncidents() {
        // Data is automatically decrypted when retrieved
        val incidents = incidentRepository.getAllIncidents()
        
        incidents.forEach { incident ->
            // The location, description, and case_number are automatically decrypted
            println("Incident: ${incident.incident_type}")
            println("Location: ${incident.location}") // Decrypted automatically
            println("Description: ${incident.description}") // Decrypted automatically
            println("Case Number: ${incident.case_number}") // Decrypted automatically
            
            // Get related evidence
            val evidence = evidenceRepository.getEvidenceByIncidentId(incident.id)
            evidence.forEach { attachment ->
                println("Evidence: ${attachment.file_name}")
                println("Path: ${attachment.file_path}") // Decrypted automatically
                println("Description: ${attachment.description}") // Decrypted automatically
            }
        }
    }
    
    /**
     * Example of searching through encrypted data.
     * Note: Searching encrypted data requires special considerations.
     */
    suspend fun searchIncidents(searchTerm: String): List<Incident> {
        // Since data is encrypted in storage, we need to decrypt and then filter
        val allIncidents = incidentRepository.getAllIncidents()
        
        return allIncidents.filter { incident ->
            incident.description.contains(searchTerm, ignoreCase = true) ||
            incident.location.contains(searchTerm, ignoreCase = true) ||
            incident.incident_type.contains(searchTerm, ignoreCase = true)
        }
    }
}
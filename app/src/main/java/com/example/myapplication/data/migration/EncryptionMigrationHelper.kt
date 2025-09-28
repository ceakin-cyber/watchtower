package com.example.myapplication.data.migration

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.data.database.IncidentDatabase
import com.example.myapplication.data.database.EncryptionConverters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EncryptionMigrationHelper(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("encryption_migration", Context.MODE_PRIVATE)
    private val database = IncidentDatabase.getDatabase(context)
    
    companion object {
        private const val MIGRATION_COMPLETED_KEY = "encryption_migration_completed"
        private const val MIGRATION_VERSION_KEY = "migration_version"
        private const val CURRENT_MIGRATION_VERSION = 1
    }
    
    suspend fun migrateToEncryptedStorage(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (isMigrationCompleted()) {
                return@withContext true
            }
            
            // Get all existing incidents and evidence
            val incidents = database.incidentDao().getAllIncidentsSuspend()
            val evidenceList = database.evidenceAttachmentDao().getAllEvidenceSuspend()
            
            // Encrypt sensitive fields for incidents
            incidents.forEach { incident ->
                val encryptedIncident = incident.copy(
                    location = EncryptionConverters.encryptString(incident.location) ?: incident.location,
                    description = EncryptionConverters.encryptString(incident.description) ?: incident.description,
                    case_number = EncryptionConverters.encryptString(incident.case_number)
                )
                database.incidentDao().updateIncident(encryptedIncident)
            }
            
            // Encrypt sensitive fields for evidence
            evidenceList.forEach { evidence ->
                val encryptedEvidence = evidence.copy(
                    file_path = EncryptionConverters.encryptString(evidence.file_path) ?: evidence.file_path,
                    description = EncryptionConverters.encryptString(evidence.description)
                )
                database.evidenceAttachmentDao().updateAttachment(encryptedEvidence)
            }
            
            // Mark migration as completed
            markMigrationCompleted()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun isMigrationCompleted(): Boolean {
        val migrationCompleted = prefs.getBoolean(MIGRATION_COMPLETED_KEY, false)
        val migrationVersion = prefs.getInt(MIGRATION_VERSION_KEY, 0)
        return migrationCompleted && migrationVersion >= CURRENT_MIGRATION_VERSION
    }
    
    private fun markMigrationCompleted() {
        prefs.edit()
            .putBoolean(MIGRATION_COMPLETED_KEY, true)
            .putInt(MIGRATION_VERSION_KEY, CURRENT_MIGRATION_VERSION)
            .apply()
    }
    
    fun shouldRunMigration(): Boolean = !isMigrationCompleted()
}
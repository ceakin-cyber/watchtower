package com.example.myapplication.data.repository

import android.content.Context
import com.example.myapplication.data.database.EvidenceAttachmentDao
import com.example.myapplication.data.database.EvidenceAttachment
import com.example.myapplication.data.database.IncidentDatabase
import com.example.myapplication.data.database.EncryptionConverters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EncryptedEvidenceRepository(context: Context) {
    
    private val evidenceDao: EvidenceAttachmentDao = IncidentDatabase.getDatabase(context).evidenceAttachmentDao()
    
    suspend fun insertEvidence(evidence: EvidenceAttachment) = withContext(Dispatchers.IO) {
        val encryptedEvidence = encryptEvidence(evidence)
        evidenceDao.insertAttachment(encryptedEvidence)
    }
    
    suspend fun getAllEvidence(): List<EvidenceAttachment> = withContext(Dispatchers.IO) {
        val evidenceList = evidenceDao.getAllEvidenceSuspend()
        evidenceList.map { decryptEvidence(it) }
    }
    
    suspend fun getEvidenceByIncidentId(incidentId: String): List<EvidenceAttachment> = withContext(Dispatchers.IO) {
        val evidenceList = evidenceDao.getAttachmentsForIncidentSuspend(incidentId)
        evidenceList.map { decryptEvidence(it) }
    }
    
    suspend fun getEvidenceById(id: String): EvidenceAttachment? = withContext(Dispatchers.IO) {
        val evidence = evidenceDao.getAttachmentById(id)
        evidence?.let { decryptEvidence(it) }
    }
    
    suspend fun updateEvidence(evidence: EvidenceAttachment) = withContext(Dispatchers.IO) {
        val encryptedEvidence = encryptEvidence(evidence)
        evidenceDao.updateAttachment(encryptedEvidence)
    }
    
    suspend fun deleteEvidence(evidence: EvidenceAttachment) = withContext(Dispatchers.IO) {
        evidenceDao.deleteAttachment(evidence)
    }
    
    private fun encryptEvidence(evidence: EvidenceAttachment): EvidenceAttachment {
        return evidence.copy(
            file_path = EncryptionConverters.encryptString(evidence.file_path) ?: evidence.file_path,
            description = EncryptionConverters.encryptString(evidence.description)
        )
    }
    
    private fun decryptEvidence(evidence: EvidenceAttachment): EvidenceAttachment {
        return evidence.copy(
            file_path = EncryptionConverters.decryptString(evidence.file_path) ?: evidence.file_path,
            description = EncryptionConverters.decryptString(evidence.description)
        )
    }
}
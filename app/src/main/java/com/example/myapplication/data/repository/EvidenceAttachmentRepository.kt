package com.example.myapplication.data.repository

import androidx.lifecycle.LiveData
import com.example.myapplication.data.database.EvidenceAttachment
import com.example.myapplication.data.database.EvidenceAttachmentDao
import com.example.myapplication.data.database.FileType

class EvidenceAttachmentRepository(private val evidenceAttachmentDao: EvidenceAttachmentDao) {
    
    fun getAttachmentsForIncident(incidentId: String): LiveData<List<EvidenceAttachment>> {
        return evidenceAttachmentDao.getAttachmentsForIncident(incidentId)
    }
    
    suspend fun insertAttachment(attachment: EvidenceAttachment) {
        evidenceAttachmentDao.insertAttachment(attachment)
    }
    
    suspend fun updateAttachment(attachment: EvidenceAttachment) {
        evidenceAttachmentDao.updateAttachment(attachment)
    }
    
    suspend fun deleteAttachment(attachment: EvidenceAttachment) {
        evidenceAttachmentDao.deleteAttachment(attachment)
    }
    
    suspend fun getAttachmentById(id: String): EvidenceAttachment? {
        return evidenceAttachmentDao.getAttachmentById(id)
    }
    
    suspend fun getAttachmentsByType(fileType: FileType): List<EvidenceAttachment> {
        return evidenceAttachmentDao.getAttachmentsByType(fileType)
    }
    
    suspend fun searchAttachmentsByFileName(searchTerm: String): List<EvidenceAttachment> {
        return evidenceAttachmentDao.searchAttachmentsByFileName(searchTerm)
    }
    
    suspend fun getAttachmentsForIncidentSuspend(incidentId: String): List<EvidenceAttachment> {
        return evidenceAttachmentDao.getAttachmentsForIncidentSuspend(incidentId)
    }
    
    suspend fun getAttachmentCountForIncident(incidentId: String): Int {
        return evidenceAttachmentDao.getAttachmentCountForIncident(incidentId)
    }
    
    suspend fun getTotalFileSizeForIncident(incidentId: String): Long {
        return evidenceAttachmentDao.getTotalFileSizeForIncident(incidentId) ?: 0L
    }
    
    suspend fun deleteAttachmentsForIncident(incidentId: String) {
        evidenceAttachmentDao.deleteAttachmentsForIncident(incidentId)
    }
}
package com.example.myapplication.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface EvidenceAttachmentDao {
    
    @Query("SELECT * FROM evidence_attachments WHERE incident_id = :incidentId ORDER BY upload_timestamp DESC")
    fun getAttachmentsForIncident(incidentId: String): LiveData<List<EvidenceAttachment>>
    
    @Query("SELECT * FROM evidence_attachments ORDER BY upload_timestamp DESC")
    suspend fun getAllEvidenceSuspend(): List<EvidenceAttachment>
    
    @Query("SELECT * FROM evidence_attachments WHERE incident_id = :incidentId ORDER BY upload_timestamp DESC")
    suspend fun getAttachmentsForIncidentSuspend(incidentId: String): List<EvidenceAttachment>
    
    @Query("SELECT * FROM evidence_attachments WHERE id = :id")
    suspend fun getAttachmentById(id: String): EvidenceAttachment?
    
    @Query("SELECT * FROM evidence_attachments WHERE file_type = :fileType ORDER BY upload_timestamp DESC")
    suspend fun getAttachmentsByType(fileType: FileType): List<EvidenceAttachment>
    
    @Query("SELECT * FROM evidence_attachments WHERE file_name LIKE '%' || :searchTerm || '%' ORDER BY upload_timestamp DESC")
    suspend fun searchAttachmentsByFileName(searchTerm: String): List<EvidenceAttachment>
    
    @Query("SELECT * FROM evidence_attachments WHERE upload_timestamp BETWEEN :startDate AND :endDate ORDER BY upload_timestamp DESC")
    suspend fun getAttachmentsByDateRange(startDate: Long, endDate: Long): List<EvidenceAttachment>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: EvidenceAttachment)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachments(attachments: List<EvidenceAttachment>)
    
    @Update
    suspend fun updateAttachment(attachment: EvidenceAttachment)
    
    @Delete
    suspend fun deleteAttachment(attachment: EvidenceAttachment)
    
    @Query("DELETE FROM evidence_attachments WHERE id = :id")
    suspend fun deleteAttachmentById(id: String)
    
    @Query("DELETE FROM evidence_attachments WHERE incident_id = :incidentId")
    suspend fun deleteAttachmentsForIncident(incidentId: String)
    
    @Query("SELECT COUNT(*) FROM evidence_attachments WHERE incident_id = :incidentId")
    suspend fun getAttachmentCountForIncident(incidentId: String): Int
    
    @Query("SELECT COUNT(*) FROM evidence_attachments WHERE file_type = :fileType")
    suspend fun getAttachmentCountByType(fileType: FileType): Int
    
    @Query("SELECT SUM(file_size) FROM evidence_attachments WHERE incident_id = :incidentId")
    suspend fun getTotalFileSizeForIncident(incidentId: String): Long?
}
package com.example.myapplication.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExportLogDao {
    
    @Insert
    suspend fun insertExportLog(exportLog: ExportLog)
    
    @Query("SELECT * FROM export_logs ORDER BY exportedAt DESC")
    fun getAllExportLogs(): Flow<List<ExportLog>>
    
    @Query("SELECT * FROM export_logs WHERE exportType = :type ORDER BY exportedAt DESC")
    fun getExportLogsByType(type: String): Flow<List<ExportLog>>
    
    @Query("SELECT * FROM export_logs WHERE exportedAt >= :startDate AND exportedAt <= :endDate ORDER BY exportedAt DESC")
    fun getExportLogsByDateRange(startDate: Long, endDate: Long): Flow<List<ExportLog>>
    
    @Query("DELETE FROM export_logs WHERE exportedAt < :cutoffDate")
    suspend fun deleteOldExportLogs(cutoffDate: Long)
    
    @Query("SELECT COUNT(*) FROM export_logs")
    suspend fun getExportLogCount(): Int
}
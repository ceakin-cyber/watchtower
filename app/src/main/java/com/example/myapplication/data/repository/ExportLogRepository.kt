package com.example.myapplication.data.repository

import com.example.myapplication.data.database.ExportLog
import com.example.myapplication.data.database.ExportLogDao
import kotlinx.coroutines.flow.Flow
import java.io.File

class ExportLogRepository(private val exportLogDao: ExportLogDao) {
    
    suspend fun logExport(
        exportType: String,
        incidentCount: Int,
        fileName: String,
        file: File,
        dateRangeStart: Long? = null,
        dateRangeEnd: Long? = null
    ) {
        val exportLog = ExportLog(
            exportType = exportType,
            exportedAt = System.currentTimeMillis(),
            incidentCount = incidentCount,
            dateRangeStart = dateRangeStart,
            dateRangeEnd = dateRangeEnd,
            fileName = fileName,
            fileSize = file.length()
        )
        exportLogDao.insertExportLog(exportLog)
    }
    
    fun getAllExportLogs(): Flow<List<ExportLog>> {
        return exportLogDao.getAllExportLogs()
    }
    
    fun getExportLogsByType(type: String): Flow<List<ExportLog>> {
        return exportLogDao.getExportLogsByType(type)
    }
    
    fun getExportLogsByDateRange(startDate: Long, endDate: Long): Flow<List<ExportLog>> {
        return exportLogDao.getExportLogsByDateRange(startDate, endDate)
    }
    
    suspend fun cleanupOldLogs(daysToKeep: Int = 30) {
        val cutoffDate = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        exportLogDao.deleteOldExportLogs(cutoffDate)
    }
    
    suspend fun getExportLogCount(): Int {
        return exportLogDao.getExportLogCount()
    }
}
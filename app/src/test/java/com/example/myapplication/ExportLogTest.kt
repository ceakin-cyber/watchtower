package com.example.myapplication

import com.example.myapplication.data.database.ExportLog
import org.junit.Test
import org.junit.Assert.*

class ExportLogTest {

    @Test
    fun testExportLogCreation() {
        val exportLog = ExportLog(
            id = 1,
            exportType = "JSON",
            exportedAt = System.currentTimeMillis(),
            incidentCount = 5,
            dateRangeStart = null,
            dateRangeEnd = null,
            fileName = "test_export.json",
            fileSize = 1024
        )

        assertEquals("JSON", exportLog.exportType)
        assertEquals(5, exportLog.incidentCount)
        assertEquals("test_export.json", exportLog.fileName)
        assertEquals(1024, exportLog.fileSize)
    }

    @Test
    fun testExportLogWithDateRange() {
        val startDate = System.currentTimeMillis() - 86400000 // yesterday
        val endDate = System.currentTimeMillis()
        
        val exportLog = ExportLog(
            exportType = "PDF",
            exportedAt = endDate,
            incidentCount = 10,
            dateRangeStart = startDate,
            dateRangeEnd = endDate,
            fileName = "filtered_export.pdf",
            fileSize = 2048
        )

        assertEquals("PDF", exportLog.exportType)
        assertEquals(10, exportLog.incidentCount)
        assertEquals(startDate, exportLog.dateRangeStart)
        assertEquals(endDate, exportLog.dateRangeEnd)
        assertEquals("filtered_export.pdf", exportLog.fileName)
    }
}
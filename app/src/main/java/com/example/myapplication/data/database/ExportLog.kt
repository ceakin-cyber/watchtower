package com.example.myapplication.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "export_logs")
data class ExportLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exportType: String, // "JSON" or "PDF"
    val exportedAt: Long, // timestamp
    val incidentCount: Int, // number of incidents exported
    val dateRangeStart: Long? = null, // start date filter (if any)
    val dateRangeEnd: Long? = null, // end date filter (if any)
    val fileName: String, // generated filename
    val fileSize: Long = 0 // file size in bytes
)
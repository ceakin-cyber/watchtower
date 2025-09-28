package com.example.myapplication.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Index
import com.example.myapplication.ui.incident.Incident

@Entity(
    tableName = "evidence_attachments",
    foreignKeys = [ForeignKey(
        entity = Incident::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("incident_id"),
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["incident_id"])]
)
@TypeConverters(EvidenceConverters::class)
data class EvidenceAttachment(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val incident_id: String,
    val file_name: String,
    val file_path: String,
    val file_type: FileType,
    val file_size: Long = 0L,
    val mime_type: String? = null,
    val upload_timestamp: Long = System.currentTimeMillis(),
    val description: String? = null,
    val hash_value: String? = null,
    val is_encrypted: Boolean = true
)

enum class FileType {
    PHOTO, AUDIO, DOCUMENT, SCREENSHOT, VIDEO, OTHER
}

class EvidenceConverters {
    @TypeConverter
    fun fromFileType(value: FileType): String {
        return value.name
    }

    @TypeConverter
    fun toFileType(value: String): FileType {
        return FileType.valueOf(value)
    }
}
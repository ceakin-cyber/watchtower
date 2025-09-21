package com.example.myapplication.ui.incident

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "incidents")
@TypeConverters(Converters::class)
data class Incident(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val incident_type: String,
    val location: String,
    val description: String,
    val evidence_attachments: List<String> = emptyList(),
    val severity_level: SeverityLevel,
    val reported_to_authorities: Boolean = false,
    val case_number: String? = null,
    
    // Add these as actual fields instead of computed properties
    val type: String = incident_type,
    val dateTime: String = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(timestamp))
)

enum class SeverityLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

class Converters {
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType) ?: emptyList()
    }

    @TypeConverter
    fun fromSeverityLevel(value: SeverityLevel): String {
        return value.name
    }

    @TypeConverter
    fun toSeverityLevel(value: String): SeverityLevel {
        return SeverityLevel.valueOf(value)
    }
}
package com.example.myapplication.ui.activitylog

import android.content.Context
import com.example.myapplication.ui.incident.Incident
import com.example.myapplication.data.repository.ExportLogRepository
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class IncidentJsonExporter(
    private val context: Context,
    private val exportLogRepository: ExportLogRepository? = null
) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val fileNameDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    suspend fun exportIncidents(incidents: List<Incident>, startDate: Long? = null, endDate: Long? = null): File {
        val fileName = "incident_report_${fileNameDateFormat.format(Date())}.json"
        val file = File(context.filesDir, fileName)
        
        // Build JSON manually to avoid Gson dependency issues
        val json = StringBuilder()
        json.append("{\n")
        
        // Metadata
        json.append("  \"metadata\": {\n")
        json.append("    \"generated_on\": \"${dateFormat.format(Date())}\",\n")
        json.append("    \"total_incidents\": ${incidents.size}")
        
        if (startDate != null && endDate != null) {
            val displayDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            json.append(",\n    \"date_range_start\": \"${displayDateFormat.format(Date(startDate))}\",\n")
            json.append("    \"date_range_end\": \"${displayDateFormat.format(Date(endDate))}\"")
        }
        
        json.append("\n  },\n")
        
        // Summary statistics
        json.append("  \"severity_summary\": {\n")
        val severityCounts = incidents.groupBy { it.severity_level }.mapValues { it.value.size }
        val summaryEntries = severityCounts.map { (severity, count) ->
            "    \"${severity.name.lowercase()}\": $count"
        }
        json.append(summaryEntries.joinToString(",\n"))
        json.append("\n  },\n")
        
        // Incidents
        json.append("  \"incidents\": [\n")
        val incidentEntries = incidents.sortedByDescending { it.timestamp }.map { incident ->
            val evidenceArray = incident.evidence_attachments.joinToString("\", \"", "\"", "\"")
            """    {
      "id": "${incident.id}",
      "timestamp": ${incident.timestamp},
      "date_time": "${incident.dateTime}",
      "incident_type": "${escapeJson(incident.incident_type)}",
      "location": "${escapeJson(incident.location)}",
      "description": "${escapeJson(incident.description)}",
      "severity_level": "${incident.severity_level.name}",
      "reported_to_authorities": ${incident.reported_to_authorities},
      "case_number": ${if (incident.case_number != null) "\"${escapeJson(incident.case_number)}\"" else "null"},
      "evidence_attachments": [${if (incident.evidence_attachments.isNotEmpty()) evidenceArray else ""}]
    }"""
        }
        json.append(incidentEntries.joinToString(",\n"))
        json.append("\n  ]\n")
        json.append("}")
        
        FileWriter(file).use { writer ->
            writer.write(json.toString())
        }
        
        // Log the export
        exportLogRepository?.logExport(
            exportType = "JSON",
            incidentCount = incidents.size,
            fileName = fileName,
            file = file,
            dateRangeStart = startDate,
            dateRangeEnd = endDate
        )
        
        return file
    }
    
    private fun escapeJson(text: String): String {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t")
    }
}
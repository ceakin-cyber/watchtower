package com.example.myapplication.data.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myapplication.ui.incident.Incident
import com.example.myapplication.ui.incident.SeverityLevel

@Dao
interface IncidentDao {
    
    @Query("SELECT * FROM incidents ORDER BY timestamp DESC")
    fun getAllIncidents(): LiveData<List<Incident>>
    
    @Query("SELECT * FROM incidents WHERE id = :id")
    suspend fun getIncidentById(id: String): Incident?
    
    @Query("SELECT * FROM incidents WHERE incident_type = :type ORDER BY timestamp DESC")
    suspend fun getIncidentsByType(type: String): List<Incident>
    
    @Query("SELECT * FROM incidents WHERE severity_level = :severityLevel ORDER BY timestamp DESC")
    suspend fun getIncidentsBySeverity(severityLevel: SeverityLevel): List<Incident>
    
    @Query("SELECT * FROM incidents WHERE reported_to_authorities = :reported ORDER BY timestamp DESC")
    suspend fun getIncidentsByAuthorityStatus(reported: Boolean): List<Incident>
    
    @Query("SELECT * FROM incidents WHERE case_number = :caseNumber")
    suspend fun getIncidentByCaseNumber(caseNumber: String): Incident?
    
    @Query("SELECT * FROM incidents WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    suspend fun getIncidentsByDateRange(startDate: Long, endDate: Long): List<Incident>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: Incident)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncidents(incidents: List<Incident>)
    
    @Update
    suspend fun updateIncident(incident: Incident)
    
    @Delete
    suspend fun deleteIncident(incident: Incident)
    
    @Query("DELETE FROM incidents WHERE id = :id")
    suspend fun deleteIncidentById(id: String)
    
    @Query("DELETE FROM incidents")
    suspend fun deleteAllIncidents()
    
    @Query("SELECT COUNT(*) FROM incidents")
    suspend fun getIncidentCount(): Int
    
    @Query("SELECT COUNT(*) FROM incidents WHERE severity_level = :severityLevel")
    suspend fun getIncidentCountBySeverity(severityLevel: SeverityLevel): Int
}
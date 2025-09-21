package com.example.myapplication.data.repository

import androidx.lifecycle.LiveData
import com.example.myapplication.data.database.IncidentDao
import com.example.myapplication.ui.incident.Incident
import com.example.myapplication.ui.incident.SeverityLevel

class IncidentRepository(private val incidentDao: IncidentDao) {

    fun getAllIncidents(): LiveData<List<Incident>> {
        return incidentDao.getAllIncidents()
    }

    suspend fun insertIncident(incident: Incident) {
        println("DEBUG: Repository inserting incident: ${incident.id}")
        incidentDao.insertIncident(incident)
        println("DEBUG: Repository insert completed")
    }

    suspend fun updateIncident(incident: Incident) {
        incidentDao.updateIncident(incident)
    }

    suspend fun deleteIncident(incident: Incident) {
        incidentDao.deleteIncident(incident)
    }

    suspend fun getIncidentById(id: String): Incident? {
        return incidentDao.getIncidentById(id)
    }

    suspend fun getIncidentsByType(type: String): List<Incident> {
        return incidentDao.getIncidentsByType(type)
    }

    suspend fun getIncidentsBySeverity(severityLevel: SeverityLevel): List<Incident> {
        return incidentDao.getIncidentsBySeverity(severityLevel)
    }

    suspend fun getIncidentsByAuthorityStatus(reported: Boolean): List<Incident> {
        return incidentDao.getIncidentsByAuthorityStatus(reported)
    }

    suspend fun getIncidentCount(): Int {
        return incidentDao.getIncidentCount()
    }
}
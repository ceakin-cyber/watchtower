package com.example.myapplication.data.repository

import android.content.Context
import com.example.myapplication.data.database.IncidentDao
import com.example.myapplication.data.database.IncidentDatabase
import com.example.myapplication.data.database.EncryptionConverters
import com.example.myapplication.ui.incident.Incident
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class EncryptedIncidentRepository(context: Context) {
    
    private val incidentDao: IncidentDao = IncidentDatabase.getDatabase(context).incidentDao()
    
    suspend fun insertIncident(incident: Incident) = withContext(Dispatchers.IO) {
        val encryptedIncident = encryptIncident(incident)
        incidentDao.insertIncident(encryptedIncident)
    }
    
    suspend fun getAllIncidents(): List<Incident> = withContext(Dispatchers.IO) {
        val incidents = incidentDao.getAllIncidentsSuspend()
        incidents.map { decryptIncident(it) }
    }
    
    suspend fun getIncidentById(id: String): Incident? = withContext(Dispatchers.IO) {
        val incident = incidentDao.getIncidentById(id)
        incident?.let { decryptIncident(it) }
    }
    
    suspend fun updateIncident(incident: Incident) = withContext(Dispatchers.IO) {
        val encryptedIncident = encryptIncident(incident)
        incidentDao.updateIncident(encryptedIncident)
    }
    
    suspend fun deleteIncident(incident: Incident) = withContext(Dispatchers.IO) {
        incidentDao.deleteIncident(incident)
    }
    
    private fun encryptIncident(incident: Incident): Incident {
        return incident.copy(
            location = EncryptionConverters.encryptString(incident.location) ?: incident.location,
            description = EncryptionConverters.encryptString(incident.description) ?: incident.description,
            case_number = EncryptionConverters.encryptString(incident.case_number)
        )
    }
    
    private fun decryptIncident(incident: Incident): Incident {
        return incident.copy(
            location = EncryptionConverters.decryptString(incident.location) ?: incident.location,
            description = EncryptionConverters.decryptString(incident.description) ?: incident.description,
            case_number = EncryptionConverters.decryptString(incident.case_number)
        )
    }
}
package com.example.myapplication.ui.incident

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.IncidentDatabase
import com.example.myapplication.data.repository.IncidentRepository
import kotlinx.coroutines.launch

class IncidentViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: IncidentRepository
    val allIncidents: LiveData<List<Incident>>

    private val _operationStatus = MutableLiveData<Pair<Boolean, String>>()
    val operationStatus: LiveData<Pair<Boolean, String>> = _operationStatus

    init {
        val incidentDao = IncidentDatabase.getDatabase(application).incidentDao()
        repository = IncidentRepository(incidentDao)
        allIncidents = repository.getAllIncidents()
    }

    fun insertIncident(incident: Incident) {
        viewModelScope.launch {
            try {
                println("DEBUG: Attempting to save incident: ${incident.id}")
                repository.insertIncident(incident)
                println("DEBUG: Incident saved successfully: ${incident.id}")
                _operationStatus.postValue(Pair(true, "Incident saved successfully"))
            } catch (e: Exception) {
                println("DEBUG: Failed to save incident: ${e.message}")
                e.printStackTrace()
                _operationStatus.postValue(Pair(false, "Failed to save incident: ${e.message}"))
            }
        }
    }

    fun updateIncident(incident: Incident) {
        viewModelScope.launch {
            try {
                repository.updateIncident(incident)
                _operationStatus.postValue(Pair(true, "Incident updated successfully"))
            } catch (e: Exception) {
                _operationStatus.postValue(Pair(false, "Failed to update incident: ${e.message}"))
            }
        }
    }

    fun deleteIncident(incident: Incident) {
        viewModelScope.launch {
            try {
                repository.deleteIncident(incident)
                _operationStatus.postValue(Pair(true, "Incident deleted successfully"))
            } catch (e: Exception) {
                _operationStatus.postValue(Pair(false, "Failed to delete incident: ${e.message}"))
            }
        }
    }

    fun getIncidentById(id: String, callback: (Incident?) -> Unit) {
        viewModelScope.launch {
            try {
                val incident = repository.getIncidentById(id)
                callback(incident)
            } catch (e: Exception) {
                callback(null)
            }
        }
    }

    fun clearOperationStatus() {
        _operationStatus.value = Pair(false, "")
    }
}
package com.example.myapplication.ui.activitylog

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.database.IncidentDatabase
import com.example.myapplication.data.repository.IncidentRepository
import com.example.myapplication.ui.incident.Incident
import kotlinx.coroutines.launch

class ActivityLogViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: IncidentRepository
    val allIncidents: LiveData<List<Incident>>
    
    private val _filteredIncidents = MutableLiveData<List<Incident>?>()
    val filteredIncidents: LiveData<List<Incident>?> = _filteredIncidents
    
    private var startDateFilter: Long? = null
    private var endDateFilter: Long? = null

    init {
        val incidentDao = IncidentDatabase.getDatabase(application).incidentDao()
        repository = IncidentRepository(incidentDao)
        allIncidents = repository.getAllIncidents()
        println("DEBUG: ActivityLogViewModel initialized")
        
        // Debug: Check if we can count incidents
        viewModelScope.launch {
            try {
                val count = incidentDao.getIncidentCount()
                println("DEBUG: Total incidents in database: $count")
            } catch (e: Exception) {
                println("DEBUG: Error counting incidents: ${e.message}")
            }
        }
    }

    fun deleteIncident(incidentId: String) {
        viewModelScope.launch {
            repository.deleteIncidentById(incidentId)
        }
    }
    
    fun setDateFilter(startDate: Long?, endDate: Long?) {
        startDateFilter = startDate
        endDateFilter = endDate
        applyDateFilter()
    }
    
    fun clearDateFilter() {
        startDateFilter = null
        endDateFilter = null
        _filteredIncidents.value = null
    }
    
    private fun applyDateFilter() {
        if (startDateFilter != null && endDateFilter != null) {
            viewModelScope.launch {
                try {
                    val filtered = repository.getIncidentsByDateRange(startDateFilter!!, endDateFilter!!)
                    _filteredIncidents.value = filtered
                } catch (e: Exception) {
                    println("DEBUG: Error filtering incidents by date: ${e.message}")
                    _filteredIncidents.value = emptyList()
                }
            }
        } else {
            _filteredIncidents.value = null
        }
    }
}
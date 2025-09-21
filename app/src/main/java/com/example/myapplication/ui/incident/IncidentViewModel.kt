package com.example.myapplication.ui.incident

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class IncidentViewModel : ViewModel() {

    private val _incidents = MutableLiveData<MutableList<Incident>>().apply {
        value = mutableListOf()
    }
    val incidents: LiveData<MutableList<Incident>> = _incidents

    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus

    fun saveIncident(incident: Incident) {
        val currentIncidents = _incidents.value ?: mutableListOf()
        currentIncidents.add(incident)
        _incidents.value = currentIncidents
        _saveStatus.value = true
    }

    fun getIncidentById(id: String): Incident? {
        return _incidents.value?.find { it.id == id }
    }

    fun deleteIncident(incidentId: String) {
        val currentIncidents = _incidents.value ?: mutableListOf()
        currentIncidents.removeAll { it.id == incidentId }
        _incidents.value = currentIncidents
    }

    fun getAllIncidents(): List<Incident> {
        return _incidents.value ?: emptyList()
    }
}
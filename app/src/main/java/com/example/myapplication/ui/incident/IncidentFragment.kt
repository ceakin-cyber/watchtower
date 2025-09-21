package com.example.myapplication.ui.incident

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentIncidentBinding
import java.text.SimpleDateFormat
import java.util.*

class IncidentFragment : Fragment() {

    private var _binding: FragmentIncidentBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var incidentViewModel: IncidentViewModel
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        incidentViewModel = ViewModelProvider(this).get(IncidentViewModel::class.java)
        
        _binding = FragmentIncidentBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setupIncidentTypeSpinner()
        setupDateTimePicker()
        setupButtons()

        return root
    }

    private fun setupIncidentTypeSpinner() {
        val incidentTypes = arrayOf(
            "Security Breach",
            "System Outage",
            "Data Loss",
            "Network Issue",
            "Hardware Failure",
            "Software Bug",
            "Other"
        )
        
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            incidentTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerIncidentType.adapter = adapter
    }

    private fun setupDateTimePicker() {
        binding.edittextIncidentDate.setText(dateFormat.format(calendar.time))
        
        binding.edittextIncidentDate.setOnClickListener {
            showDateTimePicker()
        }
    }

    private fun showDateTimePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                
                val timePickerDialog = TimePickerDialog(
                    requireContext(),
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        binding.edittextIncidentDate.setText(dateFormat.format(calendar.time))
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                )
                timePickerDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun setupButtons() {
        binding.btnSaveIncident.setOnClickListener {
            saveIncident()
        }
        
        binding.btnCancelIncident.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun saveIncident() {
        val incidentType = binding.spinnerIncidentType.selectedItem.toString()
        val description = binding.edittextIncidentDescription.text.toString()
        val dateTime = binding.edittextIncidentDate.text.toString()

        if (description.isBlank()) {
            Toast.makeText(requireContext(), "Please enter a description", Toast.LENGTH_SHORT).show()
            return
        }

        val incident = Incident(
            type = incidentType,
            description = description,
            dateTime = dateTime,
            timestamp = System.currentTimeMillis()
        )

        incidentViewModel.saveIncident(incident)
        
        Toast.makeText(requireContext(), "Incident saved successfully", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
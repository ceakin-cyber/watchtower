package com.example.myapplication.ui.incident

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
    private val evidenceFiles = mutableListOf<String>()

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris?.let {
            evidenceFiles.addAll(it.map { uri -> uri.toString() })
            updateEvidenceCount()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        incidentViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[IncidentViewModel::class.java]
        
        _binding = FragmentIncidentBinding.inflate(inflater, container, false)
        
        setupUI()
        observeViewModel()
        
        return binding.root
    }

    private fun setupUI() {
        setupIncidentTypeSpinner()
        setupSeveritySpinner()
        setupDateTimePicker()
        setupButtons()
        updateEvidenceCount()
    }

    private fun setupIncidentTypeSpinner() {
        val incidentTypes = arrayOf(
            "Online Harassment",
            "Cyberbullying",
            "Text/SMS Harassment",
            "Phone Call Harassment",
            "Stalking",
            "Vandalism",
            "Theft",
            "Assault",
            "Threats",
            "Fraud/Scam",
            "Identity Theft",
            "Trespassing",
            "Noise Complaint",
            "Property Damage",
            "Discrimination",
            "Sexual Harassment",
            "Domestic Violence",
            "Hit and Run",
            "Vehicle Break-in",
            "Burglary",
            "Suspicious Activity",
            "Lost/Stolen Item",
            "Other"
        )
        
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_incident_type_item,
            incidentTypes
        )
        adapter.setDropDownViewResource(R.layout.spinner_incident_type_item)
        binding.spinnerIncidentType.adapter = adapter
    }

    private fun setupSeveritySpinner() {
        val severityLevels = arrayOf("LOW", "MEDIUM", "HIGH", "CRITICAL")
        
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            severityLevels
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerSeverity.adapter = adapter
        
        // Set default to MEDIUM
        binding.spinnerSeverity.setSelection(1)
    }

    private fun setupDateTimePicker() {
        // Set current date/time as default
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
        println("DEBUG: Setting up buttons")
        println("DEBUG: Save button found: ${binding.btnSaveIncident}")
        
        binding.btnSaveIncident.setOnClickListener {
            println("DEBUG: Save button clicked")
            showSaveConfirmation()
        }
        
        binding.btnCancelIncident.setOnClickListener {
            findNavController().popBackStack()
        }
        
        binding.btnAddEvidence.setOnClickListener {
            filePickerLauncher.launch("*/*")
        }
    }

    private fun updateEvidenceCount() {
        val count = evidenceFiles.size
        binding.textEvidenceCount.text = "$count file${if (count != 1) "s" else ""} selected"
    }

    private fun showSaveConfirmation() {
        val incidentType = binding.spinnerIncidentType.selectedItem.toString()
        val location = binding.edittextLocation.text.toString().trim()
        val severity = binding.spinnerSeverity.selectedItem.toString()
        
        val message = "Are you sure you want to save this incident?\n\n" +
                "Type: $incidentType\n" +
                "Location: $location\n" +
                "Severity: $severity"
        
        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Save Incident")
            .setMessage(message)
            .setPositiveButton("Submit Report") { _, _ ->
                saveIncident()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveIncident() {
        // Validate required fields
        val incidentType = binding.spinnerIncidentType.selectedItem.toString()
        val location = binding.edittextLocation.text.toString().trim()
        val description = binding.edittextIncidentDescription.text.toString().trim()
        val severityLevel = binding.spinnerSeverity.selectedItem.toString()
        val reportedToAuthorities = binding.checkboxReportedAuthorities.isChecked
        val caseNumber = binding.edittextCaseNumber.text.toString().trim()

        // Validation
        if (location.isEmpty()) {
            showError("Please enter a location")
            return
        }

        if (description.isEmpty()) {
            showError("Please enter a description")
            return
        }

        // Disable save button to prevent double-click
        binding.btnSaveIncident.isEnabled = false
        binding.btnSaveIncident.text = "Submitting..."

        // Create incident object
        val incident = Incident(
            incident_type = incidentType,
            location = location,
            description = description,
            evidence_attachments = evidenceFiles.toList(),
            severity_level = SeverityLevel.valueOf(severityLevel),
            reported_to_authorities = reportedToAuthorities,
            case_number = if (caseNumber.isEmpty()) null else caseNumber,
            timestamp = calendar.timeInMillis
        )

        // Save incident
        incidentViewModel.insertIncident(incident)
    }

    private fun observeViewModel() {
        incidentViewModel.operationStatus.observe(viewLifecycleOwner) { (success, message) ->
            if (message.isNotEmpty()) {
                // Reset button state
                binding.btnSaveIncident.isEnabled = true
                binding.btnSaveIncident.text = "Submit Incident Report"
                
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                if (success) {
                    // Clear form and navigate back
                    clearForm()
                    findNavController().popBackStack()
                }
                incidentViewModel.clearOperationStatus()
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun clearForm() {
        binding.spinnerIncidentType.setSelection(0)
        binding.edittextLocation.text?.clear()
        binding.edittextIncidentDescription.text?.clear()
        binding.spinnerSeverity.setSelection(1) // Medium
        binding.checkboxReportedAuthorities.isChecked = false
        binding.edittextCaseNumber.text?.clear()
        evidenceFiles.clear()
        updateEvidenceCount()
        
        // Reset date to current
        calendar.timeInMillis = System.currentTimeMillis()
        binding.edittextIncidentDate.setText(dateFormat.format(calendar.time))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
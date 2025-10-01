package com.example.myapplication.ui.incident

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentIncidentBinding
import com.example.myapplication.data.database.EvidenceAttachment
import com.example.myapplication.data.database.FileType
import com.example.myapplication.data.repository.EvidenceAttachmentRepository
import com.example.myapplication.data.database.IncidentDatabase
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IncidentFragment : Fragment() {

    private var _binding: FragmentIncidentBinding? = null
    private val binding get() = _binding!!
    
    private val args: IncidentFragmentArgs by navArgs()
    private lateinit var incidentViewModel: IncidentViewModel
    private lateinit var evidenceRepository: EvidenceAttachmentRepository
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val evidenceFiles = mutableListOf<Uri>()
    private val savedAttachments = mutableListOf<EvidenceAttachment>()
    private var isEditMode = false
    private var editingIncident: Incident? = null

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        uris?.let {
            evidenceFiles.addAll(it)
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
        
        val database = IncidentDatabase.getDatabase(requireActivity().application)
        evidenceRepository = EvidenceAttachmentRepository(database.evidenceAttachmentDao())
        
        _binding = FragmentIncidentBinding.inflate(inflater, container, false)
        
        // Check if we're in edit mode
        isEditMode = args.incidentId != null
        
        setupUI()
        observeViewModel()
        
        if (isEditMode) {
            loadIncidentForEditing()
        }
        
        return binding.root
    }

    private fun setupUI() {
        setupIncidentTypeSpinner()
        setupSeveritySpinner()
        setupDateTimePicker()
        setupButtons()
        updateEvidenceCount()
        
        // Update UI for edit mode
        if (isEditMode) {
            binding.btnSaveIncident.text = "Update Incident"
            // Change the screen title if possible (this would need to be done via navigation or activity)
        }
    }
    
    private fun loadIncidentForEditing() {
        args.incidentId?.let { incidentId ->
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val database = IncidentDatabase.getDatabase(requireContext())
                    val incident = database.incidentDao().getIncidentById(incidentId)
                    
                    CoroutineScope(Dispatchers.Main).launch {
                        incident?.let {
                            editingIncident = it
                            populateFormWithIncident(it)
                        }
                    }
                } catch (e: Exception) {
                    CoroutineScope(Dispatchers.Main).launch {
                        showError("Failed to load incident: ${e.message}")
                    }
                }
            }
        }
    }
    
    private fun populateFormWithIncident(incident: Incident) {
        // Set incident type
        val incidentTypes = arrayOf(
            "Online Harassment", "Cyberbullying", "Text/SMS Harassment", "Phone Call Harassment",
            "Stalking", "Vandalism", "Theft", "Assault", "Threats", "Fraud/Scam",
            "Identity Theft", "Trespassing", "Noise Complaint", "Property Damage",
            "Discrimination", "Sexual Harassment", "Domestic Violence", "Hit and Run",
            "Vehicle Break-in", "Burglary", "Suspicious Activity", "Lost/Stolen Item", "Other"
        )
        val typeIndex = incidentTypes.indexOf(incident.incident_type)
        if (typeIndex >= 0) {
            binding.spinnerIncidentType.setSelection(typeIndex)
        }
        
        // Set location
        binding.edittextLocation.setText(incident.location)
        
        // Set description
        binding.edittextIncidentDescription.setText(incident.description)
        
        // Set severity
        val severityIndex = when (incident.severity_level) {
            SeverityLevel.LOW -> 0
            SeverityLevel.MEDIUM -> 1
            SeverityLevel.HIGH -> 2
            SeverityLevel.CRITICAL -> 3
        }
        binding.spinnerSeverity.setSelection(severityIndex)
        
        // Set reported to authorities
        binding.checkboxReportedAuthorities.isChecked = incident.reported_to_authorities
        
        // Set case number
        binding.edittextCaseNumber.setText(incident.case_number ?: "")
        
        // Set timestamp
        calendar.timeInMillis = incident.timestamp
        binding.edittextIncidentDate.setText(dateFormat.format(Date(incident.timestamp)))
        
        // Load existing attachments
        loadExistingAttachments(incident.id)
    }
    
    private fun loadExistingAttachments(incidentId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val attachments = evidenceRepository.getAttachmentsForIncidentSuspend(incidentId)
                CoroutineScope(Dispatchers.Main).launch {
                    savedAttachments.clear()
                    savedAttachments.addAll(attachments)
                    updateEvidenceCount()
                }
            } catch (e: Exception) {
                // Handle error silently for now
            }
        }
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
        val newFileCount = evidenceFiles.size
        val existingFileCount = savedAttachments.size
        val totalCount = newFileCount + existingFileCount
        
        binding.textEvidenceCount.text = when {
            totalCount == 0 -> "No files selected"
            isEditMode && existingFileCount > 0 && newFileCount > 0 -> 
                "$totalCount files ($existingFileCount existing, $newFileCount new)"
            isEditMode && existingFileCount > 0 -> 
                "$existingFileCount existing file${if (existingFileCount != 1) "s" else ""}"
            else -> "$newFileCount file${if (newFileCount != 1) "s" else ""} selected"
        }
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = "unknown_file"
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex) ?: "unknown_file"
                }
            }
        }
        return fileName
    }

    private fun getFileSizeFromUri(uri: Uri): Long {
        var fileSize = 0L
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex != -1) {
                    fileSize = it.getLong(sizeIndex)
                }
            }
        }
        return fileSize
    }

    private fun getMimeTypeFromUri(uri: Uri): String? {
        return requireContext().contentResolver.getType(uri)
    }

    private fun getFileTypeFromMimeType(mimeType: String?): FileType {
        return when {
            mimeType?.startsWith("image/") == true -> FileType.PHOTO
            mimeType?.startsWith("audio/") == true -> FileType.AUDIO
            mimeType?.startsWith("video/") == true -> FileType.VIDEO
            mimeType?.contains("pdf") == true || 
            mimeType?.contains("document") == true ||
            mimeType?.contains("text") == true -> FileType.DOCUMENT
            else -> FileType.OTHER
        }
    }

    private fun saveFileToInternalStorage(uri: Uri, fileName: String): String? {
        return try {
            val filesDir = File(requireContext().filesDir, "evidence_attachments")
            if (!filesDir.exists()) {
                filesDir.mkdirs()
            }
            
            val file = File(filesDir, "${System.currentTimeMillis()}_$fileName")
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun showSaveConfirmation() {
        val incidentType = binding.spinnerIncidentType.selectedItem.toString()
        val location = binding.edittextLocation.text.toString().trim()
        val severity = binding.spinnerSeverity.selectedItem.toString()
        
        val action = if (isEditMode) "update" else "save"
        val title = if (isEditMode) "Confirm Update Incident" else "Confirm Save Incident"
        val buttonText = if (isEditMode) "Update Report" else "Submit Report"
        
        val message = "Are you sure you want to $action this incident?\n\n" +
                "Type: $incidentType\n" +
                "Location: $location\n" +
                "Severity: $severity"
        
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(buttonText) { _, _ ->
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
        val buttonText = if (isEditMode) "Updating..." else "Submitting..."
        binding.btnSaveIncident.text = buttonText

        // Create or update incident object
        val incidentId = if (isEditMode) editingIncident?.id ?: java.util.UUID.randomUUID().toString() 
                        else java.util.UUID.randomUUID().toString()
        val incident = Incident(
            id = incidentId,
            incident_type = incidentType,
            location = location,
            description = description,
            evidence_attachments = emptyList(), // Will store attachment IDs separately
            severity_level = SeverityLevel.valueOf(severityLevel),
            reported_to_authorities = reportedToAuthorities,
            case_number = if (caseNumber.isEmpty()) null else caseNumber,
            timestamp = calendar.timeInMillis
        )

        // Save incident first, then attachments
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Save or update the incident in the database
                val repository = com.example.myapplication.data.repository.IncidentRepository(
                    com.example.myapplication.data.database.IncidentDatabase.getDatabase(requireContext()).incidentDao()
                )
                
                if (isEditMode) {
                    repository.updateIncident(incident)
                } else {
                    repository.insertIncident(incident)
                }
                
                // Then save files and create evidence attachments
                for (uri in evidenceFiles) {
                    val fileName = getFileNameFromUri(uri)
                    val filePath = saveFileToInternalStorage(uri, fileName)
                    
                    if (filePath != null) {
                        val mimeType = getMimeTypeFromUri(uri)
                        val fileType = getFileTypeFromMimeType(mimeType)
                        val fileSize = getFileSizeFromUri(uri)
                        
                        val attachment = EvidenceAttachment(
                            incident_id = incidentId,
                            file_name = fileName,
                            file_path = filePath,
                            file_type = fileType,
                            file_size = fileSize,
                            mime_type = mimeType
                        )
                        
                        evidenceRepository.insertAttachment(attachment)
                        savedAttachments.add(attachment)
                    }
                }
                
                // Update UI on main thread
                CoroutineScope(Dispatchers.Main).launch {
                    binding.btnSaveIncident.isEnabled = true
                    val defaultButtonText = if (isEditMode) "Update Incident" else "Submit Incident Report"
                    binding.btnSaveIncident.text = defaultButtonText
                    
                    val action = if (isEditMode) "updated" else "saved"
                    val newAttachmentCount = evidenceFiles.size
                    val totalAttachments = savedAttachments.size + newAttachmentCount
                    
                    val message = if (isEditMode && newAttachmentCount > 0) {
                        "Incident $action successfully with $newAttachmentCount new attachments (total: $totalAttachments)"
                    } else {
                        "Incident $action successfully with $totalAttachments attachments"
                    }
                    
                    Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                    if (!isEditMode) clearForm()
                    findNavController().popBackStack()
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    binding.btnSaveIncident.isEnabled = true
                    val defaultButtonText = if (isEditMode) "Update Incident" else "Submit Incident Report"
                    binding.btnSaveIncident.text = defaultButtonText
                    val action = if (isEditMode) "update" else "save"
                    showError("Failed to $action incident: ${e.message}")
                }
            }
        }
    }

    private fun observeViewModel() {
        incidentViewModel.operationStatus.observe(viewLifecycleOwner) { (success, message) ->
            if (message.isNotEmpty()) {
                // Reset button state
                binding.btnSaveIncident.isEnabled = true
                val defaultButtonText = if (isEditMode) "Update Incident" else "Submit Incident Report"
                binding.btnSaveIncident.text = defaultButtonText
                
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                if (success) {
                    // Clear form and navigate back
                    if (!isEditMode) clearForm()
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
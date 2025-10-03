package com.example.myapplication.ui.activitylog

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentActivityLogBinding
import com.example.myapplication.ui.incident.Incident
import com.example.myapplication.data.database.EvidenceAttachment
import com.example.myapplication.data.database.IncidentDatabase
import com.example.myapplication.data.repository.EvidenceAttachmentRepository
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ActivityLogFragment : Fragment() {

    private var _binding: FragmentActivityLogBinding? = null
    private val binding get() = _binding!!

    private lateinit var activityLogViewModel: ActivityLogViewModel
    private lateinit var incidentAdapter: IncidentAdapter
    private lateinit var evidenceRepository: EvidenceAttachmentRepository
    private lateinit var pdfExporter: IncidentPdfExporter
    private var currentIncidents: List<Incident> = emptyList()
    
    private var startDateFilter: Long? = null
    private var endDateFilter: Long? = null
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityLogViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[ActivityLogViewModel::class.java]

        val database = IncidentDatabase.getDatabase(requireActivity().application)
        evidenceRepository = EvidenceAttachmentRepository(database.evidenceAttachmentDao())
        pdfExporter = IncidentPdfExporter(requireContext())

        _binding = FragmentActivityLogBinding.inflate(inflater, container, false)
        
        setupRecyclerView()
        setupExportButton()
        setupDateFilterButtons()
        observeViewModel()
        
        // Debug: Add a test incident button temporarily
        addTestIncidentButton()
        
        return binding.root
    }

    private fun setupRecyclerView() {
        incidentAdapter = IncidentAdapter(
            onItemClick = { incident ->
                onIncidentClick(incident)
            },
            onEditClick = { incident ->
                onEditIncidentClick(incident)
            },
            onDeleteClick = { incident ->
                onDeleteIncidentClick(incident)
            },
            onAttachmentClick = { attachment ->
                onAttachmentClick(attachment)
            },
            getAttachmentsForIncident = { incidentId, callback ->
                lifecycleScope.launch {
                    try {
                        val attachments = evidenceRepository.getAttachmentsForIncidentSuspend(incidentId)
                        callback(attachments)
                    } catch (e: Exception) {
                        callback(emptyList())
                    }
                }
            }
        )
        
        binding.recyclerIncidents.apply {
            adapter = incidentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupRefreshButton() {
        binding.btnRefresh.setOnClickListener {
            // Simply re-observe the existing LiveData (it will automatically fetch fresh data)
            println("DEBUG: Refresh button clicked")
            binding.textIncidentCount.text = "Refreshing..."
            
            // Also do a direct count check
            lifecycleScope.launch {
                try {
                    val database = com.example.myapplication.data.database.IncidentDatabase.getDatabase(requireContext())
                    val count = database.incidentDao().getIncidentCount()
                    println("DEBUG: Direct count check: $count incidents")
                } catch (e: Exception) {
                    println("DEBUG: Direct count failed: ${e.message}")
                }
            }
        }
    }

    private fun setupExportButton() {
        binding.btnRefresh.setOnClickListener {
            // Simply re-observe the existing LiveData (it will automatically fetch fresh data)
            println("DEBUG: Refresh button clicked")
            binding.textIncidentCount.text = "Refreshing..."
            
            // Also do a direct count check
            lifecycleScope.launch {
                try {
                    val database = com.example.myapplication.data.database.IncidentDatabase.getDatabase(requireContext())
                    val count = database.incidentDao().getIncidentCount()
                    println("DEBUG: Direct count check: $count incidents")
                } catch (e: Exception) {
                    println("DEBUG: Direct count failed: ${e.message}")
                }
            }
        }
        
        // Set up PDF export button
        binding.btnExportPdf.setOnClickListener {
            exportToPdf()
        }
    }

    private fun exportToPdf() {
        if (currentIncidents.isEmpty()) {
            Toast.makeText(requireContext(), "No incidents to export", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val pdfFile = withContext(Dispatchers.IO) {
                    pdfExporter.exportIncidents(currentIncidents, startDateFilter, endDateFilter)
                }
                
                // Share the PDF file
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    pdfFile
                )
                
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Incident Activity Report")
                    putExtra(Intent.EXTRA_TEXT, "Please find the incident activity report attached.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val chooserIntent = Intent.createChooser(intent, "Export Incident Report")
                startActivity(chooserIntent)
                
                Toast.makeText(requireContext(), "PDF exported successfully", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                println("DEBUG: PDF export failed: ${e.message}")
                e.printStackTrace()
                Toast.makeText(requireContext(), "Failed to export PDF: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupDateFilterButtons() {
        binding.btnStartDate.setOnClickListener {
            showDatePicker { selectedDate ->
                startDateFilter = selectedDate
                binding.btnStartDate.text = dateFormat.format(Date(selectedDate))
                applyDateFilter()
            }
        }
        
        binding.btnEndDate.setOnClickListener {
            showDatePicker { selectedDate ->
                // Set end date to end of day (23:59:59)
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = selectedDate
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                
                endDateFilter = calendar.timeInMillis
                binding.btnEndDate.text = dateFormat.format(Date(selectedDate))
                applyDateFilter()
            }
        }
        
        binding.btnClearFilter.setOnClickListener {
            clearDateFilter()
        }
    }
    
    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth, 0, 0, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                onDateSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
    
    private fun applyDateFilter() {
        if (startDateFilter != null && endDateFilter != null) {
            if (startDateFilter!! > endDateFilter!!) {
                Toast.makeText(requireContext(), "Start date must be before end date", Toast.LENGTH_SHORT).show()
                return
            }
            activityLogViewModel.setDateFilter(startDateFilter, endDateFilter)
        }
    }
    
    private fun clearDateFilter() {
        startDateFilter = null
        endDateFilter = null
        binding.btnStartDate.text = "Start Date"
        binding.btnEndDate.text = "End Date"
        activityLogViewModel.clearDateFilter()
    }

    private fun observeViewModel() {
        // Observe filtered incidents first
        activityLogViewModel.filteredIncidents.observe(viewLifecycleOwner) { filteredIncidents ->
            if (filteredIncidents != null) {
                // Show filtered results
                updateIncidentList(filteredIncidents, "filtered")
            } else {
                // Filter was cleared, show all incidents
                activityLogViewModel.allIncidents.value?.let { allIncidents ->
                    updateIncidentList(allIncidents, "all")
                }
            }
        }
        
        // Observe all incidents
        activityLogViewModel.allIncidents.observe(viewLifecycleOwner) { incidents ->
            // Only show all incidents if no filter is active
            if (activityLogViewModel.filteredIncidents.value == null) {
                updateIncidentList(incidents, "all")
            }
        }
    }
    
    private fun updateIncidentList(incidents: List<Incident>, source: String) {
        println("DEBUG: ActivityLog received ${incidents.size} incidents from $source")
        
        // Store current incidents for export
        currentIncidents = incidents
        
        // Update count display
        val filterText = if (source == "filtered") " (filtered)" else ""
        binding.textIncidentCount.text = "${incidents.size} incident${if (incidents.size != 1) "s" else ""} found$filterText"
        
        if (incidents.isEmpty()) {
            println("DEBUG: Showing empty state")
            showEmptyState()
        } else {
            println("DEBUG: Showing incident list with ${incidents.size} items")
            incidents.forEach { incident ->
                println("DEBUG: Incident: ${incident.incident_type} at ${incident.location}")
            }
            showIncidentList()
            incidentAdapter.submitList(incidents)
        }
    }

    private fun showEmptyState() {
        binding.recyclerIncidents.visibility = View.GONE
        binding.layoutEmptyState.visibility = View.VISIBLE
    }

    private fun showIncidentList() {
        binding.recyclerIncidents.visibility = View.VISIBLE
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun onIncidentClick(incident: Incident) {
        // TODO: Navigate to incident detail view or show detailed dialog
        // For now, we'll just show a simple implementation
    }
    
    private fun onEditIncidentClick(incident: Incident) {
        val action = ActivityLogFragmentDirections.actionActivityLogToEditIncident(incident.id)
        findNavController().navigate(action)
    }
    
    private fun onDeleteIncidentClick(incident: Incident) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Incident")
            .setMessage("Are you sure you want to delete this incident?\n\nType: ${incident.incident_type}\nLocation: ${incident.location}\n\nThis action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteIncident(incident)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun deleteIncident(incident: Incident) {
        lifecycleScope.launch {
            try {
                // Delete associated evidence attachments first
                evidenceRepository.deleteAttachmentsForIncident(incident.id)
                
                // Delete the incident using ViewModel
                activityLogViewModel.deleteIncident(incident.id)
                
                Toast.makeText(requireContext(), "Incident deleted successfully", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to delete incident: ${e.message}", Toast.LENGTH_LONG).show()
                println("DEBUG: Delete incident failed: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun onAttachmentClick(attachment: EvidenceAttachment) {
        try {
            val file = File(attachment.file_path)
            if (!file.exists()) {
                Toast.makeText(requireContext(), "File not found: ${attachment.file_name}", Toast.LENGTH_SHORT).show()
                return
            }

            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, attachment.mime_type)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Check if there's an app that can handle this intent
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                // Fallback: try to open with a generic file manager or chooser
                val chooserIntent = Intent.createChooser(intent, "Open ${attachment.file_name}")
                if (chooserIntent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(chooserIntent)
                } else {
                    Toast.makeText(requireContext(), "No app found to open ${attachment.file_name}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error opening file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addTestIncidentButton() {
        // Temporarily add a test incident to debug database issues
        binding.btnRefresh.setOnLongClickListener {
            println("DEBUG: Long press detected - adding test incident")
            
            // Direct database test
            lifecycleScope.launch {
                try {
                    val database = com.example.myapplication.data.database.IncidentDatabase.getDatabase(requireContext())
                    val dao = database.incidentDao()
                    
                    val testIncident = com.example.myapplication.ui.incident.Incident(
                        incident_type = "Direct DB Test",
                        location = "Test Location Direct", 
                        description = "This is a test incident created directly via DAO",
                        severity_level = com.example.myapplication.ui.incident.SeverityLevel.HIGH
                    )
                    
                    println("DEBUG: About to insert directly to DAO")
                    dao.insertIncident(testIncident)
                    println("DEBUG: Direct DAO insert completed")
                    
                    // Check count
                    val count = dao.getIncidentCount()
                    println("DEBUG: Total incidents after direct insert: $count")
                    
                } catch (e: Exception) {
                    println("DEBUG: Direct DAO test failed: ${e.message}")
                    e.printStackTrace()
                }
            }
            true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
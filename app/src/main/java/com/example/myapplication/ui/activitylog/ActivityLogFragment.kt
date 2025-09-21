package com.example.myapplication.ui.activitylog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentActivityLogBinding
import com.example.myapplication.ui.incident.Incident
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ActivityLogFragment : Fragment() {

    private var _binding: FragmentActivityLogBinding? = null
    private val binding get() = _binding!!

    private lateinit var activityLogViewModel: ActivityLogViewModel
    private lateinit var incidentAdapter: IncidentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activityLogViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        )[ActivityLogViewModel::class.java]

        _binding = FragmentActivityLogBinding.inflate(inflater, container, false)
        
        setupRecyclerView()
        setupRefreshButton()
        observeViewModel()
        
        // Debug: Add a test incident button temporarily
        addTestIncidentButton()
        
        return binding.root
    }

    private fun setupRecyclerView() {
        incidentAdapter = IncidentAdapter { incident ->
            onIncidentClick(incident)
        }
        
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

    private fun observeViewModel() {
        activityLogViewModel.allIncidents.observe(viewLifecycleOwner) { incidents ->
            println("DEBUG: ActivityLog received ${incidents.size} incidents")
            
            // Update count display
            binding.textIncidentCount.text = "${incidents.size} incident${if (incidents.size != 1) "s" else ""} found"
            
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
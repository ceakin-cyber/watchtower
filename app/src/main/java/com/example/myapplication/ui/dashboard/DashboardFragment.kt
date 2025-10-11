package com.example.myapplication.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.FragmentDashboardBinding
import com.example.myapplication.data.database.IncidentDatabase
import com.example.myapplication.data.repository.IncidentRepository
import com.example.myapplication.ui.incident.Incident
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.formatter.PercentFormatter
import android.graphics.Color

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private lateinit var incidentRepository: IncidentRepository

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Initialize repository
        val database = IncidentDatabase.getDatabase(requireActivity().application)
        incidentRepository = IncidentRepository(database.incidentDao())

        val textView: TextView = binding.textDashboard
        dashboardViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        setupPieChart()
        loadPieChartData()

        return root
    }

    override fun onResume() {
        super.onResume()
        // Refresh pie chart data when returning to dashboard
        loadPieChartData()
    }

    private fun setupPieChart() {
        val pieChart = binding.pieChartIncidentTypes
        
        // Configure pie chart appearance
        pieChart.description.isEnabled = false
        pieChart.isRotationEnabled = true
        pieChart.setUsePercentValues(true)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.centerText = "Incident Types"
        pieChart.setCenterTextSize(16f)
        pieChart.setHoleRadius(40f)
        pieChart.setTransparentCircleRadius(45f)
        
        // Configure legend
        val legend = pieChart.legend
        legend.isEnabled = true
        legend.textSize = 12f
        legend.formSize = 14f
    }

    private fun loadPieChartData() {
        lifecycleScope.launch {
            try {
                val incidents = incidentRepository.getAllIncidentsSuspend()
                updatePieChart(incidents)
            } catch (e: Exception) {
                println("DEBUG: Error loading incidents for pie chart: ${e.message}")
                updatePieChart(emptyList())
            }
        }
    }

    private fun updatePieChart(incidents: List<Incident>) {
        val pieChart = binding.pieChartIncidentTypes
        
        if (incidents.isEmpty()) {
            pieChart.clear()
            pieChart.centerText = "No Incidents\nLogged Yet"
            binding.layoutPieChart.visibility = View.GONE
            pieChart.invalidate()
            return
        }
        
        binding.layoutPieChart.visibility = View.VISIBLE
        
        // Group incidents by type and count them
        val incidentTypeCounts = incidents.groupingBy { it.incident_type }.eachCount()
        
        // Create pie entries
        val entries = incidentTypeCounts.map { (type, count) ->
            PieEntry(count.toFloat(), type)
        }
        
        // Create pie data set
        val dataSet = PieDataSet(entries, "Incident Types")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE
        dataSet.setValueFormatter(PercentFormatter(pieChart))
        
        // Create pie data
        val data = PieData(dataSet)
        data.setValueTextSize(11f)
        
        // Set data to chart
        pieChart.data = data
        pieChart.centerText = "Incident Types\n(${incidents.size} total)"
        pieChart.invalidate() // Refresh the chart
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.myapplication.ui.activitylog

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.databinding.ItemIncidentBinding
import com.example.myapplication.ui.incident.Incident
import com.example.myapplication.ui.incident.SeverityLevel
import java.text.SimpleDateFormat
import java.util.*

class IncidentAdapter(
    private val onItemClick: (Incident) -> Unit
) : ListAdapter<Incident, IncidentAdapter.IncidentViewHolder>(IncidentDiffCallback()) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncidentViewHolder {
        val binding = ItemIncidentBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return IncidentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IncidentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class IncidentViewHolder(
        private val binding: ItemIncidentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(incident: Incident) {
            with(binding) {
                // Basic info
                textIncidentType.text = incident.incident_type
                textLocation.text = incident.location
                textDescription.text = incident.description
                textTimestamp.text = dateFormat.format(Date(incident.timestamp))

                // Severity badge
                setSeverityBadge(incident.severity_level)

                // Case number
                if (!incident.case_number.isNullOrEmpty()) {
                    textCaseNumber.text = incident.case_number
                    textCaseNumber.visibility = View.VISIBLE
                } else {
                    textCaseNumber.visibility = View.GONE
                }

                // Reported status
                if (incident.reported_to_authorities) {
                    textReported.visibility = View.VISIBLE
                } else {
                    textReported.visibility = View.GONE
                }

                // Click listener
                root.setOnClickListener {
                    onItemClick(incident)
                }
            }
        }

        private fun setSeverityBadge(severity: SeverityLevel) {
            with(binding.textSeverity) {
                text = severity.name
                when (severity) {
                    SeverityLevel.LOW -> setBackgroundResource(R.drawable.severity_badge_low)
                    SeverityLevel.MEDIUM -> setBackgroundResource(R.drawable.severity_badge_medium)
                    SeverityLevel.HIGH -> setBackgroundResource(R.drawable.severity_badge_high)
                    SeverityLevel.CRITICAL -> setBackgroundResource(R.drawable.severity_badge_critical)
                }
            }
        }
    }

    class IncidentDiffCallback : DiffUtil.ItemCallback<Incident>() {
        override fun areItemsTheSame(oldItem: Incident, newItem: Incident): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Incident, newItem: Incident): Boolean {
            return oldItem == newItem
        }
    }
}
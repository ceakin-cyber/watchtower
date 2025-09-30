package com.example.myapplication.ui.emergencycontacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemEmergencyContactBinding
import com.example.myapplication.data.database.EmergencyContact

class EmergencyContactsAdapter(
    private val onEditClick: (EmergencyContact) -> Unit,
    private val onDeleteClick: (EmergencyContact) -> Unit
) : ListAdapter<EmergencyContact, EmergencyContactsAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemEmergencyContactBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(
        private val binding: ItemEmergencyContactBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: EmergencyContact) {
            binding.apply {
                textContactName.text = contact.name.decrypt()
                textContactPhone.text = contact.phoneNumber.decrypt()
                textContactRelationship.text = contact.relationship.decrypt()
                
                if (contact.isPrimary) {
                    textPrimaryBadge.visibility = android.view.View.VISIBLE
                } else {
                    textPrimaryBadge.visibility = android.view.View.GONE
                }
                
                buttonEdit.setOnClickListener { onEditClick(contact) }
                buttonDelete.setOnClickListener { onDeleteClick(contact) }
            }
        }
    }

    class ContactDiffCallback : DiffUtil.ItemCallback<EmergencyContact>() {
        override fun areItemsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: EmergencyContact, newItem: EmergencyContact): Boolean {
            return oldItem == newItem
        }
    }
}
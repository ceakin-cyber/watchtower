package com.example.myapplication.ui.emergencycontacts

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.databinding.DialogEmergencyContactBinding
import com.example.myapplication.data.database.EmergencyContact
import com.example.myapplication.data.database.IncidentDatabase
import com.example.myapplication.data.repository.EmergencyContactRepository
import com.example.myapplication.data.security.EncryptedString

class EmergencyContactDialogFragment : DialogFragment() {

    private lateinit var binding: DialogEmergencyContactBinding
    private lateinit var viewModel: EmergencyContactsViewModel
    private var existingContact: EmergencyContact? = null

    companion object {
        private const val KEY_CONTACT = "contact"
        
        fun newInstance(contact: EmergencyContact? = null): EmergencyContactDialogFragment {
            val fragment = EmergencyContactDialogFragment()
            contact?.let {
                fragment.arguments = Bundle().apply {
                    putSerializable(KEY_CONTACT, it)
                }
            }
            return fragment
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogEmergencyContactBinding.inflate(layoutInflater)
        
        // Setup ViewModel
        val database = IncidentDatabase.getDatabase(requireContext())
        val repository = EmergencyContactRepository(database.emergencyContactDao())
        val factory = EmergencyContactsViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[EmergencyContactsViewModel::class.java]
        
        // Get existing contact if editing
        existingContact = arguments?.getSerializable(KEY_CONTACT) as? EmergencyContact
        
        setupUI()
        
        return AlertDialog.Builder(requireContext())
            .setTitle(if (existingContact == null) "Add Emergency Contact" else "Edit Emergency Contact")
            .setView(binding.root)
            .setPositiveButton("Save") { _, _ -> saveContact() }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun setupUI() {
        existingContact?.let { contact ->
            binding.apply {
                editTextName.setText(contact.name.decrypt())
                editTextPhone.setText(contact.phoneNumber.decrypt())
                editTextRelationship.setText(contact.relationship.decrypt())
                checkBoxPrimary.isChecked = contact.isPrimary
            }
        }
    }

    private fun saveContact() {
        val name = binding.editTextName.text.toString().trim()
        val phone = binding.editTextPhone.text.toString().trim()
        val relationship = binding.editTextRelationship.text.toString().trim()
        val isPrimary = binding.checkBoxPrimary.isChecked

        if (name.isEmpty() || phone.isEmpty() || relationship.isEmpty()) {
            return
        }

        val contact = existingContact?.copy(
            name = EncryptedString.fromDecrypted(name),
            phoneNumber = EncryptedString.fromDecrypted(phone),
            relationship = EncryptedString.fromDecrypted(relationship),
            isPrimary = isPrimary
        ) ?: EmergencyContact(
            name = EncryptedString.fromDecrypted(name),
            phoneNumber = EncryptedString.fromDecrypted(phone),
            relationship = EncryptedString.fromDecrypted(relationship),
            isPrimary = isPrimary
        )

        if (existingContact == null) {
            viewModel.insertContact(contact)
        } else {
            viewModel.updateContact(contact)
        }
    }
}
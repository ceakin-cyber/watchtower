package com.example.myapplication.ui.emergencycontacts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentEmergencyContactsBinding
import com.example.myapplication.data.database.IncidentDatabase
import com.example.myapplication.data.repository.EmergencyContactRepository

class EmergencyContactsFragment : Fragment() {

    private var _binding: FragmentEmergencyContactsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: EmergencyContactsViewModel
    private lateinit var adapter: EmergencyContactsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmergencyContactsBinding.inflate(inflater, container, false)
        
        // Setup database and repository
        val database = IncidentDatabase.getDatabase(requireContext())
        val repository = EmergencyContactRepository(database.emergencyContactDao())
        val factory = EmergencyContactsViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[EmergencyContactsViewModel::class.java]
        
        setupRecyclerView()
        setupAddButton()
        observeContacts()
        
        return binding.root
    }
    
    private fun setupRecyclerView() {
        adapter = EmergencyContactsAdapter(
            onEditClick = { contact ->
                EmergencyContactDialogFragment.newInstance(contact).show(
                    parentFragmentManager, "edit_contact"
                )
            },
            onDeleteClick = { contact ->
                viewModel.deleteContact(contact)
            }
        )
        
        binding.recyclerViewContacts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@EmergencyContactsFragment.adapter
        }
    }
    
    private fun setupAddButton() {
        val addContactAction = {
            EmergencyContactDialogFragment.newInstance().show(
                parentFragmentManager, "add_contact"
            )
        }
        
        binding.fabAddContact.setOnClickListener { addContactAction() }
        binding.btnAddContact.setOnClickListener { addContactAction() }
    }
    
    private fun observeContacts() {
        viewModel.allContacts.observe(viewLifecycleOwner) { contacts ->
            adapter.submitList(contacts)
            binding.textEmptyState.visibility = if (contacts.isEmpty()) View.VISIBLE else View.GONE
            binding.recyclerViewContacts.visibility = if (contacts.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.example.myapplication.ui.activitylog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentActivityLogBinding

class ActivityLogFragment : Fragment() {

    private var _binding: FragmentActivityLogBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val activityLogViewModel =
            ViewModelProvider(this).get(ActivityLogViewModel::class.java)

        _binding = FragmentActivityLogBinding.inflate(inflater, container, false)
        val root: View = binding.root

        activityLogViewModel.text.observe(viewLifecycleOwner) {
            binding.textActivityLog.text = it
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
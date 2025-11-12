package com.example.studysync.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.studysync.R
import com.example.studysync.MainActivity
import com.example.studysync.databinding.FragmentCreateGroupBinding
import com.example.studysync.models.StudyGroup
import com.example.studysync.viewmodels.GroupViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView

class CreateGroupFragment : Fragment() {

    private var _binding: FragmentCreateGroupBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: GroupViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateGroupBinding.inflate(inflater, container, false)
        val view = binding.root

        val subjects = arrayOf("Math", "Computer Science", "History", "Biology", "Literature")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, subjects)
        binding.subjectDropdown.setAdapter(adapter)

        binding.createGroupButton.setOnClickListener {
            handleCreateGroup()
        }

        return view
    }

    private fun handleCreateGroup() {
        val groupName = binding.groupNameInput.text.toString().trim()
        val description = binding.descriptionInput.text.toString().trim()
        val subject = binding.subjectDropdown.text.toString().trim()

        if (groupName.isEmpty()) {
            binding.groupNameInput.error = "Group name is required"
            binding.groupNameInput.requestFocus()
            return
        }

        if (description.isEmpty()) {
            binding.descriptionInput.error = "Description is required"
            binding.descriptionInput.requestFocus()
            return
        }

        if (subject.isEmpty() || subject == "Select Subject") {
            binding.subjectDropdown.error = "Please select a subject"
            binding.subjectDropdown.requestFocus()
            return
        }

        binding.groupNameInput.error = null
        binding.descriptionInput.error = null
        binding.subjectDropdown.error = null


        val newGroup = StudyGroup(name = groupName, description = description, subject = subject)

        sharedViewModel.addGroup(newGroup)

        Toast.makeText(requireContext(), "Group Created!", Toast.LENGTH_SHORT).show()

        clearForm()

        (activity as? MainActivity)?.findViewById<BottomNavigationView>(R.id.nav_view)?.selectedItemId = R.id.navigation_study_groups
    }

    private fun clearForm() {
        binding.groupNameInput.text?.clear()
        binding.descriptionInput.text?.clear()
        binding.subjectDropdown.text?.clear()
        binding.subjectDropdown.clearFocus()
        binding.groupNameInput.clearFocus()
        binding.descriptionInput.clearFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
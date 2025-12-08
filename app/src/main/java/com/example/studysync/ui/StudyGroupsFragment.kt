package com.example.studysync.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.studysync.databinding.FragmentStudyGroupsBinding
import com.example.studysync.viewmodels.GroupViewModel
import com.google.firebase.auth.FirebaseAuth

class StudyGroupsFragment : Fragment() {

    private var _binding: FragmentStudyGroupsBinding? = null
    private val binding get() = _binding!!
    private val sharedViewModel: GroupViewModel by activityViewModels()
    private lateinit var studyGroupAdapter: StudyGroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyGroupsBinding.inflate(inflater, container, false)
        val view = binding.root

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        if (currentUserId == null) {
            Toast.makeText(context, "Error: User not logged in.", Toast.LENGTH_LONG).show()
        }

        studyGroupAdapter = StudyGroupAdapter(
            currentUserId = currentUserId ?: "",
            onJoinClicked = { group ->
                Log.d("StudyGroupsFragment", "Join clicked for group: ${group.topic}")
                Toast.makeText(context, "Join logic for ${group.topic} not implemented yet.", Toast.LENGTH_SHORT).show()
            },
            onLeaveClicked = { group ->
                Log.d("StudyGroupsFragment", "Leave clicked for group: ${group.topic}")
                Toast.makeText(context, "Leave logic for ${group.topic} not implemented yet.", Toast.LENGTH_SHORT).show()
            },
            onDeleteClicked = { group ->
                Log.d("StudyGroupsFragment", "Delete clicked for group: ${group.topic}")
                Toast.makeText(context, "Delete logic for ${group.topic} not implemented yet.", Toast.LENGTH_SHORT).show()
            }
        )
        binding.groupsRecyclerView.adapter = studyGroupAdapter

        sharedViewModel.groups.observe(viewLifecycleOwner) { groups ->
            studyGroupAdapter.submitList(groups.toList())
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

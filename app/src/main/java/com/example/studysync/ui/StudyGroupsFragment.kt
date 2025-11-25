package com.example.studysync.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.studysync.databinding.FragmentStudyGroupsBinding
import com.example.studysync.viewmodels.GroupViewModel

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

        studyGroupAdapter = StudyGroupAdapter()
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
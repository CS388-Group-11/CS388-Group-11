package com.example.studysync.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.studysync.databinding.FragmentStudyGroupsBinding
import com.example.studysync.viewmodels.GroupViewModel

//displays other users groups NOT FULLY IMPLEMENTED YET
class StudyGroupsFragment : Fragment() {

    private var _binding: FragmentStudyGroupsBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    //This is becuase _binding is not null (avoids mem leaks by nulling out oDV)
    private val binding get() = _binding!!

    //Updates all the other frags when 1 frag is updated.
    private val sharedViewModel: GroupViewModel by activityViewModels()

    private lateinit var studyGroupAdapter: StudyGroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyGroupsBinding.inflate(inflater, container, false)
        val view = binding.root

        //connecting adapter and RecyclerView
        studyGroupAdapter = StudyGroupAdapter()
        binding.groupsRecyclerView.adapter = studyGroupAdapter

        //reactively update the RecyclerView whenever the list of groups in the ViewModel changes.
        sharedViewModel.groups.observe(viewLifecycleOwner) { groups ->
            studyGroupAdapter.submitList(groups.toList())
        }

        return view
    }

    //cheap clean up (garbage collection reclaims memory)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
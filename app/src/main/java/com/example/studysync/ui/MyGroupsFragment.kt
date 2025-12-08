package com.example.studysync.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studysync.R
import com.example.studysync.ui.StudyGroupAdapter
import com.example.studysync.models.StudyGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyGroupsFragment : Fragment(R.layout.fragment_my_groups) {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var myGroupsRecyclerView: RecyclerView
    private lateinit var studyGroupAdapter: StudyGroupAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        myGroupsRecyclerView = view.findViewById(R.id.myGroupsRecyclerView)

        setupRecyclerView()
        fetchMyGroups()
    }

    // custom RecyclerView
    private fun setupRecyclerView() {
        studyGroupAdapter = StudyGroupAdapter()
        myGroupsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = studyGroupAdapter
        }
    }

    //filter current user id
    private fun fetchMyGroups() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("groups")
            .whereEqualTo("creatorUid", currentUserId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            //get real-time updates
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("MyGroupsFragment", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    val fetchedGroups = snapshots.toObjects(StudyGroup::class.java)
                    // data received its sent to the StudyGroupAdapter to be displayed
                    studyGroupAdapter.submitList(fetchedGroups)

                    Log.d("MyGroupsFragment", "Found ${fetchedGroups.size} groups.")
                }
            }
    }
}

package com.example.studysync.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.example.studysync.R
import com.example.studysync.models.StudyGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
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

        val currentUserId = auth.currentUser?.uid

        studyGroupAdapter = StudyGroupAdapter(
            currentUserId = currentUserId, // This is the new required parameter
            onJoinClicked = { group ->
                Log.d("MyGroupsFragment", "Join clicked for group: ${group.topic}, but this is MyGroups.")
            },
            onLeaveClicked = { group ->
                // Handle the logic for leaving a group
                leaveGroup(group)
            },
            onDeleteClicked = { group ->
                // Handle the logic for deleting a group
                deleteGroup(group)
            }
        )

        myGroupsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = studyGroupAdapter
        }
    }

    //filter for groups the current user is a member of
    private fun fetchMyGroups() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("groups")
            .whereArrayContains("members", currentUserId)
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

                    Log.d("MyGroupsFragment", "Found ${fetchedGroups.size} groups for current user.")
                }
            }
    }

    // Function to handle leaving a group
    private fun leaveGroup(group: StudyGroup) {
        val userId = auth.currentUser?.uid
        if (userId == null || group.id.isNullOrEmpty()) {
            Toast.makeText(context, "Error: Could not leave group.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("groups").document(group.id)
            .update("members", FieldValue.arrayRemove(userId))
            .addOnSuccessListener {
                Log.d("MyGroupsFragment", "User $userId left group ${group.id}")
                Toast.makeText(context, "You have left the group: ${group.topic}", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("MyGroupsFragment", "Error leaving group", e)
                Toast.makeText(context, "Failed to leave group: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to handle deleting a group
    private fun deleteGroup(group: StudyGroup) {
        if (group.id.isNullOrEmpty()) {
            Toast.makeText(context, "Error: Cannot delete group without an ID.", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("groups").document(group.id)
            .delete()
            .addOnSuccessListener {
                Log.d("MyGroupsFragment", "Group ${group.id} successfully deleted.")
                Toast.makeText(context, "Group '${group.topic}' deleted.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("MyGroupsFragment", "Error deleting document", e)
                Toast.makeText(context, "Failed to delete group: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

package com.example.studysync.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studysync.databinding.FragmentDiscoverBinding
import com.example.studysync.models.StudyGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    private lateinit var studyGroupAdapter: StudyGroupAdapter
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadDiscoverGroups()
    }

    private fun setupRecyclerView() {
        val currentUserId = auth.currentUser?.uid
        studyGroupAdapter = StudyGroupAdapter(
            currentUserId = currentUserId,
            onJoinClicked = { group -> joinGroup(group) },
            onLeaveClicked = { group -> leaveGroup(group) },
            onDeleteClicked = { group -> deleteGroup(group) }
        )
        binding.discoverRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = studyGroupAdapter
            setPadding(0, getStatusBarHeight(), 0, 0)
            clipToPadding = false
        }
    }

    private fun getStatusBarHeight(): Int {
        var result = 0
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    private fun loadDiscoverGroups() {
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Log.w("DiscoverFragment", "User not logged in.")
            studyGroupAdapter.submitList(emptyList())
            return
        }

        firestore.collection("groups")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .whereNotIn("members", listOf(currentUserId))
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.w("DiscoverFragment", "Listen failed.", error)
                    return@addSnapshotListener
                }

                val groups = snapshots?.toObjects(StudyGroup::class.java) ?: emptyList()
                studyGroupAdapter.submitList(groups)
                Log.d("DiscoverFragment", "Loaded ${groups.size} groups for discovery.")
            }
    }

    private fun joinGroup(group: StudyGroup) {
        val userId = auth.currentUser?.uid
        if (userId == null || group.id.isNullOrEmpty()) {
            Toast.makeText(context, "Error: Could not join group.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("groups").document(group.id)
            .update("members", FieldValue.arrayUnion(userId))
            .addOnSuccessListener {
                Log.d("DiscoverFragment", "User $userId joined group ${group.id}")
                Toast.makeText(context, "Group joined!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w("DiscoverFragment", "Error joining group", e)
                Toast.makeText(context, "Failed to join group: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun leaveGroup(group: StudyGroup) {
        val userId = auth.currentUser?.uid
        if (userId == null || group.id.isNullOrEmpty()) {
            Toast.makeText(context, "Error: Could not leave group.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("groups").document(group.id)
            .update("members", FieldValue.arrayRemove(userId))
            .addOnSuccessListener { Log.d("DiscoverFragment", "User $userId left group ${group.id}") }
            .addOnFailureListener { e -> Log.w("DiscoverFragment", "Error leaving group", e) }
    }

    private fun deleteGroup(group: StudyGroup) {
        if (group.id.isNullOrEmpty()) {
            Toast.makeText(context, "Error: Could not delete group.", Toast.LENGTH_SHORT).show()
            return
        }
        if (auth.currentUser?.uid != group.creatorUid) {
            Toast.makeText(context, "You are not authorized to delete this group.", Toast.LENGTH_SHORT).show()
            return
        }
        firestore.collection("groups").document(group.id)
            .delete()
            .addOnSuccessListener {
                Log.d("DiscoverFragment", "Group ${group.id} deleted.")
                Toast.makeText(context, "Group deleted.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e -> Log.w("DiscoverFragment", "Error deleting group", e) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

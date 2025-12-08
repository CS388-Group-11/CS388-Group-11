package com.example.studysync.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.studysync.R
import com.example.studysync.models.StudyGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateGroupFragment : Fragment(R.layout.fragment_create_group) {

    // 1. Get instances of Firebase services
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Get references to your UI elements
        val courseCodeInput = view.findViewById<EditText>(R.id.courseCodeInput)
        val topicInput = view.findViewById<EditText>(R.id.topicInput)
        val timeInput = view.findViewById<EditText>(R.id.timeInput)
        val dateInput = view.findViewById<EditText>(R.id.dateInput)
        val locationInput = view.findViewById<EditText>(R.id.locationInput)
        val createButton = view.findViewById<Button>(R.id.createGroupButton)

        createButton.setOnClickListener {
            handleGroupCreation(
                courseCodeInput,
                topicInput,
                timeInput,
                dateInput,
                locationInput
            )
        }
    }

    private fun handleGroupCreation(
        courseCodeInput: EditText,
        topicInput: EditText,
        timeInput: EditText,
        dateInput: EditText,
        locationInput: EditText
    ) {
        val courseCode = courseCodeInput.text.toString().trim()
        val topic = topicInput.text.toString().trim()
        val time = timeInput.text.toString().trim()
        val date = dateInput.text.toString().trim()
        val location = locationInput.text.toString().trim()

        // Basic validation: ensure all fields are filled
        if (courseCode.isEmpty() || topic.isEmpty() || time.isEmpty() || date.isEmpty() || location.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all group details.", Toast.LENGTH_LONG).show()
            return
        }

        // Get the current authenticated user's ID
        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            Toast.makeText(requireContext(), "Error: User not logged in.", Toast.LENGTH_LONG).show()
            return
        }


        // Create a reference to a new document to get its ID
        val newGroupRef = db.collection("groups").document()
        val newGroupId = newGroupRef.id

        // Create the StudyGroup object, now including the ID
        val newGroup = StudyGroup(
            id = newGroupId,
            courseCode = courseCode,
            topic = topic,
            time = time,
            date = date,
            location = location,
            creatorUid = currentUserId
        )

        // Save the data to Firestore using .set() on the reference
        newGroupRef.set(newGroup)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Group created: ${newGroup.courseCode} - ${newGroup.topic}", Toast.LENGTH_LONG).show()

                findNavController().popBackStack()

            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error creating group: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}

package com.example.studysync.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.studysync.R
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var auth: FirebaseAuth
    private lateinit var userEmailTextView: TextView
    private lateinit var logoutButton: Button

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        userEmailTextView = view.findViewById(R.id.userEmailTextView)
        logoutButton = view.findViewById(R.id.logoutButton)

        val currentUser = auth.currentUser
        if (currentUser == null) {

            navigateToLogin()
            return
        }

        userEmailTextView.text = currentUser.email

        logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "You have been logged out.", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    //navigates back to login screen for good user exp
    private fun navigateToLogin() {
        val navController = findNavController()

        navController.navigate(R.id.loginFragment, null, androidx.navigation.NavOptions.Builder()
            .setPopUpTo(navController.graph.startDestinationId, true)
            .setLaunchSingleTop(true)
            .build())
    }
}

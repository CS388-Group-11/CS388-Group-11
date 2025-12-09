package com.example.studysync.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.studysync.MainActivity
import com.example.studysync.R
import androidx.navigation.ui.setupWithNavController

import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val emailInput = view.findViewById<EditText>(R.id.emailInput)
        val passwordInput = view.findViewById<EditText>(R.id.passwordInput)
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val signupButton = view.findViewById<Button>(R.id.signupButton)
        val guestButton = view.findViewById<Button>(R.id.guestButton)

        // LOGIN
        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Logged in as $email", Toast.LENGTH_SHORT).show()
                        goToMainPage()
                    } else {
                        Toast.makeText(requireContext(), "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // SIGNUP
        signupButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Account created for $email", Toast.LENGTH_SHORT).show()
                        goToMainPage()
                    } else {
                        Toast.makeText(requireContext(), "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // GUEST (ANONYMOUS LOGIN)
        guestButton.setOnClickListener {
            auth.signInAnonymously()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Continuing as Guest", Toast.LENGTH_SHORT).show()
                        goToMainPage()
                    } else {
                        Toast.makeText(requireContext(), "Guest login failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun goToMainPage() {
        val navController = requireActivity()
            .supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment)!!
            .findNavController()

        // Navigate to main fragment and show navbar
        navController.navigate(R.id.discoverFragment)

        // Show BottomNavigationView
        val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavigationView.visibility = View.VISIBLE
        bottomNavigationView.setupWithNavController(navController)
    }

}

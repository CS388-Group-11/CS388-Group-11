package com.example.studysync.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.studysync.MainActivity
import com.example.studysync.R

class LoginFragment : Fragment(R.layout.fragment_login) {

    private val PREFS_NAME = "UserAccounts"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val emailInput = view.findViewById<EditText>(R.id.emailInput)
        val passwordInput = view.findViewById<EditText>(R.id.passwordInput)
        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val signupButton = view.findViewById<Button>(R.id.signupButton)
        val guestButton = view.findViewById<Button>(R.id.guestButton)

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        loginButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val savedPassword = prefs.getString(email, null)
            if (savedPassword != null && savedPassword == password) {
                Toast.makeText(requireContext(), "Logged in as $email", Toast.LENGTH_SHORT).show()
                goToMainPage()
            } else {
                Toast.makeText(requireContext(), "Account does not exist or password is wrong", Toast.LENGTH_SHORT).show()
            }
        }

        signupButton.setOnClickListener {
            val email = emailInput.text.toString()
            val password = passwordInput.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (prefs.contains(email)) {
                Toast.makeText(requireContext(), "Account already exists", Toast.LENGTH_SHORT).show()
            } else {
                prefs.edit().putString(email, password).apply()
                Toast.makeText(requireContext(), "Account created for $email", Toast.LENGTH_SHORT).show()
                goToMainPage()
            }
        }

        guestButton.setOnClickListener {
            Toast.makeText(requireContext(), "Continuing as Guest", Toast.LENGTH_SHORT).show()
            goToMainPage()
        }
    }

    private fun goToMainPage() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}

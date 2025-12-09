package com.example.studysync

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)

        // Hide navbar initially
        bottomNavigationView.visibility = View.GONE

        if (auth.currentUser != null) {
            // User already logged in → show navbar
            bottomNavigationView.visibility = View.VISIBLE
            bottomNavigationView.setupWithNavController(navController)
            // Automatically go to discoverFragment
            navController.navigate(R.id.discoverFragment)
        }
    }
}

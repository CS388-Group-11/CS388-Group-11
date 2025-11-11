package com.example.studysync
import com.example.studysync.ui.LoginFragment
import com.example.studysync.ui.StudyGroupsFragment
import com.example.studysync.ui.ChatsFragment
import com.example.studysync.ui.CreateGroupFragment


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        navView.setOnItemSelectedListener { item ->
            val fragment: Fragment = when(item.itemId) {
                R.id.navigation_login -> LoginFragment()
                R.id.navigation_study_groups -> StudyGroupsFragment()
                R.id.navigation_chats -> ChatsFragment()
                R.id.navigation_create_group -> CreateGroupFragment()
                else -> LoginFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment)
                .commit()
            true
        }

        // Set default
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, LoginFragment())
                .commit()
        }
    }
}

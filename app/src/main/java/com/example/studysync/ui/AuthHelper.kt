package com.example.studysync.utils

import android.content.Context

object AuthHelper {
    private const val PREFS_NAME = "UserAccounts"
    private const val CURRENT_USER_KEY = "current_user"

    fun getCurrentUser(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(CURRENT_USER_KEY, null)
    }

    fun isLoggedIn(context: Context): Boolean {
        val user = getCurrentUser(context)
        return user != null && user != "guest"
    }

    fun isGuest(context: Context): Boolean {
        val user = getCurrentUser(context)
        return user == "guest"
    }
}

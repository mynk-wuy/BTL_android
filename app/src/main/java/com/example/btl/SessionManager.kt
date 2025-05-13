package com.example.btl

import android.content.Context

object SessionManager {
    private const val PREF_NAME = "user_session"

    fun saveUsername(context: Context, username: String) {
        val editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
        editor.putString("username", username)
        editor.apply()
    }

    fun getUsername(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString("username", "") ?: ""
    }

    fun clearSession(context: Context) {
        val editor = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit()
        editor.clear()
        editor.apply()
    }
}

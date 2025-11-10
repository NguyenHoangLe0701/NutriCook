package com.example.nutricook.view.notifications

import android.content.Context
import android.content.SharedPreferences

object UserPreferences {
    private const val PREF_NAME = "nutricook_prefs"
    private const val KEY_IS_NEW_USER = "is_new_user"

    fun setNewUser(context: Context, isNew: Boolean) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_IS_NEW_USER, isNew).apply()
    }

    fun isNewUser(context: Context): Boolean {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_IS_NEW_USER, true)
    }
}

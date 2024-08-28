package com.capztone.fishfy.ui.activities.Utils

import android.content.Context
import android.content.SharedPreferences
import com.capztone.fishfy.ui.activities.models.MenuItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FreshFishLocal(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences("fishfy_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val MENU_ITEMS_KEY = "menu_items_key"
    }

    fun saveMenuItems(menuItems: List<MenuItem>) {
        val json = gson.toJson(menuItems)
        preferences.edit().putString(MENU_ITEMS_KEY, json).apply()
    }

    fun getMenuItems(): List<MenuItem>? {
        val json = preferences.getString(MENU_ITEMS_KEY, null) ?: return null
        val type = object : TypeToken<List<MenuItem>>() {}.type
        return gson.fromJson(json, type)
    }

    fun clearMenuItems() {
        preferences.edit().remove(MENU_ITEMS_KEY).apply()
    }
}

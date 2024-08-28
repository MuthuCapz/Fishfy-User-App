package com.capztone.fishfy.ui.activities.Utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.capztone.fishfy.ui.activities.models.MenuItem
import com.capztone.fishfy.ui.activities.models.DiscountItem

class ShopOneLocal(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("ShopPreferences", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getMenuItems(key: String): List<MenuItem>? {
        val json = sharedPreferences.getString(key, null)
        return if (json != null) {
            val type = object : TypeToken<List<MenuItem>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    fun saveMenuItems(key: String, menuItems: List<MenuItem>) {
        val editor = sharedPreferences.edit()
        val json = gson.toJson(menuItems)
        editor.putString(key, json)
        editor.apply()
    }

    fun getDiscountItems(key: String): List<DiscountItem>? {
        val json = sharedPreferences.getString(key, null)
        return if (json != null) {
            val type = object : TypeToken<List<DiscountItem>>() {}.type
            gson.fromJson(json, type)
        } else {
            null
        }
    }

    fun saveDiscountItems(key: String, discountItems: List<DiscountItem>) {
        val editor = sharedPreferences.edit()
        val json = gson.toJson(discountItems)
        editor.putString(key, json)
        editor.apply()
    }
}

package com.capztone.fishfy.ui.activities.Utils
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.ContentProviderCompat.requireContext
import com.capztone.fishfy.ui.activities.models.CartItems
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SharedPreferencesManager(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)

    fun saveCartItems(cartItems: List<CartItems>) {
        val editor = sharedPreferences.edit()
        val json = Gson().toJson(cartItems)
        editor.putString("cart_items", json)
        editor.apply()
    }
    private fun saveCartItemsToLocalStorage(cartItems: List<CartItems>) {

        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(cartItems)
        editor.putString("cart_items", json)
        editor.apply()
    }

    fun getCartItems(): List<CartItems>? {
        val json = sharedPreferences.getString("cart_items", null)
        return if (json != null) {
            val type = object : TypeToken<List<CartItems>>() {}.type
            Gson().fromJson(json, type)
        } else {
            null
        }
    }
}

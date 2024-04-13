package com.example.seafishfy.ui.activities.ViewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun saveLocalityInFirebase(userId: String, locality: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userLocationRef = database.child("locations").child(userId)
                val locationMap = mapOf(
                    "locality" to locality
                )
                userLocationRef.setValue(locationMap).await()
                // Handle success
            } catch (e: Exception) {
                // Handle failure
            }
        }
    }

    fun storeLocationAndAddressInFirebase(userId: String, address: String, locality: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userLocationRef = database.child("locations").child(userId)
                val locationMap = mapOf(
                    "address" to address,
                    "locality" to locality
                )
                userLocationRef.setValue(locationMap).await()
                // Handle success
            } catch (e: Exception) {
                // Handle failure
            }
        }
    }

    fun saveAddress(userId: String, address: String, savedAddresses: MutableList<String>): Boolean {
        if (!savedAddresses.contains(address)) {
            savedAddresses.add(address)
            saveAddressesToSharedPreferences(savedAddresses)
            return true
        }
        return false
    }

    private fun saveAddressesToSharedPreferences(addresses: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val sharedPreferences = getApplication<Application>().getSharedPreferences("MyPrefs", android.content.Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putStringSet("SAVED_ADDRESSES", HashSet(addresses))
            editor.apply()
        }
    }
}

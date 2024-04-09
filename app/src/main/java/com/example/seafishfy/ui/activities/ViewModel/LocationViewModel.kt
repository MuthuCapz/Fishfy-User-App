package com.example.seafishfy.ui.activities.ViewModel
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference = FirebaseDatabase.getInstance().reference

    fun saveLocalityInFirebase(userId: String, locality: String) {
        val userLocationRef = database.child("locations").child(userId)
        val locationMap = mapOf(
            "locality" to locality
        )
        userLocationRef.setValue(locationMap)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener {
                // Handle failure
            }
    }

    fun storeLocationAndAddressInFirebase(userId: String, address: String, locality: String) {
        val userLocationRef = database.child("locations").child(userId)
        val locationMap = mapOf(
            "address" to address,
            "locality" to locality
        )
        userLocationRef.setValue(locationMap)
            .addOnSuccessListener {
                // Handle success
            }
            .addOnFailureListener {
                // Handle failure
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
        val sharedPreferences = getApplication<Application>().getSharedPreferences("MyPrefs", android.content.Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putStringSet("SAVED_ADDRESSES", HashSet(addresses))
        editor.apply()
    }
}

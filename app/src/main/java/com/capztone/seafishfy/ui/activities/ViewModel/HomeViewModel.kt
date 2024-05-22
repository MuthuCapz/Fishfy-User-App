package com.capztone.seafishfy.ui.activities.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeViewModel : ViewModel() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseReference: DatabaseReference = database.getReference("locations")

    private val _address = MutableLiveData<String?>()
    val address: LiveData<String?>
        get() = _address

    private val _locality = MutableLiveData<String?>()
    val locality: LiveData<String?>
        get() = _locality

    private val _latitude = MutableLiveData<Double?>()
    val latitude: MutableLiveData<Double?>
        get() = _latitude

    private val _longitude = MutableLiveData<Double?>()
    val longitude: MutableLiveData<Double?>
        get() = _longitude

    init {
        retrieveData()
    }

    private fun retrieveData() {
        viewModelScope.launch {
            retrieveAddressAndLocality()
        }
    }

    private suspend fun retrieveAddressAndLocality() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val userLocationRef = databaseReference.child(uid)
            try {
                val snapshot = userLocationRef.get().await()
                if (snapshot.exists()) {
                    val address = snapshot.child("address").getValue(String::class.java)
                    val locality = snapshot.child("locality").getValue(String::class.java)
                    val latitude = snapshot.child("latitude").getValue(Double::class.java)
                    val longitude = snapshot.child("longitude").getValue(Double::class.java)

                    _address.value = address
                    _locality.value = locality
                    _latitude.value = latitude
                    _longitude.value = longitude
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

}

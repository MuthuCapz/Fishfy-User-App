package com.example.seafishfy.ui.activities

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.seafishfy.ui.activities.models.Location
import com.google.firebase.database.*

class UserViewModel : ViewModel() {
    private val _driverLocation = MutableLiveData<Location>()
    val driverLocation: LiveData<Location>
        get() = _driverLocation

    private val database: DatabaseReference by lazy {
        FirebaseDatabase.getInstance().getReference("Driver Location")
    }

    fun listenForDriverLocation(driverId: String) {
        val driverLocationRef = database.child("Driver ID: $driverId")
        val locationListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latitude = snapshot.child("latitude").value as? Double
                val longitude = snapshot.child("longitude").value as? Double
                if (latitude != null && longitude != null) {
                    _driverLocation.value = Location(latitude, longitude)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        }
        driverLocationRef.addValueEventListener(locationListener)
    }
}

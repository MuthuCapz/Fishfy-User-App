package com.example.seafishfy.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.seafishfy.R
import com.example.seafishfy.ui.activities.models.Location
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class UserActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private val userViewModel: UserViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        userViewModel.listenForDriverLocation("Driver ID")

        // Observe changes in the driver's location and update the map
        userViewModel.driverLocation.observe(this, Observer { location ->
            updateDriverLocationOnMap(location)
        })
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    private fun updateDriverLocationOnMap(location: Location) {
        val driverLatLng = LatLng(location.latitude, location.longitude)
        googleMap.clear()

        // Add a marker for the driver's location
        googleMap.addMarker(
            MarkerOptions()
                .position(driverLatLng)
                .title("Driver's Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        )

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(driverLatLng, 15f))
    }

}
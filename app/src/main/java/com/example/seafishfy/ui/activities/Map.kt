package com.example.seafishfy.ui.activities

import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.seafishfy.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import java.io.IOException

class Map : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder

    private lateinit var distanceTextView: TextView
    private lateinit var timeTextView: TextView

    private var destinationLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this)

        distanceTextView = findViewById(R.id.distanceTextView)
        timeTextView = findViewById(R.id.timeTextView)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { userLocation ->
            userLocation?.let {
                val currentUserLocation = LatLng(userLocation.latitude, userLocation.longitude)
                mMap.addMarker(MarkerOptions().position(currentUserLocation).title("Your Location"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentUserLocation, 15f))

                // Fetch destination location
                val destinationAddress = "Tiruchendur"
                destinationLocation = getLocationFromAddress(destinationAddress)
                destinationLocation?.let { destLatLng ->
                    mMap.addMarker(MarkerOptions().position(destLatLng).title("Driver Location: $destinationAddress"))

                    // Draw curved polyline
                    val polylineOptions = PolylineOptions()
                    val pattern = listOf(Dash(30f), Gap(20f))
                    polylineOptions.addAll(getCurvedLine(currentUserLocation, destLatLng))
                    polylineOptions.color(Color.RED)
                    polylineOptions.pattern(pattern)
                    mMap.addPolyline(polylineOptions)

                    // Calculate distance and time
                    val distance = calculateDistance(currentUserLocation, destLatLng)
                    val time = calculateTime(distance)

                    distanceTextView.text = "Distance: $distance km"
                    timeTextView.text = "Time: $time"
                }
            }
        }
    }

    private fun getLocationFromAddress(address: String): LatLng? {
        return try {
            val locationList: List<Address>? = geocoder.getFromLocationName(address, 1)
            val addressLocation: Address = locationList?.get(0) ?: return null
            LatLng(addressLocation.latitude, addressLocation.longitude)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateDistance(startLatLng: LatLng, endLatLng: LatLng): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(
            startLatLng.latitude, startLatLng.longitude,
            endLatLng.latitude, endLatLng.longitude, results
        )
        // Convert meters to kilometers
        return results[0] / 1000
    }

    private fun calculateTime(distance: Float): String {
        // Calculate time based on distance with the assumption that 1 kilometer takes 5 minutes
        val totalTimeInMinutes = distance * 2
        val hours = totalTimeInMinutes.toInt() / 60
        val minutes = totalTimeInMinutes.toInt() % 60

        return if (hours > 0) {
            "$hours hr $minutes mins"
        } else {
            "$minutes mins"
        }
    }

    private fun getCurvedLine(start: LatLng, end: LatLng): List<LatLng> {
        val curveFactor = 0.3 // Change this value to adjust the curvature
        val distance = calculateDistance(start, end)
        val midPoint = LatLng((start.latitude + end.latitude) / 2, (start.longitude + end.longitude) / 2)

        val tDelta = 1.0 / 100 // Change this value to adjust the number of points
        val points = mutableListOf<LatLng>()

        var t = 0.0
        while (t <= 1) {
            val x = (1 - t) * (1 - t) * start.latitude + 2 * (1 - t) * t * midPoint.latitude + t * t * end.latitude
            val y = (1 - t) * (1 - t) * start.longitude + 2 * (1 - t) * t * midPoint.longitude + t * t * end.longitude
            points.add(LatLng(x, y))
            t += tDelta
        }

        return points
    }
}
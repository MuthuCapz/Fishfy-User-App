package com.example.seafishfy.ui.activities


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import kotlin.math.*
import android.provider.Settings
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.seafishfy.R
import com.example.seafishfy.databinding.ActivityLocationBinding
import com.example.seafishfy.ui.activities.Utils.ToastHelper
import com.example.seafishfy.ui.activities.adapters.AddressAdapter
import com.example.seafishfy.ui.activities.ViewModel.LocationViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*

class LocationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var mainBinding: ActivityLocationBinding
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences
    private val viewModel: LocationViewModel by viewModels()
    private val nearbyShops = mutableListOf<String>()
    private val permissionId = 2
    private var savedAddresses = mutableListOf<String>()
    private lateinit var adapter: AddressAdapter
    private val adminDestinations = listOf(
        Pair(8.198971, 77.303314),   // Thoothukudi
        Pair(8.6701179, 78.093077),   // Tirucendhur
        Pair(37.386051,-122.083855) ,
        Pair(8.8076189, 78.1283788),   // Thoothukudi
        Pair(8.6701179, 78.093077),   // Tirucendhur
        Pair(37.386051,-122.083855)// Chennai
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        mainBinding.Locationbutton.setOnClickListener {
            getLocation()
        }

        savedAddresses = getSavedAddressesFromSharedPreferences()

        // Set the adapter to the listView after initializing savedAddresses
        adapter = AddressAdapter(this, savedAddresses)
        mainBinding.listview.adapter = adapter

        adapter.notifyDataSetChanged()
    }
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return
                }
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: MutableList<Address>? =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        val address = list?.get(0)?.getAddressLine(0) ?: ""
                        val locality = list?.get(0)?.locality ?: ""
                        mainBinding.tvLocality.text = "Locality: $locality"

                        // Calculate and display distances to admin destinations
                        for ((index, destination) in adminDestinations.withIndex()) {
                            val distance = calculateDistance(
                                location.latitude, location.longitude,
                                destination.first, destination.second
                            )
                            // Update the TextViews with the calculated distances
                            updateDistanceTextView(distance, index)
                        }

                        // Show dialog to save address
                        showSaveAddressDialog(address, locality, location.latitude, location.longitude)
                    }
                }
            } else {
                ToastHelper.showCustomToast(this, "Please turn on location")
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    // Function to calculate distance between two points using Haversine formula
    private fun calculateDistance(
        userLat: Double, userLong: Double,
        destinationLat: Double, destinationLong: Double
    ): Double {
        val R = 6371 // Radius of the Earth in km
        val latDistance = Math.toRadians(destinationLat - userLat)
        val longDistance = Math.toRadians(destinationLong - userLong)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(userLat)) * cos(Math.toRadians(destinationLat)) *
                sin(longDistance / 2) * sin(longDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    // Function to update TextViews with distances to admin destinations
    private fun updateDistanceTextView(distance: Double, index: Int) {
        // Check if the distance is less than or equal to 10km and add the corresponding shop name to the list
        if (distance <= 10) {
            val shopName = when (index) {
                0 -> "Shop 1"
                1 -> "Shop 2"
                2 -> "Shop 3"
                3 -> "Shop 4"
                4 -> "Shop 5"
                5 -> "Shop 6"

                else -> null
            }
            shopName?.let { nearbyShops.add(it) }
        }

        // Update the respective TextViews with distance
        when (index) {
            0 -> mainBinding.distancetextview.text = "Thoothukudi: ${"%.2f".format(distance)} km"
            1 -> mainBinding.distancetextview2.text = "Spic: ${"%.2f".format(distance)} km"
            2 -> mainBinding.distancetextview3.text = "USA: ${"%.2f".format(distance)} km"
            3 -> mainBinding.distancetextview4.text = "Thoothukudi: ${"%.2f".format(distance)} km"
            4 -> mainBinding.distancetextview5.text = "Spic: ${"%.2f".format(distance)} km"
            5 -> mainBinding.distancetextview6.text = "USA: ${"%.2f".format(distance)} km"
        }

        // Update the shop names TextView if there are any nearby shops
        mainBinding.shoptextview.text = nearbyShops.joinToString(", ")
    }

    private fun showSaveAddressDialog(address: String, locality: String, latitude: Double,longitude:Double) {
        val dialogBuilder = AlertDialog.Builder(this, R.style.AlertDialogCustomStyle)
        dialogBuilder.setTitle("Confirmation")
            .setMessage("Do you want to save this address?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, _ ->
                val userId = auth.currentUser?.uid
                val shoptextview = mainBinding.shoptextview.text.toString()
                if (userId != null) {
                    viewModel.saveLocalityInFirebase(
                        userId,
                        locality,
                        latitude,
                        longitude,
                        shoptextview
                    )
                    val saved = viewModel.saveAddress(userId, address, savedAddresses)
                    if (saved) {
                        viewModel.storeLocationAndAddressInFirebase(userId, address, locality,latitude,longitude,shoptextview)
                    } else {
                        ToastHelper.showCustomToast(this, "Address already saved")
                    }
                } else {
                    ToastHelper.showCustomToast(this, "User not authenticated")
                }
                dialog.dismiss()
                navigateToMainActivity(address, locality,shoptextview)
            }
            .setNegativeButton("No") { dialog, _ ->
                val userId = auth.currentUser?.uid
                val shoptextview = mainBinding.shoptextview.text.toString()
                if (userId != null) {
                    viewModel.saveLocalityInFirebase(userId, locality,latitude,longitude,shoptextview)
                } else {
                    ToastHelper.showCustomToast(this, "User not authenticated")
                }
                dialog.dismiss()
                navigateToMainActivity(address, locality,shoptextview)
            }

        val alertDialog = dialogBuilder.create()
        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, R.color.navy))
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(this, R.color.navy))
        }
        alertDialog.show()
    }

    private fun navigateToMainActivity(address: String, locality: String,shoptextview: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("ADDRESS", address)
        intent.putExtra("LOCALITY", locality)
        intent.putExtra("SHOP_TEXT_VIEW_VALUE", shoptextview)
        startActivity(intent)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    private fun checkPermissions(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }

    private fun getSavedAddressesFromSharedPreferences(): MutableList<String> {
        val savedAddressesSet = sharedPreferences.getStringSet("SAVED_ADDRESSES", HashSet<String>()) ?: HashSet()
        return savedAddressesSet.toMutableList()
    }
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity() // This closes the entire app
    }
}

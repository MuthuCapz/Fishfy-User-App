package com.example.seafishfy.ui.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.seafishfy.R
import com.example.seafishfy.databinding.ActivityLocationBinding
import com.example.seafishfy.ui.activities.adapters.AddressAdapter
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

    private val permissionId = 2
    private val savedAddresses = mutableListOf<String>()
    private lateinit var adapter: AddressAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(mainBinding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mainBinding.Locationbutton.setOnClickListener {
            getLocation()
        }

        adapter = AddressAdapter(this, savedAddresses)
        mainBinding.listview.adapter = adapter


    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: MutableList<Address>? =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        val address = list?.get(0)?.getAddressLine(0) ?: ""
                        val locality = list?.get(0)?.locality ?: ""
                        mainBinding.tvLocality.text = "Locality: $locality"
                        showSaveAddressDialog(address, locality)
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    private fun showSaveAddressDialog(address: String, locality: String) {
        val dialogBuilder = AlertDialog.Builder(this, R.style.AlertDialogCustomStyle)
        dialogBuilder.setTitle("Confirmation")
            .setMessage("Do you want to save this address?")
            .setCancelable(false)
            .setPositiveButton("Yes") { dialog, _ ->
                saveAddress(address)
                dialog.dismiss()
                navigateToMainActivity(address, locality)
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
                navigateToMainActivity(address, locality)
            }

        val alertDialog = dialogBuilder.create()
        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(ContextCompat.getColor(this, R.color.navy))
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(ContextCompat.getColor(this, R.color.navy))
        }
        alertDialog.show()
    }

    private fun saveAddress(address: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            storeLocationAndAddressInFirebase(userId, address)
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMainActivity(address: String, locality: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("ADDRESS", address)
        intent.putExtra("LOCALITY", locality)
        startActivity(intent)
    }

    private fun storeLocationAndAddressInFirebase(userId: String, address: String) {
        val userLocationRef = database.child("locations").child(userId)
        userLocationRef.push().setValue(address)
            .addOnSuccessListener {
                Toast.makeText(this, "Address stored in Firebase", Toast.LENGTH_SHORT).show()
                savedAddresses.add(address)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to store address in Firebase", Toast.LENGTH_SHORT).show()
            }
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
}
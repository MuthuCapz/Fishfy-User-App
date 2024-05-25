package com.capztone.seafishfy.ui.activities.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.capztone.seafishfy.databinding.FragmentPayoutBottomSheetBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.math.roundToInt

// Extension function for Int to round to Int (although it's already an Int)


class PayoutBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentPayoutBottomSheetBinding? = null
    private val binding get() = _binding!!
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    fun Int.roundToInt(): Int = this
    // Shop locations
    private val shopLocations = mapOf(
        "Shop 1" to LatLng(8.198971, 77.303314),
        "Shop 2" to LatLng(13.0300, 80.2421),
        "Shop 3" to LatLng(13.0640, 77.6504),
        "Shop 4" to LatLng(8.8076189, 78.1283788),
        "Shop 5" to LatLng(8.3223816, 77.1729525),
        "Shop 6" to LatLng(8.3451335,77.18204)
    )
    private val uniqueShops = HashSet<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPayoutBottomSheetBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        getCurrentLocationAndCalculateDistance()
        binding.backButton.setOnClickListener {
            dismiss()
        }
        binding.continueButton.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    private fun getCurrentLocationAndCalculateDistance() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let { userLocation ->
                val currentUser = auth.currentUser
                currentUser?.let { user ->
                    val userId = user.uid
                    fetchOrderValue(userId) { orderValue ->
                        val cartItemsRef = database.getReference("user").child(userId).child("cartItems")
                        cartItemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val shopDeliveryCharges = StringBuilder()
                                val shops = mutableListOf<String>()
                                dataSnapshot.children.forEach { cartItemSnapshot ->
                                    val path = cartItemSnapshot.child("path").getValue(String::class.java)
                                    path?.let { shop ->
                                        if (shop !in uniqueShops) {
                                            uniqueShops.add(shop)
                                            shops.add(shop)
                                            shopDeliveryCharges.append("$shop\n")
                                        }
                                    }
                                }
                                binding.shopTextView.text = shopDeliveryCharges.toString()

                                for (shop in shops) {
                                    val shopLocation = getShopLocation(shop)
                                    calculateDeliveryCharge(userLocation, shopLocation, orderValue)
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                // Handle onCancelled
                            }
                        })
                    }
                }
            }
        }
    }

    private fun fetchOrderValue(userId: String, callback: (Double) -> Unit) {
        val cartItemsRef = database.getReference("user").child(userId).child("cartItems")
        cartItemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var orderValue = 0.0
                dataSnapshot.children.forEach { cartItemSnapshot ->
                    val foodPriceAny = cartItemSnapshot.child("foodPrice").getValue(Any::class.java)
                    val foodQuantityAny = cartItemSnapshot.child("foodQuantity").getValue(Any::class.java)

                    val foodPrice = when (foodPriceAny) {
                        is String -> foodPriceAny.toDoubleOrNull() ?: 0.0
                        is Long -> foodPriceAny.toDouble()
                        is Double -> foodPriceAny
                        else -> 0.0
                    }

                    val foodQuantity = when (foodQuantityAny) {
                        is String -> foodQuantityAny.toIntOrNull() ?: 0
                        is Long -> foodQuantityAny.toInt()
                        is Int -> foodQuantityAny
                        else -> 0
                    }

                    orderValue += foodPrice * foodQuantity
                }
                callback(orderValue)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun getShopLocation(shopName: String): LatLng {
        return shopLocations[shopName] ?: LatLng(0.0, 0.0)
    }

    private fun calculateDeliveryCharge(userLocation: Location, shopLocation: LatLng, orderValue: Double) {
        val shopLoc = Location("").apply {
            latitude = shopLocation.latitude
            longitude = shopLocation.longitude
        }

        val distanceInKm = (userLocation.distanceTo(shopLoc) / 1000).roundToInt()
        val baseFare = 20
        val distanceCharge =  if (orderValue > 500) 0 else(distanceInKm * 5).roundToInt()
        val serviceFee = 5
        val orderValueInt = orderValue.roundToInt()
        val gstOnOrderValue = (orderValueInt * 0.18).roundToInt()
        val totalBeforeGst = baseFare + distanceCharge + orderValueInt + serviceFee
        val grandTotal = totalBeforeGst + gstOnOrderValue

        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val userRef = database.getReference("Total Amount").child(userId)
            val totalRef = userRef.child("finalTotal")
            totalRef.setValue(grandTotal)
        }

        binding.basefareAmount.text = baseFare.toString()
        binding.distancechargesAmount.text = distanceCharge.toString()
        binding.servicefeesAmount.text = serviceFee.toString()
        binding.gstAmount.text = gstOnOrderValue.toString()
        binding.grandamount.text = grandTotal.toString()
        binding.ordervaluechargesAmount.text = orderValueInt.toString()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
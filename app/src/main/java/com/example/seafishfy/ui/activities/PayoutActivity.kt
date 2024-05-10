package com.example.seafishfy.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast
import com.example.seafishfy.R
import com.example.seafishfy.databinding.ActivityPayoutBinding
import com.example.seafishfy.ui.activities.fragments.CongratsBottomSheetFragment
import com.example.seafishfy.ui.activities.models.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.view.View
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.Intent
import android.location.Location
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PayoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPayoutBinding
    private lateinit var name: String
    private lateinit var address: String
    private lateinit var phoneNumber: String
    private lateinit var totalAmount: String
    private lateinit var paymentMethod: String
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var userId: String
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var adjustedTotalAmount: Int = 0

    private lateinit var foodItemName: ArrayList<String>
    private lateinit var foodItemPrice: ArrayList<String>
    private lateinit var foodItemDescription: ArrayList<String>
    private lateinit var foodItemIngredient: ArrayList<String>
    private lateinit var foodItemImage: ArrayList<String>
    private lateinit var foodItemQuantities: ArrayList<Int>


    private var status: String? = null


    private val PHONEPE_REQUEST_CODE = 101
    private val PAYTM_REQUEST_CODE = 102
    private val GOOGLE_PAY_REQUEST_CODE = 103
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialize Firebase and User Details
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setUserData()
        binding.drop.setOnClickListener { showPopupMenu(it) }

        // Get user details form Firebase
        foodItemName = intent.getStringArrayListExtra("foodItemName") as ArrayList<String>
        foodItemPrice = intent.getStringArrayListExtra("foodItemPrice") as ArrayList<String>
        foodItemDescription =
            intent.getStringArrayListExtra("foodItemDescription") as ArrayList<String>
        foodItemIngredient =
            intent.getStringArrayListExtra("foodItemIngredient") as ArrayList<String>
        foodItemImage = intent.getStringArrayListExtra("foodItemImage") as ArrayList<String>
        foodItemQuantities = intent.getIntegerArrayListExtra("foodItemQuantities") as ArrayList<Int>

        totalAmount = calculateTotalAmount().toString() + "₹"





        binding.payoutTotalAmount.isEnabled = false

        val destinationLocation = getLocationFromAddress("Spic Nagar")
        if (destinationLocation != null) {
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
        }

        mFusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    val distance = destinationLocation?.let { it1 -> calculateDistance(it, it1) }
                    val priceAdjustment = distance?.let { it1 -> adjustPriceBasedOnDistance(it1) }
                    var adjustedTotalAmount = totalAmount.dropLast(1).toInt() + priceAdjustment!!

                    adjustedTotalAmount =adjustedTotalAmount



                    binding.payoutTotalAmount.text =
                        "$totalAmount + $priceAdjustment = ₹$adjustedTotalAmount"


                }
            }


        binding.placeMyOrderButton.setOnClickListener {
            // get data from Edittext
            name = binding.payoutName.text.toString().trim()
            address = binding.payoutAddress.text.toString().trim()
            phoneNumber = binding.payoutPhoneNumber.text.toString().trim()

            if (name.isBlank() && address.isBlank() && phoneNumber.isBlank()) {
                Toast.makeText(this, "Please Enter all the Details", Toast.LENGTH_SHORT).show()
            } else if (!isValidPhoneNumber(phoneNumber)) {
                Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT).show()
            } else {
                placeTheOrder()
            }

//            val bottomSheetDialog = CongratsBottomSheetFragment()
//            bottomSheetDialog.show(supportFragmentManager,"Test")
        }
        binding.payoutBackButton.setOnClickListener {
            finish()
        }
    }


    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.payoutaddress, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_saved_location -> {
                    // Handle saved location selection
                    getLocation()
                    true
                }

                R.id.menu_current_location -> {
                    // Handle current location selection
                    getCurrentLocationAndDisplayAddress()
                    true
                }

                else -> false
            }
        }

        popupMenu.show()
    }

    private fun getCurrentLocationAndDisplayAddress() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request location permissions if not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
            return
        }

        // Get last known location
        mFusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                // Got last known location. In some rare situations this can be null.
                location?.let {
                    // Reverse geocode the coordinates to get the address
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val destinationLocation = getLocationFromAddress("Spic Nagar")
                    val distance = destinationLocation?.let { it1 -> calculateDistance(it, it1) }
                    val priceAdjustment = distance?.let { it1 -> adjustPriceBasedOnDistance(it1) }
                    val adjustedTotalAmount = totalAmount.dropLast(1).toInt() + priceAdjustment!!

                    binding.payoutTotalAmount.text = adjustedTotalAmount.toString()

                    try {
                        val addresses: List<Address>? =
                            geocoder.getFromLocation(it.latitude, it.longitude, 1)
                        if (addresses != null && addresses.isNotEmpty()) {
                            val address = addresses[0].getAddressLine(0)
                            // Update your text view with the obtained address
                            updateTextViewWithAddress(address)
                        } else {
                            // Handle case when no address is found
                            showToast("No address found for current location")
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        showToast("Error getting address for current location")
                    }
                }
            }
    }

    private fun getLocationFromAddress(address: String): Location? {
        // Implement code to get location from address
        // You can use Geocoder to convert the address to latitude and longitude
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(address, 1)
            if (addresses != null) {
                if (addresses.isNotEmpty()) {
                    val destinationLocation = Location("Spic Nagar")
                    destinationLocation.latitude = addresses!![0].latitude
                    destinationLocation.longitude = addresses!![0].longitude
                    return destinationLocation
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    private fun calculateDistance(location1: Location, location2: Location): Float {
        // Calculate distance between two locations using Location.distanceTo() method
        return location1.distanceTo(location2) / 1000 // Convert to kilometers
    }

    private fun adjustPriceBasedOnDistance(distance: Float): Int {
        var priceAdjustment = 0
        if (distance > 10) {
            priceAdjustment = PRICE_ADJUSTMENT_10_KM
        } else if (distance > 5) {
            priceAdjustment = PRICE_ADJUSTMENT_5_KM
        }else  {
            priceAdjustment = PRICE_ADJUSTMENT_0_KM
        }



        return priceAdjustment
    }


    private fun updateTextViewWithAddress(address: String) {
        binding.payoutAddress.text = address
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun getLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION
            )
            return
        }

        mFusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    // Convert the location to address
                    getAddressFromLocation(location)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting location", e)
            }
    }

    private fun getAddressFromLocation(location: Location) {
        val geocoder = Geocoder(this, Locale.getDefault())
        var addresses: List<Address>? = null
        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        } catch (ioException: IOException) {
            Log.e(TAG, "Error getting address from location", ioException)
        }

        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0].getAddressLine(0)
            displayAddress(address)
        } else {
            displayAddress("Address not found")
        }
    }

    private fun displayAddress(address: String) {
        binding.payoutAddress.text = address
    }

    private fun placeTheOrder() {

        showPaymentOptions()
    }

    private fun showPaymentOptions() {
        val paymentMethods =
            arrayOf("Google Pay", "PhonePe", "Paytm") // Add more payment methods if needed
        val builder =
            AlertDialog.Builder(this, R.style.GreenTitleAlertDialog) // Apply custom style here
        builder.setTitle("Choose Payment Method")
        builder.setItems(paymentMethods) { dialog, which ->
            val selectedMethod = paymentMethods[which]
            when (selectedMethod) {
                "Google Pay" -> {
                    // Handle Google Pay payment
                    redirectToGooglePay()
                }

                "PhonePe" -> {
                    redirectToPhonepe()
                    // Handle PhonePe payment

                }

                "Paytm" -> {
                    redirectToPaytm()
                    // Handle PhonePe payment
                }
            }
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun redirectToPhonepe() {
        paymentMethod = "Phonepe"
            val destinationLocation = getLocationFromAddress("Spic Nagar")
            if (destinationLocation != null) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Request permissions here if needed
                    return
                }
            } else {
                showToast("Error: Destination address not found")
            }
            mFusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    location?.let {
                        val distance = destinationLocation?.let { it1 -> calculateDistance(it, it1) }
                        val priceAdjustment =
                            distance?.let { it1 -> adjustPriceBasedOnDistance(it1) }
                        val adjustedTotalAmount =
                            totalAmount.dropLast(1).toInt() + priceAdjustment!!

                        binding.payoutTotalAmount.text = adjustedTotalAmount.toString()
                        val uri = Uri.Builder()
                            .scheme("upi")
                            .authority("pay")
                            .appendQueryParameter("pa", "9845779437.ibz@icici") // PhonePe VPA
                            .appendQueryParameter("pn", "Sheeba") // Recipient Name
                            .appendQueryParameter("tn", "Fish") // Transaction Note
                            .appendQueryParameter("am", adjustedTotalAmount.toString()) // Adjusted total amount
                            .appendQueryParameter("cu", "INR") // Currency
                            .build()
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setData(uri)
                        intent.setPackage(PHONEPE_PACKAGE_NAME)
                        startActivityForResult(intent, PHONEPE_REQUEST_CODE)
                        }
                    }
                }

    private fun redirectToPaytm() {
        paymentMethod = "Paytm"
        val destinationLocation = getLocationFromAddress("Spic Nagar")
        if (destinationLocation != null) {
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


        } else {
            showToast("Error: Destination address not found")
        }
        mFusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    val distance = destinationLocation?.let { it1 -> calculateDistance(it, it1) }
                    val priceAdjustment = distance?.let { it1 -> adjustPriceBasedOnDistance(it1) }
                    val adjustedTotalAmount = totalAmount.dropLast(1).toInt() + priceAdjustment!!

                    binding.payoutTotalAmount.text = adjustedTotalAmount.toString()
                    val uri = Uri.Builder()
                        .scheme("upi")
                        .authority("pay")
                        .appendQueryParameter("pa", "9845779437.ibz@icici")
                        .appendQueryParameter("pn", "Fishfy")
                        .appendQueryParameter("mc", "BCR2DN4T7XIZHYD3")
                        .appendQueryParameter("tn", "Fish")
                        .appendQueryParameter(
                            "am",
                            adjustedTotalAmount.toString()
                        ) // Adjusted total amount
                        .appendQueryParameter("cu", "INR")
                        .build()
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setData(uri)
                    intent.setPackage(PAYTM_PACKAGE_NAME)
                    startActivityForResult(intent, PAYTM_REQUEST_CODE)
                }
            }
    }


    private fun redirectToGooglePay() {
        paymentMethod = "Google Pay"
        val destinationLocation = getLocationFromAddress("Spic Nagar")
        if (destinationLocation != null) {
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


        } else {
            showToast("Error: Destination address not found")
        }
        mFusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    val distance = destinationLocation?.let { it1 -> calculateDistance(it, it1) }
                    val priceAdjustment = distance?.let { it1 -> adjustPriceBasedOnDistance(it1) }
                    val adjustedTotalAmount = totalAmount.dropLast(1).toInt() + priceAdjustment!!

                    binding.payoutTotalAmount.text = adjustedTotalAmount.toString()
                    val uri = Uri.Builder()
                        .scheme("upi")
                        .authority("pay")
                        .appendQueryParameter("pa", "9845779437.ibz@icici")
                        .appendQueryParameter("pn", "Fishfy")
                        .appendQueryParameter("mc", "BCR2DN4T7XIZHYD3")
                        .appendQueryParameter("tn", "Fish")
                        .appendQueryParameter(
                            "am",
                            adjustedTotalAmount.toString()
                        ) // Adjusted total amount
                        .appendQueryParameter("cu", "INR")
                        .build()
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setData(uri)
                    intent.setPackage(GOOGLE_TEZ_PACKAGE_NAME)
                    startActivityForResult(intent, GOOGLE_PAY_REQUEST_CODE)
                }
            }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        val destinationLocation = getLocationFromAddress("Spic Nagar")
        if (destinationLocation != null) {
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


        } else {
            showToast("Error: Destination address not found")
        }
        mFusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                location?.let {
                    val distance = destinationLocation?.let { it1 -> calculateDistance(it, it1) }
                    val priceAdjustment = distance?.let { it1 -> adjustPriceBasedOnDistance(it1) }
                    val adjustedTotalAmount = totalAmount.dropLast(1).toInt() + priceAdjustment!!

                    super.onActivityResult(requestCode, resultCode, data)
                    if (data != null) {
                        status = data.getStringExtra("Status")!!.lowercase(Locale.getDefault())
                    }
                    if (requestCode == GOOGLE_PAY_REQUEST_CODE || requestCode == PAYTM_REQUEST_CODE || requestCode == PHONEPE_REQUEST_CODE) {
                        if (resultCode == RESULT_OK && status == "success") {
                            Toast.makeText(
                                this@PayoutActivity,
                                "Transaction Successful",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {
                            Toast.makeText(
                                this@PayoutActivity,
                                "Transaction Failed",
                                Toast.LENGTH_SHORT
                            ).show()
                            navigateToCongratsFragment(adjustedTotalAmount)
                        }
                    }
                }
            }
    }


    private fun navigateToCongratsFragment(adjustedTotalAmount: Int) {
        userId = auth.currentUser?.uid ?: ""
        val timeFormat = SimpleDateFormat("HH:mm a", Locale.getDefault())
        val currentTimeMillis = System.currentTimeMillis()
        val formattedTime = timeFormat.format(currentTimeMillis)
        val time = formattedTime
        val itemPushKey = databaseReference.child("OrderDetails").push().key
        val orderDate = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date())

        val ShopNames = mutableListOf<String>()
        for (i in 0 until binding.pathContainer.childCount) {
            val textView = binding.pathContainer.getChildAt(i) as TextView
            ShopNames.add(textView.text.toString())
        }

        val orderDetails = OrderDetails(
            userId,
            name,
            foodItemName,
            foodItemPrice,
            foodItemImage,
            foodItemQuantities,
            address,
            phoneNumber,
            time,
            paymentMethod,
            adjustedTotalAmount,
            itemPushKey,
            orderDate,
            true,
            true,
            ShopNames // Add pathContainer text list to OrderDetails
        )

        val orderReference = databaseReference.child("OrderDetails").child(itemPushKey!!)
        orderReference.setValue(orderDetails)
            .addOnSuccessListener {
                val bottomSheetDialog = CongratsBottomSheetFragment()
                bottomSheetDialog.show(supportFragmentManager, "Test")
                removeItemFromCart()
                addOrderToHistory(orderDetails)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to Order 😒", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeItemFromCart() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
            val cartItemReference = databaseReference.child("user").child(uid).child("cartItems")
            cartItemReference.removeValue().addOnSuccessListener {
                // Handle success
                Toast.makeText(this, "Cart item removed successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { error ->
                // Handle failure
                Toast.makeText(this, "Failed to remove cartitems 😒", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addOrderToHistory(orderDetails: OrderDetails) {
        databaseReference.child("user").child(userId).child("BuyHistory")
            .child(orderDetails.itemPushKey!!)
            .setValue(orderDetails).addOnSuccessListener {

            }
    }

    private fun calculateTotalAmount(): Int {
        var totalAmount = 0
        for (i in 0 until foodItemPrice.size){
            var price = foodItemPrice[i]
            val lastChar = price.last()
            val priceIntValue = if (lastChar == '$') {
                price.dropLast(1).toInt()
            }else {
                price.toInt()
            }
            var quantity = foodItemQuantities[i]
            totalAmount += priceIntValue *quantity
        }

        return totalAmount
    }
    private fun setUserData() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userReference = databaseReference.child("user").child(userId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val cartItemsSnapshot = snapshot.child("cartItems")
                        if (cartItemsSnapshot.exists()) {
                            // Clear the existing views from the pathContainer
                            binding.pathContainer.removeAllViews()


                            // Iterate through each itemPushKey
                            cartItemsSnapshot.children.forEach { itemSnapshot ->
                                val path = itemSnapshot.child("path").getValue(String::class.java)
                                // Display the path for each product
                                displayProductPath(path)
                            }
                        }

                        val name = snapshot.child("name").getValue(String::class.java) ?: ""
                        val address = snapshot.child("address").getValue(String::class.java) ?: ""
                        val phoneNumber = snapshot.child("phone").getValue(String::class.java) ?: ""

                        binding.apply {
                            payoutName.setText(name)
                            payoutAddress.setText(address)
                            payoutPhoneNumber.setText(phoneNumber)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error reading user data", error.toException())
                }
            })
        }
    }

    private fun displayProductPath(path: String?) {
        // Display the path for each product in a TextView
        // For simplicity, let's assume you have a LinearLayout named pathContainer to contain the TextViews
        val textView = TextView(this)
        textView.text = path
        textView.textSize = 16f
        textView.setPadding(0, 8, 0, 8)
        binding.pathContainer.addView(textView)

    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        var formattedPhoneNumber = phoneNumber.trim()
        // Check if the phone number starts with "+91", if not, prepend it
        if (!formattedPhoneNumber.startsWith("+91")) {
            formattedPhoneNumber = "+91$formattedPhoneNumber"
        }
        return formattedPhoneNumber.matches("^\\+91[6-9][0-9]{9}$".toRegex())
    }

    companion object {
        private const val TAG = "PayoutActivity"
        private const val REQUEST_LOCATION_PERMISSION = 100



        const val PAYTM_PACKAGE_NAME = "net.one97.paytm"
        private const val GOOGLE_TEZ_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user"
         const val  PHONEPE_PACKAGE_NAME = "com.phonepe.app"
        // You can choose any integer value here

        private const val PRICE_ADJUSTMENT_5_KM = 0
        private const val PRICE_ADJUSTMENT_0_KM = 0
        private const val PRICE_ADJUSTMENT_10_KM = 2


    }
}

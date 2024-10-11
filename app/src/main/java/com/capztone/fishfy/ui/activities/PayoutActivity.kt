package com.capztone.fishfy.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.capztone.fishfy.databinding.ActivityPayoutBinding
import com.capztone.fishfy.ui.activities.models.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.view.View
import com.capztone.fishfy.R
import android.util.Log

import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.ContextCompat
import android.graphics.Color
import android.os.Build
import android.widget.ImageButton
import android.widget.LinearLayout
import com.capztone.fishfy.databinding.CustomDialogLayoutBinding
import com.capztone.fishfy.ui.activities.fragments.PayoutAddressFragment
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import kotlin.random.Random


class PayoutActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var binding: ActivityPayoutBinding

    private lateinit var address: String

    private lateinit var totalAmount: String




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
    private var selectedSlot: String? = null
    private var selectedOptionText: String = ""





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPayoutBinding.inflate(layoutInflater)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.statusBarColor = Color.TRANSPARENT
        }
        setContentView(binding.root)

        // Initialize Razorpay Checkout
        Checkout.preload(applicationContext)

        // Set default slot
        selectedSlot = "10:00 am - 12:00 pm" // default slot value
        binding.Slot.text = selectedSlot


        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setUserData()
        setDefaultAddress()
        binding.recentBackButton.setOnClickListener {
            onBackPressed()
        }
        binding.changeAddress.setOnClickListener {
            val addressFragment = PayoutAddressFragment()
            addressFragment.show(supportFragmentManager, addressFragment.tag)
        }
        binding.address.setOnClickListener {
            val addressFragment = PayoutAddressFragment()
            addressFragment.show(supportFragmentManager, addressFragment.tag)
        }

        binding.slotdrop.setOnClickListener { showPopupMenu1() }
        binding.Slot.setOnClickListener { showPopupMenu1() }
        binding.time.setOnClickListener { showPopupMenu1() }

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
        binding.payoutTotalAmount.setText(totalAmount)

        var adjustedTotalAmount = totalAmount.dropLast(1).toInt()
        retrieveFinalTotalFromFirebase()

        binding.placeMyOrderButton.setOnClickListener {
            // get data from Edittext

            address = binding.payoutAddress.text.toString().trim()


            if (address.isBlank()) {
                Toast.makeText(this, "Please Enter all the Details", Toast.LENGTH_SHORT).show()
            } else if (selectedSlot.isNullOrBlank()) {
                Toast.makeText(this, "Please select a time slot", Toast.LENGTH_SHORT).show()
            } else {
                startPayment()

            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("PayoutActivity", "onResume called")
        setDefaultAddress()
    }

    private fun setDefaultAddress() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val payoutAddressReference =
                databaseReference.child("PayoutAddress").child(userId).child("address")
            val userReference = databaseReference.child("Locations").child(userId)

            // Create a ValueEventListener for PayoutAddress
            val payoutAddressListener = object : ValueEventListener {
                override fun onDataChange(payoutSnapshot: DataSnapshot) {
                    if (payoutSnapshot.exists()) {
                        val payoutAddress = payoutSnapshot.getValue(String::class.java)
                        payoutAddress?.let {
                            val cleanedAddress =
                                cleanAddressString(it) // Remove trailing comma and format lines
                            binding.payoutAddress.setText(cleanedAddress)
                        }
                    } else {
                        // If PayoutAddress does not exist, check Locations for HOME, WORK, or OTHER addresses
                        userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(locationSnapshot: DataSnapshot) {
                                val addressFound = findFirstAvailableAddress(locationSnapshot)
                                addressFound?.let {
                                    val cleanedAddress =
                                        cleanAddressString(it) // Remove trailing comma and format lines
                                    binding.payoutAddress.setText(cleanedAddress)
                                    // Optionally, update the PayoutAddress in Firebase
                                    payoutAddressReference.setValue(it)
                                } ?: run {
                                    Log.e(
                                        "PayoutActivity",
                                        "No address found in HOME, WORK, or OTHER, and PayoutAddress is also not set"
                                    )
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e(
                                    "PayoutActivity",
                                    "Error fetching saved address",
                                    error.toException()
                                )
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("PayoutActivity", "Error fetching PayoutAddress", error.toException())
                }
            }

            // Attach the listener to PayoutAddress
            payoutAddressReference.addValueEventListener(payoutAddressListener)
        }
    }

    // Helper function to find the first available address from HOME, WORK, or OTHER
    private fun findFirstAvailableAddress(snapshot: DataSnapshot): String? {
        val addressTypes = listOf("HOME", "WORK", "OTHER")
        for (addressType in addressTypes) {
            val addressNode = snapshot.child(addressType).child("address")
            if (addressNode.exists()) {
                addressNode.getValue(String::class.java)?.let {
                    return it
                }
            }
        }
        return null
    }

    // Helper function to clean address string
    // Function to format address lines
    private fun cleanAddressString(address: String): String {
        val lines = address.split("\n") // Split address into lines

        // Trim each line and remove trailing comma if present
        val cleanedLines = lines.map { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.endsWith(',')) {
                trimmedLine.substring(0, trimmedLine.length - 1) // Remove trailing comma
            } else {
                trimmedLine
            }
        }

        return cleanedLines.joinToString("\n") // Join lines back into a single string
    }


    private fun retrieveFinalTotalFromFirebase() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val userReference = databaseReference.child("Total Amount").child(userId)

            userReference.child("finalTotal")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val finalTotal = snapshot.getValue(Double::class.java)
                        finalTotal?.let {
                            // Format the finalTotal to two decimal places
                            val formattedTotal = String.format("%.2f", it)
                            // Set the formatted total to the TextView
                            binding.amountTotal.text = "₹ $formattedTotal"
                            setDefaultAddress() // Call setDefaultAddress after setting total amount
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(
                            TAG,
                            "Error retrieving final total from Firebase",
                            error.toException()
                        )
                    }
                })
        }
    }

    private fun showPopupMenu1() {
        // Inflate the custom dialog layout using ViewBinding
        val dialogBinding = CustomDialogLayoutBinding.inflate(layoutInflater)
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogBinding.root)
        val dialog = dialogBuilder.create()
        dialog.show()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        var selectedLayout: LinearLayout? = null

        // Function to change the color of text and image
        fun setSelectionColor(layout: LinearLayout?, isSelected: Boolean) {
            val textView = layout?.getChildAt(1) as? TextView
            val imageButton = layout?.getChildAt(0) as? ImageButton
            val color = if (isSelected) R.color.navy else R.color.black
            textView?.setTextColor(ContextCompat.getColor(this, color))
            imageButton?.setColorFilter(
                ContextCompat.getColor(this, color),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }

        // Set up click listeners for each option using ViewBinding
        val options = listOf(dialogBinding.one, dialogBinding.two, dialogBinding.three)

        // Select the option based on the previously selected text
        for (option in options) {
            val optionText = (option.getChildAt(1) as TextView).text.toString()
            if (optionText == selectedOptionText) {
                selectedLayout = option
                setSelectionColor(option, true)
            }

            option.setOnClickListener {
                // Reset previous selection
                selectedLayout?.let { setSelectionColor(it, false) }

                // Update current selection
                selectedLayout = option
                selectedOptionText = (option.getChildAt(1) as TextView).text.toString()

                // Set selection color
                setSelectionColor(option, true)
            }
        }

        // Set up the OK button click listener using ViewBinding
        dialogBinding.btnOk.setOnClickListener {
            findViewById<TextView>(R.id.Slot).text =
                selectedOptionText // Update the Slot TextView with selected text
            dialog.dismiss()
        }

        // Set up the Cancel button click listener using ViewBinding
        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
    }



    private fun startPayment() {
        // Save order details before starting the payment process
        // Status is pending until payment is completed

        val checkout = Checkout()
        checkout.setKeyID("rzp_live_7rk7sJYf7JnVOk")
        val amountTotalString = binding.amountTotal.text.toString().replace("₹", "").trim()
        val adjustedTotalAmount = amountTotalString.toDoubleOrNull() ?: 0.0
        val amountInPaise = (adjustedTotalAmount * 100).toInt()
        val options = JSONObject().apply {
            put("name", "Fishfy") // Your business name
            put("description", "Order Payment")
            put("currency", "INR")
            put("amount", amountInPaise)
        }

        try {
            checkout.open(this, options)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error in payment: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    // Save order details with an initial status (pending, success, or failed)
    private fun saveOrderDetails(status: String) {
        userId = auth.currentUser?.uid ?: ""
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val currentTimeMillis = System.currentTimeMillis()
        val formattedTime = timeFormat.format(currentTimeMillis)
        val time = formattedTime

        // Parse the total amount
        val amountTotalString = binding.amountTotal.text.toString().replace("₹", "").trim()
        val adjustedTotalAmount = amountTotalString.toDoubleOrNull() ?: 0.0

        // Generate an 8-digit random number as itemPushKey
        val itemPushKey = Random.nextInt(10000000, 99999999).toString()

        // Get the order date in the desired format
        val orderDate = SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(Date())

        // Collect shop names from the pathContainer
        val shopNames = mutableListOf<String>()
        for (i in 0 until binding.pathContainer.childCount) {
            val textView = binding.pathContainer.getChildAt(i) as TextView
            shopNames.add(textView.text.toString())
        }
        val orderDetails = OrderDetails(

            userId,
            foodItemName,
            foodItemPrice,
            foodItemImage,
            foodItemQuantities,
            address,
            time,
            adjustedTotalAmount.toInt(),
            itemPushKey,
            orderDate,
            status == "success", // Order confirmed if payment is successful
            shopNames,
            selectedSlot,
            foodItemDescription,

            )

        val orderReference = databaseReference.child("OrderDetails").child(itemPushKey!!)
        orderReference.setValue(orderDetails)
            .addOnSuccessListener {
                Log.d(TAG, "Order details saved with status: $status")
                removeItemFromCart()
                removeItemFromCart1()
                addOrderToHistory(orderDetails)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save order details", Toast.LENGTH_SHORT).show()

            }
    }
    override fun onPaymentSuccess(paymentId: String) {
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show()
        saveOrderDetails("success") // Update the status to success after payment
        // Navigate to MainActivity after saving order details
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // Optionally finish the current activity

    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Payment Failed or Canceled", Toast.LENGTH_SHORT).show()
        saveOrderDetails("success") // Update the status to success after payment
        // Navigate to MainActivity after saving order details
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish() // Optionally finish the current activity



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

    private fun removeItemFromCart1() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
            val cartItemReference = databaseReference.child("Home").child(uid).child("cartItems")
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
        for (i in 0 until foodItemPrice.size) {
            val price = foodItemPrice[i]
            val priceIntValue = if (price.last() == '₹') {
                price.dropLast(1).toInt()
            } else {
                price.toInt()
            }
            val quantity = foodItemQuantities[i]
            totalAmount += priceIntValue * quantity
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
                        val address = snapshot.child("address").getValue(String::class.java) ?: ""
                        binding.apply {
                            payoutAddress.setText(address)
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
        val textView = TextView(this)
        textView.text = path
        textView.textSize = 16f
        textView.setPadding(0, 8, 0, 8)
        binding.pathContainer.addView(textView)
    }

    companion object {
        private const val TAG = "PayoutActivity"
    }
}
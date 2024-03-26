package com.example.seafishfy.ui.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.seafishfy.databinding.ActivityViewOrderDetailsBinding
import com.example.seafishfy.ui.activities.models.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import java.util.*

class ViewOrderDetails : AppCompatActivity() {

    private lateinit var binding: ActivityViewOrderDetailsBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var orderId: String
    private var orderDisplayedTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = Firebase.auth
        database = FirebaseDatabase.getInstance().reference.child("OrderDetails")

        orderId = intent.getStringExtra("order_id") ?: ""
        if (orderId.isNotEmpty()) {
            fetchOrderDetails(orderId)
            fetchOrderImages(orderId)
        } else {
            Toast.makeText(this, "Order id not found", Toast.LENGTH_SHORT).show()
            finish() // Finish activity if order id is not provided
        }
    }

    private fun fetchOrderDetails(orderId: String) {
        database.child(orderId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Order::class.java)
                order?.let {
                    displayOrderDetails(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
                Toast.makeText(
                    this@ViewOrderDetails,
                    "Error fetching order details",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun fetchOrderImages(orderId: String) {
        database.child(orderId).child("foodImage").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for ((index, imageSnapshot) in snapshot.children.withIndex()) {
                    val imageUrl = imageSnapshot.getValue(String::class.java)
                    imageUrl?.let {
                        // Load image into respective ImageView based on index
                        when (index) {
                            0 -> loadImageIntoImageView(it, binding.foodImage)
                            1 -> loadImageIntoImageView(it, binding.foodImage1)
                            2 -> loadImageIntoImageView(it, binding.foodImage2)
                            3 -> loadImageIntoImageView(it, binding.foodImage3)
                            4 -> loadImageIntoImageView(it, binding.foodImage4)
                            // Add more cases if you have more ImageViews
                        }
                    }
                }
            }


            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun loadImageIntoImageView(imageUrl: String, imageView: ImageView) {
        Glide.with(this@ViewOrderDetails)
            .load(imageUrl)
            .into(imageView)
    }


    private fun displayOrderDetails(order: Order) {
        binding.apply {
            oid.text = "Order ID: ${order.itemPushKey}"
            cid.text = "User ID: ${order.userUid}"
            foodName.text = "Food Name: ${order.foodNames}"
            foodPrice.text = "Food Price: ₹${order.adjustedTotalAmount}"
            quantity.text = "Quantity: ${order.foodQuantities}"

            orderDisplayedTime = Calendar.getInstance().timeInMillis // Capture current time

            binding.orderstatus.setOnClickListener {
                val intent = Intent(this@ViewOrderDetails, OrderStatusActivity::class.java)
                intent.putExtra("itemPushKey", order.itemPushKey)
                startActivity(intent)
            }
            radio.setOnClickListener {
                checkElapsedTime()
            }
        }
    }



    private fun checkElapsedTime() {
        val currentTime = Calendar.getInstance().timeInMillis
        val elapsedMinutes = (currentTime - orderDisplayedTime) / (1000 * 60) // Convert milliseconds to minutes

        if (elapsedMinutes >= 1) {
            showOrderTakenToast()
        } else {
            showCancelOrderDialog()
        }
    }

    private fun showOrderTakenToast() {
        Toast.makeText(this, "The order is already taken by the driver", Toast.LENGTH_SHORT).show()
    }

    private fun showCancelOrderDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("Are you sure you want to cancel your order?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                cancelOrder()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

    }

    private fun cancelOrder() {
        val cancellationMessage = "Order Cancelled"
        val orderCancellationRef = database.child(orderId).child("cancellationMessage")
        orderCancellationRef.setValue(cancellationMessage)
            .addOnSuccessListener {
                Toast.makeText(this, "Order cancelled", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to cancel order: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
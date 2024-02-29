package com.example.seafishfy.ui.activities



import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.seafishfy.databinding.ActivityOrderStatusBinding
import com.google.firebase.database.*

class OrderStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderStatusBinding
    private lateinit var database: DatabaseReference
    private lateinit var itemPushKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance().reference

        // Retrieve the itemPushKey from the intent
        itemPushKey = intent.getStringExtra("itemPushKey") ?: ""

        // Listen for changes in the status for the specific order
        listenForOrderStatusChanges(itemPushKey)

        // Listen for changes in the estimated time for the specific order
        listenForEstimatedTimeChanges(itemPushKey)
    }

    private fun listenForOrderStatusChanges(itemPushKey: String) {
        database.child("status").child(itemPushKey).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val status = dataSnapshot.child("message").getValue(String::class.java)
                // Update UI based on the status
                updateUI(status)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun listenForEstimatedTimeChanges(itemPushKey: String) {
        database.child("Estimated Time").child(itemPushKey).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val estimatedTime = dataSnapshot.child("estimated_time").getValue(String::class.java)
                // Update UI with the estimated time
                binding.estimatedtime.text = estimatedTime ?: "N/A"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun updateUI(status: String?) {
        when (status) {
            "Order confirmed" -> {
                // Display tick image for order confirmed
                binding.confirmtick.setImageResource(android.R.drawable.checkbox_on_background)
            }

            "Order picked" -> {
                // Display tick image for order picked
                binding.picktick.setImageResource(android.R.drawable.checkbox_on_background)
            }

            "Order delivered" -> {
                // Display tick image for order delivered
                binding.delivertick.setImageResource(android.R.drawable.checkbox_on_background)
            }

            else -> {
                // Hide tick images or handle other statuses
                binding.confirmtick.setImageResource(0) // Set to transparent or any other placeholder image
                binding.picktick.setImageResource(0)
                binding.delivertick.setImageResource(0)
            }
        }
    }
}

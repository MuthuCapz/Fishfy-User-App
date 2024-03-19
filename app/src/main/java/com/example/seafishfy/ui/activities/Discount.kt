// Discount.kt
package com.example.seafishfy.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.example.seafishfy.databinding.ActivityDiscountBinding // Import generated binding class
import com.example.seafishfy.ui.activities.adapters.DiscountAdapter
import com.example.seafishfy.ui.activities.models.DiscountItem
import com.google.firebase.database.*

class Discount : AppCompatActivity() {
    private lateinit var binding: ActivityDiscountBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var discountItems: MutableList<DiscountItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiscountBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database = FirebaseDatabase.getInstance()

        retrieveAndDisplayDiscountItems()
    }

    private fun retrieveAndDisplayDiscountItems() {
        val discountRef: DatabaseReference = database.reference.child("discount")
        discountItems = mutableListOf()

        discountRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (discountSnapshot in snapshot.children) {
                    val discountItem = discountSnapshot.getValue(DiscountItem::class.java)
                    discountItem?.let {
                        discountItems.add(it)
                    }
                }
                Log.d("ITEMS", "onDataChange : Data Received")
                displayDiscountItems()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ERROR", "Database Error: ${error.message}")
            }
        })
    }

    private fun displayDiscountItems() {
        val adapter = DiscountAdapter(discountItems, this)
        binding.discountRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.discountRecyclerView.adapter = adapter
    }
}

// Discount.kt
package com.example.seafishfy.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.seafishfy.databinding.ActivityDiscountBinding
import com.example.seafishfy.ui.activities.ViewModel.DiscountViewModel
import com.example.seafishfy.ui.activities.adapters.DiscountAdapter
import com.example.seafishfy.ui.activities.models.DiscountItem

import java.util.*

class Discount : AppCompatActivity() {
    private lateinit var binding: ActivityDiscountBinding
    private val viewModel: DiscountViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDiscountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.retrieveDiscountItems()
        viewModel.discountItems.observe(this, { items ->
            displayDiscountItems(items)
        })
    }

    private fun displayDiscountItems(discountItems: List<DiscountItem>) {
        val adapter = DiscountAdapter(discountItems, this)
        binding.discountRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.discountRecyclerView.adapter = adapter

        // Disable the DiscountRecyclerView based on current time
        if (!isDiscountRecyclerViewClickable()) {
            binding.discountRecyclerView.alpha = 0.5f // Set alpha to 0.5 to disable
            binding.discountRecyclerView.isEnabled = false // Disable RecyclerView clicks
        }
    }

    // Check if the DiscountRecyclerView is clickable based on the current time
    private fun isDiscountRecyclerViewClickable(): Boolean {
        val currentTime = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
        val hour = currentTime.get(Calendar.HOUR_OF_DAY)
        return hour !in 5 until 17 // Returns true if not between 5 PM and 5 AM
    }
}

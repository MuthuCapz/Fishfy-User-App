package com.example.seafishfy.ui.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.seafishfy.R
import com.example.seafishfy.databinding.ActivityViewOrderDetailsBinding
import com.example.seafishfy.ui.activities.ViewModel.ViewODViewModel
import com.example.seafishfy.ui.activities.models.Order
import java.util.*

class ViewOrderDetails : AppCompatActivity() {

    private lateinit var binding: ActivityViewOrderDetailsBinding
    private lateinit var viewModel: ViewODViewModel
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var orderId: String
    private var orderDisplayedTime: Long = 0



    private companion object {
        private const val KEY_ORDER_DISPLAYED_TIME = "orderDisplayedTime"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(ViewODViewModel::class.java)

        binding.detailGoToBackImageButton.setOnClickListener {
            finish()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        orderId = intent.getStringExtra("order_id") ?: ""
        if (savedInstanceState != null) {
            // Restore orderDisplayedTime from savedInstanceState
            orderDisplayedTime = savedInstanceState.getLong(KEY_ORDER_DISPLAYED_TIME)
        } else {
            // If savedInstanceState is null, it means the activity is created for the first time,
            // so capture the current time as the orderDisplayedTime
            orderDisplayedTime = Calendar.getInstance().timeInMillis
        }
        if (orderId.isNotEmpty()) {
            viewModel.fetchOrderDetails(orderId)
            viewModel.fetchOrderImages(orderId)
        } else {
            Toast.makeText(this, "Order id not found", Toast.LENGTH_SHORT).show()
            finish() // Finish activity if order id is not provided
        }

        sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // Check cancellation status and disable the view if necessary
        if (sharedPreferences.getBoolean("order_cancelled_$orderId", false)) {
            disableOrderView()
        }

        observeViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save orderDisplayedTime when the activity is being destroyed
        outState.putLong("orderDisplayedTime", orderDisplayedTime)
    }
    private fun observeViewModel() {
        viewModel.orderDetails.observe(this, { order ->
            if (order != null) {
                displayOrderDetails(order)
            }
        })

        viewModel.orderImages.observe(this, { imageUrls ->
            imageUrls.forEachIndexed { index, imageUrl ->
                // Load image into respective ImageView based on index
                when (index) {
                    0 -> loadImageIntoImageView(imageUrl, binding.foodImage)
                    1 -> loadImageIntoImageView(imageUrl, binding.foodImage1)
                    2 -> loadImageIntoImageView(imageUrl, binding.foodImage2)
                    3 -> loadImageIntoImageView(imageUrl, binding.foodImage3)
                    4 -> loadImageIntoImageView(imageUrl, binding.foodImage4)
                    // Add more cases if you have more ImageViews
                }
            }
        })

        viewModel.orderCancellationStatus.observe(this, { cancelled ->
            if (cancelled) {
                Toast.makeText(this, "Order cancelled", Toast.LENGTH_SHORT).show()
                // Disable the view
                disableOrderView()
                // Store cancellation status in SharedPreferences
                sharedPreferences.edit().putBoolean("order_cancelled_$orderId", true).apply()
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
            foodPrice.text = "Food Price: ${order.adjustedTotalAmount}"
            quantity.text = "Quantity: ${order.foodQuantities}"
            time.text = "${order.currentTime}"

            orderDisplayedTime = Calendar.getInstance().timeInMillis // Capture current time

            binding.orderstatus.setOnClickListener {
                val intent = Intent(this@ViewOrderDetails, OrderStatusActivity::class.java)
                intent.putExtra("itemPushKey", order.itemPushKey)
                startActivity(intent)
            }
            radio.setOnClickListener {
               showCancelOrderDialog()
            }


        }
    }

    private fun checkElapsedTime() {
        val currentTime = Calendar.getInstance().timeInMillis
        val elapsedMinutes = (currentTime - orderDisplayedTime) / (1000 * 60) // Convert milliseconds to minutes

        if (elapsedMinutes >= 1) {
            showOrderTakenDialog()
        } else {
            viewModel.cancelOrder(orderId)
        }
    }
    private fun showOrderTakenDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this, R.style.CustomDialogTheme)
        alertDialogBuilder.setMessage("You can't cancel your order because the driver has already taken it.")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    private fun showCancelOrderDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setMessage("Are you sure you want to cancel your order?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->

                checkElapsedTime()

            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

    }

    private fun disableOrderView() {
        binding.viewOrder.isEnabled = false
        binding.viewOrder.setBackgroundColor(ContextCompat.getColor(this, R.color.LiteAsh))
        binding.viewOrder.alpha = 0.5f
        // Display cancellation message on the disabled view
        val cancelledImageView = ImageView(this)
        cancelledImageView.setImageResource(R.drawable.cancelimg)
        cancelledImageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        val marginParams = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        marginParams.setMargins(150, 150, 150, 150)
        cancelledImageView.layoutParams = marginParams
        binding.viewOrder.addView(cancelledImageView)
    }
}

// OrderStatusActivity.kt

package com.example.seafishfy.ui.activities

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.airbnb.lottie.LottieAnimationView
import com.example.seafishfy.databinding.ActivityOrderStatusBinding
import com.example.seafishfy.ui.activities.ViewModel.OrderStatusViewModel

import com.google.firebase.database.*

class OrderStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderStatusBinding
    private lateinit var viewModel: OrderStatusViewModel
    private lateinit var itemPushKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        itemPushKey = intent.getStringExtra("itemPushKey") ?: ""
        viewModel = ViewModelProvider(this).get(OrderStatusViewModel::class.java)
        viewModel.init(itemPushKey, binding)

        binding.detailGoToBackImageButton.setOnClickListener {
            finish()
        }
    }
}

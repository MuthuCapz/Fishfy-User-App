// OrderStatusActivity.kt

package com.capztone.seafishfy.ui.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.capztone.seafishfy.databinding.ActivityOrderStatusBinding
import com.capztone.seafishfy.ui.activities.ViewModel.OrderStatusViewModel

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

package com.example.sea.ui.activities

import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.sea.databinding.ActivityRecentOrderBinding
import com.example.sea.ui.activities.adapters.RecentBuyAdapter
import com.example.sea.ui.activities.models.OrderDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RecentOrderActivity : AppCompatActivity() {


    private lateinit var binding: ActivityRecentOrderBinding
    private lateinit var database : FirebaseDatabase
    private lateinit var auth: FirebaseAuth
    private lateinit var userId:String
    private lateinit var allFoodName:ArrayList<String>
    private lateinit var allFoodPrice:ArrayList<String>
    private lateinit var allFoodImage:ArrayList<String>
    private lateinit var allFoodQuantity:ArrayList<Int>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRecentOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initialize Firebase
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        binding.recentBackButton.setOnClickListener {
            finish()
        }
//        val recentOrderItems = intent.getParcelableArrayListExtra<OrderDetails>("RecentBuyOrderItem")
        val recentOrderItems = intent.getSerializableExtra("RecentBuyOrderItem") as ArrayList<OrderDetails>
        recentOrderItems?.let { orderDetails->
            if (orderDetails.isNotEmpty()){
                val recentOrderItem = orderDetails[0]

                allFoodName = recentOrderItem.foodNames as ArrayList<String>
                allFoodPrice = recentOrderItem.foodPrices as ArrayList<String>
                allFoodImage = recentOrderItem.foodImage as ArrayList<String>
                allFoodQuantity = recentOrderItem.foodQuantities as ArrayList<Int>
            }
        }

        setAdapter()
    }

    private fun setAdapter() {
        binding.recentRecyclerView.layoutManager = LinearLayoutManager(this)
        val adapter = RecentBuyAdapter(this,allFoodName,allFoodPrice,allFoodImage,allFoodQuantity)
        binding.recentRecyclerView.adapter = adapter
    }

}
package com.example.seafishfy.ui.activities.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.seafishfy.databinding.RecentCartItemBinding
import com.example.seafishfy.ui.activities.ViewOrderDetails
import com.example.seafishfy.ui.activities.fragments.HistoryFragment
import com.example.seafishfy.ui.activities.models.Order

open class RecentBuyAdapter(
    private val orderList: List<Order>,
    private val context: Context,
    historyFragment: HistoryFragment
) :
    RecyclerView.Adapter<RecentBuyAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RecentCartItemBinding.inflate(inflater, parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orderList[position]
        holder.bind(order)
    }

    override fun getItemCount(): Int {
        return orderList.size
    }

    inner class OrderViewHolder(private val binding: RecentCartItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val order = orderList[position]
                    // Start the next activity here
                    val intent = Intent(context, ViewOrderDetails::class.java)
                    intent.putExtra("order_id", order.itemPushKey) // Pass any extra data needed
                    context.startActivity(intent)
                }
            }
        }

        fun bind(order: Order) {
            binding.apply {
                oid.text = "Order Id:         ${order.itemPushKey}"
                foodName.text = "Food Name:    ${order.foodNames}"

                Glide.with(binding.root)
                    .load(order.foodImage?.get(adapterPosition % order.foodImage.size)) // Use modulo to ensure index is within bounds
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.foodImage)
            }
        }
    }
}
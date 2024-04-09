package com.example.seafishfy.ui.activities.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.seafishfy.databinding.RecentCartItemBinding
import com.example.seafishfy.ui.activities.ViewOrderDetails
import com.example.seafishfy.ui.activities.models.Order
import com.example.seafishfy.ui.activities.viewmodel.HistoryViewModel

class RecentBuyAdapter(
    private val context: Context,
    private val viewModel: HistoryViewModel
) : ListAdapter<Order, RecentBuyAdapter.OrderViewHolder>(OrderDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RecentCartItemBinding.inflate(inflater, parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = getItem(position)
        holder.bind(order)
    }

    inner class OrderViewHolder(private val binding: RecentCartItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val order = getItem(position)
                    val intent = Intent(context, ViewOrderDetails::class.java)
                    intent.putExtra("order_id", order.itemPushKey)
                    context.startActivity(intent)
                }
            }
        }

        fun bind(order: Order) {
            binding.apply {
                oid.text = "Order Id:         ${order.itemPushKey}"
                foodName.text = "Food Name:    ${order.foodNames}"
                topDate.text = "${order.orderDate}"

                Glide.with(binding.root)
                    .load(order.foodImage?.get(adapterPosition % order.foodImage.size))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.foodImage)
            }
        }
    }
}

class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem.itemPushKey == newItem.itemPushKey
    }

    override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem == newItem
    }
}

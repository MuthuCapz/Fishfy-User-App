package com.example.seafishfy.ui.activities.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seafishfy.databinding.SearchItemBinding
import com.example.seafishfy.ui.activities.DetailsActivity
import com.example.seafishfy.ui.activities.models.MenuItem

class SearchAdapter(
    context1: List<Any>,
    private val context: Context
) : RecyclerView.Adapter<SearchAdapter.MenuViewHolder>() {

    private var menuItems: List<MenuItem> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = SearchItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = menuItems.size

    fun updateItems(items: List<MenuItem>) {
        menuItems = items
        notifyDataSetChanged()
    }

    inner class MenuViewHolder(private val binding: SearchItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    openDetailsActivity(position)
                }
            }
        }

        private fun openDetailsActivity(position: Int) {
            val menuItem = menuItems[position]
            val intent = Intent(context, DetailsActivity::class.java).apply {
                putExtra("MenuItemName", menuItem.foodName)
                putExtra("MenuItemPrice", menuItem.foodPrice)
                putExtra("MenuItemDescription", menuItem.foodDescription)
                putExtra("MenuItemImage", menuItem.foodImage)
            }
            context.startActivity(intent)
        }

        fun bind(position: Int) {
            val menuItem = menuItems[position]
            binding.apply {
                menuFoodName.text = menuItem.foodName
                val priceWithPrefix = "₹${menuItem.foodPrice}" // Prefixing the price with "₹"
                menuPrice.text = priceWithPrefix
                val url = Uri.parse(menuItem.foodImage)
                Glide.with(context).load(url).into(menuImage)
            }
        }
    }
}


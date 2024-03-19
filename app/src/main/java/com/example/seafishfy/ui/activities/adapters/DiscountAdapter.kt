package com.example.seafishfy.ui.activities.adapters
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seafishfy.databinding.DiscountItemsBinding
import com.example.seafishfy.ui.activities.DetailsActivity
import com.example.seafishfy.ui.activities.models.DiscountItem

class DiscountAdapter(
    private val discountItems: MutableList<DiscountItem>,
    private val context: Context
) : RecyclerView.Adapter<DiscountAdapter.DiscountViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountViewHolder {
        val binding = DiscountItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiscountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiscountViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = discountItems.size

    inner class DiscountViewHolder(private val binding: DiscountItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    openDiscountDetailsActivity(position)
                }
            }
        }

        private fun openDiscountDetailsActivity(position: Int) {
            val discountItem = discountItems[position]

            // Intent to open details activity and Pass data
            val intent = Intent(context, DetailsActivity::class.java)
            intent.putExtra("DiscountItemName", discountItem.foodNames)
            intent.putExtra("DiscountItemPrice", discountItem.foodPrices)
            intent.putExtra("DiscountItemDescription", discountItem.foodDescriptions)
            intent.putExtra("DiscountItemImage", discountItem.foodImages)
            intent.putExtra("DiscountAmount", discountItem.discounts)

            context.startActivity(intent)  // Start the details Activity
        }

        // Set data in RecyclerView items
        fun bind(position: Int) {
            val discountItem = discountItems[position]
            binding.apply {
                discountfoodname.text = discountItem.foodNames
                val priceWithPrefix = "₹${discountItem.foodPrices}" // Prefixing the price with "₹"
                discountprice.text = priceWithPrefix
                discounttextview.text=discountItem.discounts
                val url = Uri.parse(discountItem.foodImages)
                Glide.with(context).load(url).into(discountimage)
            }
        }
    }
}

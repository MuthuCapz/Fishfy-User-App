package com.example.seafishfy.ui.activities.adapters
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.seafishfy.R
import com.example.seafishfy.databinding.DiscountItemsBinding
import com.example.seafishfy.ui.activities.DetailsActivity
import com.example.seafishfy.ui.activities.models.DiscountItem
import java.util.*

class DiscountAdapter(
    private val discountItems: List<DiscountItem>,
    private val context: Context
) : RecyclerView.Adapter<DiscountAdapter.DiscountViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiscountViewHolder {
        val binding = DiscountItemsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiscountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiscountViewHolder, position: Int) {
        holder.bind(position)
        setAnimation(holder.itemView, position)
    }

    override fun getItemCount(): Int = discountItems.size

    inner class DiscountViewHolder(private val binding: DiscountItemsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    if (isDiscountRecyclerViewClickable()) {
                        openDiscountDetailsActivity(position)
                    } else {
                        // Show toast message if DiscountRecyclerView is not clickable
                        // You can customize the message as needed
                        Toast.makeText(context, "Discount items open only after 5 Pm", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Check if the DiscountRecyclerView is clickable based on the current time
        private fun isDiscountRecyclerViewClickable(): Boolean {
            val currentTime = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
            val hour = currentTime.get(Calendar.HOUR_OF_DAY)
            return hour !in 7 until 17 // Returns true if not between 7 AM and 5 PM
        }

        // Open discount details activity
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
                discounttextview.text = discountItem.discounts
                val url = Uri.parse(discountItem.foodImages)
                Glide.with(context).load(url).into(discountimage)
            }
        }
    }
    private fun setAnimation(view: View, position: Int) {
        val animation = AnimationUtils.loadAnimation(context, R.anim.slide_in_left)
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {
                // Animation started
            }

            override fun onAnimationEnd(animation: Animation?) {
                // Animation ended, trigger animation again
                view.startAnimation(animation)
            }

            override fun onAnimationRepeat(animation: Animation?) {
                // Animation repeated
            }
        })
        view.startAnimation(animation)
    }

}

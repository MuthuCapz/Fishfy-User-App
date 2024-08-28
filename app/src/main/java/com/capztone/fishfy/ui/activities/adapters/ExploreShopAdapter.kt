package com.capztone.fishfy.ui.activities.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.capztone.fishfy.R
import com.capztone.fishfy.databinding.ExploreShopBinding
import com.google.firebase.database.*

class ExploreShopAdapter(
    private var shopList: List<String>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ExploreShopAdapter.ShopViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(shopName: String)
    }

    private val localityCache = mutableMapOf<String, String>()

    inner class ShopViewHolder(val binding: ExploreShopBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(shopName: String, locality: String) {
            binding.shopTextView.text = shopName
            binding.localityTextView.text = locality

            binding.root.setOnClickListener {
                itemClickListener.onItemClick(shopName)
            }

            // Set different images for different positions
            when (absoluteAdapterPosition % 3) {
                0 -> binding.shopImageView.setImageResource(R.drawable.freshfish)
                1 -> binding.shopImageView.setImageResource(R.drawable.fishman)
                2 -> binding.shopImageView.setImageResource(R.drawable.pickles)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShopViewHolder {
        val binding = ExploreShopBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShopViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShopViewHolder, position: Int) {
        val shopName = shopList[position]

        // Check if the locality is already cached
        val cachedLocality = localityCache[shopName]
        if (cachedLocality != null) {
            // Use the cached locality
            holder.bind(shopName, cachedLocality)
        } else {
            // Fetch locality from Firebase Realtime Database
            val databaseReference = FirebaseDatabase.getInstance().reference
            databaseReference.child("ShopLocations").child(shopName).child("locality")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val locality = dataSnapshot.getValue(String::class.java) ?: ""
                        localityCache[shopName] = locality  // Cache the locality
                        holder.bind(shopName, locality)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle errors
                    }
                })
        }
    }

    override fun getItemCount(): Int = shopList.size

    // Use DiffUtil to update only changed items
    fun setShopList(newShopList: List<String>) {
        val diffCallback = ShopDiffCallback(shopList, newShopList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        shopList = newShopList
        diffResult.dispatchUpdatesTo(this)
    }

    // DiffUtil class for better performance
    class ShopDiffCallback(
        private val oldList: List<String>,
        private val newList: List<String>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}

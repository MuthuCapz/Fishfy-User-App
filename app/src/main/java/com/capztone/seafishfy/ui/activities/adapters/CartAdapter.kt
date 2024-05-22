package com.capztone.seafishfy.ui.activities.adapters

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.capztone.seafishfy.databinding.CartItemBinding
import com.capztone.seafishfy.ui.activities.Utils.ToastHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartAdapter(
    private val context: Context,
    private val cartItems: MutableList<String>,
    private val cartItemPrices: MutableList<String>,
    private var cartItemDescriptions: MutableList<String>,
    private val cartImages: MutableList<String>,
    private var cartItemQuantitys: MutableList<Int>,
    private var cartItemIngredients: MutableList<String>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // instance Firebase
    private val auth = FirebaseAuth.getInstance()

    init {
        // Initialize Firebase
        val database = FirebaseDatabase.getInstance()
        val userId = auth.currentUser?.uid ?: ""
        val cartItemNumber = cartItems.size

        itemQuantities = IntArray(cartItemNumber)
        cartItemsReference = database.reference.child("user").child(userId).child("cartItems")
    }

    companion object {
        private var itemQuantities: IntArray = intArrayOf()
        private lateinit var cartItemsReference: DatabaseReference
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = cartItems.size

    // Get updated quantity
    fun getUpdatedItemsQuantities(): MutableList<Int> {
        val itemsQuantity = mutableListOf<Int>()
        itemsQuantity.addAll(cartItemQuantitys)
        return itemsQuantity
    }

    inner class CartViewHolder(private val binding: CartItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {

                deteleImageButton.setOnClickListener {
                    val itemPosition = adapterPosition
                    if (itemPosition != RecyclerView.NO_POSITION) {
                        deleteItem(itemPosition)
                    }
                }
            }
        }

        fun bind(position: Int) {
            binding.apply {
                cartFoodNameTextView.text = cartItems[position]
                carItemPriceTextView.text = cartItemPrices[position]
                val uriString = cartImages[position]
                val uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(cartImageView)
                quantityTextView.text = cartItemQuantitys[position].toString()


                val itemName = cartItems[position]
                val firebasePaths = listOf("Shop 1", "Shop 2", "Shop 3", "Shop 4","Shop 5","Shop 6")
                fetchItemPath(itemName, firebasePaths) { path ->
                    // Update the TextView with the fetched path
                   "Item not found in any path"
                }
            }
        }


        private fun deleteItem(position: Int) {
            getUniqueKeyAtPosition(position) { uniqueKey ->
                cartItemsReference.child(uniqueKey).removeValue().addOnSuccessListener {
                    cartItems.removeAt(position)
                    cartItemPrices.removeAt(position)
                    cartItemDescriptions.removeAt(position)
                    cartImages.removeAt(position)
                    cartItemQuantitys.removeAt(position)
                    cartItemIngredients.removeAt(position)

                    notifyItemRemoved(position)
                    ToastHelper.showCustomToast(context, "Item Deleted")
                }.addOnFailureListener {
                    ToastHelper.showCustomToast(context, "Failed to Delete Item")
                }
            }
        }

        private fun getUniqueKeyAtPosition(positionRetrieve: Int, onComplete: (String) -> Unit) {
            cartItemsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var uniqueKey: String? = null
                    // loop for snapshot children
                    snapshot.children.forEachIndexed { index, dataSnapshot ->
                        if (index == positionRetrieve) {
                            uniqueKey = dataSnapshot.key
                            return@forEachIndexed
                        }
                    }
                    uniqueKey?.let { onComplete(it) }
                }

                override fun onCancelled(error: DatabaseError) {
                    ToastHelper.showCustomToast(context, "Failed to fetch data")
                }
            })
        }
    }

    private fun fetchItemPath(
        itemName: String,
        paths: List<String>,
        onComplete: (String?) -> Unit
    ) {
        val database = FirebaseDatabase.getInstance()

        // Iterate through each shop path
        for (shopPath in paths) {
            val shopReference = database.reference.child(shopPath)
            val childPaths = listOf("menu", "menu1", "menu2", "discount")
            val userId = FirebaseAuth.getInstance().currentUser?.uid

            userId?.let { uid ->

                // Iterate through each child path within the shop
                for (childPath in childPaths) {
                    val childReference = shopReference.child(childPath)
                    childReference.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            // Check if the item exists in this child path under the shop
                            snapshot.children.forEach { shopSnapshot ->
                                if (shopSnapshot.child("foodName").value == itemName) {
                                    onComplete("$shopPath")
                                    return
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            // Handle error
                        }
                    })
                }

            }
            // If item not found in any path, onComplete will be invoked with null
            onComplete(null)
        }
    }
}

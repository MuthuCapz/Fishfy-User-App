package com.capztone.seafishfy.ui.activities

import android.content.Intent
import android.graphics.text.LineBreaker
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.bumptech.glide.Glide
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.capztone.seafishfy.R

import com.capztone.seafishfy.databinding.ActivityDetailsBinding
import com.capztone.seafishfy.ui.activities.models.CartItems
import com.capztone.seafishfy.ui.activities.models.DiscountItem
import com.capztone.seafishfy.ui.activities.Utils.ToastHelper
import com.capztone.seafishfy.ui.activities.fragments.CartFragment
import com.capztone.seafishfy.ui.activities.fragments.MenuBottomSheetFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import android.text.Layout

class DetailsActivity : AppCompatActivity() {

   private lateinit var binding: ActivityDetailsBinding
   private var foodName: String? = null
   private var foodPrice: String? = null
   private var foodDescription: String? = null
   private var discount: String? = null
   private var foodImage: String? = null
   private var foodNames: String? = null
   private var foodPrices: String? = null
   private var foodDescriptions: String? = null
   private var discounts: String? = null
   private var foodImages: String? = null
   private var quantity: Int = 1
   private lateinit var auth: FirebaseAuth

   private var lastClickTime: Long = 0
   private val debounceDuration: Long = 1000 // 1 second debounce duration

   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      binding = ActivityDetailsBinding.inflate(layoutInflater)
      setContentView(binding.root)

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         binding.detailsShortDescriptionTextView.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
      }

      auth = FirebaseAuth.getInstance()
      if (intent.hasExtra("MenuItemName")) {
         foodName = intent.getStringExtra("MenuItemName")
         foodPrice = intent.getStringExtra("MenuItemPrice")
         foodDescription = intent.getStringExtra("MenuItemDescription")
         foodImage = intent.getStringExtra("MenuItemImage")
      } else if (intent.hasExtra("DiscountItemName")) {
         foodNames = intent.getStringExtra("DiscountItemName")
         foodPrices = intent.getStringExtra("DiscountItemPrice")
         foodDescriptions = intent.getStringExtra("DiscountItemDescription")
         foodImages = intent.getStringExtra("DiscountItemImage")
      }

      with(binding) {
         if (intent.hasExtra("MenuItemName")) {
            detailFoodNameTextView.text = intent.getStringExtra("MenuItemName")
            detailsShortDescriptionTextView.text = intent.getStringExtra("MenuItemDescription")
            val price = intent.getStringExtra("MenuItemPrice")
            textView21.text = "Price : ₹$price"
            Glide.with(this@DetailsActivity)
               .load(Uri.parse(intent.getStringExtra("MenuItemImage")))
               .into(detailImageView)

            val firebasePaths = listOf("Shop 1", "Shop 2", "Shop 3", "Shop 4", "Shop 5", "Shop 6")
            foodName?.let {
               val description = intent.getStringExtra("MenuItemDescription")
               if (description != null) {
                  fetchItemPath(it, description, firebasePaths) { path ->
                     shopname.text = path ?: "Item not found in any path"
                  }
               }
            }
         } else if (intent.hasExtra("DiscountItemName")) {
            detailFoodNameTextView.text = intent.getStringExtra("DiscountItemName")
            detailsShortDescriptionTextView.text = intent.getStringExtra("DiscountItemDescription")
            val price = intent.getStringExtra("DiscountItemPrice")
            textView21.text = "Price : ₹$price"
            Glide.with(this@DetailsActivity)
               .load(Uri.parse(intent.getStringExtra("DiscountItemImage")))
               .into(detailImageView)
            val firebasePaths1 = listOf("Shop 1", "Shop 2", "Shop 3", "Shop 4", "Shop 5", "Shop 6")
            foodNames?.let {
               val description = intent.getStringExtra("DiscountItemDescription")
               if (description != null) {
                  fetchItemPath1(it, description, firebasePaths1) { path ->
                     shopname.text = path ?: "Item not found in any path"
                  }
               }
            }
         } else {
            // Handle the case where neither MenuItemName nor DiscountItemName is provided
         }
      }

      binding.detailGoToBackImageButton.setOnClickListener {
         finish()
      }
      binding.plusImageButton.setOnClickListener {
         quantity++
         updateQuantityText()
      }
      binding.minusImageButton.setOnClickListener {
         if (quantity > 1) {
            quantity--
            updateQuantityText()
         }
      }
      binding.detailAddToCartButton.setOnClickListener {
         val currentTime = System.currentTimeMillis()
         if (currentTime - lastClickTime > debounceDuration) {
            lastClickTime = currentTime
            addItemToCart()
         }
      }
   }

   private fun fetchItemPath(
      itemName: String,
      itemDescription: String,
      paths: List<String>,
      onComplete: (String?) -> Unit
   ) {
      val database = FirebaseDatabase.getInstance()
      for (shopPath in paths) {
         val shopReference = database.reference.child(shopPath)
         val childPaths = listOf("menu", "menu1", "menu2")
         val userId = FirebaseAuth.getInstance().currentUser?.uid

         userId?.let { uid ->
            for (childPath in childPaths) {
               val childReference = shopReference.child(childPath)
               childReference.addListenerForSingleValueEvent(object : ValueEventListener {
                  override fun onDataChange(snapshot: DataSnapshot) {
                     snapshot.children.forEach { shopSnapshot ->
                        if (shopSnapshot.child("foodName").value == itemName &&
                           shopSnapshot.child("foodDescription").value == itemDescription
                        ) {
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
         onComplete(null)
      }
   }

   private fun fetchItemPath1(
      itemName: String,
      itemDescription: String,
      paths: List<String>,
      onComplete: (String?) -> Unit
   ) {
      val database = FirebaseDatabase.getInstance()
      val userId = FirebaseAuth.getInstance().currentUser?.uid

      userId?.let { uid ->
         paths.forEach { path ->
            val shopReference = database.reference.child(path)
            val childReference = shopReference.child("discount")

            childReference.addListenerForSingleValueEvent(object : ValueEventListener {
               override fun onDataChange(snapshot: DataSnapshot) {
                  snapshot.children.forEach { shopSnapshot ->
                     if (shopSnapshot.child("foodNames").value == itemName &&
                        shopSnapshot.child("foodDescriptions").value == itemDescription
                     ) {
                        onComplete(path)
                        return
                     }
                  }
               }

               override fun onCancelled(error: DatabaseError) {
                  onComplete(null)
               }
            })
         }
      }
   }

   private fun updateQuantityText() {
      binding.quantityText.text = quantity.toString()
   }

   private fun addItemToCart() {
      val database = FirebaseDatabase.getInstance().reference
      val userId = auth.currentUser?.uid ?: ""
      val currentShopName = binding.shopname.text.toString()
      val cartItemsRef = database.child("user").child(userId).child("cartItems")

      cartItemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
         override fun onDataChange(dataSnapshot: DataSnapshot) {
            var differentShopFound = false

            for (itemSnapshot in dataSnapshot.children) {
               val shopName = itemSnapshot.child("path").value as String
               if (shopName != currentShopName) {
                  differentShopFound = true
                  break
               }
            }

            if (differentShopFound) {
               AlertDialog.Builder(this@DetailsActivity)
                  .setTitle("Replace Item")
                  .setMessage("You have items from a different shop in your cart. Do you want to clear the cart and add this item?")
                  .setPositiveButton("Replace") { _, _ ->
                     cartItemsRef.removeValue().addOnSuccessListener {
                        addItemToCartWithoutCheck()
                     }
                  }
                  .setNegativeButton("No", null)
                  .show()
            } else {
               addItemToCartWithoutCheck()
            }
         }

         override fun onCancelled(databaseError: DatabaseError) {
            // Handle error
         }
      })
   }

   private fun addItemToCartWithoutCheck() {
      val database = FirebaseDatabase.getInstance().reference
      val userId = auth.currentUser?.uid ?: ""
      val shopname = binding.shopname.text

      if (shopname != null) {
         if (foodName != null && foodPrice != null && foodDescription != null && foodImage != null) {
            val cartQuery = database.child("user").child(userId).child("cartItems")
               .orderByChild("foodName")
               .equalTo(foodName)

            cartQuery.addListenerForSingleValueEvent(object : ValueEventListener {
               override fun onDataChange(dataSnapshot: DataSnapshot) {
                  if (dataSnapshot.exists()) {
                     ToastHelper.showCustomToast(this@DetailsActivity, "This item is already in your cart")
                  } else {
                     val cartItem = CartItems(
                        shopname.toString(),
                        foodName!!,
                        foodPrice!!,
                        foodDescription!!,
                        foodImage!!,
                        quantity
                     )
                     database.child("user").child(userId).child("cartItems").push().setValue(cartItem)
                        .addOnSuccessListener {
                           ToastHelper.showCustomToast(this@DetailsActivity, "Item added to cart successfully 🥰")
                        }.addOnFailureListener {
                           ToastHelper.showCustomToast(this@DetailsActivity, "Failed to add item 😒")
                        }
                  }
               }

               override fun onCancelled(databaseError: DatabaseError) {
                  // Handle error
               }
            })
         }
      }

      if (foodNames != null && foodPrices != null && foodDescriptions != null && foodImages != null) {
         val cartItemQuery = database.child("user").child(userId).child("cartItems")
            .orderByChild("foodName").equalTo(foodNames)

         cartItemQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
               if (snapshot.exists()) {
                  ToastHelper.showCustomToast(
                     this@DetailsActivity,
                     "This discount product is already in your cart"
                  )
               } else {
                  val cartItem = CartItems(
                     shopname.toString(),
                     foodNames!!,
                     foodPrices!!,
                     foodDescriptions!!,
                     foodImages!!,
                     quantity
                  )

                  database.child("user").child(userId).child("cartItems").push()
                     .setValue(cartItem)
                     .addOnSuccessListener {
                        ToastHelper.showCustomToast(
                           this@DetailsActivity,
                           "Discount item added to cart successfully 🥰"
                        )
                     }.addOnFailureListener {
                        ToastHelper.showCustomToast(
                           this@DetailsActivity,
                           "Failed to add discount item 😒"
                        )
                     }
               }
            }

            override fun onCancelled(error: DatabaseError) {
               // Handle onCancelled
            }
         })
      }
   }
}
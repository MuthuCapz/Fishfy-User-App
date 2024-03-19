package com.example.seafishfy.ui.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.example.seafishfy.databinding.ActivityDetailsBinding
import com.example.seafishfy.ui.activities.models.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


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
   override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)

      binding = ActivityDetailsBinding.inflate(layoutInflater)
      setContentView(binding.root)

      // initialize Firabase Auth
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
         } else if (intent.hasExtra("DiscountItemName")) {
            detailFoodNameTextView.text = intent.getStringExtra("DiscountItemName")
            detailsShortDescriptionTextView.text = intent.getStringExtra("DiscountItemDescription")
            val price = intent.getStringExtra("DiscountItemPrice")
            textView21.text = "Price : ₹$price"
            Glide.with(this@DetailsActivity)
               .load(Uri.parse(intent.getStringExtra("DiscountItemImage")))
               .into(detailImageView)
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

      // Decrease button click listener
      binding.minusImageButton.setOnClickListener {
         if (quantity > 1) {
            quantity--
            updateQuantityText()
         }
      }
      //
      binding.detailAddToCartButton.setOnClickListener {
         addItemToCart()
      }


   }

   private fun updateQuantityText() {
      binding.quantityText.text = quantity.toString()
   }

   private fun addItemToCart() {
      val database = FirebaseDatabase.getInstance().reference
      val userId = auth.currentUser?.uid ?: ""

      if (foodName != null && foodPrice != null && foodDescription != null && foodImage != null) {
         // Create a CartItems object for regular items
         val cartItem = CartItems(foodName!!, foodPrice!!, foodDescription!!, foodImage!!, quantity)

         // Save data to cart item to Firebase database
         database.child("user").child(userId).child("cartItems").push().setValue(cartItem)
            .addOnSuccessListener {
               Toast.makeText(this, "Item added to cart successfully 🥰", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
               Toast.makeText(this, "Failed to add item 😒", Toast.LENGTH_SHORT).show()
            }
      } else if (foodNames != null && foodPrices != null && foodDescriptions != null && foodImages != null) {
         // Create a CartItems object for discount items
         val cartItem = CartItems(foodNames!!, foodPrices!!, foodDescriptions!!, foodImages!!, quantity)

         // Save data to cart item to Firebase database
         database.child("user").child(userId).child("cartItems").push().setValue(cartItem)
            .addOnSuccessListener {
               Toast.makeText(
                  this,
                  "Discount item added to cart successfully 🥰",
                  Toast.LENGTH_SHORT
               ).show()
            }.addOnFailureListener {
               Toast.makeText(this, "Failed to add discount item 😒", Toast.LENGTH_SHORT).show()
            }
      } else {
         Toast.makeText(this, "Item details not found 😒", Toast.LENGTH_SHORT).show()
      }
   }
}
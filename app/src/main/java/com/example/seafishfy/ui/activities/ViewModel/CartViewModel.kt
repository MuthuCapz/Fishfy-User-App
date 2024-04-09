// CartViewModel.kt

package com.example.seafishfy.ui.activities.ViewModel

import androidx.lifecycle.ViewModel
import com.example.seafishfy.ui.activities.adapters.CartAdapter
import com.example.seafishfy.ui.activities.models.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartViewModel : ViewModel() {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private var userId: String = auth.currentUser?.uid ?: ""

    fun getOrderItemsDetail(cartAdapter: CartAdapter, orderNowCallback: (foodName: MutableList<String>, foodPrice: MutableList<String>, foodDescription: MutableList<String>, foodIngredient: MutableList<String>, foodImage: MutableList<String>, foodQuantities: MutableList<Int>) -> Unit) {
        val orderIdReference: DatabaseReference = database.reference.child("user").child(userId).child("cartItems")

        val foodQuantities = cartAdapter.getUpdatedItemsQuantities()
        val foodName = mutableListOf<String>()
        val foodPrice = mutableListOf<String>()
        val foodDescription = mutableListOf<String>()
        val foodIngredient = mutableListOf<String>()
        val foodImage = mutableListOf<String>()

        orderIdReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children){
                    val orderItems = foodSnapshot.getValue(CartItems::class.java)
                    orderItems?.let {
                        foodName.add(it.foodName.toString())
                        foodPrice.add(it.foodPrice.toString())
                        foodDescription.add(it.foodDescription.toString())
                        foodIngredient.add(it.foodIngredients.toString())
                        foodImage.add(it.foodImage.toString())
                    }
                }
                orderNowCallback(foodName, foodPrice, foodDescription, foodIngredient, foodImage, foodQuantities)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun retrieveCartItems(cartItemsCallback: (foodNames: MutableList<String>, foodPrices: MutableList<String>, foodDescriptions: MutableList<String>, foodIngredients: MutableList<String>, foodImageUri: MutableList<String>, quantity: MutableList<Int>) -> Unit) {
        val foodReferencer : DatabaseReference = database.reference.child("user").child(userId).child("cartItems")

        val foodNames = mutableListOf<String>()
        val foodPrices = mutableListOf<String>()
        val foodDescriptions = mutableListOf<String>()
        val foodIngredients = mutableListOf<String>()
        val foodImageUri = mutableListOf<String>()
        val quantity = mutableListOf<Int>()

        foodReferencer.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children){
                    val cartItems = foodSnapshot.getValue(CartItems::class.java)
                    cartItems?.let {
                        foodNames.add(it.foodName.toString())
                        foodPrices.add("₹" + it.foodPrice.toString())
                        foodDescriptions.add(it.foodDescription.toString())
                        foodIngredients.add(it.foodIngredients.toString())
                        foodImageUri.add(it.foodImage.toString())
                        it.foodQuantity?.let { it1 -> quantity.add(it1) }
                    }
                }
                cartItemsCallback(foodNames, foodPrices, foodDescriptions, foodIngredients, foodImageUri, quantity)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun isCartEmpty(cartItemsCallback: (Boolean) -> Unit) {
        val cartReference: DatabaseReference = database.reference.child("user").child(userId).child("cartItems")
        cartReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                cartItemsCallback(!snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}
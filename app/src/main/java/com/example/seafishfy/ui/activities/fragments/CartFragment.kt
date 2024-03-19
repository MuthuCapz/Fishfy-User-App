package com.example.seafishfy.ui.activities.fragments


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seafishfy.databinding.FragmentCartBinding
import com.example.seafishfy.ui.activities.PayoutActivity
import com.example.seafishfy.ui.activities.adapters.CartAdapter
import com.example.seafishfy.ui.activities.models.CartItems
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userId: String
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userId = auth.currentUser?.uid ?: ""
        retrieveCartItems()

        binding.cartProceedButton.setOnClickListener {
            getOrderItemsDetail()
        }
    }

    private fun getOrderItemsDetail() {
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
                orderNow(foodName, foodPrice, foodDescription, foodIngredient, foodImage, foodQuantities)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(),"Order Making failed. Please Tray Again", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun orderNow(
        foodName: MutableList<String>,
        foodPrice: MutableList<String>,
        foodDescription: MutableList<String>,
        foodIngredient: MutableList<String>,
        foodImage: MutableList<String>,
        foodQuantities: MutableList<Int>,

    ) {
        if (isAdded && context != null){
            val intent = Intent(requireContext(), PayoutActivity::class.java)
            intent.putExtra("foodItemName", ArrayList(foodName))
            intent.putExtra("foodItemPrice", ArrayList(foodPrice))
            intent.putExtra("foodItemDescription", ArrayList(foodDescription))
            intent.putExtra("foodItemIngredient", ArrayList(foodIngredient))
            intent.putExtra("foodItemImage", ArrayList(foodImage))
            intent.putExtra("foodItemQuantities", ArrayList(foodQuantities))
            startActivity(intent)
        }
    }

    private fun retrieveCartItems() {
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
                setAdapter(foodNames, foodPrices, foodDescriptions, foodIngredients, foodImageUri, quantity)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(),"data no fetch",Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun setAdapter(
        foodNames: MutableList<String>,
        foodPrices: MutableList<String>,
        foodDescriptions: MutableList<String>,
        foodIngredients: MutableList<String>,
        foodImageUri: MutableList<String>,
        quantity: MutableList<Int>
    ) {
        cartAdapter = CartAdapter(requireContext(), foodNames, foodPrices, foodDescriptions, foodImageUri, quantity, foodIngredients)
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.cartRecyclerView.adapter = cartAdapter
    }
}

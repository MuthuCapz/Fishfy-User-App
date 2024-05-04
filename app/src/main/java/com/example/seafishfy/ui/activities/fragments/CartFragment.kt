package com.example.seafishfy.ui.activities.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.seafishfy.databinding.FragmentCartBinding
import com.example.seafishfy.ui.activities.PayoutActivity
import com.example.seafishfy.ui.activities.Utils.ToastHelper
import com.example.seafishfy.ui.activities.adapters.CartAdapter
import com.example.seafishfy.ui.activities.ViewModel.CartViewModel

class CartFragment : Fragment(), CartProceedClickListener {
    private lateinit var binding: FragmentCartBinding
    private lateinit var viewModel: CartViewModel
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
        viewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        retrieveCartItems()

        binding.cartProceedButton.setOnClickListener {
            onCartProceedClicked()
        }
    }

    override fun onCartProceedClicked() {
        viewModel.isCartEmpty { isEmpty ->
            if (isEmpty) {
                context?.let { ToastHelper.showCustomToast(it, "First, you need to add products to the cart") }
            } else {
                viewModel.getOrderItemsDetail(cartAdapter) { foodName, foodPrice, foodDescription, foodIngredient, foodImage, foodQuantities ->
                    orderNow(foodName, foodPrice, foodDescription, foodIngredient, foodImage, foodQuantities)
                }
            }
        }
    }

    private fun orderNow(
        foodName: MutableList<String>,
        foodPrice: MutableList<String>,
        foodDescription: MutableList<String>,
        foodIngredient: MutableList<String>,
        foodImage: MutableList<String>,
        foodQuantities: MutableList<Int>
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
        viewModel.retrieveCartItems { foodNames, foodPrices, foodDescriptions, foodIngredients, foodImageUri, quantity ->
            setAdapter(foodNames, foodPrices, foodDescriptions, foodIngredients, foodImageUri, quantity)
        }
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

interface CartProceedClickListener {
    fun onCartProceedClicked()
}
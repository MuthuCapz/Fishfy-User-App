package com.capztone.fishfy.ui.activities.fragments

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.capztone.fishfy.R
import com.capztone.fishfy.databinding.FragmentCartBinding
import com.capztone.fishfy.ui.activities.PayoutActivity
import com.capztone.fishfy.ui.activities.Utils.ToastHelper
import com.capztone.fishfy.ui.activities.adapters.CartAdapter
import com.capztone.fishfy.ui.activities.ViewModel.CartViewModel
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.math.roundToInt

class CartFragment : Fragment(), CartProceedClickListener, CartAdapter.ProgressBarListener {
    private lateinit var binding: FragmentCartBinding
    private lateinit var viewModel: CartViewModel
    private lateinit var cartAdapter: CartAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private val handler = Handler(Looper.getMainLooper()) // Initialize handler


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
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        setupCartListener()
        retrieveCartItems()
        calculateDeliveryChargeAndProceed()

        binding.shopNowButton.setOnClickListener {
            findNavController().navigate(R.id.action_cartFragment_to_homefragment)
        }
        // Show loading indicator
        binding.progress.visibility = View.VISIBLE
        binding.textView6.visibility = View.GONE
        binding.summaryTotal.visibility = View.GONE
        binding.progress.setProgressVector(resources.getDrawable(R.drawable.spinload))
        binding.progress.setTextViewVisibility(true)
        binding.progress.setTextStyle(true)
        binding.progress.setTextColor(Color.YELLOW)
        binding.progress.setTextSize(12F)
        binding.progress.setTextMsg("Please Wait")
        binding.progress.setEnlarge(5)
        // Start a delay to hide the loading indicator after 5000 milliseconds (5 seconds)
        Handler(Looper.getMainLooper()).postDelayed({
            binding.progress.visibility = View.GONE
            binding.summaryTotal.visibility = View.VISIBLE
            binding.textView6.visibility=View.VISIBLE
            // Call your method to retrieve cart items or perform other operations
            retrieveCartItems()
        }, 1500)


        activity?.window?.let { window ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                window.statusBarColor = Color.WHITE
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                window.statusBarColor = Color. WHITE
            }
        }


        viewModel.retrieveCartItems { _, _, _, _, _, paths, _ ->
            val shopName = paths.firstOrNull() // Assuming the first item determines the shop name
            if (shopName != null) {
                val hasDifferentShop = paths.any { it != shopName }
                if (hasDifferentShop) {
                    // Display the dialog asking the user if they want to remove the item
                    showDialog()
                }
            }


            binding.recentBackButtonn.setOnClickListener {
                // Navigate to the profile fragment
                findNavController().navigate(R.id.action_cartFragment_to_homefragment)
            }


            binding.cartProceedButton.setOnClickListener {
                onCartProceedClicked()


            }
        }

        binding.btnRetry.setOnClickListener {
            if (isNetworkAvailable(requireContext())) {

                findNavController().popBackStack() // Example action, modify as needed
            } else {
                // Show toast if network is still not available
                Toast.makeText(requireContext(), "Please check your network", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onCartProceedClicked() {
        viewModel.retrieveCartItems { _, _, _, _, _, paths, _ ->
            val shopName = paths.firstOrNull() // Assuming the first item determines the shop name
            if (shopName != null) {
                val hasDifferentShop = paths.any { it != shopName }
                if (hasDifferentShop) {
                    // Display the dialog asking the user if they want to remove the item
                    showDialog()
                } else {
                    // Check if the cart is empty
                    viewModel.isCartEmpty { isEmpty ->
                        if (isEmpty) {
                            context?.let {
                                ToastHelper.showCustomToast(
                                    it,
                                    "First, you need to add products to the cart"
                                )
                            }
                        } else {
                            viewModel.getOrderItemsDetail(cartAdapter) { foodName, foodPrice, foodDescription, foodIngredient, foodImage, foodQuantities ->
                                orderNow(
                                    foodName,
                                    foodPrice,
                                    foodDescription,
                                    foodIngredient,
                                    foodImage,
                                    foodQuantities
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Retrieve cart items when the fragment is resumed
        retrieveCartItems()
    }

    private fun setupCartListener() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val cartItemsRef = database.getReference("user").child(userId).child("cartItems")

            val childEventListener = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    retrieveCartItems()
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    retrieveCartItems()
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    retrieveCartItems()
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    retrieveCartItems()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "Error fetching cart items", databaseError.toException())
                }
            }
            cartItemsRef.addChildEventListener(childEventListener)
        }
    }


    private fun calculateDeliveryChargeAndProceed() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            fetchUserLocation(userId) { userLocation ->
                fetchOrderValue(userId) { orderValue ->
                    val cartItemsRef = database.getReference("user").child(userId).child("cartItems")
                    val valueEventListener = object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val shops = mutableListOf<String>()
                            val uniqueShops = HashSet<String>()
                            dataSnapshot.children.forEach { cartItemSnapshot ->
                                val path = cartItemSnapshot.child("path").getValue(String::class.java)
                                path?.let { shop ->
                                    if (shop !in uniqueShops) {
                                        uniqueShops.add(shop)
                                        shops.add(shop)
                                    }
                                }
                            }
                            // Display shop names in UI

                            for (shop in shops) {
                                getShopLocation(shop, userLocation, orderValue)
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle onCancelled
                        }
                    }
                    cartItemsRef.addValueEventListener(valueEventListener)
                }
            }
        }
    }

    private fun fetchUserLocation(userId: String, callback: (Location) -> Unit) {
        val locationRef = database.getReference("Locations").child(userId)
        locationRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val latitude = dataSnapshot.child("latitude").getValue(Double::class.java)
                val longitude = dataSnapshot.child("longitude").getValue(Double::class.java)

                if (latitude != null && longitude != null) {
                    val userLocation = Location("").apply {
                        this.latitude = latitude
                        this.longitude = longitude
                    }
                    callback(userLocation)
                } else {
                    // Handle case when latitude or longitude is null
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun fetchOrderValue(userId: String, callback: (Int) -> Unit) {
        val cartItemsRef = database.getReference("user").child(userId).child("cartItems")
        cartItemsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var orderValue = 0
                dataSnapshot.children.forEach { cartItemSnapshot ->
                    val foodPriceAny = cartItemSnapshot.child("foodPrice").getValue(Any::class.java)
                    val foodQuantityAny = cartItemSnapshot.child("foodQuantity").getValue(Any::class.java)

                    val foodPrice = when (foodPriceAny) {
                        is String -> foodPriceAny.toDoubleOrNull()?.toInt() ?: 0
                        is Long -> foodPriceAny.toInt()
                        is Double -> foodPriceAny.toInt()
                        else -> 0
                    }

                    val quantity = when (foodQuantityAny) {
                        is String -> foodQuantityAny.toIntOrNull() ?: 1
                        is Long -> foodQuantityAny.toInt()
                        is Int -> foodQuantityAny
                        else -> 1
                    }

                    orderValue += foodPrice * quantity
                }
                callback(orderValue)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled
            }
        })
    }

    private fun getShopLocation(shopName: String, userLocation: Location, orderValue: Int) {
        val shopRef = database.getReference("ShopLocations").child(shopName)
        shopRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val latitude = dataSnapshot.child("latitude").getValue(Double::class.java)
                val longitude = dataSnapshot.child("longitude").getValue(Double::class.java)

                if (latitude != null && longitude != null) {
                    val shopLocation = Location("").apply {
                        this.latitude = latitude
                        this.longitude = longitude
                    }
                    calculateDeliveryCharge(userLocation, shopLocation, orderValue)
                } else {
                    // Handle case when latitude or longitude is null
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled
            }
        })
    }
    private fun calculateDeliveryCharge(userLocation: Location, shopLocation: Location, orderValue: Int) {
        val adminUserId = "spXRl1jY4yTlhDKZJzLicp8E9kc2"
        val adminRef = FirebaseDatabase.getInstance().getReference("Admins").child(adminUserId)

        adminRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Example: Log all fetched values
                Log.d(TAG, "Fetched admin data: $dataSnapshot")

                // Retrieve administrative settings from Firebase
                val baseFare = dataSnapshot.child("Base Fare").getValue(String::class.java)?.toInt() ?: 20
                val driverDistance = dataSnapshot.child("Driver Distance").getValue(String::class.java)?.toInt() ?: 10
                val gst = dataSnapshot.child("Gst").getValue(String::class.java)?.toDouble() ?: 0.00
                val peakHourCharge = dataSnapshot.child("Peak Hour Charge").getValue(String::class.java)?.toInt() ?: 50
                val perKmCharge = dataSnapshot.child("Perkm Charge").getValue(String::class.java)?.toInt() ?: 5
                val serviceCharge = dataSnapshot.child("Service Charge").getValue(String::class.java)?.toInt() ?: 5
                val speedDeliveryCharge = dataSnapshot.child("Speed Delivery Charge").getValue(String::class.java)?.toInt() ?: 30

                // Calculate distance between user and shop in kilometers
                val distanceInKm = userLocation.distanceTo(shopLocation) / 1000

                // Calculate distance charge
                val distanceCharge = if (orderValue > 500) 0 else (distanceInKm * perKmCharge).roundToInt()

                // Calculate GST on order value
                val gstOnOrderValue = gst

                // Calculate total before GST
                val totalBeforeGst = baseFare + serviceCharge + peakHourCharge

                // Calculate grand total including order value and distance charge
                val grandTotal = totalBeforeGst + orderValue + distanceCharge

                // Example: Log calculated values
                Log.d(TAG, "Base Fare: $baseFare, Distance Charge: $distanceCharge, Service Charge: $serviceCharge")
                Log.d(TAG, "Total Before GST: $totalBeforeGst, Grand Total: $grandTotal, Order Value: $orderValue")

                // Update total amount in Firebase for the current user
                saveTotalAmount(grandTotal)

                // Update UI on the main thread
                updateUI(baseFare, distanceCharge, serviceCharge, totalBeforeGst, grandTotal, orderValue,gstOnOrderValue)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database read error
                Log.e(TAG, "Failed to read value from Firebase: ${databaseError.message}")
            }
        })
    }

    private fun updateUI(
        baseFare: Int, distanceCharge: Int, serviceCharge: Int, totalBeforeGst: Int, grandTotal: Int, orderValue: Int,
        gstOnorderValue: Double
    ) {
        val currencySymbol = "₹"

        // Update UI elements with formatted currency values

        binding.distancechargesAmount.post {
            binding.distancechargesAmount.text = String.format("%s %.2f", currencySymbol, distanceCharge.toDouble())
        }
        binding.savings.post {
            binding.savings.text = String.format("%s %.2f", currencySymbol, gstOnorderValue.toDouble())
        }
        binding.servicefeesAmount.post {
            binding.servicefeesAmount.text = String.format("%s %.2f", currencySymbol, serviceCharge.toDouble())
        }
        binding.gstAmount.post {
            binding.gstAmount.text = String.format("%s %.2f", currencySymbol, totalBeforeGst.toDouble())
        }
        binding.totals.post {
            binding.totals.text = String.format("%s %.2f", currencySymbol, grandTotal.toDouble())
        }
        binding.ordervaluechargesAmount.post {
            binding.ordervaluechargesAmount.text = String.format("%s %.2f", currencySymbol, orderValue.toDouble())
        }
    }
    private fun saveTotalAmount(grandTotal: Int) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userId = user.uid
            val userRef = database.getReference("Total Amount").child(userId)
            val totalRef = userRef.child("finalTotal")
            totalRef.setValue(grandTotal)
        }


    }

    override fun showProgressBar() {
        activity?.runOnUiThread {
            binding.progress.visibility = View.VISIBLE
            binding.cartProceedButton.isEnabled = false
        }
    }

    override fun hideProgressBar() {
        activity?.runOnUiThread {
            handler.postDelayed({
                binding.progress.visibility = View.GONE
                binding.cartProceedButton.isEnabled =  true
            }, 1000) // Delay hiding for 1000 ms (1 second)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun removeFirstCartItem() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val database = FirebaseDatabase.getInstance()

        currentUser?.let { user ->
            val userId = user.uid

            // Get a reference to the user's cartItems node in the Realtime Database
            val cartItemsRef = database.getReference("user").child(userId).child("cartItems")

            // Fetch user's cart items from the Realtime Database
            cartItemsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val cartItems = dataSnapshot.children.mapNotNull { it.key to it.child("path").getValue(String::class.java) }
                    if (cartItems.isNotEmpty()) {
                        // Get the path of the first item
                        val firstItem = cartItems.first()
                        val firstKey = firstItem.first
                        val firstPath = firstItem.second

                        // Check if the path is the same for other items
                        val keysToRemove = cartItems.filter { it.second == firstPath }.map { it.first }

                        if (keysToRemove.isNotEmpty()) {
                            val tasks = keysToRemove.map { cartItemsRef.child(it!!).removeValue() }

                            // Wait for all removal tasks to complete
                            Tasks.whenAllComplete(tasks).addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // All items with the same path removed successfully
                                    // Update the fragment to reflect the changes
                                    retrieveCartItems()
                                } else {
                                    // Error handling
                                    task.exception?.let { e ->
                                        Log.e(TAG, "Error removing cart items", e)
                                    }
                                }
                            }
                        } else {
                            // Remove the first item if no other items have the same path
                            if (firstKey != null) {
                                cartItemsRef.child(firstKey).removeValue()
                                    .addOnSuccessListener {
                                        // Cart item removed successfully
                                        // Update the fragment to reflect the changes
                                        retrieveCartItems()
                                    }
                                    .addOnFailureListener { e ->
                                        // Error handling
                                        Log.e(TAG, "Error removing cart item", e)
                                    }
                            }
                        }
                    } else {
                        // Cart is empty
                        // You can handle this scenario accordingly
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Error handling
                    Log.e(TAG, "Error fetching cart items", databaseError.toException())
                }
            })
        }
    }

    override fun showDialog() {
        if (isAdded) {
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("Remove Item")
            alertDialog.setMessage("You have chosen another shop. Do you want to remove the items from the previous shop in your cart?")
            alertDialog.setPositiveButton("Remove") { _, _ ->
                // Perform the action to remove the item
                removeFirstCartItem()
            }
            alertDialog.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.show()
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
        if (isAdded && context != null) {

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
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                else -> false
            }
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }
    private fun retrieveCartItems() {

        val context = context ?: return  // Safely access context or return if null

        // First check network availability before proceeding to handle cart items
        if (!isNetworkAvailable(context)) {
            // No internet connection, show the network message regardless of cart items
            binding.cartContentLayout.visibility = View.GONE
            binding.network.visibility = View.VISIBLE
            binding.emptyCartMessage.visibility = View.GONE
            binding.shopNowButton.visibility = View.GONE
            binding.scrollViewCart.visibility = View.GONE
            binding.cartProceedButton.visibility = View.GONE
            return  // Exit the function since no internet is available
        }

        viewModel.retrieveCartItems { foodNames, foodPrices, foodDescriptions, foodIngredients, foodImageUri, paths, quantity ->
            val context = context ?: return@retrieveCartItems  // Safely access context or return if null
            setAdapter(foodNames, foodPrices, foodDescriptions, foodIngredients, foodImageUri, quantity)

            if (isNetworkAvailable(context)) {
                if (foodNames.isNotEmpty()) {
                    binding.cartContentLayout.visibility = View.VISIBLE
                    binding.network.visibility = View.GONE
                    binding.emptyCartMessage.visibility = View.GONE
                    binding.shopNowButton.visibility = View.GONE
                    binding.scrollViewCart.visibility = View.VISIBLE
                    binding.cartProceedButton.visibility = View.VISIBLE
                } else {
                    binding.cartContentLayout.visibility = View.GONE
                    binding.network.visibility = View.GONE
                    binding.emptyCartMessage.visibility = View.VISIBLE
                    binding.shopNowButton.visibility = View.VISIBLE
                    binding.scrollViewCart.visibility = View.GONE
                    binding.cartProceedButton.visibility = View.GONE
                }
            } else {
                if (foodNames.isNotEmpty()) {
                    binding.cartContentLayout.visibility = View.VISIBLE
                    binding.network.visibility = View.VISIBLE
                    binding.emptyCartMessage.visibility = View.GONE
                    binding.shopNowButton.visibility = View.GONE
                    binding.scrollViewCart.visibility = View.GONE
                    binding.cartProceedButton.visibility = View.GONE
                } else {
                    binding.cartContentLayout.visibility = View.GONE
                    binding.network.visibility = View.GONE
                    binding.emptyCartMessage.visibility = View.VISIBLE
                    binding.shopNowButton.visibility = View.VISIBLE
                    binding.scrollViewCart.visibility = View.GONE
                    binding.cartProceedButton.visibility = View.GONE
                }
            }
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
        if (isAdded) {
            cartAdapter = CartAdapter(
                requireContext(),
                foodNames,
                foodPrices,
                foodDescriptions,
                foodImageUri,
                quantity,
                this,
                foodIngredients,

                )
            binding.cartRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            binding.cartRecyclerView.adapter = cartAdapter
        }
    }
}

interface CartProceedClickListener {
    fun onCartProceedClicked()
    fun showProgressBar()
    fun hideProgressBar()
    fun showDialog()
}
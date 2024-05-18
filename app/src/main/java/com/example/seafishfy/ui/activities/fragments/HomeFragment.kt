package com.example.seafishfy.ui.activities.fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import kotlin.math.*
import androidx.annotation.RequiresApi
import kotlin.random.Random

import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.interfaces.ItemClickListener
import com.denzcoskun.imageslider.models.SlideModel
import com.example.seafishfy.R
import com.example.seafishfy.databinding.FragmentHomeBinding
import com.example.seafishfy.ui.activities.ContactusActivity
import com.example.seafishfy.ui.activities.Discount
import com.example.seafishfy.ui.activities.LocationActivity
import com.example.seafishfy.ui.activities.models.DiscountItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import androidx.navigation.fragment.findNavController
import android.Manifest
import android.content.ContentValues.TAG
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.AnimationUtils
import android.view.animation.TranslateAnimation
import com.example.seafishfy.ui.activities.Utils.ToastHelper
import com.example.seafishfy.ui.activities.ViewModel.HomeViewModel
import com.google.android.material.slider.Slider
import androidx.core.text.HtmlCompat
import com.google.android.play.integrity.internal.i


class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var discountItems: MutableList<DiscountItem>
    private lateinit var databaseReference: DatabaseReference
    private val shop1Location = LatLng(8.8076189, 78.1283788)
    private val shop2Location = LatLng(	8.6701179, 78.093077)
    private val shop3Location = LatLng(
        37.422580, -122.084330)
    private val shop4Location = LatLng(8.8076189, 78.1283788)
    private val shop6Location = LatLng(8.6701179, 78.093077)
    private val shop5Location = LatLng(37.422580, -122.084330)
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val viewModel: HomeViewModel by viewModels()
    private val imageResources = arrayOf(
        R.drawable.ribbon1,
        R.drawable.ribbon2,
        R.drawable.ribbon3,
        R.drawable.ribbon4
    )


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        observeViewModel()


        databaseReference = FirebaseDatabase.getInstance().getReference("locations")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Check if user ID exists


        if (hasLocationPermission()) {
            getUserLocation { userLocation ->
                if (userLocation != null) {
                    val shopLocations = listOf(shop1Location, shop2Location, shop3Location,shop4Location,shop5Location,shop6Location)
                    for (i in 0 until shopLocations.size) {
                        val cardView = getCardViewByIndex(i + 5) // Adjust index to match card view IDs
                        val shopLocation = shopLocations[i]
                        val distance = calculateDistance(userLocation, shopLocation)
                        if (distance <= 5) {
                            // Enable and make clickable
                            cardView.alpha= 1.0f
                            cardView.setOnClickListener {
                                // Navigate to respective shop fragment
                                navigateToShopFragment(i + 5) // Adjust index to match card view IDs
                            }
                        } else {
                            // Disable and make unclickable
                            cardView.alpha = 0.4f
                            binding.cardView.setBackgroundColor(Color.parseColor("#303235")) // Replace "#C0C0C0" with your desired color code


                            cardView.setOnClickListener {
                                // Show toast indicating the shop is not available for the user's location
                                ToastHelper.showCustomToast(requireContext(), "This shop is not delivery for your locations")
                                cardView.isEnabled=false
                            }
                        }
                    }
                }
            }
        } else {
            // Request location permission if not granted
            requestLocationPermission()
        }

        userId?.let { uid ->
            // Retrieve data under "locations" path for the current user
            val userLocationRef = databaseReference.child(uid)
            userLocationRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // Retrieve shopname for the current user
                        val shopNames = snapshot.child("shopname").getValue(String::class.java)
                        shopNames?.let { shops ->
                            // Split the shop names by comma and handle each one individually
                            val shopNameList = shops.split(",").map { it.trim() }
                            shopNameList.forEach { shop ->
                                // Blur the card view associated with the shopname
                                markUserShopsAsNormal(shopNames)
                            }
                        }
                    } else {
                        // Handle case where data doesn't exist for the user
                        // For example, if the user hasn't set their shopname yet
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }

        val database = FirebaseDatabase.getInstance()
        val shopReferences = arrayOf(
            "Shop 1",
            "Shop 2",
            "Shop 3",
            "Shop 4",
            "Shop 5",
            "Shop 6"
        )

        for (i in shopReferences.indices) {
            val reference = database.getReference(shopReferences[i]).child("discount")

            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val imageList = ArrayList<SlideModel>()

                        for (discountSnapshot in dataSnapshot.children) {
                            val discountItem = discountSnapshot.getValue(DiscountItem::class.java)
                            discountItem?.let { item ->
                                val imageUrl = item.foodImages
                                val foodName = item.foodNames
                                val discount = item.discounts

                                imageUrl?.let {
                                    // Construct title with HTML tags to set text size
                                    val title = "<font size='17'>$foodName</font> - <font size='17'>$discount Off</font>"

                                    // Construct SlideModel with image URL and title
                                    val slideModel = SlideModel(imageUrl, HtmlCompat.fromHtml(title, HtmlCompat.FROM_HTML_MODE_COMPACT).toString(), ScaleTypes.FIT)
                                    imageList.add(slideModel)
                                }
                            }
                        }

                        // Load images into the appropriate image slider based on shop index
                        when (i) {
                            0 -> {
                                binding.imageSlider1.setImageList(imageList, ScaleTypes.FIT)
                                binding.imageSlider1.setItemClickListener(object : ItemClickListener {
                                    override fun doubleClick(position: Int) {
                                        // Double click listener implementation
                                    }

                                    override fun onItemSelected(position: Int) {
                                        // Scroll to the top position of cardView5
                                        scrollToCardView5()
                                    }
                                })

                            }
                            1 ->{
                                binding.imageSlider2.setImageList(imageList, ScaleTypes.FIT)
                                binding.imageSlider2.setItemClickListener(object : ItemClickListener {
                                    override fun doubleClick(position: Int) {
                                        // Double click listener implementation
                                    }

                                    override fun onItemSelected(position: Int) {
                                        // Navigate to the same page as cardView5
                                        scrollToCardView6()
                                    }
                                })
                            }
                            2 ->{
                                binding.imageSlider3.setImageList(imageList, ScaleTypes.FIT)
                                binding.imageSlider3.setItemClickListener(object : ItemClickListener {
                                    override fun doubleClick(position: Int) {
                                        // Double click listener implementation
                                    }

                                    override fun onItemSelected(position: Int) {
                                        // Navigate to the same page as cardView5
                                        scrollToCardView7()
                                    }
                                })
                            }
                            3 -> {
                                binding.imageSlider4.setImageList(imageList, ScaleTypes.FIT)
                                binding.imageSlider4.setItemClickListener(object : ItemClickListener {
                                    override fun doubleClick(position: Int) {
                                        // Double click listener implementation
                                    }

                                    override fun onItemSelected(position: Int) {
                                        // Navigate to the same page as cardView5
                                        scrollToCardView8()
                                    }
                                })
                            }
                            4 -> {
                                binding.imageSlider5.setImageList(imageList, ScaleTypes.FIT)
                                binding.imageSlider5.setItemClickListener(object : ItemClickListener {
                                    override fun doubleClick(position: Int) {
                                        // Double click listener implementation
                                    }

                                    override fun onItemSelected(position: Int) {
                                        // Navigate to the same page as cardView5
                                        scrollToCardView9()
                                    }
                                })
                            }
                            5 -> {
                                binding.imageSlider6.setImageList(imageList, ScaleTypes.FIT)
                                binding.imageSlider6.setItemClickListener(object : ItemClickListener {
                                    override fun doubleClick(position: Int) {
                                        // Double click listener implementation
                                    }

                                    override fun onItemSelected(position: Int) {
                                        // Navigate to the same page as cardView5
                                        scrollToCardView10()
                                    }
                                })
                            }
                        }

                    } else {
                        Log.d(TAG, "No data found for ${shopReferences[i]}")
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e(TAG, "onCancelled", databaseError.toException())
                }
            })
        }

        // Retrieve and display address and locality
        retrieveAddressAndLocality()

        binding.dropdown.setOnClickListener {
            showPopupMenu(it)
        }

        binding.dotsMenu.setOnClickListener {
            showPopupMenus(it)
        }
        binding.cardView21.setOnClickListener {
            ToastHelper.showCustomToast(requireContext(), "Show shops")
        }
        this@HomeFragment.database = FirebaseDatabase.getInstance()



        return binding.root
    }
    private fun markUserShopsAsNormal(userShopNames: String) {
        // List of all shop names in your application
        val allShopNames = listOf("Shop 1", "Shop 2", "Shop 3", "Shop 4", "Shop 5", "Shop 6")

        // Split user's shop names by comma and trim whitespace
        val userShopNameList = userShopNames.split(",").map { it.trim() }

        // Find and mark the user's shop names as normal
        for (shopName in allShopNames) {
            val userCardView = getCardViewByShopName(shopName)
            if (userShopNameList.contains(shopName)) {
                // Enable and make clickable
                userCardView.alpha = 1.0f
                userCardView.setOnClickListener {
                    navigateToShopFragment(allShopNames.indexOf(shopName) + 5) // Adjust index to match card view IDs
                }
            } else {
                // Blur other card views
                userCardView.alpha = 0.4f
                userCardView.setOnClickListener{
                    ToastHelper.showCustomToast(requireContext(), "This shop is not delivery for your locations")
                    userCardView.isEnabled=false
                } // Disable click listener
            }
        }
    }

    private fun getCardViewByShopName(shopName: String): View {
        return when (shopName) {
            "Shop 1" -> binding.cardView5
            "Shop 2" -> binding.cardView6
            "Shop 3" -> binding.cardView7
            "Shop 4" -> binding.cardView8
            "Shop 5" -> binding.cardView9
            "Shop 6" -> binding.cardView10
            else -> throw IllegalArgumentException("Invalid shop name: $shopName")
        }
    }

    private fun hasLocationPermission(): Boolean {
        return (ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun getCardViewByIndex(index: Int): View {
        return when (index) {
            5 -> binding.cardView5
            6 -> binding.cardView6
            7 -> binding.cardView7
            8 -> binding.cardView8
            9 -> binding.cardView9
            10 -> binding.cardView10
            else -> throw IllegalArgumentException("Invalid card view index: $index")
        }
    }

    private fun calculateDistance(start: LatLng, end: LatLng): Double {
        val R = 6371 // Radius of the Earth in kilometers
        val latDistance = Math.toRadians(end.latitude - start.latitude)
        val lonDistance = Math.toRadians(end.longitude - start.longitude)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                (cos(Math.toRadians(start.latitude)) * cos(Math.toRadians(end.latitude)) *
                        sin(lonDistance / 2) * sin(lonDistance / 2))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }
    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSIONS_REQUEST_LOCATION
        )
    }
    private fun getUserLocation(callback: (LatLng?) -> Unit) {
        // Check for location permission
        if (hasLocationPermission()) {
            // Permission granted, get user's current location
            try {
                fusedLocationClient.lastLocation
                    .addOnSuccessListener { location ->
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            val userLocation = LatLng(location.latitude, location.longitude)
                            callback.invoke(userLocation)
                        } else {
                            callback.invoke(null) // Location could not be retrieved
                        }
                    }
            } catch (e: SecurityException) {
                // Handle SecurityException
                callback.invoke(null)
            }
        } else {
            // Location permission not granted
            callback.invoke(null)
        }
    }
    private fun observeViewModel() {
        viewModel.address.observe(viewLifecycleOwner) { address ->
            binding.tvAddress.text = address
        }

        viewModel.locality.observe(viewLifecycleOwner) { locality ->
            binding.tvLocality.text = locality
        }
        viewModel.latitude.observe(viewLifecycleOwner) { latitude ->
            binding.tvLatitude.text = latitude.toString()
        }
        viewModel.longitude.observe(viewLifecycleOwner) { longitude ->
            binding.tvLogitude.text = longitude.toString()
        }
    }


    private fun navigateToShopFragment(index: Int) {
        when (index) {
            5 -> findNavController().navigate(R.id.action_homeFragment_to_shoponefragment)
            6 -> findNavController().navigate(R.id.action_homeFragment_to_shoptwofragment)
            7 -> findNavController().navigate(R.id.action_homeFragment_to_shopthreefragment)
            8 -> findNavController().navigate(R.id.action_homeFragment_to_shopfourfragment)
            9-> findNavController().navigate(R.id.action_homeFragment_to_shopfivefragment)
            10-> findNavController().navigate(R.id.action_homeFragment_to_shopsixfragment)
            else -> throw IllegalArgumentException("Invalid card view index")
        }
    }


    private fun retrieveAddressAndLocality() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val userLocationRef = databaseReference.child(userId)
            userLocationRef.addListenerForSingleValueEvent(object : ValueEventListener {
                @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val address = snapshot.child("address").getValue(String::class.java)
                        val locality = snapshot.child("locality").getValue(String::class.java)
                        val latitude = snapshot.child("latitude").getValue(Double::class.java)
                        val longitude = snapshot.child("longitude").getValue(Double::class.java)



                        binding.tvAddress.text = address
                        binding.tvLocality.text = locality
                        binding.tvLatitude.text = latitude?.toString() ?: "N/A"
                        binding.tvLogitude.text = longitude?.toString() ?: "N/A"

                    } else {
                        // Handle case where data doesn't exist
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun showPopupMenus(view: View) {
        val popupMenus = PopupMenu(requireContext(), view)
        popupMenus.menuInflater.inflate(R.menu.option_menu, popupMenus.menu)

        // Set text color for the PopupMenu items
        val menus = popupMenus.menu

        popupMenus.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.about -> {
                    // Handle "Use Current Location" click
                    val intent = Intent(requireContext(), ContactusActivity::class.java)
                    startActivity(intent)
                    // Hide the dropdown icon after picking an address
                    binding.dropdown.visibility = View.GONE
                    true
                }
                // Add more saved address clicks as needed
                else -> false
            }
        }

        popupMenus.setOnDismissListener {
            // Dismiss the PopupMenu when it's dismissed
            popupMenus.dismiss()
        }

        popupMenus.show()
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.menuInflater.inflate(R.menu.address, popupMenu.menu)

        // Set text color for the PopupMenu items
        val menu = popupMenu.menu
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            val spannableString = SpannableString(menuItem.title.toString())
            spannableString.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.navy)),
                0,
                spannableString.length,
                0
            )
            menuItem.title = spannableString
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.use_current_location -> {
                    // Handle "Use Current Location" click
                    val intent = Intent(requireContext(), LocationActivity::class.java)
                    startActivity(intent)
                    // Hide the dropdown icon after picking an address
                    binding.dropdown.visibility = View.GONE
                    true
                }
                // Add more saved address clicks as needed
                else -> false
            }
        }

        popupMenu.setOnDismissListener {
            // Dismiss the PopupMenu when it's dismissed
            popupMenu.dismiss()
        }

        popupMenu.show()
    }








    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupImageSlider()
        startAnimation()
    }

    private fun scrollToCardView5() {
        scrollToCardView(binding.cardView5)
    }
    private fun scrollToCardView6() {
        scrollToCardView(binding.cardView6)
    }
    private fun scrollToCardView7() {
        scrollToCardView(binding.cardView7)
    }
    private fun scrollToCardView8() {
        scrollToCardView(binding.cardView8)
    }
    private fun scrollToCardView9() {
        scrollToCardView(binding.cardView9)
    }
    private fun scrollToCardView10() {
        scrollToCardView(binding.cardView10)
    }



    private fun scrollToCardView(cardView: View) {
        // Get the top position of the cardView relative to the NestedScrollView
        val scrollPosition = cardView.top
        // Scroll to the position of the cardView
        binding.scrollView.post {
            binding.scrollView.smoothScrollTo(0, scrollPosition)
        }
    }
    private fun startAnimation() {
        // Randomly select an image from the array
        val randomImageIndex = Random.nextInt(imageResources.size)
        val randomImage = imageResources[randomImageIndex]

        // Set the randomly selected image to the ImageView
        binding.discounts.setImageResource(randomImage)

        // Load the animation
        val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.discount)

        // Set animation listener to change image after exit animation completes
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                // Randomly select a new image for the next animation
                val newRandomImageIndex = Random.nextInt(imageResources.size)
                val newRandomImage = imageResources[newRandomImageIndex]
                binding.discounts.setImageResource(newRandomImage)

                // Start the animation again
                binding.discounts.startAnimation(animation)
            }
        })

        // Start the animation
        binding.discounts.startAnimation(animation)

    }

    private fun setupImageSlider() {
        val imageList = ArrayList<SlideModel>()
        // Add your image resources here
        imageList.add(SlideModel(R.drawable.ban, scaleType = ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner1, scaleType = ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner2, scaleType = ScaleTypes.FIT))
        imageList.add(SlideModel(R.drawable.banner3, scaleType = ScaleTypes.FIT))

        val imageSlide = binding.imageSlider
        imageSlide.setImageList(imageList)

        imageSlide.setItemClickListener(object : ItemClickListener {
            override fun doubleClick(position: Int) {
                // Double click listener implementation
            }

            override fun onItemSelected(position: Int) {
                // Check if the selected image is "ban"
                if (position == 3) { // Assuming "ban" image is at position 0
                    // Launch the DiscountActivity
                    val intent = Intent(requireContext(), Discount::class.java)
                    startActivity(intent)
                } else {
                    val itemMessage = "Selected Image $position"
                    Toast.makeText(requireContext(), itemMessage, Toast.LENGTH_SHORT).show()
                }
            }
        })


    }
    companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 100
    }
}



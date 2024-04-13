// MainActivity.kt
package com.example.seafishfy.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider

import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import com.example.seafishfy.R
import com.example.seafishfy.databinding.ActivityMainBinding
import com.example.seafishfy.ui.activities.ViewModel.MainViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth : FirebaseAuth
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        auth = FirebaseAuth.getInstance()

        // Initialize ViewModel
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        // Initialize Lottie animation view
        val animationView: LottieAnimationView = binding.lottieAnimationView

        // Load animation from assets folder
        loadAnimation(animationView, "shower.json")

        // Start animation
        animationView.playAnimation()

        binding.tvAddress.setOnClickListener {
            showPopupMenu(it)
        }

        // Check if fragmentContainerView exists in the layout
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
            ?: // Handle the case where fragmentContainerView is not found
            // You might want to log an error or handle it in an appropriate way
            return

        // Find NavController only if fragmentContainerView is found
        val navController = navHostFragment.findNavController()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val address = intent.getStringExtra("ADDRESS")
        val locality = intent.getStringExtra("LOCALITY")

        // Display the address and locality in TextViews
        binding.tvAddress.text = "Address: $address"
        binding.tvLocality.text = " $locality"

        // Check for null before setting up with NavController
        navController?.let {
            bottomNav.setupWithNavController(it)
        }

        // Observe the user address from ViewModel
        viewModel.userAddress.observe(this, { userAddress ->
            // Update UI with the user's address
            binding.tvAddress.text = "Address: $userAddress"
        })
    }

    private fun loadAnimation(animationView: LottieAnimationView, animationFileName: String) {
        LottieCompositionFactory.fromAsset(this, animationFileName)
            .addListener { composition ->
                animationView.setComposition(composition)
            }
    }

    private fun showPopupMenu(view: View) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.address, popupMenu.menu)

        // Set text color for the PopupMenu items
        val menu = popupMenu.menu
        for (i in 0 until menu.size()) {
            val menuItem = menu.getItem(i)
            val spannableString = SpannableString(menuItem.title.toString())
            spannableString.setSpan(ForegroundColorSpan(ContextCompat.getColor(this, R.color.navy)), 0, spannableString.length, 0)
            menuItem.title = spannableString
        }

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.use_current_location -> {
                    // Handle "Use Current Location" click
                    startActivity(Intent(this, LocationActivity::class.java))
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

    override fun onBackPressed() {
        // Check if there are fragments in the back stack
        if (supportFragmentManager.backStackEntryCount > 0) {
            // Pop the fragment from the back stack
            supportFragmentManager.popBackStack()
        } else {
            // If no fragments in the back stack, let the system handle the back button press
            super.onBackPressed()
        }
    }
}

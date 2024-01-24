package com.example.sea.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView

import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.sea.R
import com.example.sea.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Check if fragmentContainerView exists in the layout
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
            ?: // Handle the case where fragmentContainerView is not found
            // You might want to log an error or handle it in an appropriate way
            return

        // Find NavController only if fragmentContainerView is found
        val navController = navHostFragment.findNavController()

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

        // Check for null before setting up with NavController
        navController?.let {
            bottomNav.setupWithNavController(it)
        }


    }

}

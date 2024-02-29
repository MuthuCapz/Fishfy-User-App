package com.example.seafishfy.ui.activities


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.example.seafishfy.R


import android.widget.Button
import com.example.seafishfy.databinding.ActivityLoginBinding

import com.example.seafishfy.ui.activities.fragments.ProfileFragment

import com.google.android.gms.common.api.ApiException


class LoginActivity : AppCompatActivity() {
    private lateinit var loginBinding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(loginBinding.root)

        // Other initialization code for LoginActivity


        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        configureGoogleSignIn()

        // Set click listener for the Google Sign-In button
        val btnGoogleSignIn: Button = findViewById(R.id.googleLoginbutton)
        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_Id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this,MainActivity::class.java)
                    startActivity(intent)
                    finish() // Close the SigninActivity

                    // Access user information
                    val user = auth.currentUser
                    showAccountFragment(user?.displayName, user?.photoUrl.toString())
                } else {
                    // If sign in fails, display a message to the user.
                }

            }
      }
    private fun showAccountFragment(displayName: String?, photoUrl: String?) {
        val fragment = ProfileFragment.newInstance(displayName, photoUrl)
        // You can pass user data to the AccountFragment if needed
        fragment.arguments?.putString("someKey", "someValue")

        // Handle navigation to AccountFragment in MainActivity
        // For simplicity, assuming AccountFragment is a part of MainActivity
        val mainIntent = Intent(this, LocationActivity::class.java)
        mainIntent.putExtra("fragmentToShow", "accountFragment")
        startActivity(mainIntent)
        finish() // Close the SigninActivity
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}

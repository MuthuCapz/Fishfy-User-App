package com.capztone.fishfy.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.capztone.fishfy.R
import com.capztone.fishfy.databinding.ActivityLoginBinding
import com.capztone.fishfy.ui.activities.VerifyNumberActivity.Companion.phoneNumberKey
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var binding: ActivityLoginBinding
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        configureGoogleSignIn()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.statusBarColor = android.graphics.Color.TRANSPARENT
        }

        database = FirebaseDatabase.getInstance().reference

        binding.googleLoginbutton.setOnClickListener {
            signIn()
        }

        binding.btnGetOtp.setOnClickListener {
            validateNumber()
        }
    }

    private fun validateNumber() {
        val phoneNumber = binding.etPhoneNum.editableText?.toString()

        if (phoneNumber.isNullOrEmpty()) {
            binding.etPhoneNum.error = "Enter your Phone Number"
            binding.etPhoneNum.requestFocus()
            return
        }

        if (phoneNumber.length != 10) {
            Toast.makeText(this, "Enter 10 digit number", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the phone number exists in the DeletedAccounts path
        val databaseReference = FirebaseDatabase.getInstance().getReference("DeletedAccounts")
        val query = databaseReference.orderByChild("phoneNumber").equalTo(phoneNumber)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val targetActivity = if (dataSnapshot.exists()) {
                    NotThereActivity::class.java
                } else {
                    VerifyNumberActivity::class.java
                }
                val intent = Intent(this@LoginActivity, targetActivity).apply {
                    putExtra(phoneNumberKey, phoneNumber)
                }
                startActivity(intent)
                finish()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@LoginActivity, "Database error: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_Id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun signIn() {
        googleSignInClient.signOut().addOnCompleteListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val email = account.email
                if (email != null) {
                    checkDeletedAccounts(email) { isDeleted ->
                        if (isDeleted) {
                            navigateToNotThereActivity()
                        } else {
                            signInWithGoogle(account.idToken!!)
                        }
                    }
                }
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    private fun checkDeletedAccounts(email: String, callback: (Boolean) -> Unit) {
        val deletedAccountsRef = database.child("DeletedAccounts")
        deletedAccountsRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                callback(snapshot.exists())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "checkDeletedAccounts:onCancelled", error.toException())
                callback(false)
            }
        })
    }

    private fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    checkUserSetupAndStoreData(user)
                }
            } else {
                Log.w(TAG, "Google sign in failed", task.exception)
                Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUserSetupAndStoreData(user: FirebaseUser) {
        val userRef = database.child("users").child(user.uid)
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    // User exists, navigate to MainActivity
                    navigateToMainActivity()
                } else {
                    // User does not exist, navigate to LanguageActivity and then store data
                    navigateToLanguageActivity()
                    storeUserData(user)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(TAG, "checkUserSetup:onCancelled", error.toException())
                Toast.makeText(this@LoginActivity, "Failed to check user setup", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun storeUserData(user: FirebaseUser) {
        val userRef = database.child("users").child(user.uid)
        val userMap = mapOf(
            "uid" to user.uid,
            "email" to user.email,
            "username" to user.displayName
        )
        userRef.setValue(userMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "User data stored successfully")
            } else {
                Log.w(TAG, "Failed to store user data")
                Toast.makeText(baseContext, "Failed to store user data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToNotThereActivity() {
        val intent = Intent(this, NotThereActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToLanguageActivity() {
        val intent = Intent(this, LanguageActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    companion object {
        private const val TAG = "GoogleActivity"
        private const val RC_SIGN_IN = 9001
    }
}
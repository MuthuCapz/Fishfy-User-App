package com.example.seafishfy.ui.activities.fragments



import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth

import com.example.seafishfy.R
import com.example.seafishfy.databinding.FragmentProfileBinding
import com.example.seafishfy.ui.activities.LoginActivity
import com.example.seafishfy.ui.activities.models.UserModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleApiClient: GoogleApiClient
    private val database = FirebaseDatabase.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_Id))
            .requestEmail()
            .build()

        // Create a GoogleApiClient with access to the Google Sign-In API
        googleApiClient = GoogleApiClient.Builder(requireContext())
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build()

        // Connect to GoogleApiClient
        googleApiClient.connect()

        // Initialize UI and set click listeners
        binding.apply {
            profileName.isEnabled = false
            profileEmail.isEnabled = false
            profilePhoneNumber.isEnabled = false

            profileEditButton.setOnClickListener {
                profileName.isEnabled = !profileName.isEnabled
                profileEmail.isEnabled = !profileEmail.isEnabled
                profilePhoneNumber.isEnabled = !profilePhoneNumber.isEnabled
            }

            saveUserInformationButton.setOnClickListener {
                val name = profileName.text.toString().trim()
                val email = profileEmail.text.toString().trim()
                val phone = profilePhoneNumber.text.toString().trim()


                updateUserData(name,email,phone)
            }


            logout.setOnClickListener {
                // Call the sign out function
                signOut()
            }
        }

        setUserData()
        return binding.root
    }

    override fun onConnected(bundle: Bundle?) {
        // GoogleApiClient connected successfully
        val currentUser = auth.currentUser
        currentUser?.let {
            val displayName = it.displayName
            val photoUrl = it.photoUrl.toString()

            // Update UI with user information
            updateProfile(displayName, photoUrl)
        }
    }

    override fun onConnectionSuspended(i: Int) {
        // GoogleApiClient connection suspended
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        // GoogleApiClient connection failed
    }

    private fun signOut() {
        // Perform sign out
        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback {
            // Handle sign out result here

            // Navigate to LoginActivity after successful sign-out
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish() // Close the MainActivity or any other activity
        }
    }

    private fun updateUserData(name: String, email: String, phone: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = database.getReference("user").child(userId)
            val userData = hashMapOf(
                "name" to name,
                "email" to email,
                "phone" to phone,
            )

            userReference.setValue(userData).addOnSuccessListener {
                Toast.makeText(
                    requireContext(),
                    "Profile Update Successfully 😊",
                    Toast.LENGTH_SHORT
                ).show()
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Profile Update Failed 😒", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun setUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = database.getReference("user").child(userId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userProfile = snapshot.getValue(UserModel::class.java)
                        if (userProfile != null) {
                            binding.profileName.setText(userProfile.name)
                            binding.profileEmail.setText(userProfile.email)
                            binding.profilePhoneNumber.setText(userProfile.phone)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun updateProfile(displayName: String?, photoUrl: String?) {
        // Update username
        val displayNameTextView = requireView().findViewById<TextView>(R.id.profileName)
        displayNameTextView.text = displayName

        // Load the user's profile picture using Picasso
        val profileImageView = requireView().findViewById<ImageView>(R.id.profile_tv)
        Picasso.get().load(photoUrl).into(profileImageView)
    }

    companion object {
        fun newInstance(displayName: String?, photoUrl: String?): ProfileFragment {
            return ProfileFragment()
        }
    }
}

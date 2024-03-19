package com.example.seafishfy.ui.activities.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.seafishfy.databinding.FragmentProfileBinding
import com.example.seafishfy.ui.activities.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: FragmentProfileBinding
    private lateinit var userNameEditText: EditText
    private lateinit var userEmailEditText: EditText
    private lateinit var banner: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        binding.bannerName.text = user?.displayName
        userNameEditText = binding.profileName
        userEmailEditText = binding.profileEmail

        userNameEditText.setText(user?.displayName)
        userEmailEditText.setText(user?.email)
        binding.logout.setOnClickListener {
            signOut()
        }

        // Set onClickListener for logout text
        binding.text.setOnClickListener {
            signOut()
        }




        userNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Update Firebase user profile with new name
                val newName = s.toString()
                val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build()
                user?.updateProfile(userProfileChangeRequest)
            }
        })

        userEmailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Update Firebase user email if it's changed
                val newEmail = s.toString()
                if (newEmail != user?.email) {
                    user?.updateEmail(newEmail)
                }
            }
        })

        Glide.with(this)
            .load(user?.photoUrl)
            .into(binding.profileImage)
    }

    private fun signOut() {
        // Sign out from Firebase Authentication
        FirebaseAuth.getInstance().signOut()

        // Sign out from Google
        val googleSignInClient =
            GoogleSignIn.getClient(requireContext(), GoogleSignInOptions.DEFAULT_SIGN_IN)
        googleSignInClient.signOut().addOnCompleteListener {
            // Redirect to login activity
            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
    }
}
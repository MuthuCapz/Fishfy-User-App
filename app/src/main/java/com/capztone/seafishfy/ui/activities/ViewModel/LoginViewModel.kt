package com.capztone.seafishfy.ui.activities.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    fun signInWithGoogle(idToken: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        viewModelScope.launch {
            try {
                val result = auth.signInWithCredential(credential).await()
                if (result.user != null) {
                    onSuccess.invoke()
                } else {
                    onFailure.invoke("Authentication failed.")
                }
            } catch (e: Exception) {
                onFailure.invoke("Authentication failed: ${e.message}")
            }
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
}

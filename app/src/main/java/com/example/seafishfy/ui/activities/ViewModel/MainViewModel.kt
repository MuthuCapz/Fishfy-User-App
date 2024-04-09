// MainViewModel.kt
package com.example.seafishfy.ui.activities.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {

    // LiveData to hold the user's address
    private val _userAddress = MutableLiveData<String>()
    val userAddress: LiveData<String>
        get() = _userAddress

    // Function to update the user's address
    fun updateUserAddress(address: String) {
        _userAddress.value = address
    }

    // Add more ViewModel logic as needed
}

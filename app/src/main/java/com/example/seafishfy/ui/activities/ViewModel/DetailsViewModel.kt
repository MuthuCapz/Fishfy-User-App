package com.example.seafishfy.ui.activities.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DetailsViewModel : ViewModel() {
    private val _quantity = MutableLiveData<Int>()
    val quantity: LiveData<Int>
        get() = _quantity

    init {
        _quantity.value = 1
    }

    fun incrementQuantity() {
        _quantity.value = (_quantity.value ?: 1) + 1
    }

    fun decrementQuantity() {
        if ((_quantity.value ?: 1) > 1) {
            _quantity.value = (_quantity.value ?: 1) - 1
        }
    }
}

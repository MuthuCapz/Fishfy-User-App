package com.example.seafishfy.ui.activities.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.seafishfy.ui.activities.models.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ViewODViewModel : ViewModel() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private val _orderDetails = MutableLiveData<Order?>()
    val orderDetails: LiveData<Order?>
        get() = _orderDetails

    private val _orderImages = MutableLiveData<List<String>>()
    val orderImages: LiveData<List<String>>
        get() = _orderImages

    private val _orderCancellationStatus = MutableLiveData<Boolean>()
    val orderCancellationStatus: LiveData<Boolean>
        get() = _orderCancellationStatus

    fun fetchOrderDetails(orderId: String) {
        database = FirebaseDatabase.getInstance().reference.child("OrderDetails")
        database.child(orderId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val order = snapshot.getValue(Order::class.java)
                _orderDetails.value = order
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun fetchOrderImages(orderId: String) {
        database.child(orderId).child("foodImage").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val imageUrls = mutableListOf<String>()
                for (imageSnapshot in snapshot.children) {
                    val imageUrl = imageSnapshot.getValue(String::class.java)
                    imageUrl?.let {
                        imageUrls.add(it)
                    }
                }
                _orderImages.value = imageUrls
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    fun cancelOrder(orderId: String) {
        val cancellationMessage = "Order Cancelled"
        val orderCancellationRef = database.child(orderId).child("cancellationMessage")
        orderCancellationRef.setValue(cancellationMessage)
            .addOnSuccessListener {
                _orderCancellationStatus.value = true
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }
}

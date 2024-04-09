package com.example.seafishfy.ui.activities.ViewModel



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.seafishfy.ui.activities.models.DiscountItem
import com.google.firebase.database.*

class DiscountViewModel : ViewModel() {
    private lateinit var database: FirebaseDatabase
    private val _discountItems = MutableLiveData<List<DiscountItem>>()
    val discountItems: LiveData<List<DiscountItem>> = _discountItems

    fun retrieveDiscountItems() {
        database = FirebaseDatabase.getInstance()
        val discountRef: DatabaseReference = database.reference.child("discount")

        discountRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<DiscountItem>()
                for (discountSnapshot in snapshot.children) {
                    val discountItem = discountSnapshot.getValue(DiscountItem::class.java)
                    discountItem?.let {
                        items.add(it)
                    }
                }
                _discountItems.value = items
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}

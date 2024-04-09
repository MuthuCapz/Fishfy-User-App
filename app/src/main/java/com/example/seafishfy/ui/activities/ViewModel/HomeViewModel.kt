package com.example.seafishfy.ui.activities.ViewModel

// HomeViewModel.kt



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.seafishfy.ui.activities.models.DiscountItem
import com.example.seafishfy.ui.activities.models.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class HomeViewModel : ViewModel() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseReference: DatabaseReference = database.getReference("locations")

    private val _address = MutableLiveData<String?>()
    val address: MutableLiveData<String?>
        get() = _address

    private val _locality = MutableLiveData<String?>()
    val locality: MutableLiveData<String?>
        get() = _locality

    private val _popularItems = MutableLiveData<List<MenuItem>>()
    val popularItems: LiveData<List<MenuItem>>
        get() = _popularItems

    private val _menu1Items = MutableLiveData<List<MenuItem>>()
    val menu1Items: LiveData<List<MenuItem>>
        get() = _menu1Items

    private val _menu3Items = MutableLiveData<List<MenuItem>>()
    val menu3Items: LiveData<List<MenuItem>>
        get() = _menu3Items

    private val _discountItems = MutableLiveData<List<DiscountItem>>()
    val discountItems: LiveData<List<DiscountItem>>
        get() = _discountItems

    init {
        retrieveAddressAndLocality()
        retrievePopularItems()
        retrieveMenu1Items()
        retrieveMenu3Items()
        retrieveDiscountItems()
    }

    private fun retrieveAddressAndLocality() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val userLocationRef = databaseReference.child(uid)
            userLocationRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val address = snapshot.child("address").getValue(String::class.java)
                        val locality = snapshot.child("locality").getValue(String::class.java)

                        _address.value = address
                        _locality.value = locality
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun retrievePopularItems() {
        val foodRef: DatabaseReference = database.reference.child("menu")
        val menuItems = mutableListOf<MenuItem>()

        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (foodSnapshot in snapshot.children) {
                    val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                    menuItem?.let {
                        menuItems.add(it)
                    }
                }
                _popularItems.value = menuItems
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun retrieveMenu1Items() {
        val menu1Ref: DatabaseReference = database.reference.child("menu1")
        val menu1Items = mutableListOf<MenuItem>()

        menu1Ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (menu1Snapshot in snapshot.children) {
                    val menuItem = menu1Snapshot.getValue(MenuItem::class.java)
                    menuItem?.let {
                        menu1Items.add(it)
                    }
                }
                _menu1Items.value = menu1Items
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun retrieveMenu3Items() {
        val menu3Ref: DatabaseReference = database.reference.child("menu2")
        val menu3Items = mutableListOf<MenuItem>()

        menu3Ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (menu3Snapshot in snapshot.children) {
                    val menuItem = menu3Snapshot.getValue(MenuItem::class.java)
                    menuItem?.let {
                        menu3Items.add(it)
                    }
                }
                _menu3Items.value = menu3Items
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun retrieveDiscountItems() {
        val discountRef: DatabaseReference = database.reference.child("discount")
        val discountItems = mutableListOf<DiscountItem>()

        discountRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (discountSnapshot in snapshot.children) {
                    val menuItem = discountSnapshot.getValue(DiscountItem::class.java)
                    menuItem?.let {
                        discountItems.add(it)
                    }
                }
                _discountItems.value = discountItems
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }
}

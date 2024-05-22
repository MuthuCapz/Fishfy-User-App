package com.capztone.seafishfy.ui.activities.ViewModel



import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capztone.seafishfy.ui.activities.models.DiscountItem
import com.capztone.seafishfy.ui.activities.models.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ShopSixViewModel : ViewModel() {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val databaseReference: DatabaseReference = database.getReference("locations")

    private val _address = MutableLiveData<String?>()
    val address: LiveData<String?>
        get() = _address

    private val _locality = MutableLiveData<String?>()
    val locality: LiveData<String?>
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
        retrieveData()
    }

    private fun retrieveData() {
        viewModelScope.launch {
            retrieveAddressAndLocality()
            retrievePopularItems()
            retrieveMenu1Items()
            retrieveMenu3Items()
            retrieveDiscountItems()
        }
    }

    private suspend fun retrieveAddressAndLocality() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val userLocationRef = databaseReference.child(uid)
            try {
                val snapshot = userLocationRef.get().await()
                if (snapshot.exists()) {
                    val address = snapshot.child("address").getValue(String::class.java)
                    val locality = snapshot.child("locality").getValue(String::class.java)

                    _address.value = address
                    _locality.value = locality
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    private suspend fun retrievePopularItems() {
        val foodRef: DatabaseReference = database.reference.child("Shop 6").child("menu")

        val menuItems = mutableListOf<MenuItem>()
        try {
            val snapshot = foodRef.get().await()
            for (foodSnapshot in snapshot.children) {
                val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                menuItem?.let {
                    menuItems.add(it)
                }
            }

            _popularItems.value = menuItems
        } catch (e: Exception) {
            // Handle error
        }
    }

    private suspend fun retrieveMenu1Items() {
        val menu1Ref: DatabaseReference = database.reference.child("Shop 6").child("menu1")

        val menu1Items = mutableListOf<MenuItem>()
        try {
            val snapshot = menu1Ref.get().await()
            for (menu1Snapshot in snapshot.children) {
                val menuItem = menu1Snapshot.getValue(MenuItem::class.java)
                menuItem?.let {
                    menu1Items.add(it)
                }
            }

            _menu1Items.value = menu1Items
        } catch (e: Exception) {
            // Handle error
        }
    }

    private suspend fun retrieveMenu3Items() {
        val menu3Ref: DatabaseReference = database.reference.child("Shop 6").child("menu2")

        val menu3Items = mutableListOf<MenuItem>()
        try {
            val snapshot = menu3Ref.get().await()

            for (menu3Snapshot in snapshot.children) {
                val menuItem = menu3Snapshot.getValue(MenuItem::class.java)
                menuItem?.let {
                    menu3Items.add(it)
                }
            }

            _menu3Items.value = menu3Items
        } catch (e: Exception) {
            // Handle error
        }
    }

    private suspend fun retrieveDiscountItems() {
        val discountRef: DatabaseReference = database.reference.child("Shop 6").child("discount")
        val discountItems = mutableListOf<DiscountItem>()
        try {
            val snapshot = discountRef.get().await()
            for (discountSnapshot in snapshot.children) {
                val menuItem = discountSnapshot.getValue(DiscountItem::class.java)
                menuItem?.let {
                    discountItems.add(it)
                }
            }
            _discountItems.value = discountItems
        } catch (e: Exception) {
            // Handle error
        }
    }
}

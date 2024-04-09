package com.example.seafishfy.ui.activities.viewmodels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.seafishfy.ui.activities.models.MenuItem
import com.google.firebase.database.*

class SearchViewModel : ViewModel() {
    val menuItemsLiveData: MutableLiveData<List<MenuItem>> = MutableLiveData()

    private lateinit var database: FirebaseDatabase

    fun retrieveMenuItems() {
        val orignelMenuItems = mutableListOf<MenuItem>()
        database = FirebaseDatabase.getInstance()
        val foodReferencer1: DatabaseReference = database.reference.child("menu")
        val foodReferencer2: DatabaseReference = database.reference.child("menu1")
        val foodReferencer3: DatabaseReference = database.reference.child("menu2")

        val menuReferences = listOf(foodReferencer1, foodReferencer2, foodReferencer3)

        menuReferences.forEach { reference ->
            reference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (foodSnapshot in snapshot.children) {
                        val menuItem = foodSnapshot.getValue(MenuItem::class.java)
                        menuItem?.let {
                            orignelMenuItems.add(it)
                        }
                    }
                    menuItemsLiveData.postValue(orignelMenuItems)
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle onCancelled
                }
            })
        }
    }
}

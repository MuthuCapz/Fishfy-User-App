package com.capztone.fishfy.ui.activities

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.internal.LinkedTreeMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import timber.log.Timber



object RealTimeFireStoreHelper {
    private val gson = Gson() // Initialize Gson

    fun convertRealTimeToFireStore(exportedJson: JsonObject) {
        val coroutineCallConversion = CoroutineScope(Dispatchers.IO)
        coroutineCallConversion.launch {
            async {
                // Convert JsonObject to Map using Gson and cast it
                val map: Map<String, Any> = gson.fromJson(exportedJson, Map::class.java) as Map<String, Any>

                map.entries.forEach { entry ->
                    async {
                        val collection = Firebase.firestore.collection(entry.key)
                        var tempValue = entry.value

                        // Check if it.value is an instance of ArrayList
                        if (tempValue is ArrayList<*>) {
                            // Wrap ArrayList in a Map with a suitable key
                            val linkedTreeMap: LinkedTreeMap<String, Any> = LinkedTreeMap()
                            tempValue.forEachIndexed { index, item ->
                                linkedTreeMap[index.toString()] = item!!
                            }
                            tempValue = linkedTreeMap // Change tempValue to a map
                        }

                        // Ensure tempValue is of the expected type
                        if (tempValue is LinkedTreeMap<*, *>) {
                            (tempValue as LinkedTreeMap<String, Any>).forEach { subEntry ->
                                if (subEntry.value != null) {
                                    // Ensure the subEntry.value is a Map or POJO
                                    if (subEntry.value is Map<*, *>) {
                                        collection.document(subEntry.key as String).set(subEntry.value)
                                            .addOnSuccessListener {
                                                Timber.e("Conversion to Firestore succeeded!")
                                            }
                                            .addOnFailureListener {
                                                Timber.e("Conversion to Firestore failed!")
                                            }
                                    } else {
                                        Timber.e("Invalid data type for Firestore: ${subEntry.value.javaClass.simpleName}")
                                    }
                                }
                            }
                        } else {
                            Timber.e("Temp value is not a LinkedTreeMap: ${tempValue.javaClass.simpleName}")
                        }
                    }
                }
            }
        }
    }
}

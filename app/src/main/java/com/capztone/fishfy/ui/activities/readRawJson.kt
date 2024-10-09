package com.capztone.fishfy.ui.activities

import androidx.annotation.RawRes
import com.capztone.fishfy.ui.activities.fragments.CurrentLocationBottomSheet
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

inline fun <reified T> readRawJson(@RawRes rawResId: Int, context: CurrentLocationBottomSheet): T {
    val gson = Gson()
    context.resources.openRawResource(rawResId).use { inputStream ->
        InputStreamReader(inputStream).use { reader ->
            return gson.fromJson(reader, object : TypeToken<T>() {}.type)
        }
    }
}

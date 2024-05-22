package com.example.seafishfy.ui.activities.models

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable
import java.util.*

class OrderDetails() : Serializable, Parcelable {
    var userUid: String? = null
    var userName: String? = null
    var foodNames: MutableList<String>? = null
    var foodPrices: MutableList<String>? = null
    var foodImage: MutableList<String>? = null
    var foodQuantities: MutableList<Int>? = null
    var address: String? = null
    var phone: String? = null
    var orderAccepted: Boolean = true
    var paymentReceived: Boolean = true
    var itemPushKey: String? = null
    var currentTime: String? = null
    var paymentMethod: String? = null
    var adjustedTotalAmount: String? = null
    var orderDate: String? = null
    var ShopNames: MutableList<String>? = null

    constructor(parcel: Parcel) : this() {
        userUid = parcel.readString()
        userName = parcel.readString()
        address = parcel.readString()
        phone = parcel.readString()

        orderAccepted = parcel.readByte() != 0.toByte()
        paymentReceived = parcel.readByte() != 0.toByte()
        itemPushKey = parcel.readString()
        currentTime = parcel.readString()
        paymentMethod = parcel.readString()
        adjustedTotalAmount = parcel.readString()
        orderDate = parcel.readString()
        ShopNames = parcel.createStringArrayList()
    }

    constructor(
        userId: String,
        name: String,
        foodItemName: ArrayList<String>,
        foodItemPrice: ArrayList<String>,
        foodItemImage: ArrayList<String>,
        foodItemQuantities: ArrayList<Int>,
        address: String,
        phoneNumber: String,
        time: String,
        paymentMethod: String,
        adjustedTotalAmount: Int,
        itemPushKey: String?,
        orderDate: String, // New parameter for order date
        b: Boolean,
        b1: Boolean,
        pathContainer: MutableList<String>
    ) : this() {
        this.userUid = userId
        this.userName = name
        this.foodNames = foodItemName
        this.foodPrices = foodItemPrice
        this.foodImage = foodItemImage
        this.foodQuantities = foodItemQuantities
        this.address = address
        this.phone = phoneNumber
        this.currentTime = time
        this.paymentMethod = paymentMethod
        this.adjustedTotalAmount = adjustedTotalAmount.toString()
        this.itemPushKey = itemPushKey
        this.orderAccepted = orderAccepted
        this.paymentReceived = paymentReceived
        this.orderDate = orderDate // Set order date
        this.ShopNames = pathContainer

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userUid)
        parcel.writeString(userName)
        parcel.writeString(address)
        parcel.writeString(phone)
        parcel.writeByte(if (orderAccepted) 1 else 0)
        parcel.writeByte(if (paymentReceived) 1 else 0)
        parcel.writeString(itemPushKey)
        parcel.writeString(currentTime)
        parcel.writeString(paymentMethod)
        parcel.writeString(adjustedTotalAmount)
        parcel.writeString(orderDate) // Write order date
        parcel.writeStringList(ShopNames)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OrderDetails> {
        override fun createFromParcel(parcel: Parcel): OrderDetails {
            return OrderDetails(parcel)
        }

        override fun newArray(size: Int): Array<OrderDetails?> {
            return arrayOfNulls(size)
        }
    }
}
package com.capztone.fishfy.ui.activities.models

import android.os.Parcel
import android.os.Parcelable

class OrderDetails() : Parcelable {
    var userUid: String? = null

    var foodNames: MutableList<String>? = null
    var foodPrices: MutableList<String>? = null
    var foodImage: MutableList<String>? = null
    var foodQuantities: MutableList<Int>? = null
    var address: String? = null

    var orderAccepted: Boolean = true

    var itemPushKey: String? = null
    var currentTime: String? = null

    var adjustedTotalAmount: String? = null
    var orderDate: String? = null
    var ShopNames: MutableList<String>? = null
    var selectedSlot: String? = null
    var fooddescription: MutableList<String>? = null// Make sure it's mutable if needed

    constructor(parcel: Parcel) : this() {
        userUid = parcel.readString()
         address = parcel.readString()
         orderAccepted = parcel.readByte() != 0.toByte()

        itemPushKey = parcel.readString()
        currentTime = parcel.readString()

        adjustedTotalAmount = parcel.readString()
        orderDate = parcel.readString()
        ShopNames = parcel.createStringArrayList()
        selectedSlot = parcel.readString()
    // Read selectedSlot from parcel
    }

    constructor(
        userId: String?,
        foodItemName: MutableList<String>?,
        foodItemPrice: MutableList<String>?,
        foodItemImage: MutableList<String>?,
        foodItemQuantities: MutableList<Int>?,
        address: String?,
        time: String?,

        adjustedTotalAmount: Int,
        itemPushKey: String?,
        orderDate: String?,
        orderAccepted: Boolean,

        ShopNames: MutableList<String>?,
        selectedSlot: String?,
        foodItemDescription: MutableList<String>?
// Add selectedSlot to constructor
    ) : this() {
        this.userUid = userId
         this.foodNames = foodItemName
        this.foodPrices = foodItemPrice
        this.foodImage = foodItemImage
        this.foodQuantities = foodItemQuantities
        this.address = address
         this.currentTime = time

        this.adjustedTotalAmount = adjustedTotalAmount.toString()
        this.itemPushKey = itemPushKey
        this.orderDate = orderDate
        this.orderAccepted = orderAccepted

        this.ShopNames = ShopNames
        this.selectedSlot = selectedSlot
        this.fooddescription =  foodItemDescription
// Initialize selectedSlot in the constructor
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userUid)
         parcel.writeString(address)
         parcel.writeByte(if (orderAccepted) 1 else 0)

        parcel.writeString(itemPushKey)
        parcel.writeString(currentTime)

        parcel.writeString(adjustedTotalAmount)
        parcel.writeString(orderDate)
        parcel.writeStringList(ShopNames)
        parcel.writeString(selectedSlot) // Write selectedSlot to parcel
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

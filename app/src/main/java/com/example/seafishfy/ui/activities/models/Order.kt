package com.example.seafishfy.ui.activities.models


data class Order(

var userUid: String = "",
var itemPushKey: String = "",
var foodNames: List<String> = emptyList(),
var adjustedTotalAmount: String="",
var foodQuantities: List<Int> = emptyList(),
var foodImage: List<String> = emptyList(),
var orderDate: String = "",
val currentTime: String=""
)




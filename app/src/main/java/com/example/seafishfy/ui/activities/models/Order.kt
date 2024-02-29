package com.example.seafishfy.ui.activities.models


data class Order(

var userUid: String = "",
var itemPushKey: String = "",
var foodNames: List<String> = emptyList(),
var foodPrices: List<String> = emptyList(),
var foodQuantities: List<Int> = emptyList(),
var foodImage: List<String> = emptyList() // Assuming foodImage is a String representing image URLs
)




package com.example.sole8.models.api

data class CreateOrderRequest(
    val deliveryAddress: String,
    val phoneNumber: String
)
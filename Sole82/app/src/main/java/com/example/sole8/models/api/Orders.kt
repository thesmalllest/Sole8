package com.example.sole8.models.api

data class OrderCreatedResponse(
    val orderId: Int
)

data class OrderItemSimple(
    val id: Int,
    val totalPrice: Double,
    val status: String,
    val createdAt: String
)

data class OrderDetails(
    val id: Int,
    val userId: Int,
    val totalPrice: Double,
    val status: String,
    val createdAt: String,
    val deliveryAddress: String,
    val items: List<OrderItemDetails>
)

data class OrderItemDetails(
    val productId: Int,
    val productName: String,
    val sizeId: Int,
    val sizeValue: Double,
    val price: Double,
    val quantity: Int,
    val imageUrl: String
)

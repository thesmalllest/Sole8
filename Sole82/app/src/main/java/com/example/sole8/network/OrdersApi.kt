package com.example.sole8.network

import com.example.sole8.models.api.CreateOrderRequest
import com.example.sole8.models.api.OrderCreatedResponse
import com.example.sole8.models.api.OrderDetails
import com.example.sole8.models.api.OrderItemSimple
import retrofit2.http.*

interface OrdersApi {

    @POST("api/orders/create")
    suspend fun createOrder(@Body req: CreateOrderRequest): OrderCreatedResponse
    @GET("api/orders")
    suspend fun getOrders(): List<OrderItemSimple>

    @GET("api/orders/details/{orderId}")
    suspend fun getOrderDetails(@Path("orderId") orderId: Int): OrderDetails
}

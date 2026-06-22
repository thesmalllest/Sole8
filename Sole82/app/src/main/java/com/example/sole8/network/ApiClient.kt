package com.example.sole8.network

import android.content.Context
import com.example.sole8.UserPreferences
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "http://192.168.0.185:5295/"
    private lateinit var userPrefs: UserPreferences

    fun init(context: Context) {
        if (!::userPrefs.isInitialized) {
            userPrefs = UserPreferences(context.applicationContext)
        }
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()

                if (::userPrefs.isInitialized) {
                    // Извлекаем токен и принудительно очищаем его
                    val token = userPrefs.getToken()?.trim()

                    if (!token.isNullOrEmpty()) {
                        requestBuilder.removeHeader("Authorization")
                        requestBuilder.addHeader("Authorization", "Bearer $token")
                    }
                }

                chain.proceed(requestBuilder.build())
            }
            .addInterceptor(logging)
            .build()
    }

    private val retrofitInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Эндпоинты
    val userApi: UserApi get() = retrofitInstance.create(UserApi::class.java)
    val productsApi: ProductsApi get() = retrofitInstance.create(ProductsApi::class.java)
    val cartApi: CartApi get() = retrofitInstance.create(CartApi::class.java)
    val favoritesApi: FavoritesApi get() = retrofitInstance.create(FavoritesApi::class.java)
    val ordersApi: OrdersApi get() = retrofitInstance.create(OrdersApi::class.java)
}

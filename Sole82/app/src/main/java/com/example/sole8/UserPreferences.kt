package com.example.sole8

import android.content.Context
import com.example.sole8.models.api.UserProfileDto

class UserPreferences(context: Context) {

    private val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveUser(firstName: String, lastName: String, email: String, username: String) {
        prefs.edit()
            .putString("firstName", firstName)
            .putString("lastName", lastName)
            .putString("username", username)
            .putString("email", email)
            .apply()
    }

    fun getUser(): UserProfileDto {
        return UserProfileDto(
            id = prefs.getInt("userId", -1),
            firstName = prefs.getString("firstName", "") ?: "",
            lastName = prefs.getString("lastName", "") ?: "",
            birthDate = prefs.getString("birthDate", "") ?: "",
            gender = prefs.getString("gender", "") ?: "",
            username = prefs.getString("username", "") ?: "",
            email = prefs.getString("email", "") ?: ""
        )
    }

    fun setUserLoggedIn(value: Boolean) {
        prefs.edit().putBoolean("isLoggedIn", value).apply()
    }

    fun isUserLoggedIn(): Boolean {
        return prefs.getBoolean("isLoggedIn", false)
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun saveUserFromApi(user: UserProfileDto) {
        prefs.edit()
            .putInt("userId", user.id)
            .putString("firstName", user.firstName)
            .putString("lastName", user.lastName)
            .putString("birthDate", user.birthDate)
            .putString("gender", user.gender)
            .putString("username", user.username)
            .putString("email", user.email)
            .apply()
    }

    fun getUserId(): Int = prefs.getInt("userId", -1)

    // JWT ТОКЕН
    fun saveToken(token: String) {
        prefs.edit().putString("jwt_token", token).apply()
    }

    fun getToken(): String? {
         return prefs.getString("jwt_token", null)?.trim()
    }
}

package com.example.sole8

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.sole8.network.ApiClient
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class UserHubFragment : Fragment() {

    private lateinit var prefs: UserPreferences
    private lateinit var fullNameText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_user_hub, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = UserPreferences(requireContext())
        fullNameText = view.findViewById(R.id.hubUserFullName)

        // 1. Сначала  выводим локальные данные из кэша (UserPreferences)
        val cachedUser = prefs.getUser()
        displayFullName(cachedUser.firstName, cachedUser.lastName)

        // 2. Асинхронно делаем запрос на бэкенд, чтобы получить самую актуальную инфу
        lifecycleScope.launch {
            try {
                val fromApi = ApiClient.userApi.getProfile()

                prefs.saveUserFromApi(fromApi)

                displayFullName(fromApi.firstName, fromApi.lastName)

            } catch (_: Exception) {}
        }

        view.findViewById<MaterialButton>(R.id.btnPersonalInformation).setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.userFragmentContainer, UserInfoFragment())
                .addToBackStack(null)
                .commit()
        }

       view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnViewOrders).setOnClickListener {
            parentFragmentManager
                .beginTransaction()
                .replace(R.id.userFragmentContainer, OrdersListFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    // Вспомогательная функция для безопасного вывода имени
    private fun displayFullName(firstName: String, lastName: String) {
        if (firstName.isEmpty() && lastName.isEmpty()) {
            fullNameText.text = "Loading..."
        } else {
            fullNameText.text = "$firstName $lastName"
        }
    }
}

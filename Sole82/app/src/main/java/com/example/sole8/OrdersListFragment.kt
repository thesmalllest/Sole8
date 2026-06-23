package com.example.sole8

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sole8.adapters.OrdersAdapter
import com.example.sole8.network.ApiClient
import kotlinx.coroutines.launch

class OrdersListFragment : Fragment() {

    private lateinit var prefs: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_orders_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = UserPreferences(requireContext())

        val rvOrders = view.findViewById<RecyclerView>(R.id.rvOrders)
        val tvEmpty = view.findViewById<TextView>(R.id.tvOrdersEmpty)

        rvOrders.layoutManager = LinearLayoutManager(requireContext())

        lifecycleScope.launch {
            try {
                val orders = ApiClient.ordersApi.getOrders()

                if (orders.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                    rvOrders.visibility = View.GONE
                } else {
                    tvEmpty.visibility = View.GONE
                    rvOrders.visibility = View.VISIBLE
                    rvOrders.adapter = OrdersAdapter(orders)
                }

            } catch (e: Exception) {
                tvEmpty.text = getString(R.string.orders_load_error)
                tvEmpty.visibility = View.VISIBLE
                rvOrders.visibility = View.GONE
            }
        }
    }
}
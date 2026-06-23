package com.example.sole8.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sole8.R
import com.example.sole8.models.api.OrderItemSimple
import com.example.sole8.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrdersAdapter(private val orders: List<OrderItemSimple>) :
    RecyclerView.Adapter<OrdersAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderId: TextView = view.findViewById(R.id.tvOrderId)
        val status: TextView = view.findViewById(R.id.tvOrderStatus)
        val date: TextView = view.findViewById(R.id.tvOrderDate)
        val time: TextView = view.findViewById(R.id.tvOrderTime)
        val address: TextView = view.findViewById(R.id.tvOrderAddress)
        val total: TextView = view.findViewById(R.id.tvOrderTotal)
        val rvProductsHorizontal: RecyclerView = view.findViewById(R.id.rvOrderItemsHorizontal)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val order = orders[position]
        val context = holder.itemView.context

        holder.orderId.text = context.getString(R.string.order_title)
        holder.status.text = order.status

        val cleanDate = if (order.createdAt.contains("T")) {
            order.createdAt.substringBefore("T")
        } else {
            order.createdAt
        }

        holder.date.text = context.getString(R.string.order_date_format, cleanDate)

        val cleanTime = if (order.createdAt.contains("T")) {
            order.createdAt.substringAfter("T").take(5)
        } else {
            "--:--"
        }

        holder.time.text = context.getString(R.string.order_time_format, cleanTime)
        holder.total.text = context.getString(R.string.order_total_format, order.totalPrice.toInt())

        holder.rvProductsHorizontal.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val details = withContext(Dispatchers.IO) {
                    ApiClient.ordersApi.getOrderDetails(order.id)
                }

                val deliveryAddress = details.deliveryAddress ?: context.getString(R.string.order_address_not_specified)

                holder.address.text = context.getString(R.string.order_address_format, deliveryAddress)
                holder.rvProductsHorizontal.adapter = OrderProductsAdapter(details.items)

            } catch (e: Exception) {
                holder.address.text = context.getString(R.string.order_address_load_error)
                e.printStackTrace()
            }
        }
    }

    override fun getItemCount() = orders.size
}
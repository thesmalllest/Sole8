package com.example.sole8.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.sole8.R
import com.example.sole8.models.api.CartItemDto
import com.example.sole8.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CartAdapter(
    private var items: MutableList<CartItemDto>,
    private val onCartUpdated: () -> Unit
) : RecyclerView.Adapter<CartAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.findViewById(R.id.cartItemImage)
        val name: TextView = view.findViewById(R.id.cartItemName)
        val size: TextView = view.findViewById(R.id.cartItemSize)
        val price: TextView = view.findViewById(R.id.cartItemPrice)
        val minus: ImageView = view.findViewById(R.id.cartMinus)
        val plus: ImageView = view.findViewById(R.id.cartPlus)
        val qty: TextView = view.findViewById(R.id.cartQty)
        val remove: ImageView = view.findViewById(R.id.cartRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_cart, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.name.text = item.name
        holder.price.text = context.getString(R.string.cart_item_price_format, item.price.toInt())
        holder.size.text = context.getString(R.string.cart_item_size_format, formatSize(item.sizeValue))
        holder.qty.text = item.quantity.toString()

        holder.plus.isEnabled = item.quantity < item.stock
        holder.plus.alpha = if (item.quantity < item.stock) 1f else 0.4f

        Log.d("IMAGE_DEBUG", "URL = ${item.imageUrl}")

        Glide.with(context)
            .load(item.imageUrl)
            .placeholder(R.drawable.loading)
            .error(R.drawable.error_image)
            .into(holder.img)

        holder.minus.setOnClickListener {
            if (item.quantity > 1) {
                updateQuantity(item, item.quantity - 1, context)
            }
        }

        holder.plus.setOnClickListener {
            if (item.quantity >= item.stock) {
                Toast.makeText(
                    context,
                    context.getString(R.string.cart_only_stock_available, item.stock),
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            updateQuantity(item, item.quantity + 1, context)
        }

        holder.remove.setOnClickListener {
            val pos = holder.adapterPosition

            if (pos != RecyclerView.NO_POSITION) {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        ApiClient.cartApi.removeFromCart(
                            item.productId,
                            item.sizeId
                        )

                        items.removeAt(pos)
                        notifyItemRemoved(pos)
                        notifyItemRangeChanged(pos, items.size)
                        onCartUpdated()

                    } catch (e: Exception) {
                        Log.e("CART", "Remove error", e)

                        Toast.makeText(
                            context,
                            context.getString(R.string.cart_remove_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun updateQuantity(item: CartItemDto, newQty: Int, context: Context) {
        val pos = items.indexOf(item)
        if (pos == -1) return

        CoroutineScope(Dispatchers.Main).launch {
            try {
                ApiClient.cartApi.updateCart(
                    item.productId,
                    item.sizeId,
                    newQty
                )

                items[pos] = item.copy(quantity = newQty)
                notifyItemChanged(pos)
                onCartUpdated()

            } catch (e: Exception) {
                Log.e("CART", "Update error", e)

                Toast.makeText(
                    context,
                    context.getString(R.string.cart_not_enough_stock),
                    Toast.LENGTH_SHORT
                ).show()

                onCartUpdated()
            }
        }
    }

    private fun formatSize(size: Double): String {
        return if (size % 1.0 == 0.0) {
            size.toInt().toString()
        } else {
            size.toString()
        }
    }

    override fun getItemCount(): Int = items.size
}
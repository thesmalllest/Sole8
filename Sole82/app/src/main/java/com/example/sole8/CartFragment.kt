package com.example.sole8

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sole8.adapters.CartAdapter
import com.example.sole8.network.ApiClient
import com.example.sole8.models.api.CreateOrderRequest
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class CartFragment : Fragment() {

    private lateinit var prefs: UserPreferences
    private lateinit var emptyText: TextView
    private lateinit var recycler: RecyclerView
    private lateinit var clearBtn: Button
    private lateinit var totalText: TextView
    private lateinit var divider: View
    private lateinit var checkoutBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_cart, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefs = UserPreferences(requireContext())

        emptyText = view.findViewById(R.id.cartEmptyText)
        recycler = view.findViewById(R.id.cartRecycler)
        clearBtn = requireActivity().findViewById(R.id.clearCartBtn)
        totalText = view.findViewById(R.id.cartTotal)
        divider = view.findViewById(R.id.totalDivider)
        checkoutBtn = view.findViewById(R.id.checkoutBtn)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        clearBtn.setOnClickListener { clearCart() }
        checkoutBtn.setOnClickListener { showCheckoutDialog() }

        loadCart()
    }

    private fun loadCart() {
        lifecycleScope.launch {
            try {
                val items = ApiClient.cartApi.getCart()

                if (items.isEmpty()) {
                    toggleUi(false)
                    return@launch
                }

                toggleUi(true)

                recycler.adapter = CartAdapter(items.toMutableList()) {
                    loadCart()
                }

                val total = items.sumOf { it.price * it.quantity }
                totalText.text = "Total: ${total.toInt()} ₽"

            } catch (e: Exception) {
                toggleUi(false)
            }
        }
    }
    private fun clearCart() {
        lifecycleScope.launch {
            try {
                ApiClient.cartApi.clearCart()
                loadCart()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error clearing cart", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun showCheckoutDialog() {

        val dialog = BottomSheetDialog(requireContext())
        val dialogView = layoutInflater.inflate(R.layout.dialog_checkout, null)
        dialog.setContentView(dialogView)

        val etName = dialogView.findViewById<EditText>(R.id.etCheckoutName)
        val etPhone = dialogView.findViewById<EditText>(R.id.etCheckoutPhone)
        val etAddress = dialogView.findViewById<EditText>(R.id.etCheckoutAddress)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirmOrder)

        val user = prefs.getUser()
        etName.setText("${user.firstName} ${user.lastName}".trim())

        btnConfirm.setOnClickListener {

            val name = etName.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {

                    val req = CreateOrderRequest(deliveryAddress = address, phoneNumber = phone)

                    val response = ApiClient.ordersApi.createOrder(req)

                    Toast.makeText(
                        requireContext(),
                        "Order #${response.orderId} placed successfully!",
                        Toast.LENGTH_LONG
                    ).show()

                    ApiClient.cartApi.clearCart()
                    dialog.dismiss()
                    loadCart()

                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Checkout Error: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        dialog.show()
    }

    private fun toggleUi(visible: Boolean) {

        val v = if (visible) View.VISIBLE else View.GONE
        val inv = if (visible) View.GONE else View.VISIBLE

        emptyText.visibility = inv
        recycler.visibility = v
        clearBtn.visibility = v
        divider.visibility = v
        totalText.visibility = v
        checkoutBtn.visibility = v
    }
}
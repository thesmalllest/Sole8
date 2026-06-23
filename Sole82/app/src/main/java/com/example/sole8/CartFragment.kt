package com.example.sole8

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sole8.adapters.CartAdapter
import com.example.sole8.models.api.CreateOrderRequest
import com.example.sole8.network.ApiClient
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class CartFragment : Fragment() {

    private lateinit var prefs: UserPreferences
    private lateinit var emptyText: TextView
    private lateinit var recycler: RecyclerView
    private lateinit var clearBtn: TextView
    private lateinit var totalText: TextView
    private lateinit var divider: View
    private lateinit var checkoutBtn: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
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
        checkoutBtn.setOnClickListener { checkStockBeforeCheckout() }

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
                totalText.text = getString(R.string.cart_total_format, total.toInt())

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
                Toast.makeText(
                    requireContext(),
                    getString(R.string.cart_clear_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkStockBeforeCheckout() {
        lifecycleScope.launch {
            try {
                val items = ApiClient.cartApi.getCart()

                if (items.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.cart_empty),
                        Toast.LENGTH_SHORT
                    ).show()

                    loadCart()
                    return@launch
                }

                val unavailableItem = items.firstOrNull { it.quantity > it.stock }

                if (unavailableItem != null) {
                    Toast.makeText(
                        requireContext(),
                        getString(
                            R.string.cart_not_enough_stock_for_item,
                            unavailableItem.name,
                            unavailableItem.stock
                        ),
                        Toast.LENGTH_LONG
                    ).show()

                    loadCart()
                    return@launch
                }

                showCheckoutDialog()

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.cart_stock_check_error),
                    Toast.LENGTH_SHORT
                ).show()
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

        etPhone.inputType = InputType.TYPE_CLASS_PHONE

        val addresses = resources.getStringArray(R.array.checkout_addresses)

        etAddress.inputType = InputType.TYPE_NULL
        etAddress.isFocusable = false
        etAddress.isFocusableInTouchMode = false
        etAddress.isClickable = true

        etAddress.setOnClickListener {
            val listView = ListView(requireContext())

            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1,
                addresses
            )

            listView.adapter = adapter

            val popupWindow = PopupWindow(
                listView,
                etAddress.width,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

            popupWindow.isOutsideTouchable = true
            popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
            popupWindow.elevation = 8f

            listView.setOnItemClickListener { _, _, position, _ ->
                etAddress.setText(addresses[position])
                popupWindow.dismiss()
            }

            popupWindow.showAsDropDown(etAddress)
        }

        btnConfirm.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val name = etName.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty() || address.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.checkout_fill_all_fields),
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (!isValidPhone(phone)) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.checkout_invalid_phone),
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val req = CreateOrderRequest(
                        deliveryAddress = address,
                        phoneNumber = phone
                    )

                    ApiClient.ordersApi.createOrder(req)

                    Toast.makeText(
                        requireContext(),
                        getString(R.string.checkout_success),
                        Toast.LENGTH_LONG
                    ).show()

                    ApiClient.cartApi.clearCart()
                    dialog.dismiss()
                    loadCart()

                } catch (e: Exception) {
                    val errorText = e.localizedMessage ?: ""

                    Toast.makeText(
                        requireContext(),
                        getString(R.string.checkout_error_format, errorText),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        dialog.show()
    }

    private fun isValidPhone(phone: String): Boolean {
        val cleanedPhone = phone.replace("[\\s\\-()]+".toRegex(), "")
        return cleanedPhone.matches(Regex("^\\+[1-9]\\d{7,14}$"))
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
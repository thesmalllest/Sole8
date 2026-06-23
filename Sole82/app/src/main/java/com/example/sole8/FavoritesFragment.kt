package com.example.sole8

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sole8.adapters.ProductListAdapter
import com.example.sole8.network.ApiClient
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private lateinit var emptyLayout: View
    private lateinit var recycler: RecyclerView
    private lateinit var prefs: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        prefs = UserPreferences(requireContext())

        emptyLayout = view.findViewById(R.id.emptyLayout)
        recycler = view.findViewById(R.id.favRecycler)

        recycler.layoutManager = LinearLayoutManager(requireContext())

        loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun loadFavorites() {
        lifecycleScope.launch {
            try {
                val list = ApiClient.favoritesApi.getFavorites()

                if (list.isEmpty()) {
                    emptyLayout.visibility = View.VISIBLE
                    recycler.visibility = View.GONE
                } else {
                    emptyLayout.visibility = View.GONE
                    recycler.visibility = View.VISIBLE
                    recycler.adapter = ProductListAdapter(list)
                }

            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.favorites_loading_error),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
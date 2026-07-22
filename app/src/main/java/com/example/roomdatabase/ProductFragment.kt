package com.example.roomdatabase

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

import android.util.Log

class ProductFragment : Fragment() {

    private var rvProducts: RecyclerView? = null
    private var etSearch: EditText? = null
    private var shimmerView: ShimmerFrameLayout? = null
    private var allProducts: List<Product> = listOf()

    private val TAG = "ProductFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvProducts = view.findViewById(R.id.rvProducts)
        etSearch = view.findViewById(R.id.etSearch)
        shimmerView = view.findViewById(R.id.shimmerView)

        rvProducts?.layoutManager = GridLayoutManager(requireContext(), 2)

        setupSearch()
        fetchProducts()
    }

    private fun setupSearch() {
        etSearch?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterProducts(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterProducts(query: String) {
        val filteredList = if (query.isEmpty()) {
            allProducts
        } else {
            allProducts.filter {
                it.title.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault())) ||
                it.category.lowercase(Locale.getDefault()).contains(query.lowercase(Locale.getDefault()))
            }
        }
        rvProducts?.adapter = ProductAdapter(filteredList)
    }

    private fun fetchProducts() {
        Log.d(TAG, "fetchProducts: Started")
        shimmerView?.visibility = View.VISIBLE
        shimmerView?.startShimmer()
        rvProducts?.visibility = View.GONE
        
        val appContext = requireActivity().applicationContext

        lifecycleScope.launch {
            try {
                // 1. First, load from local Room database to show cached data instantly
                val db = AppDatabase.getDatabase(appContext)
                val productDao = db.productDao()
                
                val cachedProducts = withContext(Dispatchers.IO) { productDao.allProducts }
                if (cachedProducts.isNotEmpty()) {
                    allProducts = cachedProducts
                    rvProducts?.adapter = ProductAdapter(allProducts)
                    
                    // Hide shimmer if we have cached data
                    shimmerView?.stopShimmer()
                    shimmerView?.visibility = View.GONE
                    rvProducts?.visibility = View.VISIBLE
                }

                // 2. Fetch live data from API to refresh the list
                Log.d(TAG, "fetchProducts: Calling API")
                val apiService = ApiService.create()
                val remoteProducts = apiService.getProducts()
                
                if (remoteProducts.isNotEmpty()) {
                    Log.d(TAG, "fetchProducts: API Success, found ${remoteProducts.size} items")
                    
                    // 3. Update local Room database with fresh data
                    withContext(Dispatchers.IO) {
                        productDao.deleteAllProducts()
                        productDao.insertProducts(remoteProducts)
                        allProducts = productDao.allProducts
                    }
                    
                    // 4. Refresh UI with latest data
                    if (isAdded) {
                        rvProducts?.adapter = ProductAdapter(allProducts)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "fetchProducts: Exception: ${e.message}")
                if (allProducts.isEmpty() && isAdded) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } finally {
                // Final UI Cleanup
                if (isAdded) {
                    shimmerView?.stopShimmer()
                    shimmerView?.visibility = View.GONE
                    rvProducts?.visibility = View.VISIBLE
                    
                    if (allProducts.isEmpty()) {
                        Toast.makeText(requireContext(), "No products available", Toast.LENGTH_SHORT).show()
                    }
                }
                Log.d(TAG, "fetchProducts: Finished")
            }
        }
    }

    // Removed syncFromApi as it's now integrated above for better error handling and sequence
}
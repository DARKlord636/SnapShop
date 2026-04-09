package com.example.snapshop.ui.home

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.snapshop.utils.Resource
import com.example.snapshop.R
import com.example.snapshop.databinding.FragmentHomeBinding
import com.example.snapshop.ui.adapter.ProductAdapter

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val productViewModel: ProductViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeProducts()

        binding.swipeRefresh.setOnRefreshListener {
            productViewModel.fetchAllProducts()
        }
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter { product ->
            val action = HomeFragmentDirections
                .actionHomeFragmentToProductDetailFragment(product.productId)
            findNavController().navigate(action)
        }
        binding.rvProducts.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
        }
    }

    private fun observeProducts() {
        productViewModel.products.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvProducts.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    binding.rvProducts.visibility = View.VISIBLE
                    productAdapter.submitList(resource.data)
                    binding.tvEmpty.visibility =
                        if (resource.data.isEmpty()) View.VISIBLE else View.GONE
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        productViewModel.fetchAllProducts()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
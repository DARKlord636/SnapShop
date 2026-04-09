package com.example.snapshop.ui.detail

import com.example.snapshop.databinding.FragmentProductDetailBinding
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.snapshop.R
import com.example.snapshop.data.local.entity.FavoriteEntity

import com.example.snapshop.ui.adapter.ImageSliderAdapter
import com.example.snapshop.ui.favorites.FavoriteViewModel
import com.example.snapshop.ui.home.ProductViewModel
import com.example.snapshop.utils.Resource

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val productViewModel: ProductViewModel by viewModels()
    private val favoriteViewModel: FavoriteViewModel by viewModels()
    private val args: ProductDetailFragmentArgs by navArgs()
    private var isFavorite = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productViewModel.fetchProductById(args.productId)
        observeProduct()
        observeFavoriteStatus()
    }

    private fun observeProduct() {
        productViewModel.selectedProduct.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val product = resource.data
                    binding.tvTitle.text = product.title
                    binding.tvPrice.text = "₹${product.price}"
                    binding.tvDescription.text = product.description
                    binding.tvUploaderName.text = "Seller: ${product.uploaderName}"
                    binding.tvUploaderContact.text = "Contact: ${product.uploaderContact}"

                    val sliderAdapter = ImageSliderAdapter(product.imageUrls)
                    binding.viewPagerImages.adapter = sliderAdapter
                    binding.dotsIndicator.attachTo(binding.viewPagerImages)

                    binding.fabFavorite.setOnClickListener {
                        if (isFavorite) {
                            favoriteViewModel.removeFavoriteById(product.productId)
                            Toast.makeText(
                                requireContext(),
                                "Removed from favorites",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            favoriteViewModel.addFavorite(
                                FavoriteEntity(
                                    productId = product.productId,
                                    title = product.title,
                                    description = product.description,
                                    price = product.price,
                                    imageUrl = product.imageUrls.firstOrNull() ?: "",
                                    uploaderId = product.uploaderId,
                                    uploaderName = product.uploaderName,
                                    uploaderContact = product.uploaderContact
                                )
                            )
                            Toast.makeText(
                                requireContext(),
                                "Added to favorites",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun observeFavoriteStatus() {
        favoriteViewModel.isFavorite(args.productId).observe(viewLifecycleOwner) { fav ->
            isFavorite = fav
            binding.fabFavorite.setImageResource(
                if (fav) android.R.drawable.btn_star_big_on
                else android.R.drawable.btn_star_big_off
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
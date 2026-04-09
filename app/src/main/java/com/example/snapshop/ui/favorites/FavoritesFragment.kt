package com.example.snapshop.ui.favorites

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.snapshop.R
import com.example.snapshop.ui.adapter.FavoriteAdapter
import com.example.snapshop.databinding.FragmentFavoritesBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!
    private val favoriteViewModel: FavoriteViewModel by viewModels()
    private lateinit var favoriteAdapter: FavoriteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeFavorites()
    }

    private fun setupRecyclerView() {
        favoriteAdapter = FavoriteAdapter(
            onItemClick = { favorite ->
                val bundle = Bundle().apply {
                    putString("productId", favorite.productId)
                }
                findNavController().navigate(
                    R.id.action_favoritesFragment_to_productDetailFragment,
                    bundle
                )
            },
            onRemoveClick = { favorite ->
                favoriteViewModel.removeFavorite(favorite)
            }
        )
        binding.rvFavorites.adapter = favoriteAdapter
    }

    private fun observeFavorites() {
        favoriteViewModel.favorites.observe(viewLifecycleOwner) { favorites ->
            favoriteAdapter.submitList(favorites)
            binding.tvEmpty.visibility =
                if (favorites.isEmpty()) View.VISIBLE else View.GONE
            binding.rvFavorites.visibility =
                if (favorites.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

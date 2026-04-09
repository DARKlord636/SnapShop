package com.example.snapshop.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.snapshop.data.local.entity.FavoriteEntity

import com.example.snapshop.databinding.ItemFavoriteBinding



class FavoriteAdapter(
    private val onItemClick: (FavoriteEntity) -> Unit,
    private val onRemoveClick: (FavoriteEntity) -> Unit
) : ListAdapter<FavoriteEntity, FavoriteAdapter.FavoriteViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FavoriteViewHolder(
        private val binding: ItemFavoriteBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(favorite: FavoriteEntity) {
            binding.tvTitle.text = favorite.title
            binding.tvPrice.text = "₹${favorite.price}"
            binding.tvSeller.text = "Seller: ${favorite.uploaderName}"

            Glide.with(binding.root.context)
                .load(favorite.imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(binding.ivFavorite)

            binding.root.setOnClickListener { onItemClick(favorite) }
            binding.btnRemove.setOnClickListener { onRemoveClick(favorite) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<FavoriteEntity>() {
        override fun areItemsTheSame(oldItem: FavoriteEntity, newItem: FavoriteEntity) =
            oldItem.productId == newItem.productId
        override fun areContentsTheSame(oldItem: FavoriteEntity, newItem: FavoriteEntity) =
            oldItem == newItem
    }
}
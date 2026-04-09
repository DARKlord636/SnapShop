package com.example.snapshop.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import com.bumptech.glide.Glide
import com.example.snapshop.data.model.Product
import com.example.snapshop.databinding.ItemProductBinding

class ProductAdapter(
    private val onProductClick: (Product) -> Unit
) : ListAdapter<Product, ProductAdapter.ProductViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(
        private val binding: ItemProductBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: Product) {
            binding.tvTitle.text       = product.title
            binding.tvPrice.text       = "₹${product.price}"
            binding.tvDescription.text = product.shortDescription  // ✅ shows short desc on card

            Glide.with(binding.root.context)
                .load(product.imageUrls.firstOrNull())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .centerCrop()
                .into(binding.ivProduct)

            binding.root.setOnClickListener { onProductClick(product) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product) =
            oldItem.productId == newItem.productId
        override fun areContentsTheSame(oldItem: Product, newItem: Product) =
            oldItem == newItem
    }
}
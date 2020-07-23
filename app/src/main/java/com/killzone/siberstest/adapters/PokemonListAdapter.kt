package com.killzone.siberstest.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.killzone.siberstest.R
import com.killzone.siberstest.databinding.PokemonItemBinding
import com.killzone.siberstest.db.Pokemon

class PokemonListAdapter(val clickListener: PokemonListListener)
    : ListAdapter<Pokemon, PokemonListAdapter.ViewHolder>(CoordinatesDiffUtil()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // holder.bind(getItem(position), position)
        holder.bind(clickListener, getItem(position))
    }

    class ViewHolder private constructor(
        val binding: PokemonItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listener: PokemonListListener, item: Pokemon) {
            binding.pokemon = item
            binding.clickListener = listener

            val requestOptions = RequestOptions()
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_launcher_foreground)

            Glide.with(itemView.context)
                .applyDefaultRequestOptions(requestOptions)
                .load(item.imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .into(binding.ivPokemonImage)
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = PokemonItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class PokemonListListener(val clickListener: (p: Pokemon) -> Unit) {
    fun onClick(p: Pokemon) = clickListener(p)
}

class CoordinatesDiffUtil : DiffUtil.ItemCallback<Pokemon>() {
    override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
        return oldItem == newItem
    }
}
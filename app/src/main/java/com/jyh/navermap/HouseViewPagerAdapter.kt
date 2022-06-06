package com.jyh.navermap

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jyh.navermap.databinding.ItemHouseForViewpagerBinding

class HouseViewPagerAdapter(val itemClicked: (HouseModel)->Unit):ListAdapter<HouseModel, HouseViewPagerAdapter.ViewHolder>(diffUtil) {
    inner class ViewHolder(private val binding: ItemHouseForViewpagerBinding):RecyclerView.ViewHolder(binding.root){

        fun bind(house: HouseModel){
            binding.titleTextView.text = house.title
            binding.priceTextView.text = house.price

            binding.root.setOnClickListener {
                itemClicked(house)
            }

            Glide.with(binding.thumbnailImageView.context)
                .load(house.imgUrl)
                .into(binding.thumbnailImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHouseForViewpagerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    companion object {
        val diffUtil = object : DiffUtil.ItemCallback<HouseModel>() {
            override fun areItemsTheSame(oldItem: HouseModel, newItem: HouseModel): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: HouseModel, newItem: HouseModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
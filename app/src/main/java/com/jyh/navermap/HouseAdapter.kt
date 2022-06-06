package com.jyh.navermap

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.RoundedCorner
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.jyh.navermap.databinding.ItemHouseBinding

class HouseAdapter: ListAdapter<HouseModel, HouseAdapter.ViewHolder>(diffUtil) {
    inner class ViewHolder(private val binding: ItemHouseBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(house: HouseModel){
            binding.titleTextView.text = house.title
            binding.priceTextView.text = house.price

            Glide.with(binding.thumbnailImageView.context)
                .load(house.imgUrl)
                    // 3:2 비율을 조정해 줌,
                .transform(CenterCrop(), RoundedCorners(dpToPixel(binding.thumbnailImageView.context, 12)))
                .into(binding.thumbnailImageView)
        }
    }

    private fun dpToPixel(context: Context, dp:Int):Int{
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemHouseBinding.inflate(
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
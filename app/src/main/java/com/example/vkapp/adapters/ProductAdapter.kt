package com.example.vkapp.adapters

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.vkapp.R
import com.example.vkapp.databinding.ItemBinding
import com.example.vkapp.models.Product
import kotlin.math.roundToInt

class ProductAdapter : RecyclerView.Adapter<ProductAdapter.ViewHolder>() {

    private var productList = ArrayList<Product>()
    private var onClickListener: OnClickListenerProduct? = null


    fun setProductList(productList: List<Product>) {
        this.productList= productList as ArrayList<Product>
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val product = productList[position]
        val context = holder.itemView.context

        holder.binding.title.text = product.title
        holder.binding.description.text = product.description
        Glide.with(context)
            .load(product.thumbnail)
            .transform(CenterCrop(), RoundedCorners(dpToPx(5, context)))
            .into(holder.binding.thumbnail)

        holder.binding.rating.text = "${product.rating}"
        holder.binding.priceReal.text = context.getString(R.string.formatted_price, product.price)

        val discountedPrice = (product.price - (product.price * product.discountPercentage / 100)).toInt()
        holder.binding.priceSale.text = context.getString(R.string.formatted_price, discountedPrice)

        holder.binding.priceReal.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG

        holder.binding.itemRecyclerAll.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, product)
            }
        }
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    fun setOnClickListener(onClickListener: OnClickListenerProduct) {
        this.onClickListener = onClickListener
    }

    private fun dpToPx(dp: Int, context: Context): Int {
        val density = context.resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }
}
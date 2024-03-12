package com.example.vkapp.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.vkapp.R
import com.example.vkapp.databinding.ItemCategoryBinding

class CategoryAdapter : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private var categoryList = ArrayList<String>()
    private var onClickListener: OnClickListenerCategory? = null
    private var selectedCategory = "all"


    fun setCategoryList(categoryList: List<String>, selectedCategory: String) {
        this.categoryList = categoryList as ArrayList<String>
        if (categoryList[0]!= "all"){
            this.categoryList.add(0, "all")
        }
        this.selectedCategory = selectedCategory
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context))
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categoryList[position]
        val context = holder.itemView.context

        holder.binding.nameCategory.text = category

        if (category == selectedCategory) {
            holder.binding.nameCategory.setTextColor(ContextCompat.getColor(context, android.R.color.white))
            holder.binding.backLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.black))
        } else {
            holder.binding.nameCategory.setTextColor(ContextCompat.getColor(context, android.R.color.black))
            holder.binding.backLayout.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            holder.binding.backLayout.setBackgroundResource(R.drawable.back_category_card)
        }

        holder.binding.itemCategory.setOnClickListener {
            if (onClickListener != null) {
                val adapterPosition = holder.adapterPosition
                selectedCategory = categoryList[adapterPosition]
                notifyDataSetChanged()
                onClickListener!!.onClick(adapterPosition, category)
            }
        }
    }

    override fun getItemCount(): Int {
        return categoryList.size
    }

    fun setOnClickListener(onClickListener: OnClickListenerCategory) {
        this.onClickListener = onClickListener
    }
}
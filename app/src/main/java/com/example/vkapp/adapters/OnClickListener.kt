package com.example.vkapp.adapters

import com.example.vkapp.models.Product


interface OnClickListenerProduct {
    fun onClick(position: Int, model: Product)
}

interface OnClickListenerCategory {
    fun onClick(position: Int, model: String)
}
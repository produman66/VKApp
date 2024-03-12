package com.example.vkapp.retrofit

import com.example.vkapp.models.Product
import com.example.vkapp.models.Products
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface ProductApi {

    @GET("/products/{id}")
    fun getInformationAboutProduct(
        @Path("id") productId: Int
    ): Call<Product>


    @GET("products/categories")
    fun getAllCategories(): Call<List<String>>

    @GET("products/search")
    fun getSearchedProducts(
        @Query("q") query: String
    ): Call<Products>

    @GET("products{path}")
    fun getProductsByCategory(
        @Path(value = "path", encoded = true) path: String?,
        @Query("skip") skip: Int,
        @Query("limit") limit: Int
    ): Call<Products>

}
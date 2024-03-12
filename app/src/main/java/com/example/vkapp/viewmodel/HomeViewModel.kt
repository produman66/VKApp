package com.example.vkapp.viewmodel

import android.icu.text.StringSearch
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.vkapp.models.Product
import com.example.vkapp.models.Products
import com.example.vkapp.retrofit.Retrofit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.http.Query


class HomeViewModel : ViewModel() {

    private var productLiveData = MutableLiveData<List<Product>>()

    private var total = MutableLiveData<Int>()

    private var allCategoriesLiveData = MutableLiveData<List<String>>()

    private var infoProductLiveData = MutableLiveData<Product>()

    private var searchedProductLiveData = MutableLiveData<List<Product>>()

    private var loadingLiveData = MutableLiveData<Boolean>()
    private var errorHomeLiveData = MutableLiveData<Triple<Boolean, Boolean, String?>>(Triple(true, true, null))
    private var errorLiveData = MutableLiveData<Pair<Int, String?>>()

    fun getCategories(){

        loadingLiveData.value = true

        Retrofit.api.getAllCategories().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {


                if (response.isSuccessful) {
                    allCategoriesLiveData.value = response.body()
                    errorLiveData.value = Pair(response.code(), null)
                    errorHomeLiveData.value = Triple(false, errorHomeLiveData.value!!.second, null)
                    Log.i("json", "Cool: ${response.body()}")
                } else {
                    errorLiveData.value = Pair(response.code(), "Error loading data")
                    errorHomeLiveData.value = Triple(false, errorHomeLiveData.value!!.second, "Error loading data")
                    Log.e("json", "Error response: ${response.code()}, ${response.message()}")
                }
                loadingLiveData.value = false

            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Log.d("HomeViewModel", t.message.toString())
                loadingLiveData.value = false
                errorLiveData.value = Pair(0, t.message)
                errorHomeLiveData.value = Triple(false, errorHomeLiveData.value!!.second, t.message!!)
            }
        })
    }

    fun getProductsByCategory(skip:Int, category: String){

        val path = if (category != "all") "/category/$category" else ""

        loadingLiveData.value = true
        val limit = 20

        Retrofit.api.getProductsByCategory(path, skip, limit).enqueue(object : Callback<Products> {
            override fun onResponse(call: Call<Products>, response: Response<Products>) {

                if (response.isSuccessful) {
                    productLiveData.value = response.body()?.products
                    total.value = response.body()?.total
                    errorLiveData.value = Pair(response.code(), null)
                    errorHomeLiveData.value = Triple(errorHomeLiveData.value!!.first, false, null)
                    Log.i("json", "Cool: ${response.body()}")
                } else {
                    errorLiveData.value = Pair(response.code(), "Error loading data")
                    errorHomeLiveData.value = Triple(errorHomeLiveData.value!!.first, false, "Error loading data")
                    Log.e("json", "Error response: ${response.code()}, ${response.message()}")
                    Log.e("json", "$path")
                }

                loadingLiveData.value = false
            }

            override fun onFailure(call: Call<Products>, t: Throwable) {
                Log.d("HomeViewModel", t.message.toString())
                loadingLiveData.value = false
                errorLiveData.value = Pair(0, t.message)
                errorHomeLiveData.value = Triple(errorHomeLiveData.value!!.first, false, t.message)
            }
        })
    }

    fun getInfoAboutProduct(id:Int?){

        loadingLiveData.value = true

        Retrofit.api.getInformationAboutProduct(id!!).enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, response: Response<Product>) {

                loadingLiveData.value = false

                if (response.isSuccessful) {
                    infoProductLiveData.value = response.body()!!
                    errorLiveData.value = Pair(response.code(), null)
                    Log.i("json", "Cool: ${response.body()}")
                } else {
                    errorLiveData.value = Pair(response.code(), "Error loading data")
                    Log.e("json", "Error response: ${response.code()}, ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Product>, t: Throwable) {
                Log.d("HomeViewModel", t.message.toString())
                loadingLiveData.value = false
                errorLiveData.value = Pair(0, t.message)
            }
        })
    }


    fun getSearchProduct(searchQuery: String){

        loadingLiveData.value = true

        Retrofit.api.getSearchedProducts(searchQuery).enqueue(object : Callback<Products> {
            override fun onResponse(call: Call<Products>, response: Response<Products>) {


                if (response.isSuccessful) {
                    searchedProductLiveData.value = response.body()?.products
                    errorLiveData.value = Pair(response.code(), null)
                    Log.i("json", "Cool: ${response.body()}")
                } else {
                    errorLiveData.value = Pair(response.code(), "Error loading data")
                    Log.e("json", "Error response: ${response.code()}, ${response.message()}")
                }

                loadingLiveData.value = false
            }

            override fun onFailure(call: Call<Products>, t: Throwable) {
                Log.d("HomeViewModel", t.message.toString())
                loadingLiveData.value = false
                errorLiveData.value = Pair(0, t.message)
            }
        })
    }

    fun observeProductLiveData() : LiveData<List<Product>> {
        return productLiveData
    }

    fun observeCategoryLiveData() : LiveData<List<String>> {
        return allCategoriesLiveData
    }

    fun observeTotalLiveData(): LiveData<Int> {
        return total
    }

    fun observeInfoProductLiveData() : LiveData<Product> {
        return infoProductLiveData
    }

    fun observeErrorLiveData(): LiveData<Pair<Int, String?>> {
        return errorLiveData
    }

    fun observeHomeErrorLiveData(): LiveData<Triple<Boolean, Boolean, String?>> {
        return errorHomeLiveData
    }

    fun observeLoadingLiveData(): LiveData<Boolean> {
        return loadingLiveData
    }

    fun observeSearchedLiveData() : LiveData<List<Product>> {
        return searchedProductLiveData
    }


}
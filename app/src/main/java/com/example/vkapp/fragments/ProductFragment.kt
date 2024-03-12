package com.example.vkapp.fragments

import android.content.Context
import android.graphics.Paint
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.vkapp.R
import com.example.vkapp.databinding.FragmentProductBinding
import com.example.vkapp.viewmodel.HomeViewModel



class ProductFragment : Fragment() {

    private var _binding: FragmentProductBinding? = null
    private val bindingPf get() = _binding!!


    private val productViewModel: HomeViewModel by viewModels()
    private var productId = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        productId = arguments?.getString("productId").toString().toInt()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProductBinding.inflate(inflater, container, false)

        return bindingPf.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkInternet(bindingPf)

        bindingPf.btnFloating.setOnClickListener { findNavController().popBackStack() }

        bindingPf.btnRepeat.setOnClickListener { checkInternet(bindingPf) }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initViewModel(id: Int?) {
        productViewModel.getInfoAboutProduct(id)
        productViewModel.observeInfoProductLiveData()
            .observe(viewLifecycleOwner, Observer { product ->
                val imageList = ArrayList<SlideModel>()
                product.images.forEach {
                    imageList.add(SlideModel(it, ScaleTypes.CENTER_CROP))
                }

                bindingPf.imageSlider.setImageList(imageList)
                bindingPf.title.text = product.title
                bindingPf.priceReal.text = getString(R.string.formatted_price, product.price)
                val discountedPrice =
                    (product.price - (product.price * product.discountPercentage / 100)).toInt()
                bindingPf.priceSale.text = getString(R.string.formatted_price, discountedPrice)
                bindingPf.priceReal.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                bindingPf.rating.text = "${product.rating}"
                bindingPf.stock.text = "${product.stock}"
                bindingPf.sale.text = "${product.discountPercentage}"
                bindingPf.description.text = product.description
                bindingPf.brand.text = product.brand
                bindingPf.category.text = product.category
            })

        productViewModel.observeLoadingLiveData()
            .observe(viewLifecycleOwner, Observer { isLoading ->
                if (isLoading) {
                    bindingPf.imageError.visibility = View.GONE
                    bindingPf.messageError.visibility = View.GONE
                    bindingPf.btnRepeat.visibility = View.GONE
                    bindingPf.progressBar.visibility = View.VISIBLE
                } else {
                    bindingPf.progressBar.visibility = View.GONE
                }
            })

        productViewModel.observeErrorLiveData().observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                val (errorCode, errorMessage) = error
                if (errorMessage != null) {
                    showError(bindingPf, R.string.error_message_server.toString())
                } else {
                    showSuccess(bindingPf)
                }
            }
        })
    }


    private fun showError(binding: FragmentProductBinding, errorMessage: String) {
        binding.title.visibility = View.GONE
        binding.linearLayoutPrice.visibility = View.GONE
        binding.linearLayoutRating.visibility = View.GONE
        binding.linearLayoutStock.visibility = View.GONE
        binding.linearLayoutPercent.visibility = View.GONE
        binding.vector.visibility = View.GONE
        binding.descriptionLabel.visibility = View.GONE
        binding.description.visibility = View.GONE
        binding.brandLabel.visibility = View.GONE
        binding.brand.visibility = View.GONE
        binding.categoryLabel.visibility = View.GONE
        binding.category.visibility = View.GONE
        binding.btnFloating.visibility = View.GONE

        binding.imageError.visibility = View.VISIBLE
        binding.messageError.visibility = View.VISIBLE
        binding.messageError.text = errorMessage
        binding.btnRepeat.visibility = View.VISIBLE
    }

    private fun showSuccess(binding: FragmentProductBinding) {
        binding.title.visibility = View.VISIBLE
        binding.linearLayoutPrice.visibility = View.VISIBLE
        binding.linearLayoutRating.visibility = View.VISIBLE
        binding.linearLayoutStock.visibility = View.VISIBLE
        binding.linearLayoutPercent.visibility = View.VISIBLE
        binding.vector.visibility = View.VISIBLE
        binding.descriptionLabel.visibility = View.VISIBLE
        binding.description.visibility = View.VISIBLE
        binding.brandLabel.visibility = View.VISIBLE
        binding.brand.visibility = View.VISIBLE
        binding.categoryLabel.visibility = View.VISIBLE
        binding.category.visibility = View.VISIBLE
        binding.btnFloating.visibility = View.VISIBLE

        binding.imageError.visibility = View.GONE
        binding.messageError.visibility = View.GONE
        binding.btnRepeat.visibility = View.GONE
    }

    private fun checkInternet(binding: FragmentProductBinding) {
        if (isInternetAvailable(requireContext())) {
            initViewModel(productId)
        } else {
            showError(
                binding,
                "Произошла ошибка при загрузке данных,\nпроверьте подключение к сети!"
            )
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
}
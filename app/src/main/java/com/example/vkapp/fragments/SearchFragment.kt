package com.example.vkapp.fragments

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.vkapp.R
import com.example.vkapp.adapters.OnClickListenerProduct
import com.example.vkapp.adapters.ProductAdapter
import com.example.vkapp.databinding.FragmentSearchBinding
import com.example.vkapp.models.Product
import com.example.vkapp.viewmodel.HomeViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val bindingSf get() = _binding!!

    private val searchViewModel: HomeViewModel by viewModels()
    private val productAdapter = ProductAdapter()

    private val spanCount = 2
    private val marginSpan = 10
    private var searchJob: Job? = null
    private var searchEdit = "   "


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        return bindingSf.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        checkInternet(bindingSf)

        bindingSf.btnRepeat.setOnClickListener { checkInternet(bindingSf) }

        productAdapter.setOnClickListener(object : OnClickListenerProduct {
            override fun onClick(position: Int, model: Product) {
                val id = model.id.toString()
                val bundle = bundleOf("productId" to id)
                findNavController().navigate(R.id.action_searchFragment_to_productFragment, bundle)
            }
        })


        bindingSf.btnBack.setOnClickListener { findNavController().popBackStack() }


        bindingSf.editSearch.setOnEditorActionListener { _, actionId, event ->
            if (
                actionId == EditorInfo.IME_ACTION_DONE
                || (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                val nameProduct = bindingSf.editSearch.text.toString()
                initProductsViewModel(nameProduct)
                return@setOnEditorActionListener true
            }
            false
        }

        bindingSf.editSearch.addTextChangedListener { searchQuery ->
            searchJob?.cancel()
            searchJob = lifecycleScope.launch {
                delay(500)
                if (searchQuery.toString().isNotEmpty()){
                    initProductsViewModel(searchQuery.toString())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }





    private fun setupUI() {
        prepareRecyclerView()
        initProductsViewModel(searchEdit)
    }


    private fun prepareRecyclerView() {
        bindingSf.recAll.apply {
            layoutManager = GridLayoutManager(requireContext(), spanCount)
            adapter = productAdapter
        }
        bindingSf.recAll.addItemDecoration(SpaceItemDecoration(marginSpan))
    }


    private fun initProductsViewModel(nameProduct: String) {
        searchViewModel.getSearchProduct(nameProduct)
        searchViewModel.observeSearchedLiveData()
            .observe(viewLifecycleOwner, Observer { productList ->
                productAdapter.setProductList(productList)
            })

        searchViewModel.observeLoadingLiveData()
            .observe(viewLifecycleOwner, Observer { isLoading ->
                if (isLoading) {
                    bindingSf.imageError.visibility = View.GONE
                    bindingSf.messageError.visibility = View.GONE
                    bindingSf.btnRepeat.visibility = View.GONE
                    bindingSf.progressBar.visibility = View.VISIBLE
                } else {
                    bindingSf.progressBar.visibility = View.GONE
                }
            })

        searchViewModel.observeErrorLiveData().observe(viewLifecycleOwner, Observer { error ->
            if (error != null) {
                val (errorCode, errorMessage) = error
                if (errorMessage != null) {
                    showError(bindingSf, R.string.error_message_server.toString())
                } else {
                    showSuccess(bindingSf)
                }
            }
        })
    }


    private fun checkInternet(binding: FragmentSearchBinding) {
        if (isInternetAvailable(requireContext())) {
            setupUI()
        } else {
            showError(
                binding,
                R.string.error_message_network.toString()
            )
        }
    }


    private fun showError(binding: FragmentSearchBinding, errorMessage: String) {
        binding.editSearch.visibility = View.GONE
        binding.btnBack.visibility = View.GONE
        binding.recAll.visibility = View.GONE

        binding.imageError.visibility = View.VISIBLE
        binding.messageError.visibility = View.VISIBLE
        binding.messageError.text = errorMessage
        binding.btnRepeat.visibility = View.VISIBLE
    }

    private fun showSuccess(binding: FragmentSearchBinding) {
        binding.editSearch.visibility = View.VISIBLE
        binding.btnBack.visibility = View.VISIBLE
        binding.recAll.visibility = View.VISIBLE

        binding.imageError.visibility = View.GONE
        binding.messageError.visibility = View.GONE
        binding.btnRepeat.visibility = View.GONE
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
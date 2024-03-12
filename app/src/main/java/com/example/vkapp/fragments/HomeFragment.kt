package com.example.vkapp.fragments

import android.content.Context
import android.graphics.Rect
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vkapp.R
import com.example.vkapp.adapters.CategoryAdapter
import com.example.vkapp.adapters.OnClickListenerCategory
import com.example.vkapp.adapters.OnClickListenerProduct
import com.example.vkapp.adapters.ProductAdapter
import com.example.vkapp.databinding.FragmentHomeBinding
import com.example.vkapp.models.Product
import com.example.vkapp.viewmodel.HomeViewModel


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val bindingHf get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()
    private val productAdapter = ProductAdapter()
    private val categoryAdapter = CategoryAdapter()


    private var skippedProducts = 0
    private val skip = 20
    private var totalProducts = 0
    private var selectedCategory: String = "all"
    private val spanCount = 2
    private val marginSpan = 10


    companion object {
        private const val KEY_SKIP = "skip_key"
        private const val KEY_SELECTED_CATEGORY = "selected_category_key"
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SKIP, skippedProducts)
        outState.putString(KEY_SELECTED_CATEGORY, selectedCategory)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        restoreInstanceState(savedInstanceState)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        restoreInstanceState(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return bindingHf.root
    }


    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        checkInternet(bindingHf)

        bindingHf.btnRepeat.setOnClickListener { checkInternet(bindingHf) }

        bindingHf.imgSearch.setOnClickListener {
            findNavController().navigate(
                R.id.action_homeFragment_to_searchFragment
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            skippedProducts = it.getInt(KEY_SKIP, 0)
            selectedCategory =
                it.getString(KEY_SELECTED_CATEGORY, selectedCategory)
        }
    }


    private fun setupAction() {

        productAdapter.setOnClickListener(object : OnClickListenerProduct {
            override fun onClick(position: Int, model: Product) {
                val id = model.id.toString()
                val bundle = bundleOf("productId" to id)
                findNavController().navigate(R.id.action_homeFragment_to_productFragment, bundle)
            }
        })

        categoryAdapter.setOnClickListener(object : OnClickListenerCategory {
            override fun onClick(position: Int, model: String) {
                skippedProducts = 0
                selectedCategory = model
                initProductsViewModel(model)
                bindingHf.categoryTitle.text = model
            }
        })

        bindingHf.btnBack.setOnClickListener {
            if (skippedProducts != 0) {
                skippedProducts -= skip
                initViewModel(selectedCategory)
                bindingHf.nestedScroll.smoothScrollTo(0, 0)
            }
        }

        bindingHf.btnNext.setOnClickListener {
            if (skippedProducts + skip < totalProducts) {
                skippedProducts += skip
                initViewModel(selectedCategory)
                bindingHf.nestedScroll.smoothScrollTo(0, 0)
            }
        }

    }


    private fun setupUI() {
        prepareRecyclerView()
        initViewModel(selectedCategory)
        bindingHf.categoryTitle.text = selectedCategory
        setupAction()
    }


    private fun prepareRecyclerView() {
        listProductRecyclerView()
        listCategoryRecyclerView()
    }


    private fun listProductRecyclerView() {
        bindingHf.recAll.apply {
            layoutManager = GridLayoutManager(requireContext(), spanCount)
            adapter = productAdapter
        }
        bindingHf.recAll.addItemDecoration(SpaceItemDecoration(marginSpan))
    }


    private fun listCategoryRecyclerView() {
        bindingHf.recCategory.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
        }
    }


    private fun initViewModel(category: String) {
        initProductsViewModel(category)
        initCategoriesViewModel()


        homeViewModel.observeLoadingLiveData().observe(viewLifecycleOwner, Observer { isLoading ->
            if (isLoading) {
                bindingHf.imageError.visibility = View.GONE
                bindingHf.messageError.visibility = View.GONE
                bindingHf.btnRepeat.visibility = View.GONE
                bindingHf.progressBar.visibility = View.VISIBLE
            } else {
                bindingHf.progressBar.visibility = View.GONE
            }
        })

        homeViewModel.observeHomeErrorLiveData().observe(viewLifecycleOwner, Observer { error ->
            if (!error.first && !error.second) {
                val (_, _, errorMessage) = error
                if (errorMessage != null) {
                    showError(bindingHf, getString(R.string.error_message_server))
                } else {
                    showSuccess(bindingHf)
                }
            }
        })

    }

    private fun initProductsViewModel(category: String) {
        homeViewModel.getProductsByCategory(skippedProducts, category)
        homeViewModel.observeProductLiveData().observe(viewLifecycleOwner, Observer { productList ->
            productAdapter.setProductList(productList)
        })

        homeViewModel.observeTotalLiveData().observe(viewLifecycleOwner, Observer {
            totalProducts = it
        })
    }

    private fun initCategoriesViewModel() {
        homeViewModel.getCategories()
        homeViewModel.observeCategoryLiveData()
            .observe(viewLifecycleOwner, Observer { categoryList ->
                categoryAdapter.setCategoryList(categoryList, selectedCategory)
            })
    }

    private fun showError(binding: FragmentHomeBinding, errorMessage: String) {
        binding.recAll.visibility = View.GONE
        binding.recCategory.visibility = View.GONE
        binding.categoryTitle.visibility = View.GONE
        binding.imgSearch.visibility = View.GONE
        binding.btnNext.visibility = View.GONE
        binding.btnBack.visibility = View.GONE

        binding.imageError.visibility = View.VISIBLE
        binding.messageError.visibility = View.VISIBLE
        binding.messageError.text = errorMessage
        binding.btnRepeat.visibility = View.VISIBLE
    }

    private fun showSuccess(binding: FragmentHomeBinding) {
        binding.recAll.visibility = View.VISIBLE
        binding.recCategory.visibility = View.VISIBLE
        binding.categoryTitle.visibility = View.VISIBLE
        binding.imgSearch.visibility = View.VISIBLE
        binding.btnNext.visibility = View.VISIBLE
        binding.btnBack.visibility = View.VISIBLE

        binding.imageError.visibility = View.GONE
        binding.messageError.visibility = View.GONE
        binding.btnRepeat.visibility = View.GONE
    }


    private fun checkInternet(binding: FragmentHomeBinding) {
        if (isInternetAvailable(requireContext())) {
            setupUI()
        } else {
            showError(
                binding,
                getString(R.string.error_message_network)
            )
        }
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

class SpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.top = space
        outRect.bottom = space
        outRect.right = space
        outRect.left = space
    }
}
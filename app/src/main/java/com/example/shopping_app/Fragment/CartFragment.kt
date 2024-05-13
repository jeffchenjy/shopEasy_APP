package com.example.shopping_app.Fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.shopping_app.ApiHelper.ConnectToServerHelper
import com.example.shopping_app.ApiHelper.ConnectToServerHelper.Companion.clearAll
import com.example.shopping_app.ApiHelper.ItemInfoMap
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.cartMap
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.createCartList
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.itemIDMap
import com.example.shopping_app.ApiHelper.RegisterHelper
import com.example.shopping_app.CustomSnackbar
import com.example.shopping_app.FragmentShift
import com.example.shopping_app.FragmentShift.Companion.CART_CHECKOUT_FRAGMENT_SHIFT_TAG
import com.example.shopping_app.FragmentShift.Companion.CART_FRAGMENT_SHIFT_TAG
import com.example.shopping_app.ItemClickSupport
import com.example.shopping_app.Model
import com.example.shopping_app.NetworkUtils
import com.example.shopping_app.ProgressIndicatorAnimator
import com.example.shopping_app.R
import com.example.shopping_app.RecyclerViewHelper.CartRecyclerViewAdapter
import com.example.shopping_app.onItemClick
import com.google.android.material.progressindicator.LinearProgressIndicator

class CartFragment: Fragment(), CartRecyclerViewAdapter.OnDataChangedListener  {
    private lateinit var itemInfoMap: ItemInfoMap
    private lateinit var connectToServerHelper: ConnectToServerHelper
    private lateinit var fragmentShift: FragmentShift
    private lateinit var progressIndicatorAnimator: ProgressIndicatorAnimator

    private lateinit var toolbar: Toolbar
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var myCartRecyclerViewAdapter: CartRecyclerViewAdapter
    private var myCartList: MutableList<List<String>> = mutableListOf()
    /* SwipeRefreshLayout */
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    /* bottomIncludeLayout */
    private lateinit var includeLayout: View
    private lateinit var itemQuantity: TextView
    private lateinit var itemTotal: TextView
    private lateinit var goToCheckoutButton: Button
    /* includeLayout */
    private lateinit var includeLayoutNoData: View
    private lateinit var includeLayoutEmpty: View
    private lateinit var refreshButton: Button
    private lateinit var noDataTextView: TextView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentShift = FragmentShift()
        fragmentShift.setNavigationBarViewVisibility(
            requireActivity(),
            R.id.bottomNavigation,
            View.GONE
        )
        findView(view)
        setUpToolbar()
        init()
    }
    private fun findView(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        progressBar = view.findViewById(R.id.progressBar)
        cartRecyclerView = view.findViewById(R.id.cartRecyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        /* includeLayout */
        includeLayoutNoData = view.findViewById(R.id.includeLayoutNoData)
        includeLayoutEmpty = view.findViewById(R.id.includeLayoutEmpty)
        refreshButton = includeLayoutEmpty.findViewById(R.id.refreshButton)
        noDataTextView = includeLayoutNoData.findViewById(R.id.noDataTextView)
        /* bottomIncludeLayout */
        includeLayout = view.findViewById(R.id.includeLayout)
        itemQuantity = includeLayout.findViewById(R.id.itemQuantity)
        itemTotal = includeLayout.findViewById(R.id.itemTotal)
        goToCheckoutButton = includeLayout.findViewById(R.id.goToCheckoutButton)
    }
    private fun setUpToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.cart)
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_navigation_back)
        toolbar.contentInsetStartWithNavigation = 0
        toolbar.setNavigationOnClickListener {
            fragmentShift.returnBackStackFragment(requireActivity(), CART_FRAGMENT_SHIFT_TAG)
        }
    }
    private fun init() {
        itemInfoMap = ItemInfoMap()
        connectToServerHelper = ConnectToServerHelper()
        progressIndicatorAnimator = ProgressIndicatorAnimator()
        setViewVisibility()
        recyclerViewInit()
        getQuantityAndPrice()
        swipeSetOnRefresh()
        buttonOnClickListener()
        progressIndicatorAnimator.linearProgressIndicatorAnimator(cartRecyclerView, progressBar)
    }
    private fun setViewVisibility() {
        if(NetworkUtils.isNetworkOnline(requireContext())) {
            swipeRefreshLayout.isEnabled = true
            if(cartMap.isEmpty() && RegisterHelper.memberLoginFlag && ItemInfoMap.getDataFlag) {
                noDataTextView.text = getString(R.string.noData)
                includeLayoutNoData.visibility = View.VISIBLE
                includeLayoutEmpty.visibility = View.GONE
            } else if(!ItemInfoMap.getDataFlag) {
                includeLayoutNoData.visibility = View.GONE
                includeLayoutEmpty.visibility = View.VISIBLE
                swipeRefreshLayout.isEnabled = false
            } else if(!RegisterHelper.memberLoginFlag){
                noDataTextView.text = getString(R.string.noLogin)
                includeLayoutNoData.visibility = View.VISIBLE
                includeLayoutEmpty.visibility = View.GONE
            } else {
                includeLayoutNoData.visibility = View.GONE
                includeLayoutEmpty.visibility = View.GONE
            }
        } else {
            swipeRefreshLayout.isEnabled = false
            includeLayoutNoData.visibility = View.GONE
            includeLayoutEmpty.visibility = View.VISIBLE
            clearAll()
        }
    }
    private fun recyclerViewInit() {
        myCartList = createCartList()
        if(myCartList.isNotEmpty()) {
            cartRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            myCartRecyclerViewAdapter = CartRecyclerViewAdapter(requireView(), requireContext(), requireActivity(), myCartList)
            myCartRecyclerViewAdapter.setOnDataChangedListener(this)
            cartRecyclerView.adapter =  myCartRecyclerViewAdapter
            ItemClickSupport.addTo(cartRecyclerView)
            cartRecyclerView.onItemClick { recycler, position, v ->
                val itemList = (recycler.adapter as CartRecyclerViewAdapter).getItem(position)
                fragmentShift.changeToShowItemFragment(
                    ShowItemFragment(),
                    requireActivity(),
                    itemList[1],
                    itemList[2]
                )
            }
        }
    }
    private fun getQuantityAndPrice() {
        itemQuantity.text = null
        itemTotal.text = null
        var quantity = 0
        var total = 0
        if(cartMap.isNotEmpty()) {
            for(itemID in cartMap.keys) {
                quantity += cartMap[itemID]?.get(Model.quantity)?.toInt() ?: 0
                var price = cartMap[itemID]?.get(Model.cPrice)?.toInt() ?: 0
                price *=  quantity
                total += price
            }
            itemQuantity.text = quantity.toString()
            itemTotal.text = total.toString()
        }
    }
    private fun updateLayout() {
        setViewVisibility()
        recyclerViewInit()
        getQuantityAndPrice()
        fragmentShift.setNavigationBarViewCartBadge(requireActivity(), R.id.bottomNavigation)
    }
    private fun swipeSetOnRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
            swipeRefreshLayout.isRefreshing = false
        }
    }
    private fun refreshData() {
        view?.let { view ->
            connectToServerHelper.getCartData(view, requireContext()) {
                if (isAdded) {
                    updateLayout()
                }
            }
        }
    }
    private fun buttonOnClickListener() {
        /* get data */
        refreshButton.setOnClickListener{
            view?.let { view ->
                if(!ItemInfoMap.getDataFlag || !RegisterHelper.memberLoginFlag) {
                    connectToServerHelper.reloadData(view, requireContext()) {
                        if (isAdded) {
                            updateLayout()
                        }
                    }
                } else {
                    connectToServerHelper.getCartData(view, requireContext()) {
                        if (isAdded) {
                            updateLayout()
                        }
                    }
                }
            }
        }
        goToCheckoutButton.setOnClickListener{
            if(cartMap.isNotEmpty()) {
                fragmentShift.goToNextFragment(CartCheckoutFragment(),requireActivity(), CART_CHECKOUT_FRAGMENT_SHIFT_TAG,
                    CART_CHECKOUT_FRAGMENT_SHIFT_TAG)
            } else {
                CustomSnackbar.showSnackbar(view, requireContext(), getString(R.string.cartEmpty))
            }
        }
    }
    override fun onDataChanged() {
        if (isAdded) {
            updateLayout()
        }
    }

    override fun onResume() {
        super.onResume()
        if(!NetworkUtils.isNetworkOnline(requireContext())) {
            clearAll()
            updateLayout()
        }
    }
}
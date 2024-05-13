package com.example.shopping_app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.shopping_app.ApiHelper.ConnectToServerHelper
import com.example.shopping_app.ApiHelper.ItemInfoMap
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.memberOrders
import com.example.shopping_app.ApiHelper.RegisterHelper
import com.example.shopping_app.FragmentShift
import com.example.shopping_app.FragmentShift.Companion.ORDER_INFO_FRAGMENT_SHIFT_TAG
import com.example.shopping_app.ItemClickSupport
import com.example.shopping_app.NetworkUtils
import com.example.shopping_app.ProgressIndicatorAnimator
import com.example.shopping_app.R
import com.example.shopping_app.RecyclerViewHelper.OrderRecyclerViewAdapter
import com.example.shopping_app.onItemClick
import com.google.android.material.progressindicator.LinearProgressIndicator

class MemberOrderListFragment: Fragment(), OrderRecyclerViewAdapter.OnDataChangedListener {
    private lateinit var connectToServerHelper: ConnectToServerHelper
    private lateinit var fragmentShift: FragmentShift
    private lateinit var progressIndicatorAnimator: ProgressIndicatorAnimator
    private lateinit var myOrderRecyclerViewAdapter: OrderRecyclerViewAdapter

    private lateinit var toolbar: Toolbar
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var orderRecyclerView: RecyclerView
    private lateinit var includeLayoutNoData: View
    private lateinit var includeLayoutEmpty: View
    private lateinit var refreshButton: Button
    private lateinit var noDataTextView: TextView
    /* SwipeRefreshLayout */
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var myList: List<List<String>>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_member_order_list, container, false)
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
        orderRecyclerView = view.findViewById(R.id.orderRecyclerView)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        progressBar = view.findViewById(R.id.progressBar)
        /* include View */
        includeLayoutNoData = view.findViewById(R.id.includeLayoutNoData)
        includeLayoutEmpty = view.findViewById(R.id.includeLayoutEmpty)
        refreshButton = includeLayoutEmpty.findViewById(R.id.refreshButton)
        noDataTextView = includeLayoutNoData.findViewById(R.id.noDataTextView)
    }
    private fun setUpToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.orderHistory)
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_navigation_back)
        toolbar.contentInsetStartWithNavigation = 0
        toolbar.setNavigationOnClickListener {
            fragmentShift.returnBackStackFragment(requireActivity(),
                FragmentShift.MEMBER_ORDER_LIST_FRAGMENT_SHIFT_TAG
            )
        }
    }
    private fun init() {
        connectToServerHelper = ConnectToServerHelper()
        progressIndicatorAnimator = ProgressIndicatorAnimator()
        recyclerViewInit()
        setViewVisibility()
        swipeSetOnRefresh()
        buttonOnClickListener()
        progressIndicatorAnimator.linearProgressIndicatorAnimator(orderRecyclerView, progressBar)
    }
    private fun setViewVisibility() {
        if(NetworkUtils.isNetworkOnline(requireContext())) {
            swipeRefreshLayout.isEnabled = true
            if(memberOrders.isEmpty() && RegisterHelper.memberLoginFlag && ItemInfoMap.getDataFlag) {
                noDataTextView.text = getString(R.string.noOrder)
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
            ItemInfoMap.getDataFlag = false
            includeLayoutNoData.visibility = View.GONE
            includeLayoutEmpty.visibility = View.VISIBLE
            ConnectToServerHelper.clearAll()
        }
    }

    private fun recyclerViewInit() {
        myList = createListForRecyclerView()
        orderRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        myOrderRecyclerViewAdapter = OrderRecyclerViewAdapter(requireView(), requireContext(), myList)
        myOrderRecyclerViewAdapter.setOnDataChangedListener(this)
        orderRecyclerView.adapter = myOrderRecyclerViewAdapter
        ItemClickSupport.addTo(orderRecyclerView)
        orderRecyclerView.onItemClick { recycler, position, v ->
            val itemList = (recycler.adapter as OrderRecyclerViewAdapter).getItem(position)
            fragmentShift.changeToOrderInfoFragment(
                OrderInfoFragment(),
                requireActivity(),
                itemList[0]
            )
        }
    }

    private fun createListForRecyclerView(): List<List<String>> {
        val tempList: MutableList<List<String>> = mutableListOf()
        if(memberOrders.isNotEmpty()) {
            for (orderResponse in memberOrders) {
                val orderList = listOf(
                    orderResponse.id,
                    orderResponse.orderInfo.customer.name,
                    orderResponse.orderInfo.recipient.name,
                    orderResponse.orderInfo.createTime,
                    orderResponse.orderInfo.payment.totalPrice,
                )
                tempList.add(orderList)
            }
        }
        return tempList
    }
    private fun updateLayout() {
        // 更新布局
        setViewVisibility()
        recyclerViewInit()
        fragmentShift.setNavigationBarViewCartBadge(requireActivity(), R.id.bottomNavigation)
    }
    private fun swipeSetOnRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
            swipeRefreshLayout.isRefreshing = false
        }
    }
    private fun buttonOnClickListener() {
        /* get all data */
        refreshButton.setOnClickListener{
            view?.let { view ->
                connectToServerHelper.reloadData(view, requireContext()) {
                    if (isAdded) {
                        updateLayout()
                    }
                }
            }
        }
    }
    private fun refreshData() {
        view?.let { view ->
            if(!ItemInfoMap.getDataFlag || !RegisterHelper.memberLoginFlag) {
                connectToServerHelper.reloadData(view, requireContext()) {
                    if (isAdded) {
                        updateLayout()
                    }
                }
            } else {
                connectToServerHelper.getOrder(view, requireContext()) {
                    if (isAdded) {
                        updateLayout()
                    }
                }
            }

        }
    }

    override fun onDataChanged() {
        if (isAdded) {
            updateLayout()
        }
    }
}
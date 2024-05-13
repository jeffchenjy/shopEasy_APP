package com.example.shopping_app.Fragment

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.getDataFlag
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.itemIDMap
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.memberFavoriteMap
import com.example.shopping_app.ApiHelper.ConnectToServerHelper
import com.example.shopping_app.ApiHelper.ConnectToServerHelper.Companion.clearAll
import com.example.shopping_app.ApiHelper.RegisterHelper.Companion.memberLoginFlag
import com.example.shopping_app.Constants.DELAY_DURATION
import com.example.shopping_app.FragmentShift
import com.example.shopping_app.ItemClickSupport
import com.example.shopping_app.Model
import com.example.shopping_app.NetworkUtils
import com.example.shopping_app.ProgressIndicatorAnimator
import com.example.shopping_app.R
import com.example.shopping_app.RecyclerViewHelper.FavoriteRecyclerViewAdapter
import com.example.shopping_app.onItemClick
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.progressindicator.LinearProgressIndicator

class FavoriteFragment: Fragment(), FavoriteRecyclerViewAdapter.OnDataChangedListener {
    private lateinit var connectToServerHelper: ConnectToServerHelper
    private lateinit var myFavoriteRecyclerViewAdapter: FavoriteRecyclerViewAdapter
    private lateinit var fragmentShift: FragmentShift
    private lateinit var progressIndicatorAnimator: ProgressIndicatorAnimator
    private var myList: MutableList<List<String>> = mutableListOf()

    private lateinit var toolbar: Toolbar
    private lateinit var progressBar: LinearProgressIndicator
    private lateinit var favoriteRecyclerView: RecyclerView
    private lateinit var includeLayoutNoData: View
    private lateinit var includeLayoutEmpty: View
    private lateinit var refreshButton: Button
    private lateinit var noDataTextView: TextView
    /* SwipeRefreshLayout */
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentShift = FragmentShift()
        fragmentShift.setNavigationBarViewVisibility(
            requireActivity(),
            R.id.bottomNavigation,
            View.VISIBLE
        )
        findView(view)
        setUpToolbar()
        init()
    }
    private fun findView(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        favoriteRecyclerView = view.findViewById(R.id.favoriteRecyclerView)
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
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.favoriteTitle)
    }
    private fun init() {
        connectToServerHelper = ConnectToServerHelper()
        progressIndicatorAnimator = ProgressIndicatorAnimator()
        myRecyclerViewFunction()
        buttonOnClickListener()
        swipeSetOnRefresh()
        progressIndicatorAnimator.linearProgressIndicatorAnimator(favoriteRecyclerView, progressBar)
    }
    private fun myRecyclerViewFunction() {
            setViewVisibility()
        if(memberFavoriteMap.isNotEmpty()) {
            myList = createFavoriteList()
            recyclerViewInit()
        }
    }
    private fun setViewVisibility() {
        if(NetworkUtils.isNetworkOnline(requireContext())) {
            swipeRefreshLayout.isEnabled = true
            if(memberFavoriteMap.isEmpty() && memberLoginFlag && getDataFlag) {
                noDataTextView.text = getString(R.string.noData)
                includeLayoutNoData.visibility = View.VISIBLE
                includeLayoutEmpty.visibility = View.GONE
            } else if(!getDataFlag) {
                includeLayoutNoData.visibility = View.GONE
                includeLayoutEmpty.visibility = View.VISIBLE
                swipeRefreshLayout.isEnabled = false
            } else if(!memberLoginFlag){
                noDataTextView.text = getString(R.string.noLogin)
                includeLayoutNoData.visibility = View.VISIBLE
                includeLayoutEmpty.visibility = View.GONE
            } else {
                includeLayoutNoData.visibility = View.GONE
                includeLayoutEmpty.visibility = View.GONE
            }
        } else {
            swipeRefreshLayout.isEnabled = false
            getDataFlag = false
            includeLayoutNoData.visibility = View.GONE
            includeLayoutEmpty.visibility = View.VISIBLE
            clearAll()
        }
    }
    private fun recyclerViewInit() {
        favoriteRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        myFavoriteRecyclerViewAdapter = FavoriteRecyclerViewAdapter(requireView(), requireContext(), requireActivity(), myList)
        myFavoriteRecyclerViewAdapter.setOnDataChangedListener(this)
        favoriteRecyclerView.adapter = myFavoriteRecyclerViewAdapter
        ItemClickSupport.addTo(favoriteRecyclerView)
        favoriteRecyclerView.onItemClick { recycler, position, v ->
            val itemList = (recycler.adapter as FavoriteRecyclerViewAdapter).getItem(position)
            fragmentShift.changeToShowItemFragment(
                ShowItemFragment(),
                requireActivity(),
                itemList[1],
                itemList[2]
            )
        }
    }
    private fun createFavoriteList(): MutableList<List<String>> {
        val dataList: MutableList<List<String>> = mutableListOf()
        if(memberFavoriteMap.isNotEmpty()) {
            for (itemID in memberFavoriteMap.keys) {
                val newData = listOf(
                    itemIDMap[itemID]!![Model.cID] ?: "",
                    itemIDMap[itemID]!![Model.cName] ?: "",
                    itemIDMap[itemID]!![Model.cImage] ?: "",
                    itemIDMap[itemID]!![Model.cPrice] ?: "",
                    itemIDMap[itemID]!![Model.cInventory] ?: "",
                    memberFavoriteMap[itemID]!![Model.addedDate] ?: "",
                )
                dataList.add(newData)
            }
        }
        return dataList
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
    private fun swipeSetOnRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
            swipeRefreshLayout.isRefreshing = false
        }
    }
    private fun updateLayout() {
        // 更新布局
        myRecyclerViewFunction()
        fragmentShift.setNavigationBarViewCartBadge(requireActivity(), R.id.bottomNavigation)
    }
    private fun refreshData() {
        view?.let { view ->
            if(!getDataFlag || !memberLoginFlag) {
                connectToServerHelper.reloadData(view, requireContext()) {
                    if (isAdded) {
                        updateLayout()
                    }
                }
            } else {
                connectToServerHelper.getMemberFavorite(view, requireContext()) {
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
    override fun onResume() {
        super.onResume()
        val bottomNavigation = requireActivity().findViewById<NavigationBarView>(R.id.bottomNavigation)
        if(!bottomNavigation.menu.findItem(R.id.barCollection).isChecked) {
            bottomNavigation.menu.findItem(R.id.barCollection).setIcon(R.drawable.ic_collection)
            bottomNavigation.menu.findItem(R.id.barCart).setIcon(R.drawable.ic_cart_empty)
            bottomNavigation.menu.findItem(R.id.barCollection).isChecked = true
            bottomNavigation.menu.findItem(R.id.barCart).isChecked = false
            bottomNavigation.selectedItemId = R.id.barCollection
        }
        if(!NetworkUtils.isNetworkOnline(requireContext())) {
            clearAll()
            updateLayout()
        }
    }
}
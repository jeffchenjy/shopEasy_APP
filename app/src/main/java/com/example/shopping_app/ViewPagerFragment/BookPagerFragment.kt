package com.example.shopping_app

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.shopping_app.ApiHelper.ItemInfoMap
import com.example.shopping_app.ApiHelper.ConnectToServerHelper
import com.example.shopping_app.RecyclerViewHelper.MyRecyclerViewManager

class BookPagerFragment : Fragment() {
    private lateinit var connectToServerHelper: ConnectToServerHelper
    private lateinit var myRecyclerViewManager: MyRecyclerViewManager
    private lateinit var fragmentShift: FragmentShift
    private lateinit var includeLayout: View
    private lateinit var refreshButton: Button
    /* SwipeRefreshLayout */
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    /*  RecyclerView */
    private lateinit var itemRecyclerView: RecyclerView
    private lateinit var itemRecyclerView2: RecyclerView
    private lateinit var itemRecyclerView3: RecyclerView
    private lateinit var itemRecyclerViewText: TextView
    private lateinit var itemRecyclerView2Text: TextView
    private lateinit var itemRecyclerView3Text: TextView
    private var myListPair: MutableList<Pair<String, String>> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pager_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentShift = FragmentShift()
        findView(view)
        init()
    }
    private fun findView(view: View) {
        itemRecyclerView = view.findViewById(R.id.itemRecyclerView)
        itemRecyclerView2 = view.findViewById(R.id.itemRecyclerView2)
        itemRecyclerView3 = view.findViewById(R.id.itemRecyclerView3)
        itemRecyclerViewText = view.findViewById(R.id.itemRecyclerViewText)
        itemRecyclerView2Text = view.findViewById(R.id.itemRecyclerView2Text)
        itemRecyclerView3Text = view.findViewById(R.id.itemRecyclerView3Text)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        /* includeLayout */
        includeLayout = view.findViewById(R.id.includeLayout)
        refreshButton = includeLayout.findViewById(R.id.refreshButton)
    }
    private fun init() {
        includeLayout.visibility = View.GONE
        itemRecyclerViewText.text = getText(R.string.japaneseBook)
        itemRecyclerView2Text.text = getText(R.string.chineseBook)
        itemRecyclerView3Text.text = getText(R.string.magazine)
        connectToServerHelper = ConnectToServerHelper()
        myRecyclerViewManager = MyRecyclerViewManager()
        swipeSetOnRefresh()
        buttonOnClickListener()
        updateLayout()
    }
    private fun myRecyclerViewFunction() {

        myListPair = ItemInfoMap.checkClassKey("japanese book")
        myRecyclerViewManager.homeRecyclerViewSet(requireContext(), requireActivity(), itemRecyclerView, myListPair)

        myListPair = ItemInfoMap.checkClassKey("chinese book")
        myRecyclerViewManager.homeRecyclerViewSet(requireContext(), requireActivity(), itemRecyclerView2, myListPair)

        myListPair = ItemInfoMap.checkClassKey("magazine")
        myRecyclerViewManager.homeRecyclerViewSet(requireContext(), requireActivity(), itemRecyclerView3, myListPair)

    }
    private fun swipeSetOnRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            refreshData()
            swipeRefreshLayout.isRefreshing = false
        }
    }
    private fun updateLayout() {
        // 更新布局
        if(ItemInfoMap.getDataFlag) {
            swipeRefreshLayout.isEnabled = true
            includeLayout.visibility = View.GONE
            myRecyclerViewFunction()
        } else {
            swipeRefreshLayout.isEnabled = false
            includeLayout.visibility = View.VISIBLE
        }
        fragmentShift.setNavigationBarViewCartBadge(requireActivity(), R.id.bottomNavigation)
    }
    override fun onResume() {
        super.onResume()
        if(!NetworkUtils.isNetworkOnline(requireContext())) {
            ConnectToServerHelper.clearAll()
            updateLayout()
        }
    }
    private fun buttonOnClickListener() {
        refreshButton.setOnClickListener{
            refreshData()
        }
    }
    private fun refreshData() {
        view?.let { view ->
            connectToServerHelper.reloadData(view, requireContext()) {
                if (isAdded) {
                    updateLayout()
                }
            }
        }
    }
}
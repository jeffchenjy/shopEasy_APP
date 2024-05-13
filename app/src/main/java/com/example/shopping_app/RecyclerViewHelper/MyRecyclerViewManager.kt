package com.example.shopping_app.RecyclerViewHelper

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping_app.Fragment.ShowItemFragment
import com.example.shopping_app.FragmentShift
import com.example.shopping_app.HomeRecyclerViewAdapter
import com.example.shopping_app.ItemClickSupport
import com.example.shopping_app.onItemClick

class MyRecyclerViewManager {
    private lateinit var myHomeRecyclerViewAdapter: HomeRecyclerViewAdapter
    private val fragmentShift: FragmentShift = FragmentShift()
    fun homeRecyclerViewSet(context: Context, activity: FragmentActivity, recyclerView: RecyclerView, myListPair: MutableList<Pair<String, String>>) {
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        myHomeRecyclerViewAdapter = HomeRecyclerViewAdapter(myListPair)
        recyclerView.adapter = myHomeRecyclerViewAdapter
        ItemClickSupport.addTo(recyclerView)
        recyclerView.onItemClick { recycler, position, v ->
            val itemPair = (recycler.adapter as HomeRecyclerViewAdapter).getItem(position)
            fragmentShift.changeToShowItemFragment(
                ShowItemFragment(),
                activity,
                itemPair.first,
                itemPair.second
            )
        }
    }
}
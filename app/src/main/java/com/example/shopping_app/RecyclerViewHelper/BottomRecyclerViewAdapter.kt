package com.example.shopping_app.RecyclerViewHelper

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping_app.ItemClickSupportViewHolder
import com.example.shopping_app.R

class BottomRecyclerViewAdapter (private var dataList: List<String>) : RecyclerView.Adapter<BottomRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        ItemClickSupportViewHolder {
        //儲存 View 的 class
        val textView: TextView = itemView.findViewById(R.id.item_text)
        override val isLongClickable: Boolean get() = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //連結布局xml檔item。
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.recyclerview_bottom_sheet,
                    parent,
                    false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 因為 ViewHolder 會重複使用，
        // 我們要在這個 function 依據 position
        // 把正確的資料跟 ViewHolder 綁定在一起。
        holder.textView.text = dataList[position]
    }

    override fun getItemCount() = dataList.size

    fun addData(newData: String) {
        dataList += newData
        notifyDataSetChanged()
    }

    fun containsData(address: String): Boolean {
        return dataList.any { it.contains(address) }
    }

    fun clearData() {
        dataList = emptyList()
        notifyDataSetChanged()
    }
    fun getItem(position: Int): String {
        return dataList[position]
    }
}
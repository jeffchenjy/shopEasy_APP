package com.example.shopping_app.RecyclerViewHelper

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping_app.ApiHelper.ConnectToServerHelper
import com.example.shopping_app.ItemClickSupportViewHolder
import com.example.shopping_app.R

class OrderRecyclerViewAdapter(private var view: View, private var context: Context, private var dataList: List<List<String>>) : RecyclerView.Adapter<OrderRecyclerViewAdapter.ViewHolder>() {
    private val connectToServerHelper: ConnectToServerHelper = ConnectToServerHelper()
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        ItemClickSupportViewHolder {
        val order_id: TextView = itemView.findViewById(R.id.order_id)
        val customer_name: TextView = itemView.findViewById(R.id.customer_name)
        val recipient_name: TextView = itemView.findViewById(R.id.recipient_name)
        val added_date: TextView = itemView.findViewById(R.id.added_date)
        val order_price: TextView = itemView.findViewById(R.id.order_price)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
        override val isLongClickable: Boolean get() = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.recyclerview_order_list,
                    parent,
                    false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        val orderID = data[0]
        val customerName = data[1]
        val recipientName = data[2]
        val addedDate = data[3]
        val order_price = data[4]
        holder.order_id.text = context.getString(R.string.orderID) + orderID
        holder.customer_name.text = context.getString(R.string.customer) + " : " + customerName
        holder.recipient_name.text = context.getString(R.string.recipient) + " : " + recipientName
        holder.added_date.text = context.getString(R.string.orderDate) + addedDate
        holder.order_price.text = context.getString(R.string.orderPrice) + order_price

        holder.deleteButton.setOnClickListener{
            connectToServerHelper.deleteOrder(view, context, orderID) {
                onDataChangedListener?.onDataChanged()
            }
        }
    }

    override fun getItemCount() = dataList.size

    fun addData(newData: List<String>) {
        val convertedData = listOf(newData) // convert to list
        dataList += convertedData
        notifyDataSetChanged()
    }

    fun clearData() {
        dataList = emptyList()
        notifyDataSetChanged()
    }

    fun getItem(position: Int): List<String> {
        return dataList[position]
    }
    private var onDataChangedListener: OnDataChangedListener? = null

    interface OnDataChangedListener {
        fun onDataChanged()
    }

    fun setOnDataChangedListener(listener: OnDataChangedListener) {
        this.onDataChangedListener = listener
    }

}
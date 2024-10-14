package com.example.shopping_app.RecyclerViewHelper

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopping_app.ApiHelper.ConnectToServerHelper
import com.example.shopping_app.ApiHelper.ItemInfoMap
import com.example.shopping_app.FragmentShift
import com.example.shopping_app.ItemClickSupportViewHolder
import com.example.shopping_app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FavoriteRecyclerViewAdapter(private var basicView: View, private var context: Context, private var activity: FragmentActivity,
                                  private var dataList: List<List<String>>) : RecyclerView.Adapter<FavoriteRecyclerViewAdapter.ViewHolder>() {
    private val connectToServerHelper: ConnectToServerHelper = ConnectToServerHelper()
    private var fragmentShift: FragmentShift = FragmentShift()
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        ItemClickSupportViewHolder {
        val imageView: ImageView = itemView.findViewById(R.id.item_image)
        val item_name: TextView = itemView.findViewById(R.id.item_name)
        val item_price: TextView = itemView.findViewById(R.id.item_price)
        val item_inventory: TextView = itemView.findViewById(R.id.item_inventory)
        val item_addedDate: TextView = itemView.findViewById(R.id.item_addedDate)
        val deleteItemButton: Button = itemView.findViewById(R.id.deleteItemButton)
        val addCartButton: Button = itemView.findViewById(R.id.addCartButton)
        override val isLongClickable: Boolean get() = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.recyclerview_favorite,
                    parent,
                    false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = dataList[position]
        val itemID = data[0]
        val itemName = data[1]
        val imagePath = data[2]
        val itemPrice = data[3]
        val itemInventory = data[4]
        val itemAddedDate = data[5]
        holder.item_name.text = itemName
        holder.item_price.text = "JPY¥$itemPrice"
        val itemInventoryNumber = itemInventory.toInt()
        if (itemInventoryNumber == 0) {
            holder.item_inventory.text = context.getString(R.string.notInventory)
        } else {
            holder.item_inventory.text = context.getString(R.string.inventory)
        }
        holder.item_addedDate.text = context.getString(R.string.orderDate) + itemAddedDate
        //holder.item_inventory.text = "Inventory: $itemInventory"

        // 清除imageView上的圖片
        holder.imageView.setImageDrawable(null)
        // 使用 Glide 或其他圖片載入庫載入圖片
        Glide.with(holder.itemView.context)
            .load(imagePath)
            .placeholder(R.drawable.ic_error_outline) // 設置占位符，當圖片加載時顯示
            .error(R.drawable.ic_error_outline) // 設置加載錯誤時顯示的圖片
            .into(holder.imageView)
        // Set Button ClickListener
        holder.deleteItemButton.setOnClickListener {
            connectToServerHelper.deleteMemberLike(itemID) {
                onDataChangedListener?.onDataChanged()
            }
        }

        holder.addCartButton.setOnClickListener {
            if(!ItemInfoMap.checkCartList(basicView, context, itemID)) {
                /** Show progress indicators **/
                val builder = AlertDialog.Builder(context)
                val dialogView: View = activity.layoutInflater.inflate(R.layout.dialog_progress_indicators, null)
                builder.setView(dialogView)
                val dialog = builder.create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
                connectToServerHelper.addTOCartFunction(basicView, context,
                    itemID, itemName, itemPrice, 1){
                    dialog.dismiss()
                    fragmentShift.setNavigationBarViewCartBadge(activity, R.id.bottomNavigation)
                }
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
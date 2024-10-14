package com.example.shopping_app.RecyclerViewHelper


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopping_app.ItemClickSupportViewHolder
import com.example.shopping_app.R


class FinalCheckRecyclerViewAdapter(private var context: Context, private var dataList: List<List<String>>) : RecyclerView.Adapter<FinalCheckRecyclerViewAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        ItemClickSupportViewHolder {
        val imageView: ImageView = itemView.findViewById(R.id.item_image)
        val item_name: TextView = itemView.findViewById(R.id.item_name)
        val item_price: TextView = itemView.findViewById(R.id.item_price)
        val item_inventory: TextView = itemView.findViewById(R.id.item_inventory)
        val item_quantity: TextView = itemView.findViewById(R.id.item_quantity)
        val item_addedDate: TextView = itemView.findViewById(R.id.item_addedDate)
        override val isLongClickable: Boolean get() = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.recyclerview_cart_final_check,
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
        val itemQuantity = data[4]
        val itemAddedDate = data[5]
        val itemInventory = data[6]

        holder.item_name.text = itemName
        holder.item_price.text = "JPY¥ $itemPrice"
        val itemInventoryNumber = itemInventory.toInt()
        if (itemInventoryNumber == 0) {
            holder.item_inventory.text = context.getString(R.string.notInventory)
        } else {
            holder.item_inventory.text = context.getString(R.string.inventory)
        }
        holder.item_quantity.text = context.getString(R.string.itemNumber) + itemQuantity
        holder.item_addedDate.text = context.getString(R.string.orderDate) + itemAddedDate
        // 清除imageView上的圖片
        holder.imageView.setImageDrawable(null)
        // 使用 Glide 或其他圖片載入庫載入圖片
        Glide.with(holder.itemView.context)
            .load(imagePath)
            .placeholder(R.drawable.ic_error_outline) // 設置占位符，當圖片加載時顯示
            .error(R.drawable.ic_error_outline) // 設置加載錯誤時顯示的圖片
            .into(holder.imageView)
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

}
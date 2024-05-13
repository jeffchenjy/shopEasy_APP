package com.example.shopping_app

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.truncateString

class HomeRecyclerViewAdapter(private var dataList: List<Pair<String, String>>) : RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder>() {
    private val maxLength = 8
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), ItemClickSupportViewHolder {
        val textView: TextView = itemView.findViewById(R.id.item_text)
        val imageView: ImageView = itemView.findViewById(R.id.item_image)
        override val isLongClickable: Boolean get() = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.recyclerview_home,
                    parent,
                    false
                )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (text, imagePath) = dataList[position] // 取出文字和圖片路徑
        val truncatedText = truncateString(text, maxLength)
        holder.textView.text = truncatedText
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

    fun addData(newData: Pair<String, String>) {
        dataList += newData
        notifyDataSetChanged()
    }

    fun containsData(address: String): Boolean {
        return dataList.any { it.first.contains(address) }
    }

    fun clearData() {
        dataList = emptyList()
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Pair<String, String> {
        return dataList[position]
    }

}

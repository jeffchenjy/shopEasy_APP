package com.example.shopping_app.RecyclerViewHelper

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.shopping_app.ApiHelper.ConnectToServerHelper
import com.example.shopping_app.ApiHelper.ItemInfoMap
import com.example.shopping_app.ApiHelper.RegisterHelper
import com.example.shopping_app.CustomSnackbar
import com.example.shopping_app.ItemClickSupportViewHolder
import com.example.shopping_app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

class CartRecyclerViewAdapter(private var basicView: View, private var context: Context,  private var activity: FragmentActivity,
                              private var dataList: List<List<String>>) : RecyclerView.Adapter<CartRecyclerViewAdapter.ViewHolder>() {
    private val connectToServerHelper: ConnectToServerHelper = ConnectToServerHelper()
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        ItemClickSupportViewHolder {
        val imageView: ImageView = itemView.findViewById(R.id.item_image)
        val item_name: TextView = itemView.findViewById(R.id.item_name)
        val item_price: TextView = itemView.findViewById(R.id.item_price)
        val item_inventory: TextView = itemView.findViewById(R.id.item_inventory)
        val item_addedDate: TextView = itemView.findViewById(R.id.item_addedDate)
        val deleteItemButton: Button = itemView.findViewById(R.id.deleteItemButton)
        val addFavoriteButton: Button = itemView.findViewById(R.id.addFavoriteButton)
        val quantityTextInputLayout: TextInputLayout = itemView.findViewById(R.id.quantityTextInputLayout)
        val quantityEditText: EditText = itemView.findViewById(R.id.quantityEditText)
        override val isLongClickable: Boolean get() = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    R.layout.recyclerview_cart,
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
            holder.item_inventory.text = "在庫切れ"
        } else {
            holder.item_inventory.text = "在庫あり"
        }
        holder.item_addedDate.text = "追加日 : $itemAddedDate"
        var finalQuantity = 1
        holder.quantityEditText.setText(itemQuantity)
        holder.quantityEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No need to implement
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable?) {
                val charCount = s?.length ?: 0
                val quantity = s.toString()
                when {
                    s.isNullOrEmpty() -> {
                        holder.quantityTextInputLayout.error = null
                        holder.quantityEditText.error = "商品数を空にすることはできません"
                    }
                    RegisterHelper.containsNumberCharacter(quantity) -> {
                        holder.quantityTextInputLayout.error = "不正な文字が含まれています"
                        holder.quantityEditText.error = null
                    }
                    quantity == "0" -> {
                        holder.quantityTextInputLayout.error = "商品数をゼロにすることはできません"
                        finalQuantity = quantity.toInt()
                    }
                    quantity == itemQuantity -> {
                        holder.quantityTextInputLayout.error = null
                        finalQuantity = quantity.toInt()
                    }
                    else -> {
                        holder.quantityTextInputLayout.error = null
                        finalQuantity = quantity.toInt()
                    }
                }
            }
        })
        holder.quantityEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // 隐藏鍵盤與光標
                val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(holder.quantityEditText.windowToken, 0)
                holder.quantityEditText.clearFocus()
                /** Show progress indicators **/
                val builder = AlertDialog.Builder(context)
                val dialogView: View = activity.layoutInflater.inflate(R.layout.dialog_progress_indicators, null)
                builder.setView(dialogView)
                val dialog: AlertDialog = builder.create()
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.show()
                connectToServerHelper.addTOCartFunction(basicView, context, itemID, itemName, itemPrice, finalQuantity) {
                    onDataChangedListener?.onDataChanged()
                    dialog.dismiss()
                }
                true
            } else {
                false
            }
        }

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
            connectToServerHelper.deleteCartItem(itemID) {
                onDataChangedListener?.onDataChanged()
            }
        }
        holder.addFavoriteButton.setOnClickListener {
            if(itemID in ItemInfoMap.memberFavoriteMap.keys) {
                CustomSnackbar.showSnackbar(basicView, context, context.getString(R.string.alreadyIntoFavorite))
            } else {
                connectToServerHelper.addFavoriteMerchandise(basicView, context, itemID) {
                    CustomSnackbar.showSnackbar(basicView, context, context.getString(R.string.intoFavorite))
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

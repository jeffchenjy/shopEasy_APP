package com.example.shopping_app.ApiHelper

import android.content.Context
import android.view.View
import com.example.shopping_app.ApiHelper.RegisterHelper.Companion.memberLoginFlag
import com.example.shopping_app.CustomSnackbar
import com.example.shopping_app.Model
import com.example.shopping_app.R

class ItemInfoMap {
    companion object {
        var getDataFlag: Boolean = false

        // Item Information Map
        val itemClassMap: MutableMap<String, MutableList<Pair<String, String>>> = mutableMapOf()
        val itemInfoMap: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
        val itemIDMap: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
        val memberFavoriteMap: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
        val cartMap: MutableMap<String, MutableMap<String, String>> = mutableMapOf()
        var cartQuantity: Int = 0
        var memberOrders: List<Model.OrderResponse> = emptyList()
        // check Key
        fun checkClassKey(classKey: String) : MutableList<Pair<String, String>>{
            return if(itemClassMap.containsKey(classKey)) {
                itemClassMap[classKey]!!
            } else {
                mutableListOf()
            }
        }
        //cut String
        fun truncateString(text: String, maxLength: Int): String {
            return if (text.length >= maxLength) {
                text.substring(0, maxLength - 3) + "..."
            } else {
                text
            }
        }
        fun checkCartList(view: View, context: Context, itemID: String): Boolean {
            var checkCartFlag = false
            if(cartMap.isNotEmpty()) {
                for (key in ItemInfoMap.cartMap.keys) {
                    if(itemID == key) {
                        CustomSnackbar.showSnackbar(view, context, context.getString(R.string.alreadyInCart))
                        checkCartFlag = true
                        return checkCartFlag
                    }
                }
            }
            return checkCartFlag
        }
        fun getQuantity() {
            var quantity = 0
            if(cartMap.isNotEmpty()) {
                for(itemID in cartMap.keys) {
                    quantity += cartMap[itemID]?.get(Model.quantity)?.toInt() ?: 0
                }
                cartQuantity = quantity
            }
        }
        fun createCartList(): MutableList<List<String>> {
            val dataList: MutableList<List<String>> = mutableListOf()
            if(cartMap.isNotEmpty()) {
                for (itemID in cartMap.keys) {
                    val newData = listOf(
                        cartMap[itemID]!![Model.cID] ?: "",
                        cartMap[itemID]!![Model.cName] ?: "",
                        itemIDMap[itemID]!![Model.cImage] ?: "",
                        cartMap[itemID]!![Model.cPrice] ?: "",
                        cartMap[itemID]!![Model.quantity] ?: "",
                        cartMap[itemID]!![Model.addedDate] ?: "",
                        itemIDMap[itemID]!![Model.cInventory] ?: "",
                    )
                    dataList += newData
                }
            }
            return dataList
        }

    }
}

package com.example.shopping_app.ApiHelper

import android.content.Context
import android.view.View
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.cartMap
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.cartQuantity
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.getDataFlag
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.getQuantity
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.memberFavoriteMap
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.memberOrders
import com.example.shopping_app.ApiHelper.RegisterHelper.Companion.memberLoginFlag
import com.example.shopping_app.CustomSnackbar
import com.example.shopping_app.EncryptionUtils
import com.example.shopping_app.Model
import com.example.shopping_app.MyApiManager
import com.example.shopping_app.MyApiManager.token
import com.example.shopping_app.NetworkUtils
import com.example.shopping_app.R
import com.example.shopping_app.RequestCallback
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class ConnectToServerHelper {
    private val myApiService = MyApiManager.myApiService
    private lateinit var encryptionUtils: EncryptionUtils
    private lateinit var memberInfoHelper: MemberInfoHelper
    companion object {
        private var memberEmail: String? = null
        private var memberPassword: String? = null
        fun clearAll() {
            ItemInfoMap.itemClassMap.clear()
            ItemInfoMap.itemInfoMap.clear()
            ItemInfoMap.itemIDMap.clear()
            memberFavoriteMap.clear()
            cartMap.clear()
            cartQuantity = 0
            token = ""
            getDataFlag = false
            memberLoginFlag = false
        }
    }
    constructor()
    constructor(memberEmail: String?, memberPassword: String?){
        ConnectToServerHelper.memberEmail = memberEmail
        ConnectToServerHelper.memberPassword = memberPassword
    }
    private fun authVerification(view: View, context: Context, callback: (() -> Unit)?) {
        if(token ==  "") {
            memberLoginFlag = false
            val credentials = mapOf(
                "username" to MyApiManager.username,
                "password" to MyApiManager.password
            )
            val call = myApiService.login(credentials)
            call.enqueue(object : Callback<Model.TokenResponse> {
                override fun onResponse(call: Call<Model.TokenResponse>, response: Response<Model.TokenResponse>) {
                    if (response.isSuccessful) {
                        token = response.body()?.token //save token
                        callback?.invoke()
                    } else {
                        val responseStatusCode = response.code()
                        CustomSnackbar.showSnackbar(view, context, responseStatusCode.toString())
                        callback?.invoke()
                    }
                }
                override fun onFailure(call: Call<Model.TokenResponse>, t: Throwable) {
                    val message: String = when (t) {
                        is SocketTimeoutException -> context.getString(R.string.connectTimeOut)
                        is IOException -> context.getString(R.string.connectFailed)
                        else -> context.getString(R.string.serverError)
                    }
                    CustomSnackbar.showSnackbar(view, context, message)
                    call.cancel()
                    callback?.invoke()
                }
            })
        } else {
            callback?.invoke()
        }
    }
    private fun getItemApiData(token: String?, view: View, context: Context, callback: (() -> Unit)?) {
        val headers = HashMap<String, String>()
        headers["Authorization"] = "Token $token"
        myApiService.getItems(headers).enqueue(object : Callback<List<Model.Item>> {
            override fun onResponse(call: Call<List<Model.Item>>, response: Response<List<Model.Item>>) {
                if (response.isSuccessful) {
                    getDataFlag = true
                    ItemInfoMap.itemClassMap.clear()
                    ItemInfoMap.itemInfoMap.clear()
                    val items = response.body()
                    items?.forEach { item ->
                        insertData(
                            item.cID,
                            item.cSort,
                            item.cClass,
                            item.cName,
                            item.cImage,
                            item.cPrice,
                            item.cAuthor,
                            item.cCompany,
                            item.cDescription,
                            item.cInventory
                        )
                    }
                    callback?.invoke()
                } else {
                    // 請求失敗時的處理邏輯
                    ItemInfoMap.getDataFlag = false
                    val responseStatusCode = response.code()
                    CustomSnackbar.showSnackbar(view, context, responseStatusCode.toString())
                    callback?.invoke()
                }
            }
            override fun onFailure(call: Call<List<Model.Item>>, t: Throwable) {
                // 請求失敗時的處理邏輯
                ItemInfoMap.getDataFlag = false
                val message: String = when (t) {
                    is SocketTimeoutException -> context.getString(R.string.connectTimeOut)
                    is IOException -> context.getString(R.string.connectFailed)
                    else -> context.getString(R.string.serverError)
                }
                CustomSnackbar.showSnackbar(view, context, message)
                call.cancel()
                callback?.invoke()
            }
        })
    }
    private fun memberLogin(view: View, context: Context, callback: (() -> Unit)?) {
        memberInfoHelper = MemberInfoHelper()
        if(!memberLoginFlag) {
            if(memberEmail != null && memberPassword != null) {
                val requestMap = mapOf(
                    Model.cEmail to  memberEmail!!,
                    Model.cPassword to  memberPassword!!
                )
                myApiService.memberLogin(requestMap).enqueue(object : Callback<Model.MemberResponse> {
                    override fun onResponse(call: Call<Model.MemberResponse>, response: Response<Model.MemberResponse>) {
                        if (response.isSuccessful) {
                            //成功
                            MemberInfoHelper.memberInfoMap.clear()
                            val infoList = response.body()
                            if (infoList != null) {
                                memberInfoHelper.createMemberInfoMap(infoList.cName, infoList.cNickName, infoList.cEmail,
                                    infoList.cPhone, infoList.cAddr, infoList.cCountry, infoList.cBirthday, infoList.cSex, infoList.cImage)
                            }
                            memberLoginFlag = true
                            callback?.invoke()
                        } else {
                            //失敗
                            RegisterHelper.memberLoginFlag = false
                            val responseStatusCode = response.code()
                            CustomSnackbar.showSnackbar(view, context, responseStatusCode.toString())
                            callback?.invoke()
                        }
                    }
                    override fun onFailure(call: Call<Model.MemberResponse>, t: Throwable) {
                        RegisterHelper.memberLoginFlag = false
                        val message: String = when (t) {
                            is SocketTimeoutException -> context.getString(R.string.connectTimeOut)
                            is IOException -> context.getString(R.string.connectFailed)
                            else -> context.getString(R.string.serverError)
                        }
                        CustomSnackbar.showSnackbar(view, context, message)
                        call.cancel()
                        callback?.invoke()
                    }
                })
            } else {
                callback?.invoke()
            }
        } else {
            callback?.invoke()
        }
    }
    fun getMemberFavorite(view: View, context: Context, callback: (() -> Unit)?) {
        if (memberEmail != null) {
            val requestMap = mapOf(
                Model.cEmail to memberEmail!!
            )
            myApiService.getMemberFavor(requestMap).enqueue(object : Callback<Model.MemberFavorite> {
                override fun onResponse(call: Call<Model.MemberFavorite>, response: Response<Model.MemberFavorite>) {
                    if (response.isSuccessful) {
                        val memberFavorite = response.body()
                        memberFavoriteMap.clear()
                        memberFavorite?.favoriteMerchandiseList?.forEach { (key, merchandiseItem) ->
                            val itemMap: MutableMap<String, String> = mutableMapOf(
                                Model.cID to key,
                                Model.cName to merchandiseItem.name,
                                Model.addedDate to merchandiseItem.addedDate
                            )
                            memberFavoriteMap[key] = itemMap
                        }
                        callback?.invoke()
                    } else {
                        // 失敗時的處理邏輯
                        callback?.invoke()
                        val responseStatusCode = response.code()
                        //CustomSnackbar.showSnackbar(view, context, responseStatusCode.toString())
                    }
                }
                override fun onFailure(call: Call<Model.MemberFavorite>, t: Throwable) {
                    // 請求失敗時的處理邏輯
                    CustomSnackbar.showSnackbar(view, context, context.getString(R.string.serverError))
                    call.cancel()
                    callback?.invoke()
                }
            })
        } else {
            callback?.invoke()
        }
    }
    fun getCartData(view: View, context: Context, callback: (() -> Unit)?) {
        if (memberEmail != null) {
            val requestMap = mapOf(
                Model.cEmail to memberEmail!!
            )
            myApiService.getCart(requestMap).enqueue(object : Callback<Model.MemberCart> {
                override fun onResponse(call: Call<Model.MemberCart>, response: Response<Model.MemberCart>) {
                    if (response.isSuccessful) {
                        val memberCart = response.body()
                        /* Save memberCart Info into cartMap */
                        cartMap.clear()
                        memberCart?.merchandiseList?.forEach { (key, merchandiseItem) ->
                            val itemMap: MutableMap<String, String> = mutableMapOf(
                                Model.cID to key,
                                Model.cName to merchandiseItem.name,
                                Model.cPrice to merchandiseItem.price,
                                Model.quantity to merchandiseItem.quantity.toString(),
                                Model.addedDate to merchandiseItem.addedDate
                            )
                            //Log.d("itemMap", itemMap["name"].toString())
                            cartMap[key] = itemMap
                        }
                        getQuantity()
                        callback?.invoke()
                    } else {
                        // 失敗時的處理邏輯
                        callback?.invoke()
                    }
                }
                override fun onFailure(call: Call<Model.MemberCart>, t: Throwable) {
                    // 請求失敗時的處理邏輯
                    cartMap.clear()
                    cartQuantity = 0
                    call.cancel()
                    callback?.invoke()
                }
            })
        } else {
            callback?.invoke()
        }
    }

    fun insertData(itemID: Int, itemSort: String, itemClass: String, itemName: String, itemImageUrl: String,
                           itemPrice: Int, itemAuthor: String, itemCompany: String, itemDescription: String, itemInventory: Int) {
        /* 分類 */
        val pair = Pair(itemName, itemImageUrl)
        if (ItemInfoMap.itemClassMap.containsKey(itemClass)) {
            ItemInfoMap.itemClassMap[itemClass]!!.add(pair)
        } else {
            ItemInfoMap.itemClassMap[itemClass] = mutableListOf(pair)
        }
        /* Use Name as Map Key */
        /* ItemInfoMap */
        val newItem = mutableMapOf<String, String>()
        newItem[Model.cID] = itemID.toString()
        newItem[Model.cName] = itemName
        newItem[Model.cAuthor] = itemAuthor
        newItem[Model.cCompany] = itemCompany
        newItem[Model.cSort] = itemSort
        newItem[Model.cClass] = itemClass
        newItem[Model.cPrice] = itemPrice.toString()
        newItem[Model.cDescription] = itemDescription
        newItem[Model.cInventory] = itemInventory.toString()
        ItemInfoMap.itemInfoMap[itemName] = newItem

        /* Use ID as Map Key */
        val itemIDInnerMap = mutableMapOf<String, String>()
        itemIDInnerMap[Model.cID] = itemID.toString()
        itemIDInnerMap[Model.cName] = itemName
        itemIDInnerMap[Model.cImage] = itemImageUrl
        itemIDInnerMap[Model.cPrice] = itemPrice.toString()
        itemIDInnerMap[Model.cInventory] = itemInventory.toString()
        ItemInfoMap.itemIDMap[itemID.toString()] = itemIDInnerMap
    }

    private fun getMemberSharedPreferences(context: Context) {
        encryptionUtils = EncryptionUtils(context)
        if (encryptionUtils.containsKey(EncryptionUtils.SharedPreferencesEmailKey) && encryptionUtils.containsKey(
                EncryptionUtils.SharedPreferencesPasswordKey
            )) {
            if(encryptionUtils.getDecryptedData(EncryptionUtils.SharedPreferencesEmailKey) != null &&
                encryptionUtils.getDecryptedData(EncryptionUtils.SharedPreferencesPasswordKey) != null) {
                memberEmail = encryptionUtils.getDecryptedData(EncryptionUtils.SharedPreferencesEmailKey)!!
                memberPassword = encryptionUtils.getDecryptedData(EncryptionUtils.SharedPreferencesPasswordKey)!!
            }
        }
    }
    /** reloadData **/
    fun reloadData(view: View, context: Context, callback: (() -> Unit)?) {
        if(NetworkUtils.isNetworkOnline(context)) {
            getMemberSharedPreferences(context)
            authVerification(view, context) {
                getItemApiData(token,view, context) {
                    memberLogin(view, context, null)
                    getMemberFavorite(view, context, null)
                    getCartData(view, context) {
                        getOrder(view, context) {
                            callback?.invoke()
                        }
                    }
                }
            }
        } else {
            CustomSnackbar.showSnackbar(view, context, context.getString(R.string.connectFailed))
            callback?.invoke()
        }
    }
    fun addFavoriteMerchandise(view: View, context: Context, itemID: String, callback: (() -> Unit)?) {
        if(memberEmail != null) {
                // PrepareData
                val requestMap = mapOf(
                    Model.cEmail to memberEmail!!,
                    "merchandise_id" to itemID
                )
                myApiService.addFavorite(requestMap).enqueue(object : Callback<Model.MemberFavorite> {
                    override fun onResponse(call: Call<Model.MemberFavorite>, response: Response<Model.MemberFavorite>) {
                        if (response.isSuccessful) {
                            val memberFavorite = response.body()
                            memberFavoriteMap.clear()
                            memberFavorite?.favoriteMerchandiseList?.forEach { (key, merchandiseItem) ->
                                val itemMap: MutableMap<String, String> = mutableMapOf(
                                    Model.cID to key,
                                    Model.cName to merchandiseItem.name,
                                    Model.addedDate to merchandiseItem.addedDate
                                )
                                memberFavoriteMap[key] = itemMap
                            }
                            callback?.invoke()
                        } else {
                            // 失敗時的處理邏輯
                            callback?.invoke()
                        }
                    }
                    override fun onFailure(call: Call<Model.MemberFavorite>, t: Throwable) {
                        // 請求失敗時的處理邏輯
                        val message: String = when (t) {
                            is SocketTimeoutException -> context.getString(R.string.connectTimeOut)
                            is IOException -> context.getString(R.string.connectFailed)
                            else -> context.getString(R.string.serverError)
                        }
                        CustomSnackbar.showSnackbar(view, context, message)
                        call.cancel()
                        callback?.invoke()
                    }
                })
        } else {
            CustomSnackbar.showSnackbar(view, context, context.getString(R.string.noLogin))
            callback?.invoke()
        }
    }

    fun deleteMemberLike(merchandiseID: String, callback: (() -> Unit)?) {
        if (memberEmail != null) {
            val requestMap = mapOf(
                Model.cEmail to  memberEmail!!,
                "merchandiseID" to  merchandiseID
            )
            myApiService.deleteFavorite(requestMap).enqueue(object : Callback<Model.MemberFavorite> {
                override fun onResponse(call: Call<Model.MemberFavorite>, response: Response<Model.MemberFavorite>) {
                    if (response.isSuccessful) {
                        val memberFavorite = response.body()
                        memberFavoriteMap.clear()
                        memberFavorite?.favoriteMerchandiseList?.forEach { (key, merchandiseItem) ->
                            val itemMap: MutableMap<String, String> = mutableMapOf(
                                Model.cID to key,
                                Model.cName to merchandiseItem.name,
                                Model.addedDate to merchandiseItem.addedDate
                            )
                            memberFavoriteMap[key] = itemMap
                        }
                        callback?.invoke()
                    } else {
                        // 失敗時的處理邏輯
                        callback?.invoke()
                    }
                }
                override fun onFailure(call: Call<Model.MemberFavorite>, t: Throwable) {
                    // 請求失敗時的處理邏輯
                    call.cancel()
                    callback?.invoke()
                }
            })
        } else {
            callback?.invoke()
        }
    }

    fun addTOCartFunction(view: View, context: Context, merchandiseID: String?, merchandiseName: String?, merchandisePrice: String?, quantity: Int, callback: (() -> Unit)?) {
        if (memberEmail != null) {
            if( merchandiseID!=null && merchandiseName!=null && merchandisePrice!=null) {
                val requestMap = mapOf(
                    Model.cEmail to  memberEmail!!,
                    Model.cID to  merchandiseID,
                    Model.cName to merchandiseName,
                    Model.cPrice to merchandisePrice,
                    Model.quantity to quantity.toString()
                )
                myApiService.addToCart(requestMap).enqueue(object : Callback<Model.MemberCart> {
                    override fun onResponse(call: Call<Model.MemberCart>, response: Response<Model.MemberCart>) {
                        if (response.isSuccessful) {
                            val memberCart = response.body()
                            /* Save memberCart Info into cartMap */
                            memberCart?.merchandiseList?.forEach { (key, merchandiseItem) ->
                                val itemMap: MutableMap<String, String> = mutableMapOf(
                                    Model.cID to key,
                                    Model.cName to merchandiseItem.name,
                                    Model.cPrice to merchandiseItem.price,
                                    Model.quantity to merchandiseItem.quantity.toString(),
                                    Model.addedDate to merchandiseItem.addedDate
                                )
                                //Log.d("itemMap", itemMap["name"].toString())
                                cartMap[key] = itemMap
                            }
                            getQuantity()
                            callback?.invoke()
                        } else {
                            callback?.invoke()
                            // 失敗時的處理邏輯
                        }
                    }
                    override fun onFailure(call: Call<Model.MemberCart>, t: Throwable) {
                        // 請求失敗時的處理邏輯
                        val message: String = when (t) {
                            is SocketTimeoutException -> context.getString(R.string.connectTimeOut)
                            is IOException -> context.getString(R.string.connectFailed)
                            else -> context.getString(R.string.serverError)
                        }
                        CustomSnackbar.showSnackbar(view, context, message)
                        call.cancel()
                        callback?.invoke()
                    }
                })
            }
        } else {
            callback?.invoke()
        }
    }
    fun deleteCartItem(merchandiseID: String, callback: (() -> Unit)?) {
        if (memberEmail != null) {
            val requestMap = mapOf(
                Model.cEmail to  memberEmail!!,
                Model.cID to  merchandiseID
            )
            myApiService.deleteCartItem(requestMap).enqueue(object : Callback<Model.MemberCart> {
                override fun onResponse(call: Call<Model.MemberCart>, response: Response<Model.MemberCart>) {
                    if (response.isSuccessful) {
                        /* 更新手機中的資料 */
                        val memberCart = response.body()
                        /* Save memberCart Info into cartMap */
                        cartMap.clear()
                        memberCart?.merchandiseList?.forEach { (key, merchandiseItem) ->
                            val itemMap: MutableMap<String, String> = mutableMapOf(
                                Model.cID to key,
                                Model.cName to merchandiseItem.name,
                                Model.cPrice to merchandiseItem.price,
                                Model.quantity to merchandiseItem.quantity.toString(),
                                Model.addedDate to merchandiseItem.addedDate
                            )
                            //Log.d("itemMap", itemMap["name"].toString())
                            cartMap[key] = itemMap
                        }
                        getQuantity()
                        callback?.invoke()
                    } else {
                        // 失敗時的處理邏輯
                        cartMap.clear()
                        callback?.invoke()
                    }
                }
                override fun onFailure(call: Call<Model.MemberCart>, t: Throwable) {
                    // 請求失敗時的處理邏輯
                    call.cancel()
                    callback?.invoke()
                }
            })
        } else {
            callback?.invoke()
        }
    }
    fun getOrder(view: View, context: Context, callback: (() -> Unit)?) {
        if (memberEmail != null) {
            val requestMap = mapOf(
                Model.cEmail to  memberEmail!!,
            )
            myApiService.getOrder(requestMap).enqueue(object : Callback<List<Model.OrderResponse>> {
                override fun onResponse(call: Call<List<Model.OrderResponse>>, response: Response<List<Model.OrderResponse>>) {
                    if (response.isSuccessful) {
                        memberOrders = emptyList()
                        val orderResponses = response.body() ?: emptyList()
                        memberOrders = orderResponses
                        callback?.invoke()
                    } else {
                        // 失敗時的處理邏輯
                        memberOrders = emptyList()
                        callback?.invoke()
                    }
                }
                override fun onFailure(call: Call<List<Model.OrderResponse>>, t: Throwable) {
                    // 請求失敗時的處理邏輯
                    call.cancel()
                    callback?.invoke()
                }
            })
        } else {
            callback?.invoke()
        }
    }
    fun deleteOrder(view: View, context: Context, orderID: String, callback: (() -> Unit)?) {
        if (memberEmail != null) {
            val requestMap = mapOf(
                "orderID" to orderID,
                Model.cEmail to  memberEmail!!,
            )
            myApiService.deleteOrder(requestMap).enqueue(object : Callback<List<Model.OrderResponse>> {
                override fun onResponse(call: Call<List<Model.OrderResponse>>, response: Response<List<Model.OrderResponse>>) {
                    if (response.isSuccessful) {
                        CustomSnackbar.showSnackbar(view, context, "注文が正常にキャンセルされました")
                        memberOrders = emptyList()
                        val orderResponses = response.body() ?: emptyList()
                        memberOrders = orderResponses
                        callback?.invoke()
                    } else {
                        // 失敗時的處理邏輯
                        callback?.invoke()
                    }
                }
                override fun onFailure(call: Call<List<Model.OrderResponse>>, t: Throwable) {
                    // 請求失敗時的處理邏輯
                    call.cancel()
                    callback?.invoke()
                }
            })
        } else {
            callback?.invoke()
        }
    }

    fun changePassword(view: View, context: Context, email: String, password: String?, callback: RequestCallback) {
        var requestMap: Map<String, String> = emptyMap()
        if(password == null) {
            requestMap = mapOf(
                Model.cEmail to email
            )
            myApiService.changePassword(requestMap).enqueue(object : Callback<Model.ResponseMessage> {
                override fun onResponse(call: Call<Model.ResponseMessage>, response: Response<Model.ResponseMessage>) {
                    if (response.isSuccessful) {
                        callback.invoke(true)
                    } else {
                        // 失敗時的處理邏輯
                        callback.invoke(false)
                    }
                }
                override fun onFailure(call: Call<Model.ResponseMessage>, t: Throwable) {
                    // 請求失敗時的處理邏輯
                    val message: String = when (t) {
                        is SocketTimeoutException -> context.getString(R.string.connectTimeOut)
                        is IOException -> context.getString(R.string.connectFailed)
                        else -> context.getString(R.string.serverError)
                    }
                    CustomSnackbar.showSnackbar(view, context, message)
                    call.cancel()
                    callback.invoke(false)
                }
            })
        } else {
            requestMap = mapOf(
                Model.cEmail to email,
                Model.cPassword to password,
            )
            myApiService.changePassword(requestMap).enqueue(object : Callback<Model.ResponseMessage> {
                override fun onResponse(call: Call<Model.ResponseMessage>, response: Response<Model.ResponseMessage>) {
                    if (response.isSuccessful) {
                        callback.invoke(true)
                    } else {
                        // 失敗時的處理邏輯
                        val error = response.body()?.error
                        CustomSnackbar.showSnackbar(view, context, error.toString())
                        callback.invoke(false)
                    }
                }
                override fun onFailure(call: Call<Model.ResponseMessage>, t: Throwable) {
                    // 請求失敗時的處理邏輯
                    val message: String = when (t) {
                        is SocketTimeoutException -> context.getString(R.string.connectTimeOut)
                        is IOException -> context.getString(R.string.connectFailed)
                        else -> context.getString(R.string.serverError)
                    }
                    CustomSnackbar.showSnackbar(view, context, message)
                    call.cancel()
                    callback.invoke(false)
                }
            })
        }
    }
}


package com.example.shopping_app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping_app.ApiHelper.ItemInfoMap
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.cartMap
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.cartQuantity
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.createCartList
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.memberOrders
import com.example.shopping_app.CustomSnackbar
import com.example.shopping_app.FragmentShift
import com.example.shopping_app.FragmentShift.Companion.CART_FINAL_CHECK_FRAGMENT_SHIFT_TAG
import com.example.shopping_app.Model
import com.example.shopping_app.MyApiManager
import com.example.shopping_app.R
import com.example.shopping_app.RecyclerViewHelper.FinalCheckRecyclerViewAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException


class CartFinalCheckFragment: Fragment() {
    private val myApiService = MyApiManager.myApiService
    private lateinit var fragmentShift: FragmentShift
    private lateinit var itemInfoMap: ItemInfoMap
    private lateinit var toolbar: Toolbar
    /* TextView */
    private lateinit var finalShippingMethod: TextView
    private lateinit var finalPayment: TextView
    private lateinit var finalQuantityPrice: TextView
    private lateinit var finalName: TextView
    private lateinit var finalPhone: TextView
    private lateinit var finalEmail: TextView
    private lateinit var finalRecipient: TextView
    private lateinit var finalRecipientPhone: TextView
    private lateinit var finalRecipientEmail: TextView
    private lateinit var finalShippingAddress: TextView
    private lateinit var finalDeliveryTime: TextView
    /* Button */
    private lateinit var previousButton: Button
    private lateinit var checkButton: Button
    /* RecyclerView */
    private lateinit var finalItemRecyclerView: RecyclerView
    /* String */
    private var shippingMethod: String? = null
    private var payment: String? = null
    private var deliveryTime: String? = null
    private var customerName: String? = null
    private var customerPhone: String? = null
    private var customerEmail: String? = null
    private var recipientName: String? = null
    private var recipientPhone: String? = null
    private var recipientEmail: String? = null
    private var shippingAddress: String? = null
    private var totalQuantity: String? = null
    private var totalPrice: String? = null
    private var myCartList: MutableList<List<String>> = mutableListOf()
    private lateinit var myFinalCheckRecyclerViewAdapter: FinalCheckRecyclerViewAdapter
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        shippingMethod = arguments?.getString("shippingMethod")
        payment = arguments?.getString("payment")
        deliveryTime = arguments?.getString("deliveryTime")
        customerName = arguments?.getString("customerName")
        customerPhone = arguments?.getString("customerPhone")
        customerEmail = arguments?.getString("customerEmail")
        recipientName = arguments?.getString("recipientName")
        recipientPhone = arguments?.getString("recipientPhone")
        recipientEmail = arguments?.getString("recipientEmail")
        shippingAddress = arguments?.getString("shippingAddress")
        return inflater.inflate(R.layout.fragment_cart_final_check, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentShift = FragmentShift()
        itemInfoMap = ItemInfoMap()
        findView(view)
        setUpToolbar()
        init()
        buttonClickListener()
    }
    private fun findView(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        finalShippingMethod = view.findViewById(R.id.finalShippingMethod)
        finalPayment = view.findViewById(R.id.finalPayment)
        finalQuantityPrice = view.findViewById(R.id.finalQuantityPrice)
        finalName = view.findViewById(R.id.finalName)
        finalPhone = view.findViewById(R.id.finalPhone)
        finalEmail = view.findViewById(R.id.finalEmail)
        finalRecipient = view.findViewById(R.id.finalRecipient)
        finalRecipientPhone = view.findViewById(R.id.finalRecipientPhone)
        finalRecipientEmail = view.findViewById(R.id.finalRecipientEmail)
        finalShippingAddress = view.findViewById(R.id.finalShippingAddress)
        finalDeliveryTime = view.findViewById(R.id.finalDeliveryTime)
        previousButton = view.findViewById(R.id.previousButton)
        checkButton = view.findViewById(R.id.checkButton)
        finalItemRecyclerView = view.findViewById(R.id.finalItemRecyclerView)
    }
    private fun setUpToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.checkout)
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_navigation_back)
        toolbar.contentInsetStartWithNavigation = 0
        toolbar.setNavigationOnClickListener {
            fragmentShift.returnBackStackFragment(requireActivity(),
                CART_FINAL_CHECK_FRAGMENT_SHIFT_TAG
            )
        }
    }
    private fun init() {
        finalShippingMethod.text = shippingMethod
        finalPayment.text = payment
        finalName.text = customerName
        finalPhone.text = customerPhone
        finalEmail.text = customerEmail
        finalRecipient.text = recipientName
        finalRecipientPhone.text = recipientPhone
        finalRecipientEmail.text = recipientEmail
        finalShippingAddress.text = shippingAddress
        finalDeliveryTime.text = deliveryTime
        countQuantityAndPrice()

        finalQuantityPrice.text = totalQuantity + getString(R.string.orderInfoString) + totalPrice
        recyclerViewInit()
    }
    private fun countQuantityAndPrice() {
        var quantity = 0
        var total = 0
        if(cartMap.isNotEmpty()) {
            for(itemID in cartMap.keys) {
                quantity += cartMap[itemID]?.get(Model.quantity)?.toInt() ?: 0
                var price = cartMap[itemID]?.get(Model.cPrice)?.toInt() ?: 0
                price *=  quantity
                total += price
            }
            totalQuantity = quantity.toString()
            totalPrice = total.toString()
        }
        if(shippingMethod.equals(getString(R.string.homeDelivery))) {
            totalPrice = (totalPrice!!.toInt()+756).toString()
        } else if(shippingMethod.equals(getString(R.string.smallCourier))) {
            totalPrice = (totalPrice!!.toInt()+640).toString()
        } else if(shippingMethod.equals(getString(R.string.mailService))) {
            totalPrice = (totalPrice!!.toInt()+250).toString()
        }
    }
    private fun recyclerViewInit() {
        myCartList = createCartList()
        if(myCartList.isNotEmpty()) {
            finalItemRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            myFinalCheckRecyclerViewAdapter = FinalCheckRecyclerViewAdapter(requireContext(), myCartList)
            finalItemRecyclerView.adapter = myFinalCheckRecyclerViewAdapter
        }
    }
    private fun buttonClickListener() {
        previousButton.setOnClickListener(checkoutButtonClickListener())
        checkButton.setOnClickListener(checkoutButtonClickListener())
    }
    private fun checkoutButtonClickListener(): View.OnClickListener {
        return View.OnClickListener {
            val view = it
            val viewId = view?.id
            when(viewId) {
                R.id.previousButton -> {
                    fragmentShift.returnBackStackFragment(requireActivity(),
                        CART_FINAL_CHECK_FRAGMENT_SHIFT_TAG
                    )
                }
                R.id.checkButton -> {
                    orderSend{
                        fragmentShift.setNavigationBarViewCartBadge(requireActivity(), R.id.bottomNavigation)
                        val fragmentManager = requireActivity().supportFragmentManager
                        fragmentManager.popBackStack(null, POP_BACK_STACK_INCLUSIVE)
                    }
                }
            }
        }
    }
    private fun orderSend(callback: (() -> Unit)?) {
        //prepareData
        val requestMerchandiseMap: MutableList<List<String>> = mutableListOf()
        if (cartMap.isNotEmpty()) {
            for ((itemID, itemInfo) in cartMap) {
                val infoMap: List<String> = listOf(
                    itemInfo[Model.cID].toString(),
                    itemInfo[Model.cName].toString(),
                    itemInfo[Model.cPrice].toString(),
                    itemInfo[Model.quantity].toString(),
                    itemInfo[Model.addedDate].toString(),
                )
                requestMerchandiseMap += infoMap
            }
        }
        val request = Model.OrderRequest(
            customerName = customerName!!,
            customerEmail = customerEmail!!,
            customerPhone = customerPhone!!,
            recipientName = recipientName!!,
            recipientEmail = recipientEmail!!,
            recipientPhone = recipientPhone!!,
            shippingAddress = shippingAddress!!,
            shippingMethod = shippingMethod!!,
            payment = payment!!,
            deliveryTime = deliveryTime!!,
            merchandise = requestMerchandiseMap,
            totalPrice = totalPrice!!,
            totalQuantity = totalQuantity!!,
        )
        val call = myApiService.createOrder(request)
        call.enqueue(object : Callback<List<Model.OrderResponse>> {
            override fun onResponse(call: Call<List<Model.OrderResponse>>, response: Response<List<Model.OrderResponse>>) {
                if (response.isSuccessful) {
                    cartMap.clear()
                    cartQuantity = 0
                    memberOrders = emptyList()
                    val orderResponses = response.body() ?: emptyList()
                    memberOrders = orderResponses
                    callback?.invoke()
                } else {
                    val responseStatusCode = response.code()
                    CustomSnackbar.showSnackbar(view, requireContext(), responseStatusCode.toString())
                    callback?.invoke()
                }
            }
            override fun onFailure(call: Call<List<Model.OrderResponse>>, t: Throwable) {
                // 請求失敗時的處理邏輯
                val message: String = when (t) {
                    is SocketTimeoutException -> getString(R.string.connectTimeOut)
                    is IOException -> getString(R.string.connectFailed)
                    else -> getString(R.string.serverError)
                }
                CustomSnackbar.showSnackbar(view, requireContext(), message)
                call.cancel()
                callback?.invoke()
            }
        })
    }
}
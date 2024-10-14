package com.example.shopping_app.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shopping_app.ApiHelper.ItemInfoMap
import com.example.shopping_app.ApiHelper.ItemInfoMap.Companion.memberOrders
import com.example.shopping_app.FragmentShift
import com.example.shopping_app.Model
import com.example.shopping_app.R
import com.example.shopping_app.RecyclerViewHelper.FinalCheckRecyclerViewAdapter

class OrderInfoFragment: Fragment() {
    private lateinit var fragmentShift: FragmentShift
    private lateinit var itemInfoMap: ItemInfoMap
    private lateinit var toolbar: Toolbar
    /* TextView */
    private lateinit var shippingMethod: TextView
    private lateinit var payment: TextView
    private lateinit var quantityPrice: TextView
    private lateinit var customerName: TextView
    private lateinit var customerPhone: TextView
    private lateinit var customerEmail: TextView
    private lateinit var recipientName: TextView
    private lateinit var recipientPhone: TextView
    private lateinit var recipientEmail: TextView
    private lateinit var shippingAddress: TextView
    private lateinit var deliveryTime: TextView
    /* RecyclerView */
    private lateinit var orderItemRecyclerView: RecyclerView
    private var myOrderItemList: MutableList<List<String>> = mutableListOf()
    private lateinit var myFinalCheckRecyclerViewAdapter: FinalCheckRecyclerViewAdapter
    private var orderID: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        orderID = arguments?.getString("orderID")

        return inflater.inflate(R.layout.fragment_order_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentShift = FragmentShift()
        itemInfoMap = ItemInfoMap()
        findView(view)
        setUpToolbar()
        init()
    }
    private fun findView(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        shippingMethod = view.findViewById(R.id.shippingMethod)
        payment = view.findViewById(R.id.payment)
        quantityPrice = view.findViewById(R.id.quantityPrice)
        customerName = view.findViewById(R.id.customerName)
        customerPhone = view.findViewById(R.id.customerPhone)
        customerEmail = view.findViewById(R.id.customerEmail)
        recipientName = view.findViewById(R.id.recipientName)
        recipientPhone = view.findViewById(R.id.recipientPhone)
        recipientEmail = view.findViewById(R.id.recipientEmail)
        shippingAddress = view.findViewById(R.id.shippingAddress)
        deliveryTime = view.findViewById(R.id.deliveryTime)
        orderItemRecyclerView = view.findViewById(R.id.orderItemRecyclerView)
    }
    private fun setUpToolbar() {
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.orderInfo)
        toolbar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_navigation_back)
        toolbar.contentInsetStartWithNavigation = 0
        toolbar.setNavigationOnClickListener {
            fragmentShift.returnBackStackFragment(requireActivity(),
                FragmentShift.ORDER_INFO_FRAGMENT_SHIFT_TAG
            )
        }
    }
    private fun init() {
        for(orderResponse in memberOrders) {
            if(orderResponse.id == orderID) {
                shippingMethod.text = orderResponse.orderInfo.shipping.shippingMethod
                payment.text = orderResponse.orderInfo.payment.payment
                val totalQuantity = orderResponse.orderInfo.payment.totalQuantity
                val totalPrice = orderResponse.orderInfo.payment.totalPrice
                quantityPrice.text = totalQuantity + getString(R.string.orderInfoString) + totalPrice
                customerName.text = orderResponse.orderInfo.customer.name
                customerPhone.text = orderResponse.orderInfo.customer.phone
                customerEmail.text = orderResponse.orderInfo.customer.email
                recipientName.text = orderResponse.orderInfo.recipient.name
                recipientPhone.text = orderResponse.orderInfo.recipient.phone
                recipientEmail.text = orderResponse.orderInfo.recipient.email
                shippingAddress.text = orderResponse.orderInfo.shipping.shippingAddress
                deliveryTime.text = orderResponse.orderInfo.shipping.deliveryTime
            }
        }
        recyclerViewInit()
    }
    private fun recyclerViewInit() {
        myOrderItemList = createOrderItemList()
        if(myOrderItemList.isNotEmpty()) {
            orderItemRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            myFinalCheckRecyclerViewAdapter = FinalCheckRecyclerViewAdapter(requireContext(), myOrderItemList)
            orderItemRecyclerView.adapter = myFinalCheckRecyclerViewAdapter
        }
    }
    private fun createOrderItemList(): MutableList<List<String>> {
        val dataList: MutableList<List<String>> = mutableListOf()
        if(memberOrders.isNotEmpty()) {
            for (orderResponse in memberOrders) {
                if(orderResponse.id == orderID) {
                    val merchandise = orderResponse.orderInfo.merchandise
                    for(itemID in merchandise.keys) {
                        val data = listOf(
                            itemID,
                            merchandise[itemID]!!.name,
                            ItemInfoMap.itemIDMap[itemID]!![Model.cImage] ?: "",
                            merchandise[itemID]!!.price,
                            merchandise[itemID]!!.quantity.toString(),
                            merchandise[itemID]!!.addedDate,
                            ItemInfoMap.itemIDMap[itemID]!![Model.cInventory] ?: "",
                        )
                        dataList.add(data)
                    }
                }
            }
        }
        return dataList
    }
}
package com.example.shopping_app

import com.google.gson.annotations.SerializedName

object Model {
    /* user */
    val cID = "cID"
    val cName = "cName"
    val cPassword = "cPassword"
    val cNickName = "cNickName"
    val cEmail = "cEmail"
    val cPhone = "cPhone"
    val cAddr = "cAddr"
    val cCountry = "cCountry"
    val cBirthday = "cBirthday"
    val cSex = "cSex"
    val cImage = "cImage"
    /* Item */
    val cAuthor = "cAuthor"
    val cCompany = "cCompany"
    val cSort = "cSort"
    val cClass = "cClass"
    val cPrice = "cPrice"
    val cDate = "cDate"
    val cDescription = "cDescription"
    val cInventory = "cInventory"
    val quantity = "quantity"
    val addedDate = "addedDate"
    // 使用於身分驗證
    data class TokenResponse(
        val token: String
    )
    data class ResponseMessage(
        val message: String?,
        val error: String?
    )
    data class MemberResponse(
        val cID: Int,
        val cName: String,
        val cNickName: String?,
        val cEmail: String,
        val cPhone: String?,
        val cAddr: String?,
        val cCountry: String?,
        val cBirthday: String?,
        val cSex: String?,
        val cImage: String?
    )

    data class Item(
        val cID: Int,
        val cName: String,
        val cAuthor: String,
        val cCompany: String,
        val cSort: String,
        val cClass: String,
        val cPrice: Int,
        val cDate: String,
        val cDescription: String,
        val cInventory: Int,
        val cImageName: String,
        val cImage: String,
    )

    data class MemberFavorite(
        val member: String,
        @SerializedName("cLikeMerchandiseList")
        val favoriteMerchandiseList: Map<String, FavoriteMerchandiseInfo>,
    )
    data class FavoriteMerchandiseInfo(
        val name: String,
        val addedDate: String
    )

    data class MemberCart(
        val member: String,
        @SerializedName("cMerchandiseList")
        val merchandiseList: Map<String, MerchandiseInfo>,
    )

    data class MerchandiseInfo(
        val name: String,
        val price: String,
        val quantity: Int,
        val addedDate: String
    )

    data class OrderRequest(
        val customerName: String,
        val customerEmail: String,
        val customerPhone: String,
        val recipientName: String,
        val recipientEmail: String,
        val recipientPhone: String,
        val shippingAddress: String,
        val shippingMethod: String,
        val payment: String,
        val deliveryTime: String,
        val merchandise: List<List<String>>,
        val totalQuantity: String,
        val totalPrice: String,
    )

    /* getOrder data class */
    data class OrderResponse(
        val id: String,
        val orderInfo: OrderInfo,
    )
    data class OrderInfo(
        val payment: PaymentInfo,
        val customer: CustomerInfo,
        val recipient: RecipientInfo,
        val shipping: ShippingInfo,
        val createTime: String,
        val merchandise: Map<String, ItemInfo>
    )
    data class PaymentInfo(
        val payment: String,
        val totalPrice: String,
        val totalQuantity: String
    )
    data class CustomerInfo(
        val name: String,
        val email: String,
        val phone: String
    )
    data class ShippingInfo(
        val deliveryTime: String,
        val shippingMethod: String,
        val shippingAddress: String
    )
    data class RecipientInfo(
        val name: String,
        val email: String,
        val phone: String
    )
    data class ItemInfo(
        val name: String,
        val price: String,
        val quantity: Int,
        val addedDate: String
    )
}
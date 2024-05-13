package com.example.shopping_app

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface MyApiService {
    @POST("auth/login/") // App身分驗證
    fun login(@Body credentials: Map<String, String>): Call<Model.TokenResponse>

    @GET("api/merchandises/")
    fun getItems(@HeaderMap headers: Map<String, String>): Call<List<Model.Item>>

    /* Member */

    @POST("register/")
    fun register(@Body credentials: Map<String, String>): Call<Model.MemberResponse>

    @POST("member/login/")
    fun memberLogin(@Body credentials: Map<String, String>): Call<Model.MemberResponse>

    @POST("changePassword/")
    fun changePassword(@Body credentials: Map<String, String>): Call<Model.ResponseMessage>

    @POST("getMemberData/")
    fun getMemberData(@Body email: Map<String, String>): Call<Model.MemberResponse>

    @POST("edit/memberInfo/")
    fun editMemberInfo(@Body credentials: Map<String, String?>): Call<Model.MemberResponse>

    @Multipart
    @POST("upload/image/")
    fun uploadImage(
        @Part image: MultipartBody.Part,
        @Part("email") email: RequestBody
    ): Call<Model.MemberResponse>

    @POST("delete/member/")
    fun deleteMember(@Body email: Map<String, String>): Call<Model.ResponseMessage>


    /* Favorite */
    @POST("like/merchandise/")
    fun addFavorite(@Body request: Map<String, String>): Call<Model.MemberFavorite> //回傳更新後的資料

    @POST("get/likeMerchandise/")
    fun getMemberFavor(@Body email: Map<String, String>): Call<Model.MemberFavorite>  //回傳資料

    @POST("delete/likeMerchandise/")
    fun deleteFavorite(@Body request: Map<String, String>): Call<Model.MemberFavorite> //回傳更新後的資料

    /* Cart */
    @POST("addToCart/")
    fun addToCart(@Body request: Map<String, String>): Call<Model.MemberCart>

    @POST("getCart/")
    fun getCart(@Body request: Map<String, String>): Call<Model.MemberCart>

    @POST("deleteCartItem/")
    fun deleteCartItem(@Body request: Map<String, String>): Call<Model.MemberCart>

    @POST("createOrder/")
    fun createOrder(@Body requestData: Model.OrderRequest): Call<List<Model.OrderResponse>>

    @POST("getOrder/")
    fun getOrder(@Body request: Map<String, String>): Call<List<Model.OrderResponse>>

    @POST("deleteOrder/")
    fun deleteOrder(@Body request: Map<String, String>): Call<List<Model.OrderResponse>>
    /* use GET */
//    @GET("getmemberdata/")
//    fun getMemberData(@Query("email") email: String): Call<Model.MemberResponse>
}
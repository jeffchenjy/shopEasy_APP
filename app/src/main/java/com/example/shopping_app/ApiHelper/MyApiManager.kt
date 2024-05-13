package com.example.shopping_app

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object MyApiManager {
    const val username = "GetApi"
    const val password = "qwer2196"
    const val baseUrl = "https://shopeasy-zgnu.onrender.com/"
    var token: String? = ""

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .writeTimeout(5, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
        .build()
    private val retrofit: Retrofit = Retrofit.Builder()
        //https://shopeasy-zgnu.onrender.com/
        //http://192.168.58.159:8080/
        //http://192.168.59.56:8080/
        //http://192.168.50.223:8080/
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val myApiService: MyApiService = retrofit.create(MyApiService::class.java)
}
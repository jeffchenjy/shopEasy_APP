package com.example.shopping_app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkUtils {

    fun isNetworkOnline(context: Context) :Boolean {
        var isOnline = false
        try {
            val manager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities: NetworkCapabilities = manager.getNetworkCapabilities(manager.activeNetwork) as NetworkCapabilities
            isOnline = (capabilities != null) && (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isOnline
    }

}
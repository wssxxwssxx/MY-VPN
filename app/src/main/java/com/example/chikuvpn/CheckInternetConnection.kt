package com.example.chikuvpn

import android.content.Context
import android.net.ConnectivityManager

class CheckInternetConnection {

    /**
     * Check internet status
     * @param context
     * @return: internet connection status
     */
    fun netCheck(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val nInfo = cm.activeNetworkInfo
        val isConnected = nInfo != null && nInfo.isConnectedOrConnecting
        return isConnected
    }
}
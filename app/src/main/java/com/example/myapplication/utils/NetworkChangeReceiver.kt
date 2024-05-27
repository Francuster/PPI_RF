package com.example.myapplication.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

class NetworkChangeReceiver(private val callback: (Boolean) -> Unit) : BroadcastReceiver() {

    // Secondary constructor that calls the primary constructor with a default callback
    constructor() : this({})
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == ConnectivityManager.CONNECTIVITY_ACTION) {
            val isConnected = checkInternetConnection(context)
            callback(isConnected)
        }
    }

    private fun checkInternetConnection(context: Context?): Boolean {
        val connectivityManager =
            context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val networkInfo = connectivityManager?.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }
}
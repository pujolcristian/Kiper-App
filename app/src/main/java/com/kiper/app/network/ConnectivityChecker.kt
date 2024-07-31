package com.kiper.app.network

interface ConnectivityChecker {
    fun checkInternetAccess(event: String)
}
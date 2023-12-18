package com.kemarport.kyms.helper

class Constants {

    companion object {
        const val DEFAULT_USERNAME = "admin"
        const val DEFAULT_PASSWORD = "password"

        const val NETWORK_FAILURE = "Network Failure"
        const val NO_INTERNET = "No Internet Connection"
        const val CONFIG_ERROR = "Please configure network details"

        const val BASE_URL = "http://192.168.1.205:5800/service/api/"
        const val BASE_URL_NEW = "http://10.80.100.16:6600/service/api/"

        const val MODE_TRAIN = "Train"
        const val MODE_TRUCK = "Truck"
        const val DEFAULT_LOCATION_RAIL = "RAILWAY PLATFORM"
        const val BERTH_LOCATION_1 = "BERTH NO. 5A"
        const val BERTH_LOCATION_2 = "BERTH NO. 10"
    }
}
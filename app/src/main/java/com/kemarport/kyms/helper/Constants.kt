package com.kemarport.kyms.helper

object Constants  {
        const val DEFAULT_USERNAME = "admin"
        const val DEFAULT_PASSWORD = "password"
        const val SHARED_PREF = "yms_shared_pref"
        const val NETWORK_FAILURE = "Network Failure"
        const val NO_INTERNET = "No Internet Connection"
        const val CONFIG_ERROR = "Please configure network details"
        const val BASE_URL_NEW = "http://192.168.1.205:1414/service/api/"
        //const val BASE_URL_NEW = "http://10.80.100.16:6600/service/api/"
        const val MODE_TRAIN = "Train"
        const val MODE_TRUCK = "Truck"
        const val DEFAULT_LOCATION_RAIL = "RAILWAY PLATFORM"
        const val BERTH_LOCATION_1 = "BERTH NO. 5A"
        const val BERTH_LOCATION_2 = "BERTH NO. 10"
        val LONGITUDE: String = "longitude"
        val LATITUDE: String = "latitude"
        const val KEY_USER_ID = "id"
        const val LOGGEDIN = "loggedIn"
        const val IS_ADMIN = "isAdmin"
        const val USERNAME = "username"
        const val TOKEN = "token"
        const val USER_COORDINATES = "coordinates"
        const val INCOMPLETE_DETAILS = "Please fill the required details"
        const val EXCEPTION_ERROR = "No Data Found"
        const val HTTP_ERROR_MESSAGE = "errorMessage"
        const val HTTP_HEADER_AUTHORIZATION = "Authorization"
        const val GET = 1
        const val POST = 2
        const val HTTP_OK = 200
        const val HTTP_CREATED = 201
        const val HTTP_EXCEPTION = 202
        const val HTTP_UPDATED = 204
        const val HTTP_FOUND = 302
        const val HTTP_NOT_FOUND = 404
        const val HTTP_CONFLICT = 409
        const val HTTP_INTERNAL_SERVER_ERROR = 500
        const val HTTP_ERROR = 400




        ///api endpoints

        const val GET_JOB_MASTER_MOBILE = "ImportJobMaster/getJobMasterMobile"
        const val GET_JOB_DETAILS_MOBILE = "ImportJobMaster/getJobDetailMobile"
        const val GET_RECEIVED_PRODUCT = "ImportStock/ReceivedProduct"
        const val GET_STORED_PRODUCT = "ImportStock/StoreProduct"

    }

package com.kemarport.kyms.helper

import android.app.Application
import android.content.Context
import android.content.SyncContext
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.widget.Toast



object Utils {

   /* fun parseString(input: String): MutableList<LatLng>? {
        try {
            // Remove 'lat/lng:' and replace '),' with '],'

            // Parse the modified string as a JSON arra
            val indexOfOpeningParenthesis = input.indexOf('(')
            val indexOfClosingParenthesis = input.indexOf(')')
            val latLongList = mutableListOf<LatLng>()
            val subStringLocaion = input.substring(indexOfOpeningParenthesis+1, indexOfClosingParenthesis)
            val list = subStringLocaion.split(",")
            latLongList.add(LatLng(list[0].toDouble(),list[1].toDouble()))
            return latLongList
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null

    }*/


    fun setSharedPrefs(context: Context, key: String?, value: String?) {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getSharedPrefs(context: Context, key: String?): String? {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getString(key, "")
    }

    fun getSharedPrefs(context: Context, key: String?, defValue: String?): String? {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getString(key, defValue)
    }

    fun setSharedPrefsInteger(context: Context, key: String?, value: Int) {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getSharedPrefsInteger(context: Context, key: String?): Int {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getInt(key, 0)
    }

    fun getSharedPrefsDefaultIndex(context: Context, key: String?): Int {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getInt(key, -1)
    }

    fun getSharedPrefsInteger(context: Context, key: String?, defValue: Int): Int {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getInt(key, defValue)
    }

    fun setSharedPrefsLong(context: Context, key: String?, value: Long) {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun getSharedPrefsLong(context: Context, key: String?): Long {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getLong(key, 0)
    }

    fun setSharedPrefsBoolean(context: Context, key: String?, value: Boolean) {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getSharedPrefsBoolean(context: Context, key: String?): Boolean {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getBoolean(key, true)
    }

    fun getSharedPrefsBoolean(context: Context, key: String?, defValue: Boolean): Boolean {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        return pref.getBoolean(key, defValue)
    }

    fun removeSharedPrefs(context: Context, key: String?) {
        val pref = context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.remove(key)
        editor.apply()
    }

    fun hasInternetConnection(application: Application): Boolean {
        val connectivityManager = application.getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }

        return false
    }

    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
    }


}
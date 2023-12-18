package com.kemarport.kyms.helper

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    // Shared Preferences
    var sharedPrefer: SharedPreferences

    // Editor for Shared preferences
    var editor: SharedPreferences.Editor

    // Context
    var context: Context

    // Shared Pref mode
    var PRIVATE_MODE = 0

    // Constructor
    init {
        this.context = context
        sharedPrefer = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE)
        editor = sharedPrefer.edit()
    }

    /**
     * Call this method on/after login to store the details in session
     */
    fun createLoginSession(
        userId: String?,
        catId: String?,
        userRole: String?,
        batchId: String?,
        password: String?,
        firstName: String?,
        lastName: String?,
        jwtToken: String?,
        refreshToken: String?,
        email: String?
    ) {

        // Storing userId in pref
        editor.putString(KEY_USERID, userId)

        // Storing catId in pref
        editor.putString(KEY_CATID, catId)

        // Storing catType in pref
        editor.putString(KEY_USER_ROLE, userRole)

        // Storing catType in pref
        editor.putString(KEY_BATCHID, batchId)

        // Storing password in pref
        editor.putString(KEY_PASSWORD, password)

        // Storing isLoggedIn in Pref
        editor.putBoolean(KEY_ISLOGGEDIN, true)

        editor.putString(KEY_FIRST_NAME, firstName)
        editor.putString(KEY_LAST_NAME, lastName)
        editor.putString(KEY_JWT_TOKEN, jwtToken)
        editor.putString(KEY_REFRESH_TOKEN, refreshToken)
        editor.putString(KEY_EMAIL, email)

        // commit changes
        editor.commit()
    }

    fun logoutUser() {
        editor.putBoolean(KEY_ISLOGGEDIN, false)
        editor.commit()
    }

    /**
     * Call this method anywhere in the project to Get the stored session data
     */
    fun getUserDetails(): HashMap<String, String?> {
        val user = HashMap<String, String?>()
        user["userId"] = sharedPrefer.getString(KEY_USERID, null)
        user["batchId"] = sharedPrefer.getString(KEY_BATCHID, null)
        user["catId"] = sharedPrefer.getString(KEY_CATID, null)
        user["userRole"] = sharedPrefer.getString(KEY_USER_ROLE, null)
        user["password"] = sharedPrefer.getString(KEY_PASSWORD, null)
        user["firstName"] = sharedPrefer.getString(KEY_FIRST_NAME, null)
        user["lastName"] = sharedPrefer.getString(KEY_LAST_NAME, null)
        user["jwtToken"] = sharedPrefer.getString(KEY_JWT_TOKEN, null)
        user["refreshToken"] = sharedPrefer.getString(KEY_REFRESH_TOKEN, null)
        user["email"] = sharedPrefer.getString(KEY_EMAIL, null)
        return user
    }

    fun isAlreadyLoggedIn(): HashMap<String, Boolean> {
        val user = HashMap<String, Boolean>()
        user["isLoggedIn"] = sharedPrefer.getBoolean(KEY_ISLOGGEDIN, false)
        return user
    }

    fun getAdminDetails(): HashMap<String, String?> {
        val admin = HashMap<String, String?>()
        admin["serverIp"] = sharedPrefer.getString(KEY_SERVER_IP, null)
        admin["port"] = sharedPrefer.getString(KEY_PORT, null)
        return admin
    }

    fun saveAdminDetails(serverIp: String?, portNumber: String?) {
        editor.putString(KEY_SERVER_IP, serverIp)
        editor.putString(KEY_PORT, portNumber)
        editor.putBoolean(KEY_ISLOGGEDIN, false)
        editor.commit()
    }

    fun clearSharedPrefs() {
        editor.clear()
        editor.commit()
    }

    companion object {
        // Shared Pref file name
        private const val PREF_NAME = "MySession"

        // SHARED PREF KEYS FOR ALL DATA
        // User's UserId
        const val KEY_USERID = "userId"

        // User's categoryId
        const val KEY_CATID = "catId"

        // User's role[Supervisor, Picker, etc.,]
        const val KEY_USER_ROLE = "userRole"

        // User's batchId[like class or level or batch]
        const val KEY_BATCHID = "batchId"

        //User's Password
        const val KEY_PASSWORD = "password"

        //User isLoggedIn Check
        const val KEY_ISLOGGEDIN = "isLoggedIn"

        const val KEY_FIRST_NAME = "firstName"
        const val KEY_LAST_NAME = "lastName"
        const val KEY_JWT_TOKEN = "jwtToken"
        const val KEY_REFRESH_TOKEN = "refreshToken"
        const val KEY_EMAIL = "email"

        //Admin Shared Prefs
        const val KEY_SERVER_IP = "serverIp"
        const val KEY_PORT = "port"
    }
}
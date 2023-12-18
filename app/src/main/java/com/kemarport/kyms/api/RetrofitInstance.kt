package com.kemarport.kyms.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/*
class RetrofitInstance {


    companion object {
        private val retrofit: Retrofit? = null
        fun api(uri: String): KYMS_API  {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            Retrofit.Builder()
                .baseUrl(uri)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
            return retrofit!!.create(KYMS_API::class.java)
        }

    }
}*/

class RetrofitInstance {

    companion object {
        private var baseUrl = ""
     /*   private val retrofit by lazy {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
        }*/
     fun create(baseUrl: String): Retrofit {
         val logging = HttpLoggingInterceptor()
         logging.setLevel(HttpLoggingInterceptor.Level.BODY)
         val client = OkHttpClient.Builder()
             .addInterceptor(logging)
             .connectTimeout(100, TimeUnit.SECONDS)
             .readTimeout(100, TimeUnit.SECONDS)
             .writeTimeout(100, TimeUnit.SECONDS)
             .build()

         return Retrofit.Builder()
             .baseUrl(baseUrl)
             .addConverterFactory(GsonConverterFactory.create())
             .client(client)
             .build()
     }

        /*val api by lazy {
            retrofit.create(KYMS_API::class.java)
        }*/
      /*  fun api(baseUrl: String): KYMS_API {
            this.baseUrl = baseUrl
            return retrofit.create(KYMS_API::class.java)
        }*/
        fun api(baseUrl: String): KYMS_API {
            val retrofit = create(baseUrl)
            //this.baseUrl = baseUrl
            return retrofit.create(KYMS_API::class.java)
        }

    }
}

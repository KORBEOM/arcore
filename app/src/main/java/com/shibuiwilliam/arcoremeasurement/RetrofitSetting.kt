package com.shibuiwilliam.arcoremeasurement

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit
import javax.net.SocketFactory

object RetrofitSetting {
    val API_BASE_URL = "http://211.184.227.81:8000"
    val httpClient = OkHttpClient.Builder()


    val client = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())



    fun <S> createBaseService(serviceClass: Class<S>?): S? {
        val retrofit = client.client(httpClient.build()).build()
        return serviceClass?.let { retrofit.create(it) }

    }
}
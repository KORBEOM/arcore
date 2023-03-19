package com.shibuiwilliam.arcoremeasurement

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitSetting {
    val API_BASE_URL = "http://192.168.0.48:3000"
    val httpClient = OkHttpClient.Builder()

    val client = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .addConverterFactory(ScalarsConverterFactory.create())
        .addConverterFactory(GsonConverterFactory.create())


    fun <S> createBaseService(serviceClass: Class<S>?): S {
        val retrofit = client.client(httpClient.build()).build()
        return retrofit.create(serviceClass)
    }
}
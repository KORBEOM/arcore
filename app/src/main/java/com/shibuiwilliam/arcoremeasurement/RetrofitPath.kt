package com.shibuiwilliam.arcoremeasurement

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RetrofitPath {
    @Multipart
    @POST("/")
    fun imageSend(
        @Part img : MultipartBody.Part
    ): Call<String>
}

interface RetrofitFailPath {
    @Multipart
    @POST("/fail")
    fun imageSend(
        @Part img : MultipartBody.Part
    ): Call<String>
}


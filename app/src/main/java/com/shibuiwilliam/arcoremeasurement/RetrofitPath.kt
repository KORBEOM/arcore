package com.shibuiwilliam.arcoremeasurement

import okhttp3.MultipartBody

import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

import retrofit2.http.PartMap

interface RetrofitPath {
    @Multipart
    @POST("/upload")
    fun imageSend(
        @Part image : MultipartBody.Part
    ): Call<String>
}

interface RetrofitFailPath {
    @Multipart
    @POST("/fail")
    fun imageFailSend(
        @Part image : MultipartBody.Part
    ): Call<String>
}

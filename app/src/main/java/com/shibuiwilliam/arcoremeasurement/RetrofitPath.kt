package com.shibuiwilliam.arcoremeasurement

import okhttp3.MultipartBody
import okhttp3.RequestBody

import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface RetrofitPath {
    @Multipart
    @POST("/upload")
    fun imageSend(
        @Part image: MultipartBody.Part
        //@Part image : RequestBody
        //@Part pixel: RequestBody
    ): Call<String>
}
interface RetrofitPath2 {
    @Multipart
    @POST("/test")
    fun imageSend(
        @Part image: MultipartBody.Part?
        //@Part image : RequestBody
        //@Part pixel: RequestBody
    ): Call<String>
}
interface RetrofitPath3 {
    @Multipart
    @POST("/emit")
    fun imageSend(
        @Part image: List<MultipartBody.Part?>,
        @Part("count") count: RequestBody
        //@Part pixel: RequestBody
    ): Call<String>
}
interface RetrofitFailPath {
    @Multipart
    @POST("/fail")
    fun imageFailSend(
        @Part image : MultipartBody.Part
    ): Call<String>
}

package com.shibuiwilliam.arcoremeasurement

import okhttp3.MultipartBody
import okhttp3.RequestBody

import retrofit2.Call
import retrofit2.http.Body
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
interface ApiService {
    @POST("login")  // 서버의 실제 로그인 엔드포인트에 맞게 수정
    fun login(@Body request: LoginRequest): Call<LoginResponse>
}
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val success: Boolean, val message: String)
interface RetrofitPath2 {
    @Multipart
    @POST("/emit")
    fun imageSend(
        @Part image: MultipartBody.Part?,
        @Part("count") count: RequestBody,
        @Part("user") user: RequestBody
    ): Call<String>
}
interface RetrofitPath3 {
    @Multipart
    @POST("/emitest")
    fun imageSend(
        @Part image: List<MultipartBody.Part?>,
        @Part("count") count: RequestBody
        //@Part pixel: RequestBody
    ): Call<String>
}
interface RetrofitPath4 {
    @Multipart
    @POST("/emit")
    fun imageSend(
        @Part image: List<MultipartBody.Part?>,
        @Part("count") count: RequestBody,
        @Part("user") user: RequestBody
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

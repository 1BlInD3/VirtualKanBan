package com.fusetech.virtualkanban.Retrofit

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.fusetech.virtualkanban.Activities.MainActivity.Companion.mainUrl
import retrofit2.http.*

interface SendAPI{
    @Multipart
    @POST("uploadFile/{path}")
    fun uploadXml(
        @Part("path") path: RequestBody,
        @Part xml: MultipartBody.Part,
        @Part("file") desc: RequestBody
    ): Call<UploadResponse>

    @GET("test")
    fun getTest():Call<UploadResponse>

    @GET("config")
    fun loadConfig():Call<UploadConfig>

    companion object{
        operator fun invoke(): SendAPI{
            return Retrofit.Builder()
                .baseUrl(mainUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(SendAPI::class.java)
        }
    }
}
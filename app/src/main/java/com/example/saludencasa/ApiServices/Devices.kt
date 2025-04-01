package com.example.saludencasa.ApiServices

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface Devices {

    @POST("device/")
    suspend fun postDevice(@Body requestBody: RequestBody): Response<ResponseBody>
}
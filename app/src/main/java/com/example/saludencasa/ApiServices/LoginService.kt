package com.example.saludencasa.ApiServices
import com.example.saludencasa.Modelo.Login
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface LoginService {
    @GET("Login/") // Reemplazar url
    suspend fun getLogin(): List<Login>

    @POST("Login/")
    suspend fun postLogin(@Body requestBody: RequestBody): Response<ResponseBody>

    @PUT("Login/{id}/")
    suspend fun putLogin(@Path("id") id: Int,@Body requestBody: RequestBody): Response<ResponseBody>
}
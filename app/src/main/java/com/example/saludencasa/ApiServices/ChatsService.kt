package com.example.saludencasa.ApiServices
import com.example.saludencasa.Modelo.Chats
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface ChatsService {
    @GET("Chat/") // Reemplazar url
    fun getChats(): Call<List<Chats>>

    @POST("Chat/")
    suspend fun postChat(@Body requestBody: RequestBody): Response<ResponseBody>

    @GET("Chat/") // Reemplazar url
    suspend fun getChatSimple(): List<Chats>

    @PUT("Chat/{id}/")
    suspend fun putChat(@Path("id") id: Int, @Body requestBody: RequestBody): Response<ResponseBody>
}
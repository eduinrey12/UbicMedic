package com.example.saludencasa.ApiServices
import com.example.saludencasa.Modelo.Chats
import com.example.saludencasa.Modelo.Mensajes
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface MensajesService {
    @GET("Mensaje/") // Reemplazar url
    fun getMensajes(): Call<List<Mensajes>>

    @POST("Mensaje/")
    suspend fun postMensaje(@Body requestBody: RequestBody): Response<ResponseBody>

    @PUT("Mensaje/{id}/")
    suspend fun putMensaje(@Path("id") id: Int, @Body requestBody: RequestBody): Response<ResponseBody>
}
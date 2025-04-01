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


interface ReportesService {
    @GET("ReporteUsuario/") // Reemplazar url
    fun getReportes(): Call<List<Chats>>

    @POST("ReporteUsuario/")
    suspend fun postReportes(@Body requestBody: RequestBody): Response<ResponseBody>

    @GET("ReporteUsuario/") // Reemplazar url
    suspend fun getReportesSimple(): List<Chats>

    @PUT("ReporteUsuario/{id}/")
    suspend fun putReportes(@Path("id") id: Int, @Body requestBody: RequestBody): Response<ResponseBody>
}
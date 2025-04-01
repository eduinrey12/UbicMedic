package com.example.saludencasa

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.BoringLayout
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saludencasa.Adaptador.EnfermedadesClienteAdapter
import com.example.saludencasa.Adaptador.TipoReporteAdapter
import com.example.saludencasa.ApiServices.CalificacionService
import com.example.saludencasa.ApiServices.EnfermedadesClienteService
import com.example.saludencasa.ApiServices.ReportesService
import com.example.saludencasa.ApiServices.TipoReporteService
import com.example.saludencasa.Constante.urlUbicMedic
import com.example.saludencasa.Modelo.Chats
import com.example.saludencasa.Modelo.EnfermedadesCliente
import com.example.saludencasa.Modelo.TipoReporte
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.time.OffsetDateTime

class ReportarPerfil : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var fondoProgress: View

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reportar_perfil)

        progressBar = findViewById(R.id.progressBar)
        fondoProgress = findViewById(R.id.fondoProgress)
        deshabilitarInteraccion()

        val idCliente = intent.getIntExtra("idCliente", 0)
        val idClienteTrabajador = intent.getIntExtra("idClienteTrabajador", 0)
        var comentario = ""
        var listaReportes : List<Int> = emptyList()
        var adapterTipoReporte: TipoReporteAdapter
        var txtComentario : EditText = findViewById(R.id.txtComentario)
        var rcvTipoReporte: RecyclerView = findViewById(R.id.rcvTipoReporte)
        val btnReportar : Button = findViewById(R.id.btnReportar)
        val btnAtras = findViewById<ImageButton>(R.id.btnAtrasReporte)
        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        btnAtras.setOnClickListener {
            super.onBackPressed()
        }

        btnReportar.setOnClickListener {
            val apiServiceReporte = retrofit.create(ReportesService::class.java)
            comentario = txtComentario.text.toString()
            if (comentario==null || comentario==""){comentario="Sin comentario"}

            if (!listaReportes.isEmpty()){
                try{
                    listaReportes.forEach{itemDato ->
                        val jsonObjectReporte = JSONObject()
                        jsonObjectReporte.put("id_usuario_reportador", idCliente)
                        jsonObjectReporte.put("id_usuario_reportado", idClienteTrabajador)
                        jsonObjectReporte.put("id_tiporeporte", itemDato)
                        jsonObjectReporte.put("motivo_reporte", comentario)
                        jsonObjectReporte.put("fecha_reporte", OffsetDateTime.now())
                        jsonObjectReporte.put("estado", true)
                        val jsonStringReporte = jsonObjectReporte.toString()
                        val requestBodyReporte = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringReporte)
                        runBlocking {
                            try {
                                //Chat
                                val responseChat = apiServiceReporte.postReportes(requestBodyReporte)
                                if (responseChat.isSuccessful) {
                                    val responseBodyChat = responseChat.body()
                                    val responseDataChat = responseBodyChat?.string()
                                } else {

                                }
                            } catch (e: Exception) {
                                // Manejar el error aquí
                                e.printStackTrace()
                            }
                        }
                    }
                    Toast.makeText(applicationContext,"Se envio su reporte", Toast.LENGTH_LONG).show()
                    super.onBackPressed()
                }
                catch (e:Exception){
                    e.printStackTrace()
                }
            }else{
                Toast.makeText(applicationContext,"Seleccion algun tipo de reporte", Toast.LENGTH_SHORT).show()
            }
        }

        rcvTipoReporte.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        val serviceTipoReporte = retrofit.create(TipoReporteService::class.java)
        val callTipoReporte = serviceTipoReporte.getTipoReporte()
        callTipoReporte.enqueue(object : Callback<List<TipoReporte>> {
            override fun onResponse(call: Call<List<TipoReporte>>, response: Response<List<TipoReporte>>) {
                if (response.isSuccessful) {
                    val tipoReporteResponse = response.body()
                    if (tipoReporteResponse != null) {
                        adapterTipoReporte = TipoReporteAdapter(tipoReporteResponse) { idtipoReporte, listaTiposReportes ->
                            // Aquí se recibe el id_profesiones del item clickeado
                            val tipoReporte = tipoReporteResponse.find { it.id_tiporeporte == idtipoReporte }
                            if (tipoReporte != null) {
                                listaReportes = listaTiposReportes
                            } else {
                                Toast.makeText(applicationContext, "Error al obtener la información del tipo de reporte", Toast.LENGTH_SHORT).show()
                            }
                        }
                        rcvTipoReporte.adapter = adapterTipoReporte
                        habilitarInteraccion()
                    }
                } else {
                    // Manejar error en la respuesta del servicio
                }
            }

            override fun onFailure(call: Call<List<TipoReporte>>, t: Throwable) {
                // Manejar error en la llamada al servicio
                habilitarInteraccion()
            }
        })

    }

    private fun deshabilitarInteraccion() {
        progressBar.visibility = View.VISIBLE
        fondoProgress.visibility = View.VISIBLE
        window?.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun habilitarInteraccion() {
        progressBar.visibility = View.GONE
        fondoProgress.visibility = View.GONE
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}
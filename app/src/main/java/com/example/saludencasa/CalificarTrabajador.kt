package com.example.saludencasa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import coil.load
import com.example.saludencasa.ApiServices.CalificacionService
import com.example.saludencasa.ApiServices.CitasService
import com.example.saludencasa.ApiServices.EnfermedadesClienteService
import com.example.saludencasa.Constante.urlUbicMedic
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import org.w3c.dom.Text
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CalificarTrabajador : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calificar_trabajador)

        val btnAtras = findViewById<ImageButton>(R.id.btnAtrasCalificar)
        btnAtras.setOnClickListener {
            super.onBackPressed()
        }

        val btncalificar_1 : ImageButton = findViewById(R.id.btnCalifica_1)
        val btncalificar_2 : ImageButton = findViewById(R.id.btnCalifica_2)
        val btncalificar_3 : ImageButton = findViewById(R.id.btnCalifica_3)
        val btncalificar_4 : ImageButton = findViewById(R.id.btnCalifica_4)
        val btncalificar_5 : ImageButton = findViewById(R.id.btnCalifica_5)
        val txtComentario : EditText = findViewById(R.id.txtComentario)
        val txtnombre : TextView = findViewById(R.id.txtvNombrePerfil)
        val btnCalificar : Button = findViewById(R.id.btnCalificar)
        val imgfoto : ImageView = findViewById(R.id.imgFotoPerfilFrag)

        val idCliente = intent.getIntExtra("idCliente", 0)
        val idTrabajador = intent.getIntExtra("idTrabajador", 0)
        val nombreTrabajador = intent.getStringExtra("nombreTrabajador")
        val foto = intent.getStringExtra("fotoT")
        var comentario = ""
        var puntuacion = 0

        txtnombre.setText(nombreTrabajador)

        if (foto != null && foto != "") {
            imgfoto.load(foto)
        } else {
            imgfoto.setImageResource(R.drawable.foto_predeterminada)
        }


        btnCalificar.setOnClickListener {
            val retrofit = Retrofit.Builder()
                .baseUrl(urlUbicMedic)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val apiServiceCalificacion = retrofit.create(CalificacionService::class.java)

            comentario = txtComentario.text.toString()
            if (comentario == null || comentario == ""){
                comentario = "Sin comentario"
            }
            val jsonObjectCalificacion = JSONObject()
            jsonObjectCalificacion.put("id_paciente", idCliente)
            jsonObjectCalificacion.put("id_trabajador", idTrabajador)
            jsonObjectCalificacion.put("puntuacion", puntuacion)
            jsonObjectCalificacion.put("comentario", comentario)
            val jsonStringCalificacion = jsonObjectCalificacion.toString()
            val requestBodyCalificacion = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCalificacion)
            runBlocking {
                try {
                    //Chat
                    val responseChat = apiServiceCalificacion.postCalificacion(requestBodyCalificacion)
                    if (responseChat.isSuccessful) {
                        val responseBodyChat = responseChat.body()
                        val responseDataChat = responseBodyChat?.string()
                        Toast.makeText(applicationContext,"Se califico correctamente", Toast.LENGTH_LONG).show()
                        super.onBackPressed()
                    } else {

                    }
                } catch (e: Exception) {
                    // Manejar el error aquí
                    e.printStackTrace()
                }
            }
        }

        btncalificar_1.setOnClickListener {
            btncalificar_1.setImageResource(R.drawable.calificacion_profesional)
            btncalificar_2.setImageResource(R.drawable.calificacion_vacia)
            btncalificar_3.setImageResource(R.drawable.calificacion_vacia)
            btncalificar_4.setImageResource(R.drawable.calificacion_vacia)
            btncalificar_5.setImageResource(R.drawable.calificacion_vacia)
            puntuacion = 1
        }

        btncalificar_2.setOnClickListener {
            btncalificar_1.setImageResource(R.drawable.calificacion_profesional)
            btncalificar_2.setImageResource(R.drawable.calificacion_profesional)
            btncalificar_3.setImageResource(R.drawable.calificacion_vacia)
            btncalificar_4.setImageResource(R.drawable.calificacion_vacia)
            btncalificar_5.setImageResource(R.drawable.calificacion_vacia)
            puntuacion = 2
        }

        btncalificar_3.setOnClickListener {
            btncalificar_1.setImageResource(R.drawable.calificacion_profesional)
            btncalificar_2.setImageResource(R.drawable.calificacion_profesional)
            btncalificar_3.setImageResource(R.drawable.calificacion_profesional)
            btncalificar_4.setImageResource(R.drawable.calificacion_vacia)
            btncalificar_5.setImageResource(R.drawable.calificacion_vacia)
            puntuacion = 3
        }

        btncalificar_4.setOnClickListener {
            btncalificar_1.setImageResource(R.drawable.calificacion_profesional)
            btncalificar_2.setImageResource(R.drawable.calificacion_profesional)
            btncalificar_3.setImageResource(R.drawable.calificacion_profesional)
            btncalificar_4.setImageResource(R.drawable.calificacion_profesional)
            btncalificar_5.setImageResource(R.drawable.calificacion_vacia)
            puntuacion = 4
        }

        btncalificar_5.setOnClickListener {
            btncalificar_1.setImageResource(R.drawable.calificacion_profesional)
            btncalificar_2.setImageResource(R.drawable.calificacion_profesional)
            btncalificar_3.setImageResource(R.drawable.calificacion_profesional)
            btncalificar_4.setImageResource(R.drawable.calificacion_profesional)
            btncalificar_5.setImageResource(R.drawable.calificacion_profesional)
            puntuacion = 5
        }
    }
}
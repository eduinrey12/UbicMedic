package com.example.saludencasa

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.saludencasa.Adaptador.EnfermedadesClienteAdapter
import com.example.saludencasa.ApiServices.EnfermedadesClienteService
import com.example.saludencasa.Constante.urlUbicMedic
import com.example.saludencasa.Modelo.EnfermedadesCliente
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayoutManager
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class GestionarEnfermedades : AppCompatActivity() {

    interface ApiServiceEnfermedad {
        @GET("Enfermedades/")
        fun obtenerDatos(): Call<List<clsEnfermedad>>
    }
    data class clsEnfermedad(val id_enfermedad: Int, val descripcion: String)

    private lateinit var adapterEnfermedadesCliente: EnfermedadesClienteAdapter
    private lateinit var rcvEnfermedadesCliente: RecyclerView
    private lateinit var rcvCronicas: RecyclerView
    private lateinit var rcvCongenitas: RecyclerView

    var idEnfermedad: Int=0
    private lateinit var spnEnfermedad: Spinner
    private lateinit var apiEnfermedad: ApiServiceEnfermedad

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gestionar_enfermedades)


        rcvEnfermedadesCliente = findViewById(R.id.rcvAlergias)
        rcvCronicas = findViewById(R.id.rcvCronicas)
        rcvCongenitas = findViewById(R.id.rcvCongenitas)
        rcvEnfermedadesCliente.layoutManager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        rcvCronicas.layoutManager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)
        rcvCongenitas.layoutManager = FlexboxLayoutManager(this, FlexDirection.ROW, FlexWrap.WRAP)

        var idCliente: Int = intent.extras!!.getInt("IDCLIENTE",0)
        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiEnfermedad = retrofit.create(ApiServiceEnfermedad::class.java)
        val serviceEnfermedadesCliente = retrofit.create(EnfermedadesClienteService::class.java)
        val callEnfermedadesCliente = serviceEnfermedadesCliente.getEnfermedadesCliente()
        callEnfermedadesCliente.enqueue(object : Callback<List<EnfermedadesCliente>> {

            override fun onResponse(call: Call<List<EnfermedadesCliente>>, response: Response<List<EnfermedadesCliente>>) {
                if (response.isSuccessful) {
                    val todasenfermedadesClienteResponse = response.body()
                    val enfermedadesClienteResponse = mutableListOf<EnfermedadesCliente>()
                    val alergias = mutableListOf<EnfermedadesCliente>()
                    val cronicas = mutableListOf<EnfermedadesCliente>()
                    val congenitas = mutableListOf<EnfermedadesCliente>()

                    if (todasenfermedadesClienteResponse != null){
                        todasenfermedadesClienteResponse.forEach{ itemDato ->
                            if (itemDato.id_cliente == idCliente){
                                enfermedadesClienteResponse.add(itemDato)
                            }
                        }
                    }

                    if (enfermedadesClienteResponse != null) {
                        enfermedadesClienteResponse.forEach { itemDato ->
                            if (itemDato.idclasienfermedad == "1") {
                                alergias.add(itemDato)
                            }else if(itemDato.idclasienfermedad == "2"){
                                cronicas.add(itemDato)
                            }else{
                                congenitas.add(itemDato)
                            }
                        }
                    }

                    if (alergias != null) {
                        adapterEnfermedadesCliente = EnfermedadesClienteAdapter(alergias) { idEnfermedadCliente ->
                            // Aquí se recibe el id_profesiones del item clickeado
                            val enfermedadesCliente = alergias.find { it.id_enfermedadesxpaciente == idEnfermedadCliente }
                            if (enfermedadesCliente != null) {
                                Toast.makeText(applicationContext,"Enfermdad: " + enfermedadesCliente.descripcion,
                                    Toast.LENGTH_LONG).show()
                                /*val intent = Intent(requireContext(), ProfesionDetailActivity::class.java)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_ID_PROFESION, enfermedadesCliente.id_cliente)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_DESCRIPCION_PROFESION, enfermedadesCliente.descripcion)
                                startActivity(intent)*/
                            } else {
                                Toast.makeText(applicationContext, "Error al obtener la información de la profesión", Toast.LENGTH_SHORT).show()
                            }
                        }
                        rcvEnfermedadesCliente.adapter = adapterEnfermedadesCliente
                    }

                    if (cronicas != null) {
                        adapterEnfermedadesCliente = EnfermedadesClienteAdapter(cronicas) { idEnfermedadCliente ->
                            // Aquí se recibe el id_profesiones del item clickeado
                            val enfermedadesCliente = cronicas.find { it.id_enfermedadesxpaciente == idEnfermedadCliente }
                            if (enfermedadesCliente != null) {
                                Toast.makeText(applicationContext,"Enfermdad: " + enfermedadesCliente.descripcion,
                                    Toast.LENGTH_LONG).show()
                                /*val intent = Intent(requireContext(), ProfesionDetailActivity::class.java)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_ID_PROFESION, enfermedadesCliente.id_cliente)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_DESCRIPCION_PROFESION, enfermedadesCliente.descripcion)
                                startActivity(intent)*/
                            } else {
                                Toast.makeText(applicationContext, "Error al obtener la información de la profesión", Toast.LENGTH_SHORT).show()
                            }
                        }
                        rcvCronicas.adapter = adapterEnfermedadesCliente
                    }

                    if (congenitas != null) {
                        adapterEnfermedadesCliente = EnfermedadesClienteAdapter(congenitas) { idEnfermedadCliente ->
                            // Aquí se recibe el id_profesiones del item clickeado
                            val enfermedadesCliente = congenitas.find { it.id_enfermedadesxpaciente == idEnfermedadCliente }
                            if (enfermedadesCliente != null) {
                                Toast.makeText(applicationContext,"Enfermdad: " + enfermedadesCliente.descripcion,
                                    Toast.LENGTH_LONG).show()
                                /*val intent = Intent(requireContext(), ProfesionDetailActivity::class.java)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_ID_PROFESION, enfermedadesCliente.id_cliente)
                                intent.putExtra(ProfesionDetailActivity.EXTRA_DESCRIPCION_PROFESION, enfermedadesCliente.descripcion)
                                startActivity(intent)*/
                            } else {
                                Toast.makeText(applicationContext, "Error al obtener la información de la profesión", Toast.LENGTH_SHORT).show()
                            }
                        }
                        rcvCongenitas.adapter = adapterEnfermedadesCliente
                    }
                } else {
                    // Manejar error en la respuesta del servicio
                }
            }

            override fun onFailure(call: Call<List<EnfermedadesCliente>>, t: Throwable) {
                // Manejar error en la llamada al servicio
            }
        })

        val btnAgregar : Button = findViewById(R.id.btnAgregarEnfermedad)
        btnAgregar.setOnClickListener {
            showDialog2(idCliente)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showDialog2(idCliente: Int) {
        val dialogCita = Dialog(this)
        dialogCita.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogCita.setCancelable(true)
        dialogCita.setContentView(R.layout.dialog_gestionar_enfermedades)
        dialogCita.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val motivo : EditText = dialogCita.findViewById(R.id.txtMotivoDialog)
        val btnEnviar : Button = dialogCita.findViewById(R.id.btnBloquear)
        spnEnfermedad = dialogCita.findViewById(R.id.spnEnfermedad)
        obtenerDatosDelWebServiceEnfermedad()
        btnEnviar.setOnClickListener {
            val jsonObjectEnfermedad = JSONObject()
            jsonObjectEnfermedad.put("id_enfermedad", idEnfermedad)
            jsonObjectEnfermedad.put("id_cliente", idCliente)
            jsonObjectEnfermedad.put("descripcion", motivo.text.toString())
            jsonObjectEnfermedad.put("estado", true)
            val jsonStringEnfermedad = jsonObjectEnfermedad.toString()

            val retrofit = Retrofit.Builder()
                .baseUrl(urlUbicMedic)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiServiceEnfermedad = retrofit.create(EnfermedadesClienteService::class.java)
            val requestBodyEnfermedad =
                jsonStringEnfermedad.toRequestBody("application/json".toMediaTypeOrNull())
            runBlocking {
                try {
                    //Chat
                    val responseChat = apiServiceEnfermedad.postEnfermedadesCliente(requestBodyEnfermedad)
                    if (responseChat.isSuccessful) {
                        val responseBodyChat = responseChat.body()
                        val responseDataChat = responseBodyChat?.string()
                        Toast.makeText(applicationContext,"Se realizo la cita correctamente",Toast.LENGTH_LONG).show()
                    } else {

                    }


                } catch (e: Exception) {
                    // Manejar el error aquí
                    e.printStackTrace()
                }
            }
            dialogCita.dismiss()
        }
        dialogCita.show()
    }

    private fun obtenerDatosDelWebServiceEnfermedad() {
        val call = apiEnfermedad.obtenerDatos()
        call.enqueue(object : Callback<List<clsEnfermedad>> {
            override fun onResponse(
                call: Call<List<clsEnfermedad>>,
                response: Response<List<clsEnfermedad>>
            ) {
                if (response.isSuccessful) {
                    val datos = response.body()
                    if (datos != null) {
                        val nombres = datos.map { it.descripcion }
                        val ids = datos.map { it.id_enfermedad }

                        val adapter = ArrayAdapter(
                            this@GestionarEnfermedades,
                            android.R.layout.simple_spinner_item,
                            nombres
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnEnfermedad.adapter = adapter

                        spnEnfermedad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                idEnfermedad = ids[position]
                            }
                            override fun onNothingSelected(parent: AdapterView<*>?) {
                            }
                        }
                    }
                } else {
                    // Manejar la respuesta no exitosa
                }
            }

            override fun onFailure(call: Call<List<clsEnfermedad>>, t: Throwable) {
                // Manejar el error de la solicitud
            }
        })
    }
}
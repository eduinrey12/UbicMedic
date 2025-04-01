package com.example.saludencasa

import android.app.DatePickerDialog
import android.icu.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.saludencasa.ApiServices.ClienteService
import com.example.saludencasa.Constante.urlUbicMedic
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class EditarPerfilTrabajador : AppCompatActivity() {

    interface ApiServiceTipoSangre {
        @GET("TipoSangre/")
        fun obtenerDatos(): Call<List<clsTipoSangre>>
    }

    interface ApiServiceGenero {
        @GET("Sexo/")
        fun obtenerDatos(): Call<List<clsGenero>>
    }

    interface ApiServicePais {
        @GET("Pais/")
        fun obtenerDatos(): Call<List<clsPais>>
    }

    interface ApiServiceProvincia {
        @GET("Provincia/")
        fun obtenerDatos(): Call<List<clsProvincia>>
    }

    interface ApiServiceCiudad {
        @GET("Ciudad/")
        fun obtenerDatos(): Call<List<clsCiudad>>
    }

    data class clsTipoSangre(val id_tiposangre: Int, val descripcion: String)
    data class clsGenero(val id_sexo: Int, val descripcion: String)
    data class clsPais(val id_pais: Int, val nombre: String)
    data class clsProvincia(val id_provincia: Int, val nombre: String)
    data class clsCiudad(val id_ciudad: Int, val nombre: String)

    var idTipoSangre: Int=0
    var idGenero: Int=0
    var idPais: Int=0
    var idProvincia: Int=0
    var idCiudad: Int=0
    private lateinit var spnTipoSangre: Spinner
    private lateinit var spnGenero: Spinner
    private lateinit var spnPais: Spinner
    private lateinit var spnProvincia: Spinner
    private lateinit var spnCiudad: Spinner
    private lateinit var apiTipoSangre: ApiServiceTipoSangre
    private lateinit var apiGenero: ApiServiceGenero
    private lateinit var apiPais: ApiServicePais
    private lateinit var apiProvincia: ApiServiceProvincia
    private lateinit var apiCiudad: ApiServiceCiudad
    private lateinit var txt_i_fecha: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil_trabajador)

        var wsNombre = ""
        var wsApellido = ""
        var wsPais = ""
        var wsCiudad = ""
        var wsFecha = ""
        var wsCedula = ""
        var wsReferencia = ""
        var wsTelefono = ""
        var boolFecha = false
        val txtcedula : EditText =findViewById(R.id.txtCedulaET)
        val txtnombreD : EditText =findViewById(R.id.txtNombreET)
        val txtapellidoD : EditText =findViewById(R.id.txtApellidoET)
        val txtFechaET : EditText =findViewById(R.id.txtFechaET)
        val txtTelefono : EditText =findViewById(R.id.txtTelefonoET)
        val txtUbicacion : EditText =findViewById(R.id.txtUbicacionET)
        val txtReferencia : EditText =findViewById(R.id.txtReferenciaET)
        spnTipoSangre = findViewById(R.id.spnTipoSangreC)
        spnGenero = findViewById(R.id.spnEnfermedad)
        spnPais = findViewById(R.id.spnPais)
        spnProvincia = findViewById(R.id.spnProvincia)
        spnCiudad = findViewById(R.id.spnCiudad)
        spnTipoSangre.isFocusable = false
        spnGenero.isFocusable = false

        txt_i_fecha = findViewById(R.id.txt_i_Fecha)
        txt_i_fecha.setEndIconOnClickListener {
            if (boolFecha){
                obtenerFecha()
            }
        }

        val llCedula : LinearLayout =findViewById(R.id.llOpcionesCedula)
        val llNombre : LinearLayout =findViewById(R.id.llOpcionesNombre)
        val llApellido : LinearLayout =findViewById(R.id.llEditApellido)
        val llFecha : LinearLayout =findViewById(R.id.llOpcionesFecha)
        val llGenero : LinearLayout =findViewById(R.id.llOpcionesSexo)
        val llTelefono : LinearLayout =findViewById(R.id.llOpcionesTelefono)
        val llSangre : LinearLayout =findViewById(R.id.llOpcionesTipoSangre)
        val llUbicacion : LinearLayout =findViewById(R.id.llOpcionesUbicacion)
        val llEditUbicacion : LinearLayout =findViewById(R.id.llEditUbicacion)
        val llPais : LinearLayout =findViewById(R.id.llEditPais)
        val llProvincia : LinearLayout =findViewById(R.id.llEditProvincia)
        val llCiudad : LinearLayout =findViewById(R.id.llEditCiudad)
        val llReferencia : LinearLayout =findViewById(R.id.llEditReferencia)
        llCedula.visibility = View.GONE
        llNombre.visibility = View.GONE
        llFecha.visibility = View.GONE
        llApellido.visibility = View.GONE
        llGenero.visibility = View.GONE
        llTelefono.visibility = View.GONE
        llUbicacion.visibility = View.GONE
        llSangre.visibility = View.GONE
        llPais.visibility = View.GONE
        llProvincia.visibility = View.GONE
        llCiudad.visibility = View.GONE
        llReferencia.visibility = View.GONE

        val btnEditCedula : ImageButton =findViewById(R.id.btnEditCedula)
        val btnEditNombre : ImageButton =findViewById(R.id.btnEditNombre)
        val btnEditFecha : ImageButton =findViewById(R.id.btnEditFecha)
        val btnEditSangre : ImageButton =findViewById(R.id.btnEditTipoSangre)
        val btnEditGenero : ImageButton =findViewById(R.id.btnEditSexo)
        val btnEditTelefono : ImageButton =findViewById(R.id.btnEditTelefono)
        val btnEditUbicacion : ImageButton =findViewById(R.id.btnEditUbicacion)
        btnEditCedula.setOnClickListener {
            txtcedula.isEnabled = true
            llCedula.visibility = View.VISIBLE
            btnEditCedula.visibility = View.GONE}
        btnEditNombre.setOnClickListener {
            txtapellidoD.isEnabled = true
            txtnombreD.isEnabled = true
            txtnombreD.setText(wsNombre)
            llNombre.visibility = View.VISIBLE
            llApellido.visibility = View.VISIBLE
            btnEditNombre.visibility = View.GONE}
        btnEditFecha.setOnClickListener {
            boolFecha = true
            txtFechaET.isEnabled = true
            llFecha.visibility = View.VISIBLE
            btnEditFecha.visibility = View.GONE}
        btnEditTelefono.setOnClickListener {
            txtTelefono.isEnabled = true
            llTelefono.visibility = View.VISIBLE
            btnEditTelefono.visibility = View.GONE}
        btnEditSangre.setOnClickListener {
            spnTipoSangre.isFocusable = true
            llSangre.visibility = View.VISIBLE
            btnEditSangre.visibility = View.GONE}
        btnEditGenero.setOnClickListener {
            spnGenero.isFocusable = true
            llGenero.visibility = View.VISIBLE
            btnEditGenero.visibility = View.GONE}
        btnEditUbicacion.setOnClickListener {
            llEditUbicacion.visibility = View.GONE
            llReferencia.visibility = View.VISIBLE
            llUbicacion.visibility = View.VISIBLE
            llPais.visibility = View.VISIBLE
            llProvincia.visibility = View.VISIBLE
            llCiudad.visibility = View.VISIBLE
            btnEditUbicacion.visibility = View.GONE}

        val btnCancelCedula : Button =findViewById(R.id.btnCancelarCedula)
        val btnCancelNombre : Button =findViewById(R.id.btnCancelarNombre)
        val btnCancelFecha : Button =findViewById(R.id.btnCancelarFecha)
        val btnCancelTelefono : Button =findViewById(R.id.btnCancelarTelefono)
        val btnCancelGenero : Button =findViewById(R.id.btnCancelarSexo)
        val btnCancelSangre : Button =findViewById(R.id.btnCancelarTipoSangre)
        val btnCancelUbicacion : Button =findViewById(R.id.btnCancelarUbicacion)
        btnCancelCedula.setOnClickListener {
            txtcedula.isEnabled = false
            llCedula.visibility = View.GONE
            btnEditCedula.visibility = View.VISIBLE}
        btnCancelNombre.setOnClickListener {
            txtapellidoD.isEnabled = false
            txtnombreD.isEnabled = false
            txtnombreD.setText(wsNombre + " " + wsApellido)
            llNombre.visibility = View.GONE
            llApellido.visibility = View.GONE
            btnEditNombre.visibility = View.VISIBLE}
        btnCancelFecha.setOnClickListener {
            txtFechaET.isEnabled = false
            txt_i_fecha.isEnabled = false
            llFecha.visibility = View.GONE
            btnEditFecha.visibility = View.VISIBLE}
        btnCancelTelefono.setOnClickListener {
            txtTelefono.isEnabled = false
            llTelefono.visibility = View.GONE
            btnEditTelefono.visibility = View.VISIBLE}
        btnCancelSangre.setOnClickListener {
            spnTipoSangre.isFocusable = false
            llSangre.visibility = View.GONE
            btnEditSangre.visibility = View.VISIBLE}
        btnCancelGenero.setOnClickListener {
            spnGenero.isFocusable = false
            llGenero.visibility = View.GONE
            btnEditGenero.visibility = View.VISIBLE}
        btnCancelUbicacion.setOnClickListener {
            llEditUbicacion.visibility = View.VISIBLE
            llReferencia.visibility = View.GONE
            llUbicacion.visibility = View.GONE
            llPais.visibility = View.GONE
            llProvincia.visibility = View.GONE
            llCiudad.visibility = View.GONE
            btnEditUbicacion.visibility = View.VISIBLE}


        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiTipoSangre = retrofit.create(ApiServiceTipoSangre::class.java)
        apiGenero = retrofit.create(ApiServiceGenero::class.java)
        apiPais = retrofit.create(ApiServicePais::class.java)
        apiProvincia = retrofit.create(ApiServiceProvincia::class.java)
        apiCiudad = retrofit.create(ApiServiceCiudad::class.java)

        obtenerDatosDelWebServiceTipoSangre()
        obtenerDatosDelWebServiceGenero()
        obtenerDatosDelWebServicePais()
        obtenerDatosDelWebServiceProvincia()
        obtenerDatosDelWebServiceCiudad()

        val apiServiceCliente = retrofit.create(ClienteService::class.java)
        var idCliente: Int = intent.extras!!.getInt("IDCLIENTE",0)

        val btnGuardarNombre : Button =findViewById(R.id.btnGuardarNombre)
        val btnGuardarFecha : Button =findViewById(R.id.btnGuardarFecha)
        val btnGuardarTelefono : Button =findViewById(R.id.btnGuardarTelefono)
        val btnGuardarGenero : Button =findViewById(R.id.btnGuardarSexo)
        val btnGuardarTipoSangre : Button =findViewById(R.id.btnGuardarTipoSangre)
        val btnGuardarUbicacion : Button =findViewById(R.id.btnGuardarUbicacion)
        btnGuardarNombre.setOnClickListener {
            val jsonObjectCliente = JSONObject()
            jsonObjectCliente.put("id_cliente", idCliente)
            jsonObjectCliente.put("cedula", wsCedula)
            jsonObjectCliente.put("nombre", txtnombreD.text.toString())
            jsonObjectCliente.put("apellido", txtapellidoD.text.toString())
            jsonObjectCliente.put("fecha_nacimiento", wsFecha)
            jsonObjectCliente.put("sexo", idGenero)
            jsonObjectCliente.put("telefono", wsTelefono)
            jsonObjectCliente.put("pais", idPais)
            jsonObjectCliente.put("provincia", idProvincia)
            jsonObjectCliente.put("ciudad", idCiudad)
            jsonObjectCliente.put("referencia_de_domicilio",wsReferencia)
            jsonObjectCliente.put("tipo_sangre", idTipoSangre)
            val jsonStringCliente = jsonObjectCliente.toString()
            val requestBodyCliente = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCliente)
            runBlocking {
                try {
                    val responseCliente = apiServiceCliente.putCliente(idCliente,requestBodyCliente)
                    if (responseCliente.isSuccessful) {
                        Toast.makeText(applicationContext,"Se actualizaron los datos",Toast.LENGTH_SHORT).show()
                        wsNombre = txtnombreD.text.toString()
                        wsApellido = txtapellidoD.text.toString()
                        txtapellidoD.isEnabled = false
                        txtnombreD.isEnabled = false
                        txtnombreD.setText(wsNombre + " " + wsApellido)
                        llNombre.visibility = View.GONE
                        llApellido.visibility = View.GONE
                        btnEditNombre.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        btnGuardarFecha.setOnClickListener {
            val jsonObjectCliente = JSONObject()
            jsonObjectCliente.put("id_cliente", idCliente)
            jsonObjectCliente.put("cedula", wsCedula)
            jsonObjectCliente.put("nombre", wsNombre)
            jsonObjectCliente.put("apellido", wsApellido)
            jsonObjectCliente.put("fecha_nacimiento", txtFechaET.text.toString())
            jsonObjectCliente.put("sexo", idGenero)
            jsonObjectCliente.put("telefono", wsTelefono)
            jsonObjectCliente.put("pais", idPais)
            jsonObjectCliente.put("provincia", idProvincia)
            jsonObjectCliente.put("ciudad", idCiudad)
            jsonObjectCliente.put("referencia_de_domicilio",wsReferencia)
            jsonObjectCliente.put("tipo_sangre", idTipoSangre)
            val jsonStringCliente = jsonObjectCliente.toString()
            val requestBodyCliente = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCliente)
            runBlocking {
                try {
                    val responseCliente = apiServiceCliente.putCliente(idCliente,requestBodyCliente)
                    if (responseCliente.isSuccessful) {
                        Toast.makeText(applicationContext,"Se actualizaron los datos",Toast.LENGTH_SHORT).show()
                        txtFechaET.isEnabled = false
                        llFecha.visibility = View.GONE
                        btnEditFecha.visibility = View.VISIBLE
                        wsFecha = txtFechaET.text.toString()
                        txtFechaET.setText(wsFecha)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        btnGuardarTelefono.setOnClickListener {
            val jsonObjectCliente = JSONObject()
            jsonObjectCliente.put("id_cliente", idCliente)
            jsonObjectCliente.put("cedula", wsCedula)
            jsonObjectCliente.put("nombre", wsNombre)
            jsonObjectCliente.put("apellido", wsApellido)
            jsonObjectCliente.put("fecha_nacimiento", wsFecha)
            jsonObjectCliente.put("sexo", idGenero)
            jsonObjectCliente.put("telefono", txtTelefono.text.toString())
            jsonObjectCliente.put("pais", idPais)
            jsonObjectCliente.put("provincia", idProvincia)
            jsonObjectCliente.put("ciudad", idCiudad)
            jsonObjectCliente.put("referencia_de_domicilio",wsReferencia)
            jsonObjectCliente.put("tipo_sangre", idTipoSangre)
            val jsonStringCliente = jsonObjectCliente.toString()
            val requestBodyCliente = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCliente)
            runBlocking {
                try {
                    val responseCliente = apiServiceCliente.putCliente(idCliente,requestBodyCliente)
                    if (responseCliente.isSuccessful) {
                        Toast.makeText(applicationContext,"Se actualizaron los datos",Toast.LENGTH_SHORT).show()
                        txtTelefono.isEnabled = false
                        txt_i_fecha.isEnabled = false
                        llTelefono.visibility = View.GONE
                        btnEditTelefono.visibility = View.VISIBLE
                        wsTelefono = txtTelefono.text.toString()
                        txtTelefono.setText(wsTelefono)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        btnGuardarGenero.setOnClickListener {
            val jsonObjectCliente = JSONObject()
            jsonObjectCliente.put("id_cliente", idCliente)
            jsonObjectCliente.put("cedula", wsCedula)
            jsonObjectCliente.put("nombre", wsNombre)
            jsonObjectCliente.put("apellido", wsApellido)
            jsonObjectCliente.put("fecha_nacimiento", wsFecha)
            jsonObjectCliente.put("sexo", idGenero)
            jsonObjectCliente.put("telefono", wsTelefono)
            jsonObjectCliente.put("pais", idPais)
            jsonObjectCliente.put("provincia", idProvincia)
            jsonObjectCliente.put("ciudad", idCiudad)
            jsonObjectCliente.put("referencia_de_domicilio",wsReferencia)
            jsonObjectCliente.put("tipo_sangre", idTipoSangre)
            val jsonStringCliente = jsonObjectCliente.toString()
            val requestBodyCliente = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCliente)
            runBlocking {
                try {
                    val responseCliente = apiServiceCliente.putCliente(idCliente,requestBodyCliente)
                    if (responseCliente.isSuccessful) {
                        Toast.makeText(applicationContext,"Se actualizaron los datos",Toast.LENGTH_SHORT).show()
                        spnGenero.isFocusable = false
                        llGenero.visibility = View.GONE
                        btnEditGenero.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        btnGuardarTipoSangre.setOnClickListener {
            val jsonObjectCliente = JSONObject()
            jsonObjectCliente.put("id_cliente", idCliente)
            jsonObjectCliente.put("cedula", wsCedula)
            jsonObjectCliente.put("nombre", wsNombre)
            jsonObjectCliente.put("apellido", wsApellido)
            jsonObjectCliente.put("fecha_nacimiento", wsFecha)
            jsonObjectCliente.put("sexo", idGenero)
            jsonObjectCliente.put("telefono", wsTelefono)
            jsonObjectCliente.put("pais", idPais)
            jsonObjectCliente.put("provincia", idProvincia)
            jsonObjectCliente.put("ciudad", idCiudad)
            jsonObjectCliente.put("referencia_de_domicilio",wsReferencia)
            jsonObjectCliente.put("tipo_sangre", idTipoSangre)
            val jsonStringCliente = jsonObjectCliente.toString()
            val requestBodyCliente = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCliente)
            runBlocking {
                try {
                    val responseCliente = apiServiceCliente.putCliente(idCliente,requestBodyCliente)
                    if (responseCliente.isSuccessful) {
                        Toast.makeText(applicationContext,"Se actualizaron los datos",Toast.LENGTH_SHORT).show()
                        spnTipoSangre.isFocusable = false
                        llSangre.visibility = View.GONE
                        btnEditSangre.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        btnGuardarUbicacion.setOnClickListener {
            val jsonObjectCliente = JSONObject()
            jsonObjectCliente.put("id_cliente", idCliente)
            jsonObjectCliente.put("cedula", wsCedula)
            jsonObjectCliente.put("nombre", wsNombre)
            jsonObjectCliente.put("apellido", wsApellido)
            jsonObjectCliente.put("fecha_nacimiento", wsFecha)
            jsonObjectCliente.put("sexo", idGenero)
            jsonObjectCliente.put("telefono", wsTelefono)
            jsonObjectCliente.put("pais", idPais)
            jsonObjectCliente.put("provincia", idProvincia)
            jsonObjectCliente.put("ciudad", idCiudad)
            jsonObjectCliente.put("referencia_de_domicilio",wsReferencia)
            jsonObjectCliente.put("tipo_sangre", idTipoSangre)
            val jsonStringCliente = jsonObjectCliente.toString()
            val requestBodyCliente = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCliente)
            runBlocking {
                try {
                    val responseCliente = apiServiceCliente.putCliente(idCliente,requestBodyCliente)
                    if (responseCliente.isSuccessful) {
                        Toast.makeText(applicationContext,"Se actualizaron los datos",Toast.LENGTH_SHORT).show()
                        llEditUbicacion.visibility = View.VISIBLE
                        llReferencia.visibility = View.GONE
                        llUbicacion.visibility = View.GONE
                        llPais.visibility = View.GONE
                        llProvincia.visibility = View.GONE
                        llCiudad.visibility = View.GONE
                        wsPais = spnPais.selectedItem.toString()
                        wsCiudad = spnCiudad.selectedItem.toString()
                        txtUbicacion.setText(wsPais + ", " + wsCiudad)
                        btnEditUbicacion.visibility = View.VISIBLE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        lifecycleScope.launch {
            try {
                val response = apiServiceCliente.getCliente()
                for (dataItem in response) {
                    wsNombre = dataItem.nombre
                    wsApellido = dataItem.apellido
                    val wsIdCliente = dataItem.id_cliente
                    if (wsIdCliente == idCliente) {
                        wsPais = dataItem.paisdescrip
                        wsCiudad = dataItem.ciudaddescrip
                        wsFecha = dataItem.fecha_nacimiento
                        wsCedula = dataItem.cedula
                        wsReferencia = dataItem.referencia_de_domicilio
                        wsTelefono = dataItem.telefono
                        txtReferencia.setText(dataItem.referencia_de_domicilio)
                        txtUbicacion.setText(wsPais + ", " + wsCiudad)
                        txtcedula.setText(dataItem.cedula)
                        txtnombreD.setText(wsNombre + " " + wsApellido)
                        txtapellidoD.setText(wsApellido)
                        txtFechaET.setText(dataItem.fecha_nacimiento)
                        spnGenero.setSelection(dataItem.sexo-1)
                        spnTipoSangre.setSelection(dataItem.tipo_sangre-1)
                        txtTelefono.setText(dataItem.telefono)
                        spnPais.setSelection(dataItem.pais-1)
                        spnProvincia.setSelection(dataItem.provincia-1)
                        spnCiudad.setSelection(dataItem.ciudad-1)
                        idProvincia = dataItem.provincia
                        idCiudad = dataItem.ciudad
                        idPais = dataItem.pais
                        idGenero = dataItem.sexo
                        idTipoSangre = dataItem.tipo_sangre
                        //foto
                        val imgPerfil : ImageView = findViewById(R.id.imgFotoPerfil)
                        var imagen: String = dataItem.foto
                        if (dataItem.foto != null) {
                            imgPerfil.load(imagen)
                        } else {
                            imgPerfil.setImageResource(R.drawable.foto_perfil_trabajador)
                        }
                        break
                    }
                }
            } catch (e: Exception) {
                // Maneja cualquier error que pueda ocurrir
                e.printStackTrace()
            }
        }
    }

    private fun obtenerFecha() {
        val calendario = Calendar.getInstance()
        val año = calendario[Calendar.YEAR]
        val mes = calendario[Calendar.MONTH]
        val día = calendario[Calendar.DAY_OF_MONTH]
        val datePickerDialog = DatePickerDialog(this,
            { view, year, month, dayOfMonth -> // Aquí puedes obtener la fecha seleccionada y actualizar el campo de fecha
                val selectedDate = year.toString() + "-" + (month + 1) + "-" + dayOfMonth.toString()
                txt_i_fecha.editText?.setText(selectedDate)
            }, año, mes, día
        )
        datePickerDialog.show()
    }

    private fun obtenerDatosDelWebServiceTipoSangre() {
        val call = apiTipoSangre.obtenerDatos()
        call.enqueue(object : Callback<List<clsTipoSangre>> {
            override fun onResponse(
                call: Call<List<clsTipoSangre>>,
                response: Response<List<clsTipoSangre>>
            ) {
                if (response.isSuccessful) {
                    val datos = response.body()
                    if (datos != null) {
                        val nombres = datos.map { it.descripcion }
                        val ids = datos.map { it.id_tiposangre }

                        val adapter = ArrayAdapter(
                            this@EditarPerfilTrabajador,
                            android.R.layout.simple_spinner_item,
                            nombres
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnTipoSangre.adapter = adapter

                        spnTipoSangre.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                idTipoSangre = ids[position]
                            }
                            override fun onNothingSelected(parent: AdapterView<*>?) {
                            }
                        }
                    }
                } else {
                    // Manejar la respuesta no exitosa
                }
            }

            override fun onFailure(call: Call<List<clsTipoSangre>>, t: Throwable) {
                // Manejar el error de la solicitud
            }
        })
    }

    private fun obtenerDatosDelWebServiceGenero() {
        val call = apiGenero.obtenerDatos()
        call.enqueue(object : Callback<List<clsGenero>> {
            override fun onResponse(
                call: Call<List<clsGenero>>,
                response: Response<List<clsGenero>>
            ) {
                if (response.isSuccessful) {
                    val datos = response.body()
                    if (datos != null) {
                        val nombres = datos.map { it.descripcion }
                        val ids = datos.map { it.id_sexo }

                        val adapter = ArrayAdapter(
                            this@EditarPerfilTrabajador,
                            android.R.layout.simple_spinner_item,
                            nombres
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnGenero.adapter = adapter

                        spnGenero.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                idGenero = ids[position]
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                // No se seleccionó ningún elemento
                            }
                        }
                    }
                } else {
                    // Manejar la respuesta no exitosa
                }
            }

            override fun onFailure(call: Call<List<clsGenero>>, t: Throwable) {
                // Manejar el error de la solicitud
            }
        })
    }

    private fun obtenerDatosDelWebServicePais() {
        val call = apiPais.obtenerDatos()
        call.enqueue(object : Callback<List<clsPais>> {
            override fun onResponse(
                call: Call<List<clsPais>>,
                response: Response<List<clsPais>>
            ) {
                if (response.isSuccessful) {
                    val datos = response.body()
                    if (datos != null) {
                        val nombres = datos.map { it.nombre }
                        val ids = datos.map { it.id_pais }

                        val adapter = ArrayAdapter(
                            this@EditarPerfilTrabajador,
                            android.R.layout.simple_spinner_item,
                            nombres
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnPais.adapter = adapter

                        spnPais.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                idPais = ids[position]
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                // No se seleccionó ningún elemento
                            }
                        }
                    }
                } else {
                    // Manejar la respuesta no exitosa
                }
            }

            override fun onFailure(call: Call<List<clsPais>>, t: Throwable) {
                // Manejar el error de la solicitud
            }
        })
    }

    private fun obtenerDatosDelWebServiceProvincia() {
        val call = apiProvincia.obtenerDatos()
        call.enqueue(object : Callback<List<clsProvincia>> {
            override fun onResponse(
                call: Call<List<clsProvincia>>,
                response: Response<List<clsProvincia>>
            ) {
                if (response.isSuccessful) {
                    val datos = response.body()
                    if (datos != null) {
                        val nombres = datos.map { it.nombre }
                        val ids = datos.map { it.id_provincia }

                        val adapter = ArrayAdapter(
                            this@EditarPerfilTrabajador,
                            android.R.layout.simple_spinner_item,
                            nombres
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnProvincia.adapter = adapter

                        spnProvincia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                idProvincia = ids[position]
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                // No se seleccionó ningún elemento
                            }
                        }
                    }
                } else {
                    // Manejar la respuesta no exitosa
                }
            }

            override fun onFailure(call: Call<List<clsProvincia>>, t: Throwable) {
                // Manejar el error de la solicitud
            }
        })
    }

    private fun obtenerDatosDelWebServiceCiudad() {
        val call = apiCiudad.obtenerDatos()
        call.enqueue(object : Callback<List<clsCiudad>> {
            override fun onResponse(
                call: Call<List<clsCiudad>>,
                response: Response<List<clsCiudad>>
            ) {
                if (response.isSuccessful) {
                    val datos = response.body()
                    if (datos != null) {
                        val nombres = datos.map { it.nombre }
                        val ids = datos.map { it.id_ciudad }

                        val adapter = ArrayAdapter(
                            this@EditarPerfilTrabajador,
                            android.R.layout.simple_spinner_item,
                            nombres
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnCiudad.adapter = adapter

                        spnCiudad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                idCiudad = ids[position]
                            }

                            override fun onNothingSelected(parent: AdapterView<*>?) {
                                // No se seleccionó ningún elemento
                            }
                        }
                    }
                } else {
                    // Manejar la respuesta no exitosa
                }
            }

            override fun onFailure(call: Call<List<clsCiudad>>, t: Throwable) {
                // Manejar el error de la solicitud
            }
        })
    }
}
package com.example.saludencasa

import android.app.DatePickerDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.icu.util.Calendar
import org.json.JSONObject
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.saludencasa.ApiServices.ClienteService
import com.example.saludencasa.ApiServices.LoginService
import com.example.saludencasa.Constante.urlUbicMedic
import com.example.saludencasa.Modelo.Clientes
import com.google.android.material.textfield.TextInputLayout
import com.shuhart.stepview.StepView
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import java.text.SimpleDateFormat
import java.util.Locale

class CrearCuentaCliente : AppCompatActivity() {

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

    val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            ImagenPerfil.setImageURI(uri)
        } else {
            // no seleccionada
        }
    }
    lateinit var btnFotoPerfil: Button
    lateinit var ImagenPerfil: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var fondoProgress: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_cuenta_cliente)

        progressBar = findViewById(R.id.progressBar)
        fondoProgress = findViewById(R.id.fondoProgress)
        deshabilitarInteraccion()

        spnTipoSangre = findViewById(R.id.spnTipoSangreC)
        spnGenero = findViewById(R.id.spnEnfermedad)
        spnPais = findViewById(R.id.spnPaisC)
        spnProvincia = findViewById(R.id.spnProvinciaC)
        spnCiudad = findViewById(R.id.spnCiudadC)

        val retrofitGet = Retrofit.Builder()
            .baseUrl(urlUbicMedic) // Reemplaza con la URL de tu web service
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiTipoSangre = retrofitGet.create(ApiServiceTipoSangre::class.java)
        apiGenero = retrofitGet.create(ApiServiceGenero::class.java)
        apiPais = retrofitGet.create(ApiServicePais::class.java)
        apiProvincia = retrofitGet.create(ApiServiceProvincia::class.java)
        apiCiudad = retrofitGet.create(ApiServiceCiudad::class.java)

        obtenerDatosDelWebServiceTipoSangre()
        obtenerDatosDelWebServiceGenero()
        obtenerDatosDelWebServicePais()
        obtenerDatosDelWebServiceProvincia()
        obtenerDatosDelWebServiceCiudad()

        btnFotoPerfil = findViewById(R.id.btnFotoPerfil)
        ImagenPerfil = findViewById(R.id.ImagenPerfil)

        btnFotoPerfil.setOnClickListener {
            pickMedia.launch("image/*")
        }

        var stepView: StepView = findViewById(R.id.stepView)

        stepView.getState().steps(listOf(
            "Perfil",
            "Ubicación",
            "Login"
        )).commit()
        stepView.go(0, true);

        var txtvPasoTitulo: TextView = findViewById(R.id.txtvPasoTitulo)
        var txtvPasoSubTitulo: TextView = findViewById(R.id.txtvPasoSubTitulo)
        var llDatosPersonales: LinearLayout = findViewById(R.id.llDatosPersonales)
        llDatosPersonales.visibility = View.VISIBLE
        var llUbicacion: LinearLayout = findViewById(R.id.llUbicacion)
        llUbicacion.visibility = View.GONE
        var llCredenciales: LinearLayout = findViewById(R.id.llCredenciales)
        llCredenciales.visibility = View.GONE


        var txtCedula: EditText = findViewById(R.id.txtCedulaC)
        var txtNombre: EditText = findViewById(R.id.txtNombreC)
        var txtApellido: EditText = findViewById(R.id.txtApellidoC)
        var txtNacimiento: EditText = findViewById(R.id.txtFechaET)
        var txtTelefono: EditText = findViewById(R.id.txtTelefonoC)
        var txtReferencia: EditText = findViewById(R.id.txtReferenciaDomicilioC)
        var txtCorreo: EditText = findViewById(R.id.txtCorreoC)
        var txtClave: EditText = findViewById(R.id.txtClaveC)
        var txtConfirmar: EditText = findViewById(R.id.txtConfirmarClaveC)

        var cedula = txtCedula.text.toString()
        var nombre = txtNombre.text.toString()
        var apellido = txtApellido.text.toString()
        var fecha = txtNacimiento.text.toString()
        var telefono = txtTelefono.text.toString()
        var referencia = txtReferencia.text.toString()
        var correo = txtCorreo.text.toString()
        var clave = txtClave.text.toString()
        var confirmar = txtConfirmar.text.toString()
        val jsonObjectCliente = JSONObject()
        val jsonObjectLogin = JSONObject()


        txt_i_fecha = findViewById<TextInputLayout>(R.id.txt_i_Fecha)
        txt_i_fecha.setEndIconOnClickListener {
            obtenerFecha()
        }


        var btnCrear2: Button = findViewById(R.id.btnCrearCuenta)
        btnCrear2.visibility = View.GONE

        habilitarInteraccion()

        val apiServiceCliente = retrofitGet.create(ClienteService::class.java)
        val apiServiceLogin = retrofitGet.create(LoginService::class.java)
        val listCedula = mutableListOf<String>()
        val listCorreo = mutableListOf<String>()

        lifecycleScope.launch {
            //Datos Cedula
            try {
                val response = apiServiceCliente.getCliente()
                for (dataItem in response) {
                    listCedula.add(dataItem.cedula)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            //Datos Correo
            try {
                val response = apiServiceLogin.getLogin()
                for (dataItem in response) {
                    listCorreo.add(dataItem.usuario)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        var btnContinuar: Button = findViewById(R.id.btnContinuar)
        btnContinuar.setOnClickListener {
                if (stepView.currentStep == 0) {
                    val txt_i_cedula = findViewById<TextInputLayout>(R.id.cedula_text_input_layout)
                    val txt_i_nombre = findViewById<TextInputLayout>(R.id.nombre_text_input_layout)
                    val txt_i_apellido = findViewById<TextInputLayout>(R.id.apellido_text_input_layout)
                    val txt_i_telefono = findViewById<TextInputLayout>(R.id.telefono_text_input_layout)
                    cedula = txtCedula.text.toString()
                    nombre = txtNombre.text.toString()
                    apellido = txtApellido.text.toString()
                    fecha = txtNacimiento.text.toString()
                    telefono = txtTelefono.text.toString()
                    var comprobarCedula = true
                    for (dataItem in listCedula){
                        if (cedula == dataItem){
                            comprobarCedula = false
                        }
                    }
                    if(comprobarCedula){
                        txt_i_cedula.error=null
                        txt_i_cedula.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                        if (cedula.trim().isEmpty() || nombre.trim().isEmpty() || apellido.trim().isEmpty() ||
                            fecha.trim().isEmpty() || telefono.trim().isEmpty()){

                            //cedula
                            if (cedula.isEmpty()){
                                txt_i_cedula.error="El campo no puede estar vacio"
                                txt_i_cedula.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                            }
                            else{
                                txt_i_cedula.error=null
                                txt_i_cedula.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                            }
                            //nombre
                            if (nombre.isEmpty()){
                                txt_i_nombre.error="El campo no puede estar vacio"
                                txt_i_nombre.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                            }
                            else{
                                txt_i_nombre.error=null
                                txt_i_nombre.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                            }
                            //apellido
                            if (apellido.isEmpty()){
                                txt_i_apellido.error="El campo no puede estar vacio"
                                txt_i_apellido.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                            }
                            else{
                                txt_i_apellido.error=null
                                txt_i_apellido.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                            }
                            //fecha
                            if (fecha.isEmpty()){
                                txt_i_fecha.error="El campo no puede estar vacio"
                                txt_i_fecha.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                            }
                            else{
                                txt_i_fecha.error=null
                                txt_i_fecha.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                            }
                            //telefono
                            if (telefono.isEmpty()){
                                txt_i_telefono.error="El campo no puede estar vacio"
                                txt_i_telefono.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                            }
                            else{
                                txt_i_telefono.error=null
                                txt_i_telefono.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                            }

                        }
                        else{
                            val imgRegistro: ImageView = findViewById(R.id.imgRegistro)
                            imgRegistro.setImageDrawable(
                                resources.getDrawable(
                                    R.drawable.registro_ubicacion,
                                    null
                                )
                            )
                            llDatosPersonales.visibility = View.GONE
                            llUbicacion.visibility = View.VISIBLE
                            txtvPasoTitulo.setText("Datos de ubicación")
                            txtvPasoSubTitulo.setText("Completa la siguiente informacion o usa el mapa.")
                            stepView.go(1, true);
                            stepView.requestFocus()
                        }
                    }else{
                        txt_i_cedula.error="Cedula ya existente"
                        txt_i_cedula.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                    }


                }
                else if (stepView.currentStep == 1) {
                    val txt_i_referencia = findViewById<TextInputLayout>(R.id.referenciaDomicilio_text_input_layout)
                    referencia = txtReferencia.text.toString()
                    if (referencia.isEmpty()){
                        txt_i_referencia.error="El campo no puede estar vacio"
                        txt_i_referencia.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                    }
                    else{
                        txt_i_referencia.error=null
                        txt_i_referencia.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                        val imgRegistro: ImageView = findViewById(R.id.imgRegistro)
                        imgRegistro.setImageDrawable(
                            resources.getDrawable(
                                R.drawable.registro_login,
                                null
                            )
                        )
                        llUbicacion.visibility = View.GONE
                        llCredenciales.visibility = View.VISIBLE
                        btnCrear2.visibility = View.VISIBLE
                        btnContinuar.visibility = View.GONE
                        txtvPasoTitulo.setText("Datos de credenciales")
                        txtvPasoSubTitulo.setText("Completa la siguiente informacion.")
                        stepView.go(2, true);
                        stepView.requestFocus()
                    }
                }
        }

        btnCrear2.setOnClickListener {

            var nuevaId: Int=0
            val intent: Intent = Intent(this, MainActivity:: class.java)
            //val txtvpasos2: TextView = findViewById(R.id.txtvPasos)
            val txt_i_correo = findViewById<TextInputLayout>(R.id.correo_text_input_layout)
            val txt_i_clave = findViewById<TextInputLayout>(R.id.clave_text_input_layout)
            val txt_i_confirmar = findViewById<TextInputLayout>(R.id.confirmarClave_text_input_layout)
            correo = txtCorreo.text.toString()
            clave = txtClave.text.toString()
            confirmar = txtConfirmar.text.toString()

            var comprobarCorreo = true
            for (dataItem in listCorreo){
                if (correo == dataItem){
                    comprobarCorreo = false
                }
            }
            if(comprobarCorreo){
                txt_i_correo.error=null
                txt_i_correo.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                if (correo.trim().isEmpty() || clave.trim().isEmpty() || confirmar.trim().isEmpty()){

                    //correo
                    if (correo.isEmpty()){
                        txt_i_correo.error="El campo no puede estar vacio"
                        txt_i_correo.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                    }
                    else{
                        txt_i_correo.error=null
                        txt_i_correo.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                    }
                    //clave
                    if (clave.isEmpty()){
                        txt_i_clave.error="El campo no puede estar vacio"
                        txt_i_clave.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                    }
                    else{
                        txt_i_clave.error=null
                        txt_i_clave.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                    }
                    //confirmar
                    if (confirmar.isEmpty()){
                        txt_i_confirmar.error="El campo no puede estar vacio"
                        txt_i_confirmar.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                    }
                    else{
                        txt_i_confirmar.error=null
                        txt_i_confirmar.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                    }

                }
                else{
                    if (clave.trim()!=confirmar.trim()){
                        val txtError = findViewById<TextView>(R.id.txtvError3)
                        txtError.visibility=View.VISIBLE
                    }
                    else{
                        val txtError = findViewById<TextView>(R.id.txtvError3)
                        txtError.visibility=View.GONE
                        jsonObjectCliente.put("cedula", cedula)
                        jsonObjectCliente.put("nombre", nombre)
                        jsonObjectCliente.put("apellido", apellido)
                        jsonObjectCliente.put("fecha_nacimiento", fecha)
                        jsonObjectCliente.put("sexo", idGenero)
                        jsonObjectCliente.put("telefono", telefono)
                        jsonObjectCliente.put("pais", idPais)
                        jsonObjectCliente.put("provincia", idProvincia)
                        jsonObjectCliente.put("ciudad", idCiudad)
                        jsonObjectCliente.put("referencia_de_domicilio",referencia)
                        jsonObjectCliente.put("tipo_sangre", idTipoSangre)

                        jsonObjectLogin.put("usuario", correo)
                        jsonObjectLogin.put("contrasenia", clave)
                        jsonObjectLogin.put("tipo_login", "Cliente")
                        jsonObjectLogin.put("estado", true)


                        val jsonStringCliente = jsonObjectCliente.toString()

                        val retrofit = Retrofit.Builder()
                            .baseUrl(urlUbicMedic)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build()


                        val apiServiceCliente = retrofit.create(ClienteService::class.java)
                        val requestBodyCliente = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCliente)


                        runBlocking {
                            try {
                                val responseCliente = apiServiceCliente.postCliente(requestBodyCliente)
                                if (responseCliente.isSuccessful) {
                                    val responseBody = responseCliente.body()
                                    val responseData = responseBody?.string()
                                    if (responseBody != null) {
                                        val jsonObject = JSONObject(responseData)
                                        nuevaId = jsonObject.getInt("id_cliente")
                                        jsonObjectLogin.put("id_cliente", nuevaId)
                                        val jsonStringLogin = jsonObjectLogin.toString()
                                        val apiServiceLogin = retrofit.create(LoginService::class.java)
                                        val requestBodyLogin = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringLogin)
                                        val responseLogin = apiServiceLogin.postLogin(requestBodyLogin)
                                        if (responseLogin.isSuccessful) {
                                            val responseBodyLogin = responseLogin.body()
                                            val responseDataLogin = responseBodyLogin?.string()
                                        }
                                        else {
                                            //txtvpasos2.setText( "Error en la solicitud. Código de error: ${responseLogin.code()}")
                                        }
                                    }
                                    startActivity(intent)
                                    //habilitarInteraccion()
                                } else {
                                    //txtvpasos2.setText( "Error en la solicitud. Código de error: ${responseCliente.code()}")
                                }


                            } catch (e: Exception) {
                                // Manejar el error aquí
                                e.printStackTrace()
                            }
                        }
                    }
                }
            }else{
                txt_i_correo.error="El correo ya existe"
                txt_i_correo.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
            }


        }

    }

    private fun deshabilitarInteraccion() {
        progressBar.visibility = View.VISIBLE
        fondoProgress.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        )
    }

    private fun habilitarInteraccion() {
        progressBar.visibility = View.GONE
        fondoProgress.visibility = View.GONE
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
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
                            this@CrearCuentaCliente,
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
                                // No se seleccionó ningún elemento
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
                            this@CrearCuentaCliente,
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
                            this@CrearCuentaCliente,
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
                            this@CrearCuentaCliente,
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
                            this@CrearCuentaCliente,
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
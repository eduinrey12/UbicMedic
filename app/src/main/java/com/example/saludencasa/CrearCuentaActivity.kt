package com.example.saludencasa

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.icu.util.Calendar
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import android.provider.OpenableColumns
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.saludencasa.ApiServices.ClienteService
import com.example.saludencasa.ApiServices.LoginService
import com.example.saludencasa.ApiServices.ProfesionesService
import com.example.saludencasa.ApiServices.TrabajadorService
import kotlinx.coroutines.runBlocking
import com.example.saludencasa.Constante.urlUbicMedic
import com.example.saludencasa.Modelo.Profesiones
import com.google.android.material.textfield.TextInputLayout
import com.shuhart.stepview.StepView
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.text.DecimalFormat

class CrearCuentaActivity : AppCompatActivity(), MapEventsReceiver {

    interface ApiServiceProfesionesxEmpleado {
        @POST("ProfesionesxTrabajador/")
        suspend fun postProfesiones(@Body requestBody: RequestBody): Response<ResponseBody>
    }

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

    var idTipoProfesiones: Int=0
    var idTipoSangre: Int=0
    var idGenero: Int=0
    var idPais: Int=0
    var idProvincia: Int=0
    var idCiudad: Int=0
    private lateinit var spnTipoProfesiones: Spinner
    private lateinit var spnTipoSangre: Spinner
    private lateinit var spnGenero: Spinner
    private lateinit var spnPais: Spinner
    private lateinit var spnProvincia: Spinner
    private lateinit var spnCiudad: Spinner
    private lateinit var apiTipoProfesiones: ProfesionesService
    private lateinit var apiTipoSangre: ApiServiceTipoSangre
    private lateinit var apiGenero: ApiServiceGenero
    private lateinit var apiPais: ApiServicePais
    private lateinit var apiProvincia: ApiServiceProvincia
    private lateinit var apiCiudad: ApiServiceCiudad

    private lateinit var mapView: MapView
    private lateinit var countryEditText: EditText
    private lateinit var cityEditText: EditText
    private lateinit var cityNameTextView: TextView
    private lateinit var myLocationOverlay: MyLocationNewOverlay
    private var country: String = ""
    private var city: String = ""

    private val PICK_PDF_REQUEST = 1
    private var selectedButton: Button? = null

    val pickMedia = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            ImagenPerfil.setImageURI(uri)
        } else {
            // no seleccionada
        }
    }
    lateinit var btnFotoPerfil: Button
    lateinit var ImagenPerfil: ImageView
    lateinit var btnSelectPDF: Button
    lateinit var button3: Button
    private lateinit var txt_i_fecha: TextInputLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var fondoProgress: View

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_cuenta)

        progressBar = findViewById(R.id.progressBar)
        fondoProgress = findViewById(R.id.fondoProgress)
        deshabilitarInteraccion()

        spnTipoProfesiones = findViewById(R.id.spnProfesión)
        spnTipoSangre = findViewById(R.id.spnTipoSangre)
        spnGenero = findViewById(R.id.spnGenero)
        spnPais = findViewById(R.id.spnPais)
        spnProvincia = findViewById(R.id.spnProvincia)
        spnCiudad = findViewById(R.id.spnCiudad)

        val retrofitGet = Retrofit.Builder()
            .baseUrl(urlUbicMedic) // Reemplaza con la URL de tu web service
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiTipoProfesiones = retrofitGet.create(ProfesionesService::class.java)
        apiTipoSangre = retrofitGet.create(ApiServiceTipoSangre::class.java)
        apiGenero = retrofitGet.create(ApiServiceGenero::class.java)
        apiPais = retrofitGet.create(ApiServicePais::class.java)
        apiProvincia = retrofitGet.create(ApiServiceProvincia::class.java)
        apiCiudad = retrofitGet.create(ApiServiceCiudad::class.java)

        obtenerDatosDelWebServiceTipoProfesiones()
        obtenerDatosDelWebServiceTipoSangre()
        obtenerDatosDelWebServiceGenero()
        obtenerDatosDelWebServicePais()
        obtenerDatosDelWebServiceProvincia()
        obtenerDatosDelWebServiceCiudad()

        var stepView: StepView = findViewById(R.id.stepView)

        stepView.getState().steps(listOf(
            "Perfil",
            "Ubicación",
            "Login",
            "Profesion"
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
        var llProfesion: LinearLayout = findViewById(R.id.llProfesiones)
        llProfesion.visibility = View.GONE
        var btnCrear2: Button = findViewById(R.id.btnCrearCuenta)
        btnCrear2.visibility = View.GONE

        var txtCedula: EditText = findViewById(R.id.txtCedula)
        var txtNombre: EditText = findViewById(R.id.txtNombre)
        var txtApellido: EditText = findViewById(R.id.txtApellido)
        var txtNacimiento: EditText = findViewById(R.id.txtFecha)
        var txtTelefono: EditText = findViewById(R.id.txtTelefono)
        var txtReferencia: EditText = findViewById(R.id.txtReferenciaDomicilio)
        var txtCorreo: EditText = findViewById(R.id.txtCorreo)
        var txtClave: EditText = findViewById(R.id.txtClave)
        var txtCoorY: EditText = findViewById(R.id.txtCoorY)
        var txtCoorX: EditText = findViewById(R.id.txtCoorX)
        var txtTitulo: EditText = findViewById(R.id.txtTitulo)
        var txtConfirmar: EditText = findViewById(R.id.txtConfirmarClave)

        var cedula = txtCedula.text.toString()
        var nombre = txtNombre.text.toString()
        var apellido = txtApellido.text.toString()
        var fecha = txtNacimiento.text.toString()
        var telefono = txtTelefono.text.toString()
        var referencia = txtReferencia.text.toString()
        var correo = txtCorreo.text.toString()
        var clave = txtClave.text.toString()
        var confirmar = txtConfirmar.text.toString()
        var coorX = txtCoorX.text.toString()
        var coorY = txtCoorY.text.toString()
        var titulo = txtTitulo.text.toString()
        val jsonObjectCliente = JSONObject()
        val jsonObjectLogin = JSONObject()
        val jsonObjectTrabajador = JSONObject()
        val jsonObjectProfesiones = JSONObject()

        mapView = findViewById(R.id.mapView)
        countryEditText = findViewById(R.id.txtCoorY)
        cityEditText = findViewById(R.id.txtCoorX)
        cityNameTextView = findViewById(R.id.cityNameTextView)

        requestPermissions()

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.controller.setZoom(16)

        mapView.setMultiTouchControls(true)

        val overlay = MapEventsOverlay(this)
        mapView.overlays.add(overlay)

        myLocationOverlay = MyLocationNewOverlay(mapView)
        myLocationOverlay.enableMyLocation()
        myLocationOverlay.enableFollowLocation()
        myLocationOverlay.isDrawAccuracyEnabled = true
        mapView.overlays.add(myLocationOverlay)

        mapView.addOnFirstLayoutListener(object : MapView.OnFirstLayoutListener {
            override fun onFirstLayout(v: View, left: Int, top: Int, right: Int, bottom: Int) {
                val startPoint = myLocationOverlay.myLocation
                if (startPoint != null) {
                    updateLocationInfo(startPoint.latitude, startPoint.longitude)
                }
            }
        })

        val startPoint = myLocationOverlay.myLocation
        if (startPoint != null) {
            updateLocationInfo(startPoint.latitude, startPoint.longitude)
        }

        val obtenerCoordenadasButton = findViewById<Button>(R.id.obtenerCoordenadasButton)
        obtenerCoordenadasButton.setOnClickListener {
            val currentLocation = myLocationOverlay.myLocation
            if (currentLocation != null) {
                updateLocationInfo(currentLocation.latitude, currentLocation.longitude)
                mapView.controller.setCenter(currentLocation)
                mapView.invalidate()
            }
        }

        btnFotoPerfil = findViewById(R.id.btnFotoPerfil)
        ImagenPerfil = findViewById(R.id.ImagenPerfil)
        btnSelectPDF = findViewById(R.id.btnSelectPDF)
        button3 = findViewById(R.id.button3)

        btnFotoPerfil.setOnClickListener {
            pickMedia.launch("image/*")
        }

        txt_i_fecha = findViewById<TextInputLayout>(R.id.txt_i_Fecha)
        txt_i_fecha.setEndIconOnClickListener {
            obtenerFecha()
        }

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
                        imgRegistro.requestFocus()
                    }
                }else{
                    txt_i_cedula.error="Cedula ya existente"
                    txt_i_cedula.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                }


            }
            else if (stepView.currentStep == 1) {
                val txt_i_referencia = findViewById<TextInputLayout>(R.id.referenciaDomicilio_text_input_layout)
                referencia = txtReferencia.text.toString()
                coorX = txtCoorX.text.toString()
                coorY = txtCoorY.text.toString()
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
                    txtvPasoTitulo.setText("Datos de credenciales")
                    txtvPasoSubTitulo.setText("Completa la siguiente informacion.")
                    stepView.go(2, true);
                    imgRegistro.requestFocus()
                }

            } else if (stepView.currentStep == 2) {
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

                    }else{
                        if (clave.trim()!=confirmar.trim()){
                            val txtError = findViewById<TextView>(R.id.txtvError3)
                            txtError.visibility=View.VISIBLE
                        }
                        else{
                            val imgRegistro: ImageView = findViewById(R.id.imgRegistro)
                            imgRegistro.setImageDrawable(
                                resources.getDrawable(
                                    R.drawable.registro_profesion,
                                    null
                                )
                            )
                            llCredenciales.visibility = View.GONE
                            llProfesion.visibility = View.VISIBLE
                            btnCrear2.visibility = View.VISIBLE
                            btnContinuar.visibility = View.GONE
                            txtvPasoTitulo.setText("Datos de credenciales")
                            txtvPasoSubTitulo.setText("Completa la siguiente informacion.")
                            stepView.go(3, true);
                            imgRegistro.requestFocus()
                        }
                    }
                }else{
                    txt_i_correo.error="El correo ya existe"
                    txt_i_correo.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                }
            }
        }

        btnSelectPDF.setOnClickListener {
            openFilePicker(btnSelectPDF)
        }

        button3.setOnClickListener {
            openFilePicker(button3)
        }

        btnCrear2.setOnClickListener {
            //deshabilitarInteraccion()
            titulo = txtTitulo.text.toString()
            var nuevaId: Int=0
            val intent: Intent = Intent(this, MainActivity:: class.java)
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
            //jsonObjectCliente.put("foto", "images/Captura_de_pantalla_2023-05-18_224751.png")

            jsonObjectLogin.put("usuario", correo)
            jsonObjectLogin.put("contrasenia", clave)
            jsonObjectLogin.put("tipo_login", "Cliente")
            jsonObjectLogin.put("estado", true)

            jsonObjectTrabajador.put("latitud", BigDecimal(coorY))
            jsonObjectTrabajador.put("longitud", BigDecimal(coorX))
            jsonObjectTrabajador.put("estado", 1)
            jsonObjectTrabajador.put("id_tipo_trabajador", 1)

            jsonObjectProfesiones.put("numero_titulo", titulo)
            jsonObjectProfesiones.put("id_profesiones", idTipoProfesiones)
            jsonObjectProfesiones.put("estado", true)

            val jsonStringCliente = jsonObjectCliente.toString()

            val retrofit = Retrofit.Builder()
                .baseUrl(urlUbicMedic)
                .addConverterFactory(GsonConverterFactory.create())
                .build()


            val apiServiceCliente = retrofit.create(ClienteService::class.java)
            val requestBodyCliente = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringCliente)


            runBlocking {
                try {
                    //Cliente
                    val responseCliente = apiServiceCliente.postCliente(requestBodyCliente)
                    if (responseCliente.isSuccessful) {
                        val responseBody = responseCliente.body()
                        val responseData = responseBody?.string()
                        if (responseBody != null) {
                            //Login
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
                                if (responseBodyLogin != null) {
                                    //Trabajador
                                    jsonObjectTrabajador.put("id_cliente", nuevaId)
                                    val jsonStringTrabajador = jsonObjectTrabajador.toString()
                                    val apiServiceTrabajador = retrofit.create(TrabajadorService::class.java)
                                    val requestBodyTrabajador = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringTrabajador)
                                    val responseTrabajador = apiServiceTrabajador.postTrabajador(requestBodyTrabajador)
                                    if (responseTrabajador.isSuccessful) {
                                        val responseBodyTrabajador = responseTrabajador.body()
                                        val responseDataTrabajador = responseBodyTrabajador?.string()
                                        if (responseBodyTrabajador != null) {
                                            //Profesiones
                                            val jsonObject = JSONObject(responseDataTrabajador)
                                            nuevaId = jsonObject.getInt("id_trabajador")
                                            jsonObjectProfesiones.put("id_trabajador", nuevaId)
                                            val jsonStringProfesiones = jsonObjectProfesiones.toString()
                                            val apiServiceProfesiones = retrofit.create(ApiServiceProfesionesxEmpleado::class.java)
                                            val requestBodyProfesiones = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringProfesiones)
                                            val responseProfesiones = apiServiceProfesiones.postProfesiones(requestBodyProfesiones)
                                            if (responseProfesiones.isSuccessful) {
                                                val responseBodyProfesiones = responseProfesiones.body()
                                                val responseDataProfesiones = responseBodyProfesiones?.string()
                                            }
                                        }
                                    }
                                }
                            }
                            else {
                                Toast.makeText(applicationContext, "Error en la solicitud. Código de error: ${responseLogin.code()}",Toast.LENGTH_SHORT).show()
                            }
                        }
                        startActivity(intent)
                        //habilitarInteraccion()
                    } else {
                        Toast.makeText(applicationContext, "Error en la solicitud. Código de error: ${responseCliente.code()}",Toast.LENGTH_SHORT).show()
                    }


                } catch (e: Exception) {
                    // Manejar el error aquí
                    e.printStackTrace()
                }
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

    private fun obtenerDatosDelWebServiceTipoProfesiones() {
        val call = apiTipoProfesiones.getProfesiones()
        call.enqueue(object : Callback<List<Profesiones>> {
            override fun onResponse(
                call: Call<List<Profesiones>>,
                response: Response<List<Profesiones>>
            ) {
                if (response.isSuccessful) {
                    val datos = response.body()
                    if (datos != null) {
                        val nombres = datos.map { it.descripcion }
                        val ids = datos.map { it.id_profesiones }

                        val adapter = ArrayAdapter(
                            this@CrearCuentaActivity,
                            android.R.layout.simple_spinner_item,
                            nombres
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spnTipoProfesiones.adapter = adapter

                        spnTipoProfesiones.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                idTipoProfesiones = ids[position]
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

            override fun onFailure(call: Call<List<Profesiones>>, t: Throwable) {
                // Manejar el error de la solicitud
            }
        })
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
                            this@CrearCuentaActivity,
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
                            this@CrearCuentaActivity,
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
                            this@CrearCuentaActivity,
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
                            this@CrearCuentaActivity,
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
                            this@CrearCuentaActivity,
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

    private fun openFilePicker(button: Button) {
        selectedButton = button
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
        }
        startActivityForResult(intent, PICK_PDF_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                val selectedPDFName = getSelectedPDFName(uri)
                Toast.makeText(this, "PDF seleccionado: $selectedPDFName", Toast.LENGTH_SHORT).show()
                selectedButton?.text = selectedPDFName
                savePDFFile(uri)
            }
        }
    }

    private fun getSelectedPDFName(uri: Uri): String {
        val cursor = contentResolver.query(uri, null, null, null, null)
        val nameColumnIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        cursor?.moveToFirst()
        val selectedPDFName = cursor?.getString(nameColumnIndex ?: 0) ?: ""
        cursor?.close()
        return selectedPDFName
    }

    private fun savePDFFile(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)
        val outputDirectory = getExternalFilesDir(null) // Carpeta donde se guardará el archivo
        val outputFile = File(outputDirectory, "documento.pdf")
        try {
            inputStream?.use { input ->
                FileOutputStream(outputFile).use { output ->
                    val buffer = ByteArray(4 * 1024) // Buffer para copiar el archivo
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                }
            }
            // Archivo guardado exitosamente
            Toast.makeText(this, "Archivo PDF guardado correctamente", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            // Error al guardar el archivo
            Toast.makeText(this, "Error al guardar el archivo PDF", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
    override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
        val selectedPoint = p ?: return true
        updateLocationInfo(selectedPoint.latitude, selectedPoint.longitude)
        mapView.controller.setCenter(selectedPoint)
        mapView.invalidate()
        return true
    }

    override fun longPressHelper(p: GeoPoint?): Boolean {
        return false
    }

    private fun requestPermissions() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), 1)
        }
    }

    private fun updateLocationInfo(latitude: Double, longitude: Double) {
        Thread {
            val locationInfo = getLocationInfo(latitude, longitude)

            runOnUiThread {
                val formattedLatitude = formatCoordinate(latitude)
                val formattedLongitude = formatCoordinate(longitude)
                countryEditText.setText(formattedLatitude)
                cityEditText.setText(formattedLongitude)
                cityNameTextView.text = locationInfo

                val fullLocationInfo = "Latitud: $formattedLatitude\nLongitud: $formattedLongitude\n$locationInfo"
                Toast.makeText(applicationContext, fullLocationInfo, Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun formatCoordinate(coordinate: Double): String {
        val decimalFormat = DecimalFormat("#.########")
        return decimalFormat.format(coordinate)
    }
//metodos del mapa
    private fun getLocationInfo(latitude: Double, longitude: Double): String {
        var locationInfo = ""

        try {
            val url = "https://nominatim.openstreetmap.org/search?format=json&lat=$latitude&lon=$longitude"
            val json = Jsoup.connect(url).ignoreContentType(true).execute().body()
            val jsonArray = JSONArray(json)

            if (jsonArray.length() > 0) {
                val jsonObject = jsonArray.getJSONObject(0)
                val country = jsonObject.optString("address.country", "")
                val province = jsonObject.optString("address.state", "")
                val city = jsonObject.optString("address.city", "")
                val town = jsonObject.optString("address.town", "")

                locationInfo = "País: $country\nProvincia: $province\nCiudad: ${city.takeIf { it.isNotBlank() } ?: town}"
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return locationInfo
    }
}

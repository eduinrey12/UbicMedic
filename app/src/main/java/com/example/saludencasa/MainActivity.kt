package com.example.saludencasa

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.saludencasa.ApiServices.ClienteService
import com.example.saludencasa.ApiServices.Devices
import com.example.saludencasa.ApiServices.LoginService
import com.example.saludencasa.ApiServices.TrabajadorService
import com.example.saludencasa.Constante.urlUbicMedic
import com.example.saludencasa.Modelo.Login
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import org.w3c.dom.Text
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class MainActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var fondoProgress: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnCrearCuenta: TextView = findViewById(R.id.txt_v_CrearCuenta)
        btnCrearCuenta.setOnClickListener{
            val intent: Intent = Intent(this, SeleccionarTipoCuenta:: class.java)
            startActivity(intent)
        }

        val txt_i_Usuario = findViewById<TextInputLayout>(R.id.usuario_text_input_layout)
        val txt_i_Clave = findViewById<TextInputLayout>(R.id.contraseña_text_input_layout)
        val btnAcceder: TextView = findViewById(R.id.btnAcceder)
        btnAcceder.setOnClickListener{
            val txtUsuario : EditText = findViewById(R.id.txtUsuario)
            val txtClave : EditText = findViewById(R.id.txtContraseña)
            val usuario : String = txtUsuario.text.toString()
            val clave : String = txtClave.text.toString()
            var estado : Boolean = false
            var acceso : Boolean = false
            val intent: Intent = Intent(this, MainActivity3:: class.java)
            val bdLogin = Bundle()


            if (txtUsuario.text.toString().trim().isEmpty() || txtClave.text.toString().trim().isEmpty()){
                //startActivity(intent)
                if (txtUsuario.text.toString().trim().isEmpty()){
                    txt_i_Usuario.error="El campo no puede estar vacio"
                    txt_i_Usuario.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                }
                else{
                    txt_i_Usuario.error=null
                    txt_i_Usuario.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                }
                if (txtClave.text.toString().trim().isEmpty()){
                    txt_i_Clave.error="El campo no puede estar vacio"
                    txt_i_Clave.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#E91E63"))
                }
                else{
                    txt_i_Clave.error=null
                    txt_i_Clave.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                }

            }else{
                txt_i_Usuario.error=null
                txt_i_Usuario.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))
                txt_i_Clave.error=null
                txt_i_Clave.defaultHintTextColor = ColorStateList.valueOf(Color.parseColor("#2196F3"))

                progressBar = findViewById(R.id.progressBar)
                fondoProgress = findViewById(R.id.fondoProgress)
                deshabilitarInteraccion()
                val retrofit = Retrofit.Builder()
                    .baseUrl(urlUbicMedic)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val apiServiceLogin = retrofit.create(LoginService::class.java)
                var idCliente : Int = 0

                lifecycleScope.launch {
                    try {
                        val response = apiServiceLogin.getLogin()
                        for (dataItem in response) {
                            val wsUsuario = dataItem.usuario
                            val wsClave = dataItem.contrasenia

                            if (wsUsuario == usuario && wsClave == clave) {
                                idCliente = dataItem.id_cliente
                                bdLogin.putInt("IDLOGIN",dataItem.id_login)
                                bdLogin.putInt("IDCLIENTE",dataItem.id_cliente)
                                bdLogin.putString("TIPOLOGIN",dataItem.tipo_login)
                                estado = dataItem.estado
                                acceso = true

                                FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                                    if (!task.isSuccessful) {
                                        Log.w("TAG", "Fetching FCM registration token failed", task.exception)
                                        return@OnCompleteListener
                                    }

                                    // Get new FCM registration token
                                    val token = task.result

                                    Log.d("TAG", token)
                                    //Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()


                                val retrofit = Retrofit.Builder()
                                    .baseUrl(urlUbicMedic)
                                    .addConverterFactory(GsonConverterFactory.create())
                                    .build()

                                val jsonObjectLogin = JSONObject()
                                jsonObjectLogin.put("name", usuario)
                                jsonObjectLogin.put("registration_id", token)
                                jsonObjectLogin.put("type", "android")
                                val jsonStringLogin = jsonObjectLogin.toString()
                                val requestBodyLogin = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringLogin)
                                val apiServiceDevice = retrofit.create(Devices::class.java)

                                runBlocking {
                                    try {
                                        val responseCliente = apiServiceDevice.postDevice(requestBodyLogin)
                                        if (responseCliente.isSuccessful) {
                                            //Toast.makeText(applicationContext,"Se actualizaron los datos",Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                    catch (e: Exception) {
                                        // Manejar el error aquí
                                        e.printStackTrace()
                                    }}})
                                break
                            }
                        }
                        if (acceso){
                            val apiServiceTrabajador = retrofit.create(TrabajadorService::class.java)
                            lifecycleScope.launch {
                                try {
                                    val response = apiServiceTrabajador.getTrabajadoresSimple()
                                    for (dataItem in response) {
                                        val wsIdCliente = dataItem.id_cliente
                                        if (wsIdCliente == idCliente) {
                                            bdLogin.putInt("IDTrabajador",dataItem.id_trabajador)
                                            break
                                        }
                                    }
                                    intent.putExtras(bdLogin)
                                    startActivity(intent)
                                    habilitarInteraccion()
                                } catch (e: Exception) {
                                    // Maneja cualquier error que pueda ocurrir
                                    e.printStackTrace()
                                    habilitarInteraccion()
                                }
                            }

                        }else{
                            val txtError: TextView = findViewById(R.id.txtvError)
                            txtError.visibility = View.VISIBLE
                            habilitarInteraccion()
                        }
                    } catch (e: Exception) {
                        // Maneja cualquier error que pueda ocurrir
                        e.printStackTrace()
                    }

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
}
package com.example.saludencasa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.saludencasa.ApiServices.ClienteService
import com.example.saludencasa.ApiServices.LoginService
import com.example.saludencasa.Constante.urlUbicMedic
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class EditarCredencialesTrabajador : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_credenciales_trabajador)

        var wsClave = ""
        var wsCorreo = ""
        var wsidCliente = 0
        var wsTipo = ""
        var wsEstado = true
        var idLogin: Int = intent.extras!!.getInt("IDLOGIN",0)

        val txtCorreo : EditText =findViewById(R.id.txtCorreo)
        val txtClave : EditText =findViewById(R.id.txtClaveActual)
        val txtClaveNueva : EditText =findViewById(R.id.txtClaveNueva)
        val txtClaveConfirmar : EditText =findViewById(R.id.txtClaveConfirmar)
        txtCorreo.isEnabled = false
        txtClave.isEnabled = false

        val llClaveNueva : LinearLayout =findViewById(R.id.llEditClaveNueva)
        val llClaveConfirmar : LinearLayout =findViewById(R.id.llEditClaveConfirmar)
        val llClave : LinearLayout =findViewById(R.id.llOpcionesClave)
        val llCorreo : LinearLayout =findViewById(R.id.llOpcionesCorreo)
        llClaveNueva.visibility = View.GONE
        llClaveConfirmar.visibility = View.GONE
        llClave.visibility = View.GONE
        llCorreo.visibility = View.GONE

        val btnEditCorreo : ImageButton =findViewById(R.id.btnEditCorreo)
        val btnEditClave : ImageButton =findViewById(R.id.btnEditClave)
        btnEditCorreo.setOnClickListener {
            txtCorreo.isEnabled = true
            llCorreo.visibility = View.VISIBLE
            btnEditCorreo.visibility = View.GONE}
        btnEditClave.setOnClickListener {
            txtClave.isEnabled = true
            llClave.visibility = View.VISIBLE
            llClaveConfirmar.visibility = View.VISIBLE
            llClaveNueva.visibility = View.VISIBLE
            btnEditClave.visibility = View.GONE}

        val btnCancelCorreo : Button =findViewById(R.id.btnCancelarCorreo)
        val btnCancelClave : Button =findViewById(R.id.btnCancelarClave)
        btnCancelCorreo.setOnClickListener {
            txtCorreo.isEnabled = false
            llCorreo.visibility = View.GONE
            btnEditCorreo.visibility = View.VISIBLE}
        btnCancelClave.setOnClickListener {
            txtClave.isEnabled = false
            llClave.visibility = View.GONE
            llClaveConfirmar.visibility = View.GONE
            llClaveNueva.visibility = View.GONE
            txtCorreo.setText(wsCorreo)
            btnEditClave.visibility = View.VISIBLE
            txtClave.setText(null)
            txtClaveNueva.setText(null)
            txtClaveConfirmar.setText(null)}

        val retrofit = Retrofit.Builder()
            .baseUrl(urlUbicMedic)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiServiceLogin = retrofit.create(LoginService::class.java)

        val btnGuardarCorreo : Button =findViewById(R.id.btnGuardarCorreo)
        btnGuardarCorreo.setOnClickListener {
            val jsonObjectLogin = JSONObject()
            jsonObjectLogin.put("id_login", idLogin)
            jsonObjectLogin.put("usuario", txtCorreo.text.toString())
            jsonObjectLogin.put("id_cliente", wsidCliente)
            jsonObjectLogin.put("contrasenia", wsClave)
            jsonObjectLogin.put("tipo_login", wsTipo)
            jsonObjectLogin.put("estado", wsEstado)
            val jsonStringLogin = jsonObjectLogin.toString()
            val requestBodyLogin = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringLogin)
            runBlocking {
                try {
                    val responseCliente = apiServiceLogin.putLogin(idLogin,requestBodyLogin)
                    if (responseCliente.isSuccessful) {
                        Toast.makeText(applicationContext,"Se actualizaron los datos",Toast.LENGTH_SHORT).show()
                        txtCorreo.isEnabled = false
                        llCorreo.visibility = View.GONE
                        btnEditCorreo.visibility = View.VISIBLE
                        wsCorreo = txtCorreo.text.toString()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        val btnGuardarClave : Button =findViewById(R.id.btnGuardarClave)
        btnGuardarClave.setOnClickListener {
            val jsonObjectLogin = JSONObject()
            jsonObjectLogin.put("id_login", idLogin)
            jsonObjectLogin.put("usuario", wsCorreo)
            jsonObjectLogin.put("id_cliente", wsidCliente)
            jsonObjectLogin.put("contrasenia", txtClaveNueva.text.toString())
            jsonObjectLogin.put("tipo_login", wsTipo)
            jsonObjectLogin.put("estado", wsEstado)
            val jsonStringLogin = jsonObjectLogin.toString()
            val requestBodyLogin = RequestBody.create("application/json".toMediaTypeOrNull(), jsonStringLogin)
            if (txtClave.text.toString()==wsClave){
                if (txtClaveNueva.text.toString()==txtClaveConfirmar.text.toString()){
                    runBlocking {
                        try {
                            val responseCliente = apiServiceLogin.putLogin(idLogin,requestBodyLogin)
                            if (responseCliente.isSuccessful) {
                                Toast.makeText(applicationContext,"Se actualizaron los datos",Toast.LENGTH_SHORT).show()
                                txtClave.isEnabled = false
                                llClave.visibility = View.GONE
                                llClaveConfirmar.visibility = View.GONE
                                llClaveNueva.visibility = View.GONE
                                btnEditClave.visibility = View.VISIBLE
                                wsClave = txtClaveNueva.text.toString()
                                txtClave.setText(null)
                                txtClaveNueva.setText(null)
                                txtClaveConfirmar.setText(null)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }else{
                    Toast.makeText(this,"No coinceden las contraseñas",Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this,"Ingrese bien la contraseña actual",Toast.LENGTH_SHORT).show()
            }

        }

        lifecycleScope.launch {
            try {
                val response = apiServiceLogin.getLogin()
                for (dataItem in response) {
                    val wsIdLogin = dataItem.id_login
                    if (wsIdLogin == idLogin) {
                        wsClave = dataItem.contrasenia
                        wsCorreo = dataItem.usuario
                        wsidCliente = dataItem.id_cliente
                        wsTipo = dataItem.tipo_login
                        wsEstado = dataItem.estado
                        txtCorreo.setText(wsCorreo)
                        break
                    }
                }
            } catch (e: Exception) {
                // Maneja cualquier error que pueda ocurrir
                e.printStackTrace()
            }
        }
    }
}
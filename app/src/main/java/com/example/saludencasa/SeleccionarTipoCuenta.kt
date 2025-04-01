package com.example.saludencasa

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView

class SeleccionarTipoCuenta : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccionar_tipo_cuenta)

        val btnPacientes: Button = findViewById(R.id.btnPacientes)
        btnPacientes.setOnClickListener{
            val intent: Intent = Intent(this, CrearCuentaCliente:: class.java)
            startActivity(intent)
        }
        val btnTrabajador: Button = findViewById(R.id.btnTrabajador)
        btnTrabajador.setOnClickListener{
            val intent: Intent = Intent(this, CrearCuentaActivity:: class.java)
            startActivity(intent)
        }

        val imagen: ImageView = findViewById(R.id.imgSeleccion)

    }
}
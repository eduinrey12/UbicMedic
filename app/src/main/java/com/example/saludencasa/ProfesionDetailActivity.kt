package com.example.saludencasa

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.saludencasa.Constante.urlUbicMedic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

data class Trabajadores(
    val id_trabajador: Int,
    val cliente: String,
    val profesiones: List<String>,
    val estadoid: String
)

interface ApiServi {
    @GET("Trabajador/")
    suspend fun obtenerTrabajadores(): List<Trabajadores>
}

class ProfesionDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID_PROFESION = "extra_id_profesion"
        const val EXTRA_DESCRIPCION_PROFESION = "extra_descripcion_profesion"
    }

    private val apiService = Retrofit.Builder()
        .baseUrl(urlUbicMedic)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiServi::class.java)

    private val trabajadorAdapter by lazy { TrabajadorAdapter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profesion_detail)
        val recyclerView: RecyclerView = findViewById(R.id.rvcFiltro)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = trabajadorAdapter

        val idProfesion = intent.getIntExtra(EXTRA_ID_PROFESION, -1)
        val descripcionProfesion = intent.getStringExtra(EXTRA_DESCRIPCION_PROFESION)

        if (idProfesion != -1 && descripcionProfesion != null) {
            val idProfesionTextView: TextView = findViewById(R.id.txtIdProfesion)
            val descripcionTextView: TextView = findViewById(R.id.txtDescripcion)

            idProfesionTextView.text = "ID de la profesión: $idProfesion"
            descripcionTextView.text = "Descripción: $descripcionProfesion"

            obtenerTrabajadoresPorFiltros(descripcionProfesion)
        } else {
            Toast.makeText(this, "Error al obtener la información de la profesión", Toast.LENGTH_SHORT).show()
            finish() // Cerramos la actividad si no se proporcionó correctamente la información.
        }
    }

    private fun obtenerTrabajadoresPorFiltros(profesion: String) {
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val trabajadores = apiService.obtenerTrabajadores().filter {
                    it.profesiones.contains(profesion) && it.estadoid == "Aceptado"
                }
                mostrarTrabajadores(trabajadores)
            } catch (e: Exception) {
                Toast.makeText(
                    this@ProfesionDetailActivity,
                    "Error al obtener los trabajadores",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun mostrarTrabajadores(trabajadores: List<Trabajadores>) {
        trabajadorAdapter.actualizarTrabajadores(trabajadores)
    }

    inner class TrabajadorAdapter : RecyclerView.Adapter<TrabajadorAdapter.TrabajadorViewHolder>() {
        private val trabajadores = mutableListOf<Trabajadores>()
        inner class TrabajadorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nombreTextView: TextView = itemView.findViewById(R.id.txtNombreCer)
            val profesionTextView: TextView = itemView.findViewById(R.id.txtProfesion)
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrabajadorViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_home_trabajdor, parent, false)
            return TrabajadorViewHolder(itemView)
        }
        override fun onBindViewHolder(holder: TrabajadorViewHolder, position: Int) {
            val trabajador = trabajadores[position]
            holder.nombreTextView.text = trabajador.cliente
            holder.profesionTextView.text = trabajador.profesiones.joinToString(", ")
        }
        override fun getItemCount() = trabajadores.size
        fun actualizarTrabajadores(listaTrabajadores: List<Trabajadores>) {
            trabajadores.clear()
            trabajadores.addAll(listaTrabajadores)
            notifyDataSetChanged() }
    }
}
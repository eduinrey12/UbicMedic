package com.example.saludencasa.Adaptador

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.saludencasa.Modelo.Trabajador
import com.example.saludencasa.R
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class TrabajadorAdapter(
    private val trabajadores: MutableList<Trabajador>,
    private val homeLatitude: Double,
    private val homeLongitude: Double
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_TRABAJADOR = 0
    private val VIEW_TYPE_VER_MAS = 1
    interface OnItemClickListener {
        fun onItemClick(trabajador: Trabajador)
        fun onVerMasClick()
        fun getLocation(): Pair<Double, Double>
        fun onVerMasClick2()
    }
    private var onItemClickListener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }

    fun ordenarCalificacion() {
        trabajadores.sortByDescending { it.puntuaciones }
        notifyDataSetChanged()
    }

    fun ordenarAtenciones() {
        trabajadores.sortByDescending { it.atenciones }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_TRABAJADOR -> {
                val itemView =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_home_trabajdor, parent, false)
                TrabajadorViewHolder(itemView).apply {
                    itemView.setOnClickListener {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val clickedItem = trabajadores[position]
                            onItemClickListener?.onItemClick(clickedItem)
                        }
                    }
                }
            }
            VIEW_TYPE_VER_MAS -> {
                val itemView =
                    LayoutInflater.from(parent.context).inflate(R.layout.item_ver_mas, parent, false)
                VerMasViewHolder(itemView).apply {
                    itemView.setOnClickListener {
                        onItemClickListener?.onVerMasClick()
                    }
                }
            }
            else -> throw IllegalArgumentException("Tipo de vista desconocido")
        }
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TrabajadorViewHolder -> {
                val trabajador = trabajadores[position]
                holder.usernameTextView.text = trabajador.cliente
                val profesioneslst = trabajador.profesiones.joinToString(", ")
                holder.txtprofesion.text = profesioneslst
                val latidud = "${trabajador.latitud}"
                val longitud= " ${trabajador.longitud}"
                holder.txtpuntuacion.text = trabajador.puntuaciones
                //desde aqui
                fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
                    val R = 6371.0 // Radio de la Tierra en kilómetros
                    val dLat = Math.toRadians(lat2 - lat1)
                    val dLon = Math.toRadians(lon2 - lon1)
                    val a = sin(dLat / 2) * sin(dLat / 2) +
                            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                            sin(dLon / 2) * sin(dLon / 2)
                    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
                    return (R * c) // Distancia en kilómetros
                }
                val distance = calculateHaversineDistance(latidud.toDouble(), longitud.toDouble(), homeLatitude, homeLongitude)
                val formattedNumber = String.format("%.2f", distance)
                holder.txtDistancia.text = "$formattedNumber km"
                Log.e("lati y longi", homeLatitude.toString()+homeLongitude.toString())
                if (trabajador.foto != null) {
                    holder.imgPerfilCer.load(trabajador.foto)
                } else {
                    holder.imgPerfilCer.setImageResource(R.drawable.foto_predeterminada)
                }
            }
        }
    }
    override fun getItemCount(): Int {
        return minOf(trabajadores.size, 5) + 1
    }
    override fun getItemViewType(position: Int): Int {
        return if (position == itemCount - 1) {
            VIEW_TYPE_VER_MAS
        } else {
            VIEW_TYPE_TRABAJADOR
        }
    }
    class VerMasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    class TrabajadorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.txtNombreCer)
        val txtDistancia: TextView = itemView.findViewById(R.id.txtDistancia)
        val txtprofesion: TextView = itemView.findViewById(R.id.txtProfesion)
        val imgPerfilCer: ImageView = itemView.findViewById(R.id.imgPerfilCer)
        val txtpuntuacion: TextView = itemView.findViewById(R.id.txtValoracion)
    }
}
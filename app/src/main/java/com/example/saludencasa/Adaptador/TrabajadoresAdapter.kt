package com.example.saludencasa.Adaptador

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.saludencasa.Modelo.Trabajador
import com.example.saludencasa.R
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class TrabajadoresAdapter(
    var trabajadores: MutableList<Trabajador>,
    private val latitude: Double,
    private val longitude: Double
) :
    RecyclerView.Adapter<TrabajadoresAdapter.TrabajadorViewHolder>(){
    private var onItemClick: ((Trabajador) -> Unit)? = null
    fun setOnItemClickListener(listener: (Trabajador) -> Unit) {
        onItemClick = listener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrabajadorViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todos_trabajadores, parent, false)
        return TrabajadorViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: TrabajadorViewHolder, position: Int) {
        val trabajador = trabajadores[position]
        holder.bind(trabajador)
        val latidud = "${trabajador.latitud}"
        val longitud= " ${trabajador.longitud}"
        holder.txtpuntuacion.text = trabajador.puntuaciones

        //caclular distancia
        fun calculateHaversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val R = 6371.0 // Radio de la Tierra en kilómetros
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return (R * c)
        }
        val distance = calculateHaversineDistance(latidud.toDouble(), longitud.toDouble(), latitude, longitude)
        val formattedNumber = String.format("%.2f", distance)
        holder.txtDistanciaCer.text = "$formattedNumber km"
        if (trabajador.foto != null) {
            holder.imgPerfilCer.load(trabajador.foto)
        } else {
            holder.imgPerfilCer.setImageResource(R.drawable.foto_predeterminada)
        }
    }
    override fun getItemCount() = trabajadores.size
    inner class TrabajadorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtNombreTrabajador: TextView = itemView.findViewById(R.id.txtNombreTrabajador)
        private val txtProfesionesTrabajador: TextView = itemView.findViewById(R.id.txtProfesionesTrabajador)
        val txtDistanciaCer: TextView = itemView.findViewById(R.id.txtDistanciaCer)
        val imgPerfilCer: ImageView = itemView.findViewById(R.id.imgFotoPerfilFrag)
        val txtpuntuacion: TextView = itemView.findViewById(R.id.txtValoracion)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(trabajadores[adapterPosition])
            }
        }

        fun bind(trabajador: Trabajador) {
            txtNombreTrabajador.text = trabajador.cliente
            txtProfesionesTrabajador.text = trabajador.profesiones.joinToString(", ")
        }
    }
}


package com.example.saludencasa.Adaptador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.saludencasa.HomeFragment
import com.example.saludencasa.Modelo.Profesiones
import com.example.saludencasa.R

class ProfesionesAdapter(val profesiones: List<Profesiones>, private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<ProfesionesAdapter.ProfesionesViewHolder>() {

    //agregar un inner al class
    class ProfesionesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val usernameTextView: TextView = itemView.findViewById(R.id.txtvEnfermedad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfesionesViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_enfermedad, parent, false)
        return ProfesionesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProfesionesViewHolder, position: Int) {
        val profesion = profesiones[position]
        holder.usernameTextView.text = profesion.descripcion
        holder.itemView.setOnClickListener {
            onItemClick(profesion.id_profesiones)
        }
    }

    override fun getItemCount(): Int {
        return if (showAllProfesiones) profesiones.size else minOf(profesiones.size, 5)
    }

    private var showAllProfesiones = false

    fun setShowAllProfesiones(showAll: Boolean) {
        showAllProfesiones = showAll
        notifyDataSetChanged()
    }
}
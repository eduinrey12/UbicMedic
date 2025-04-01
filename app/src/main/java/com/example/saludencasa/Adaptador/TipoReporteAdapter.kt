package com.example.saludencasa.Adaptador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.saludencasa.HomeFragment
import com.example.saludencasa.Modelo.Profesiones
import com.example.saludencasa.Modelo.TipoReporte
import com.example.saludencasa.R

class TipoReporteAdapter(val tiporeporte: List<TipoReporte>, private val onItemClick: (Int, List<Int>) -> Unit) : RecyclerView.Adapter<TipoReporteAdapter.ProfesionesViewHolder>() {

    val itemsSelecionados = mutableListOf<Int>()

    class ProfesionesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rdbtipoReporte: RadioButton = itemView.findViewById(R.id.rdbTipoReporte)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfesionesViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_tipo_reporte, parent, false)
        return ProfesionesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProfesionesViewHolder, position: Int) {
        val tiporeport = tiporeporte[position]
        holder.rdbtipoReporte.text = tiporeport.descripcion

        holder.rdbtipoReporte.isChecked = itemsSelecionados.contains(tiporeport.id_tiporeporte)

        holder.rdbtipoReporte.setOnClickListener {
            if (itemsSelecionados.contains(tiporeport.id_tiporeporte)) {
                itemsSelecionados.remove(tiporeport.id_tiporeporte)
            }
            else{
                itemsSelecionados.add(tiporeport.id_tiporeporte)
            }
            holder.rdbtipoReporte.isChecked = itemsSelecionados.contains(tiporeport.id_tiporeporte)
            onItemClick(tiporeport.id_tiporeporte,itemsSelecionados)
        }
    }

    override fun getItemCount(): Int {
        return tiporeporte.size
    }
}
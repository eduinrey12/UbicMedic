package com.example.saludencasa.Adaptador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.saludencasa.Modelo.EnfermedadesCliente
import com.example.saludencasa.Modelo.Profesiones
import com.example.saludencasa.R

class EnfermedadesClienteAdapter(private val enfermedadesCliente: List<EnfermedadesCliente>, private val onItemClick: (Int) -> Unit) : RecyclerView.Adapter<EnfermedadesClienteAdapter.EnfermedadesClienteViewHolder>() {

    class EnfermedadesClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtEnfermedad: TextView = itemView.findViewById(R.id.txtvEnfermedad)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EnfermedadesClienteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_enfermedad, parent, false)
        return EnfermedadesClienteViewHolder(itemView)
    }

    //clase actualizada
    override fun onBindViewHolder(holder: EnfermedadesClienteViewHolder, position: Int) {
        val enfermedad = enfermedadesCliente[position]
        holder.txtEnfermedad.text = enfermedad.enfermedad // Mostrar la descripción de la profesión en el TextView

        holder.itemView.setOnClickListener {
            onItemClick(enfermedad.id_enfermedadesxpaciente) // Llamamos al listener cuando se haga clic en un elemento y pasamos el id_profesiones
        }
    }

    override fun getItemCount(): Int {
        return enfermedadesCliente.size
    }
}
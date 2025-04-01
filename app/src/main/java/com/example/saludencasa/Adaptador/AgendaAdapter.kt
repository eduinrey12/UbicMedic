package com.example.saludencasa.Adaptador

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.saludencasa.Modelo.Agenda
import com.example.saludencasa.R

class AgendaAdapter(private val verificarDia: Boolean,private val diaActual: Int ,private val daysData: List<Agenda>, private val onItemClick: (Int) -> Unit) :
    RecyclerView.Adapter<AgendaAdapter.DayViewHolder>() {

    var selectedItemPosition: Int = -1


    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val diaMes: TextView = itemView.findViewById(R.id.dayNumberTextView)
        val diaNombre: TextView = itemView.findViewById(R.id.dayNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int ): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dia, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val dayData = daysData[position]
        holder.diaMes.text = dayData.dayOfMonth.toString()
        holder.diaNombre.text = dayData.dayOfWeek

        if (position == selectedItemPosition) {
            holder.itemView.setBackgroundResource(R.color.white) // Cambia a tu recurso de fondo seleccionado
            holder.diaMes.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.selecionado))
            holder.diaNombre.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.selecionado))
        } else {
            holder.itemView.setBackgroundResource(R.color.selecionado) // Cambia a tu recurso de fondo predeterminado
            holder.diaMes.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
            holder.diaNombre.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        }
        if(dayData.dayOfMonth == diaActual && verificarDia){
            holder.itemView.setBackgroundResource(R.color.white) // Cambia a tu recurso de fondo seleccionado
            holder.diaMes.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.error_rojo))
            holder.diaNombre.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.error_rojo))
        }

        holder.itemView.setOnClickListener {
            selectedItemPosition = position
            notifyDataSetChanged()
            onItemClick(dayData.dayOfMonth)
        }
    }

    override fun getItemCount() = daysData.size
}
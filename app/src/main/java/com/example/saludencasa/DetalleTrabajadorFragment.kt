package com.example.saludencasa

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class DetalleTrabajadorFragment : Fragment(R.layout.fragment_detalle_trabajador) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Obtén los argumentos pasados al fragmento (el ID del trabajador seleccionado)
        val trabajadorId = arguments?.getInt(ARG_TRABAJADOR_ID)

        // Aquí puedes usar el ID para obtener los detalles completos del trabajador desde la API
        // Si lo deseas, puedes almacenar todos los trabajadores en una lista en el fragmento "HomeFragment"
        // y usar el ID para buscar el trabajador específico por su ID en esa lista.

        // En este ejemplo, simplemente mostraremos un mensaje con el ID del trabajador seleccionado.
        val detalleTextView: TextView = view.findViewById(R.id.txtDetalleTrabajador)
        detalleTextView.text = "Detalles del Trabajador con ID: $trabajadorId"
    }

    companion object {
        private const val ARG_TRABAJADOR_ID = "trabajador_id"

        // Método estático para crear una nueva instancia de DetalleTrabajadorFragment
        fun newInstance(trabajadorId: Int): DetalleTrabajadorFragment {
            val fragment = DetalleTrabajadorFragment()
            val args = Bundle()
            args.putInt(ARG_TRABAJADOR_ID, trabajadorId)
            fragment.arguments = args
            return fragment
        }
    }
}
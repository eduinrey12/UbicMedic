package com.example.saludencasa.Modelo

data class EnfermedadesCliente(
    val id_enfermedadesxpaciente: Int,
    val id_enfermedad: Int,
    val id_cliente: Int,
    val descripcion: String,
    val idclasienfermedad: String,
    val enfermedad: String,
    val estado: Boolean
    )
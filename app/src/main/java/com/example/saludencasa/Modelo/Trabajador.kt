package com.example.saludencasa.Modelo

data class Trabajador(
    val id_trabajador: Int,
    val cliente: String,
    val foto: String,
    val id_tipo_trabajador: Int,
    val estado: Int,
    val estadoid: String,
    val id_cliente: Int,
    val profesiones: List<String>,
    val latitud: String,
    val longitud: String,
    val puntuaciones: String,
    val atenciones: Int,
)
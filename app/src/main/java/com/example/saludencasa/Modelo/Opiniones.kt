package com.example.saludencasa.Modelo

data class Opiniones(
    val id_paciente: Int,
    val cliente: String,
    val id_trabajador: Int,
    val foto: String,
    val puntuacion : Int,
    val comentario : String
)
package com.example.saludencasa.Modelo

data class Citas(
    val id_cita: Int,
    val id_trabajador: Int,
    val id_cliente_trabajador: Int,
    val id_cliente: Int,
    val descripcion_motivo: String,
    val fecha_creacion: String,
    val fecha_inicioatencion: String,
    val fecha_finatencion: String,
    val fecha_confirmacion: String,
    val notificacion_trabajador: Boolean,
    val notificacion_cliente: Boolean,
    val notificacion_calificacion: Boolean,
    val latitud: String,
    val longitud: String,
    val estado: Int,
    val estadoid : String,
    val cliente : String,
    val trabajador : String,
    val fotoC : String,
    val fotoT : String
)